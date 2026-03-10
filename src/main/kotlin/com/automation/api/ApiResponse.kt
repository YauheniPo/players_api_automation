package com.automation.api

import com.automation.dto.StringLengthModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.response.Response

@PublishedApi
internal val listMapper =
    jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(StringLengthModule())

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()

    data class Error(val response: Response) : ApiResponse<Nothing>() {
        val statusCode: Int get() = response.statusCode
        val body: String get() = response.body.asString()
    }

    fun assertSuccess(): T =
        when (this) {
            is Success -> data
            is Error -> throw AssertionError(
                "Expected 2xx response but got HTTP $statusCode.\nBody: $body",
            )
        }

    fun assertError(): Response =
        when (this) {
            is Error -> response
            is Success -> throw AssertionError(
                "Expected error response but got 2xx with data: $data",
            )
        }
}

inline fun <reified T> Response.toApiResponse(): ApiResponse<T> =
    if (statusCode in 200..299) {
        ApiResponse.Success(`as`(T::class.java))
    } else {
        ApiResponse.Error(this)
    }

inline fun <reified T> Response.toApiResponseList(): ApiResponse<List<T>> =
    if (statusCode in 200..299) {
        val javaType = listMapper.typeFactory.constructCollectionType(List::class.java, T::class.java)
        ApiResponse.Success(listMapper.readValue(body.asString(), javaType))
    } else {
        ApiResponse.Error(this)
    }

fun Response.toApiResponseEmpty(): ApiResponse<Unit> =
    if (statusCode in 200..299) {
        ApiResponse.Success(Unit)
    } else {
        ApiResponse.Error(this)
    }
