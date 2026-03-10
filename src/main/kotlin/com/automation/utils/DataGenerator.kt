package com.automation.utils

import com.automation.dto.StringLength
import com.automation.dto.request.PlayerRequestDTO
import net.datafaker.Faker
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object DataGenerator {
    private val faker = Faker()
    private val counter = AtomicInteger(1)

    fun generatePlayer(): PlayerRequestDTO {
        val idx = counter.getAndIncrement()
        val constraints = stringConstraints(PlayerRequestDTO::class)
        // no special chars — some APIs reject them in passwords
        val passMin = constraints["passwordChange"]?.min ?: 6
        val pass = faker.internet().password(passMin, passMin + 14, true, false, true)
        val suffix = "${idx}_${faker.number().digits(4)}"

        return PlayerRequestDTO(
            currencyCode = faker.money().currencyCode()
                .fit(constraints["currencyCode"]),
            email = "player_$suffix@automation.test"
                .fit(constraints["email"]),
            name = faker.name().firstName().letters(fallback = "TestName")
                .fit(constraints["name"]),
            surname = faker.name().lastName().letters(fallback = "TestSurname")
                .fit(constraints["surname"]),
            username = "usr_$suffix".fit(constraints["username"]),
            passwordChange = pass,
            passwordRepeat = pass,
        )
    }

    fun generatePlayers(count: Int): List<PlayerRequestDTO> = (1..count).map { generatePlayer() }

    /**
     * Returns a map of constructor-parameter-name → @StringLength for every annotated
     * String parameter in the primary constructor of [klass].
     */
    fun <T : Any> stringConstraints(klass: KClass<T>): Map<String, StringLength> {
        val ctor = klass.primaryConstructor ?: return emptyMap()
        return ctor.parameters
            .mapNotNull { param ->
                val ann = param.annotations.filterIsInstance<StringLength>().firstOrNull()
                if (ann != null && param.name != null) param.name!! to ann else null
            }
            .toMap()
    }

    /**
     * Trims to [StringLength.max] characters and pads with 'x' up to [StringLength.min].
     * Returns the original string unchanged when [constraint] is null.
     */
    private fun String.fit(constraint: StringLength?): String {
        constraint ?: return this
        val trimmed = if (length > constraint.max) take(constraint.max) else this
        return if (trimmed.length < constraint.min) trimmed.padEnd(constraint.min, 'x') else trimmed
    }

    // strips non-ASCII letters (apostrophes, hyphens, accented chars, etc.)
    private fun String.letters(fallback: String): String = replace(Regex("[^a-zA-Z]"), "").ifEmpty { fallback }
}
