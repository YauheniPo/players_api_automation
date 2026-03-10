package com.automation.dto

/**
 * Declares the allowed character length range for a String field.
 *
 * - **DataGenerator** uses it to produce strings that fit the API constraints.
 * - **Jackson** validates the constraint on every serialization and deserialization
 *   via [StringLengthModule], which must be registered on every ObjectMapper used in the project.
 *
 * @param min minimum number of characters (inclusive, >= 0)
 * @param max maximum number of characters (inclusive, >= min)
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringLength(val min: Int = 1, val max: Int = 255)
