package com.automation.api

import com.automation.dto.request.PlayerRequestDTO
import com.automation.dto.request.PlayerRequestOneDTO
import com.automation.dto.response.PlayerResponseDTO
import io.qameta.allure.Step
import io.restassured.RestAssured.given

/**
 * Client for the /api/automationTask endpoints.
 *
 * Endpoints that return a body are wrapped in [ApiResponse]:
 *   createPlayer  - ApiResponse of PlayerResponseDTO
 *   getOnePlayer  - ApiResponse of PlayerResponseDTO
 *   getAllPlayers - ApiResponse of List of PlayerResponseDTO
 *
 * Delete has no response body - returns raw [Response].
 */
class PlayerApiClient(bearerToken: String) : BaseApiClient(bearerToken) {
    companion object {
        private const val CREATE_ENDPOINT = "/api/automationTask/create"
        private const val GET_ONE_ENDPOINT = "/api/automationTask/getOne"
        private const val GET_ALL_ENDPOINT = "/api/automationTask/getAll"
        private const val DELETE_ONE_ENDPOINT = "/api/automationTask/deleteOne/{id}"
    }

    @Step("POST /api/automationTask/create - username: [{playerRequest.username}]")
    fun createPlayer(playerRequest: PlayerRequestDTO): ApiResponse<PlayerResponseDTO> =
        given()
            .spec(requestSpec)
            .body(playerRequest)
            .`when`()
            .post(CREATE_ENDPOINT)
            .then().extract().response()
            .toApiResponse()

    @Step("POST /api/automationTask/getOne - email: [{request.email}]")
    fun getOnePlayer(request: PlayerRequestOneDTO): ApiResponse<PlayerResponseDTO> =
        given()
            .spec(requestSpec)
            .body(request)
            .`when`()
            .post(GET_ONE_ENDPOINT)
            .then().extract().response()
            .toApiResponse()

    @Step("GET /api/automationTask/getAll")
    fun getAllPlayers(): ApiResponse<List<PlayerResponseDTO>> =
        given()
            .spec(requestSpec)
            .`when`()
            .get(GET_ALL_ENDPOINT)
            .then().extract().response()
            .toApiResponseList()

    @Step("DELETE /api/automationTask/deleteOne/{id} - id: [{id}]")
    fun deletePlayer(id: String): ApiResponse<Unit> =
        given()
            .spec(requestSpec)
            .pathParam("id", id)
            .`when`()
            .delete(DELETE_ONE_ENDPOINT)
            .then().extract().response()
            .toApiResponseEmpty()
}
