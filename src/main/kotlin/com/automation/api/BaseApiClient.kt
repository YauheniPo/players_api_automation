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

    private fun baseSpecBuilder(): RequestSpecBuilder =
        RequestSpecBuilder()
            .setBaseUri(ConfigProvider.config.baseUrl())
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
}
