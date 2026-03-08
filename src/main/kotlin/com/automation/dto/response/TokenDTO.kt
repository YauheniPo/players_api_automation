package com.automation.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenDTO(
    val accessToken: String,
) {
    init {
        require(accessToken.isNotBlank()) { "accessToken must not be blank" }
    }
}
