package com.automation.dto.request

data class CredentialsLoginDTO(
    val grantType: String,
    val username: String? = null,
    val password: String? = null,
)
