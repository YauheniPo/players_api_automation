package com.automation.tests

import com.automation.base.BaseTest
import com.automation.dto.request.PlayerRequestDTO
import com.automation.dto.request.PlayerRequestOneDTO
import com.automation.dto.response.PlayerResponseDTO
import com.automation.utils.DataGenerator
import com.automation.utils.TestContext
import io.qameta.allure.Description
import io.qameta.allure.Feature
import io.qameta.allure.Story
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.testng.annotations.AfterClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Feature("Player Management")
class PlayerTest : BaseTest() {
    companion object {
        private const val PLAYERS_COUNT = 12
    }

    // ── 1. Create 12 players ─────────────────────────────────────────────────

    @DataProvider(name = "playerData", parallel = true)
    fun playerData(): Array<Array<Any>> =
        DataGenerator.generatePlayers(PLAYERS_COUNT)
            .map { arrayOf<Any>(it) }
            .toTypedArray()

    @Test(
        dataProvider = "playerData",
        groups = ["create"],
        description = "Register $PLAYERS_COUNT players in parallel and verify 2xx + response body",
    )
    @Story("Player registration")
    @Description(
        "POST /api/automationTask/create x$PLAYERS_COUNT (parallel)\n" +
            "Expected: 2xx response, body contains required fields.",
    )
    fun createPlayers(request: PlayerRequestDTO) {
        val created = playerApiClient().createPlayer(request).assertSuccess()
        TestContext.addCreatedPlayer(created)
    }

    // ── 2. Get one player profile ─────────────────────────────────────────────

    @Test(
        dependsOnGroups = ["create"],
        groups = ["read"],
        description = "Fetch profile of the first created player by email and verify the email matches",
    )
    @Story("Get player profile")
    @Description(
        "POST /api/automationTask/getOne\n" +
            "Expected: 2xx response, returned player's email matches the requested email.",
    )
    fun getOnePlayer() {
        val players = TestContext.createdPlayers
        assertThat(players).`as`("created players list").isNotEmpty()

        val target = players.first()
        val email = requireNotNull(target.email) { "email of first created player must not be null" }

        val profile = playerApiClient()
            .getOnePlayer(PlayerRequestOneDTO(email = email))
            .assertSuccess()

        assertThat(profile.email).`as`("email").isEqualTo(email)
    }

    // ── 3. Get all players, sort by name ──────────────────────────────────────

    @Test(
        dependsOnGroups = ["create"],
        groups = ["read"],
        description = "Fetch all players and verify HTTP 200 + response body",
    )
    @Story("Get all players sorted by name")
    @Description("GET /api/automationTask/getAll")
    fun getAllPlayersSortedByName() {
        @Suppress("ktlint:standard:backing-property-naming")
        val _sortedPlayers = playerApiClient()
            .getAllPlayers()
            .assertSuccess()
            .sortedWith(PlayerResponseDTO.BY_NAME)
    }

    // ── 4. Delete all created players ─────────────────────────────────────────

    @Test(
        dependsOnGroups = ["read"],
        groups = ["delete"],
        description = "Delete every player created in the 'create' phase",
    )
    @Story("Delete players")
    @Description(
        "DELETE /api/automationTask/deleteOne/{id}\n" +
            "Expected: each deletion returns a 2xx status code.",
    )
    fun deleteAllCreatedPlayers() {
        val players = TestContext.createdPlayers
        assertThat(players).`as`("players to delete").isNotEmpty()

        val client = playerApiClient()
        SoftAssertions.assertSoftly { softly ->
            players.forEach { player ->
                softly.check {
                    client.deletePlayer(player._id).assertSuccess()
                }
            }
        }
    }

    // ── 5. Verify created players were actually deleted ───────────────────────

    @Test(
        dependsOnGroups = ["delete"],
        groups = ["verify"],
        description = "Verify none of the created players appear in getAll after deletion",
    )
    @Story("Verify created players were deleted")
    @Description(
        "GET /api/automationTask/getAll\n" +
            "Expected: HTTP 200, none of the players created in 'create' phase appear in the response.",
    )
    fun playerListIsEmptyAfterDeletion() {
        val allPlayers = playerApiClient()
            .getAllPlayers()
            .assertSuccess()

        val createdIds = TestContext.createdPlayers.map { it._id }.toSet()

        assertThat(allPlayers.map { it._id })
            .`as`("created players must not appear in the list after deletion")
            .doesNotContainAnyElementsOf(createdIds)
    }

    @AfterClass(alwaysRun = true)
    fun tearDown() {
        runCatching { TestContext.clearCreatedPlayers() }
            .onFailure { log.warn("tearDown: failed to clear created players context.", it) }
    }
}
