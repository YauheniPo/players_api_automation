package com.automation.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class PlayerRequestDTO(
    @JsonProperty("currency_code") val currencyCode: String,
    val email: String,
    val name: String,
    @JsonProperty("password_change") val passwordChange: String,
    @JsonProperty("password_repeat") val passwordRepeat: String,
    val surname: String,
    val username: String,
)
