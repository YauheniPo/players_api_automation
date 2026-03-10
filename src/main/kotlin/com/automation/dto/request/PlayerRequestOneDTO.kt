package com.automation.dto.request

import com.automation.dto.StringLength

data class PlayerRequestOneDTO(
    @StringLength(min = 5)
    val email: String,
)
