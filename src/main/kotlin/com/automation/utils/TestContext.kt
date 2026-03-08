package com.automation.utils

import com.automation.dto.response.PlayerResponseDTO
import java.util.concurrent.CopyOnWriteArrayList

object TestContext {
    @Volatile
    var authToken: String? = null

    private val _createdPlayers = CopyOnWriteArrayList<PlayerResponseDTO>()

    val createdPlayers: List<PlayerResponseDTO>
        get() = _createdPlayers.toList()

    fun addCreatedPlayer(player: PlayerResponseDTO) {
        _createdPlayers.add(player)
    }

    fun removeCreatedPlayer(player: PlayerResponseDTO) {
        _createdPlayers.remove(player)
    }

    fun clearCreatedPlayers() {
        _createdPlayers.clear()
    }

    fun reset() {
        authToken = null
        _createdPlayers.clear()
    }
}
