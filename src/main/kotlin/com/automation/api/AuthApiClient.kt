package com.automation.api

import com.automation.dto.request.CredentialsDTO
import com.automation.dto.response.TokenDTO
import io.qameta.allure.Step
import io.restassured.RestAssured.given

class AuthApiClient : BaseApiClient() {
    companion object {
        private const val LOGIN_ENDPOINT = "/api/tester/login"
    }

    @Step("POST /api/tester/login - JSON login [{credentialsDTO.email}]")
    fun login(credentialsDTO: CredentialsDTO): ApiResponse<TokenDTO> =
        given()
            .spec(requestSpec)
            .body(credentialsDTO)
            .`when`()
            .post(LOGIN_ENDPOINT)
            .then().extract().response()
            .toApiResponse()
}
