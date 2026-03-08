package com.automation.api

import com.automation.config.ConfigProvider
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

abstract class BaseApiClient {
    protected val requestSpec: RequestSpecification

    constructor() {
        requestSpec = baseSpecBuilder().build()
    }

    constructor(bearerToken: String) {
        requestSpec = baseSpecBuilder()
            .addHeader("Authorization", "Bearer $bearerToken")
            .build()
    }

    private fun baseSpecBuilder(): RequestSpecBuilder {
        val baseUrl = ConfigProvider.config.baseUrl()
        require(baseUrl.isNotBlank()) {
            "base.url is not configured. Set BASE_URL env var, -Dbase.url JVM property, " +
                "or add it to src/main/resources/config.local.properties"
        }
        return RequestSpecBuilder()
            .setBaseUri(baseUrl)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
    }
}
