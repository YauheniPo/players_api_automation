package com.automation.dto.request

import com.automation.dto.StringLength
import com.fasterxml.jackson.annotation.JsonProperty

data class PlayerRequestDTO(
    @JsonProperty("currency_code") val currencyCode: String,
    @StringLength(min = 5)
    val email: String,
    @StringLength(min = 2)
    val name: String,
    @StringLength(min = 6)
    @JsonProperty("password_change") val passwordChange: String,
    @StringLength(min = 6)
    @JsonProperty("password_repeat") val passwordRepeat: String,
    @StringLength(min = 2)
    val surname: String,
    @StringLength(min = 3)
    val username: String,
)
