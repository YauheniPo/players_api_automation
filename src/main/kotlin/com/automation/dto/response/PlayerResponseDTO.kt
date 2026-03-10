package com.automation.dto.response

import com.automation.dto.StringLength
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerResponseDTO(
    // TODO https://github.com/YauheniPo/players_api_automation/issues/3
    @JsonAlias("id")
    val _id: String,
    @StringLength(min = 4)
    val username: String,
    @StringLength(min = 4)
    val email: String,
    @StringLength(min = 4)
    val name: String,
    @StringLength(min = 4)
    val surname: String,
) {
    companion object {
        val BY_NAME: Comparator<PlayerResponseDTO> = compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
}
