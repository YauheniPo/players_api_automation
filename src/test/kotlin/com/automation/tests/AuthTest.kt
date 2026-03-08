package com.automation.tests

import com.automation.base.BaseTest
import com.automation.config.ConfigProvider
import com.automation.dto.request.CredentialsDTO
import io.qameta.allure.Description
import io.qameta.allure.Feature
import io.qameta.allure.Story
import org.testng.annotations.Test

@Feature("Authentication")
class AuthTest : BaseTest() {
    @Test(description = "Login with valid credentials and receive a 2xx response")
    @Story("User login")
    @Description(
        "POST /api/tester/login\n" +
            "Body: { email, password } (application/json)\n" +
            "Expected: 2xx response with a valid TokenDTO body.",
    )
    fun loginReturnsValidToken() {
        val credentials = CredentialsDTO(
            email = ConfigProvider.config.adminLogin(),
            password = ConfigProvider.config.adminPassword(),
        )

        authApiClient()
            .login(credentials)
            .assertSuccess()
    }
}
