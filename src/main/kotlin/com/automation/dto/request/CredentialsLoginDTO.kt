package com.automation.dto.request

data class CredentialsLoginDTO(
    val grantType: String, // required
    val username: String? = null, // optional per spec
    val password: String? = null, // optional per spec
)
