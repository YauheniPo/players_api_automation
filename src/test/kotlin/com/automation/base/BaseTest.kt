package com.automation.base

import com.automation.api.AuthApiClient
import com.automation.api.PlayerApiClient
import com.automation.config.ConfigProvider
import com.automation.dto.request.CredentialsDTO
import com.automation.utils.TestContext
import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.qameta.allure.restassured.AllureRestAssured
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.ResponseLoggingFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.BeforeSuite

abstract class BaseTest {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    @BeforeSuite(alwaysRun = true)
    fun globalSetUp() {
        synchronized(BaseTest::class.java) {
            configureRestAssured()
            if (TestContext.authToken == null) {
                log.info("BeforeSuite: fetching auth token...")
                authenticate()
            }
        }
    }

    protected fun authApiClient(): AuthApiClient = AuthApiClient()

    protected fun playerApiClient(): PlayerApiClient =
        PlayerApiClient(
            requireNotNull(TestContext.authToken) {
                "Auth token is null - did globalSetUp() run successfully?"
            },
        )

    private fun configureRestAssured() {
        val mapper = JsonMapper.builder()
            .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION) // show actual JSON in deserialization errors
            .addModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.StrictNullChecks, true)
                    .build(),
            )
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            .build()

        RestAssured.config = RestAssuredConfig.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig()
                    .jackson2ObjectMapperFactory { _, _ -> mapper },
            )

        if (RestAssured.filters().none { it is AllureRestAssured }) {
            RestAssured.filters(AllureRestAssured())
        }
        if (RestAssured.filters().none { it is ResponseLoggingFilter }) {
            RestAssured.filters(ResponseLoggingFilter(LogDetail.ALL))
        }
    }

    private fun authenticate() {
        val credentials = CredentialsDTO(
            email = ConfigProvider.config.adminLogin(),
            password = ConfigProvider.config.adminPassword(),
        )

        val tokenDTO = authApiClient()
            .login(credentials)
            .assertSuccess()

        TestContext.authToken = tokenDTO.accessToken
    }
}
