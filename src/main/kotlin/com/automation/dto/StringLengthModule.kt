package com.automation.dto

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import kotlin.reflect.full.primaryConstructor

/**
 * Jackson module that validates [@StringLength] constraints during serialization and
 * deserialization. Works by reading annotations from Kotlin primary-constructor parameters
 * directly via Kotlin reflection — no @JacksonAnnotationsInside required.
 *
 * Register once on every ObjectMapper used in the project.
 */
class StringLengthModule : SimpleModule("StringLengthModule") {
    init {
        setDeserializerModifier(Deserializer())
        setSerializerModifier(Serializer())
    }

    private class Deserializer : BeanDeserializerModifier() {
        override fun modifyDeserializer(
            config: DeserializationConfig,
            beanDesc: BeanDescription,
            deserializer: JsonDeserializer<*>,
        ): JsonDeserializer<*> {
            val constraints = constraintsFor(beanDesc.beanClass)
            return if (constraints.isEmpty()) {
                deserializer
            } else {
                ValidatingDeserializer(deserializer, constraints)
            }
        }
    }

    private class Serializer : BeanSerializerModifier() {
        @Suppress("UNCHECKED_CAST")
        override fun modifySerializer(
            config: SerializationConfig,
            beanDesc: BeanDescription,
            serializer: JsonSerializer<*>,
        ): JsonSerializer<*> {
            val constraints = constraintsFor(beanDesc.beanClass)
            return if (constraints.isEmpty()) {
                serializer
            } else {
                ValidatingSerializer(serializer as JsonSerializer<Any>, constraints)
            }
        }
    }
}

private fun constraintsFor(clazz: Class<*>): Map<String, StringLength> {
    val ctor = clazz.kotlin.primaryConstructor ?: return emptyMap()
    return ctor.parameters.mapNotNull { param ->
        val ann = param.annotations.filterIsInstance<StringLength>().firstOrNull()
        if (ann != null && param.name != null) param.name!! to ann else null
    }.toMap()
}

private fun readStringField(
    bean: Any,
    fieldName: String,
): String? =
    try {
        bean.javaClass.getDeclaredField(fieldName).also { it.isAccessible = true }.get(bean) as? String
    } catch (_: NoSuchFieldException) {
        null
    }

private class ValidatingDeserializer<T>(
    private val delegate: JsonDeserializer<T>,
    private val constraints: Map<String, StringLength>,
) : JsonDeserializer<T>(), ResolvableDeserializer {
    override fun resolve(ctxt: DeserializationContext) {
        if (delegate is ResolvableDeserializer) {
            delegate.resolve(ctxt)
        }
    }

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): T {
        val result = delegate.deserialize(p, ctxt)
        result?.let { bean ->
            constraints.forEach { (name, constraint) ->
                val value = readStringField(bean, name) ?: return@forEach
                if (value.length < constraint.min) {
                    throw JsonMappingException.from(
                        ctxt,
                        "Field '$name': value length ${value.length} is below minimum " +
                            "${constraint.min} (value='$value')",
                    )
                }
                if (value.length > constraint.max) {
                    throw JsonMappingException.from(
                        ctxt,
                        "Field '$name': value length ${value.length} exceeds maximum " +
                            "${constraint.max} (value='${value.take(30)}…')",
                    )
                }
            }
        }
        return result
    }
}

private class ValidatingSerializer<T : Any>(
    private val delegate: JsonSerializer<T>,
    private val constraints: Map<String, StringLength>,
) : JsonSerializer<T>() {
    override fun serialize(
        value: T,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        constraints.forEach { (name, constraint) ->
            val fieldValue = readStringField(value, name) ?: return@forEach
            if (fieldValue.length < constraint.min) {
                throw JsonMappingException.from(
                    gen,
                    "Field '$name': value length ${fieldValue.length} is below minimum " +
                        "${constraint.min} (value='$fieldValue')",
                )
            }
            if (fieldValue.length > constraint.max) {
                throw JsonMappingException.from(
                    gen,
                    "Field '$name': value length ${fieldValue.length} exceeds maximum " +
                        "${constraint.max} (value='${fieldValue.take(30)}…')",
                )
            }
        }
        delegate.serialize(value, gen, provider)
    }
}
