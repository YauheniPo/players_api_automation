package com.automation.utils

import com.automation.dto.request.PlayerRequestDTO
import net.datafaker.Faker
import java.util.concurrent.atomic.AtomicInteger

object DataGenerator {
    private val faker = Faker()
    private val counter = AtomicInteger(1)

    fun generatePlayer(): PlayerRequestDTO {
        val idx = counter.getAndIncrement()
        // no special chars — some APIs reject them in passwords
        val pass = faker.internet().password(6, 12, true, false, true)
        val suffix = "${idx}_${faker.number().digits(4)}"

        return PlayerRequestDTO(
            currencyCode = faker.money().currencyCode(),
            email = "player_$suffix@automation.test",
            name = faker.name().firstName().letters(fallback = "TestName"),
            surname = faker.name().lastName().letters(fallback = "TestSurname"),
            username = "usr_$suffix",
            passwordChange = pass,
            passwordRepeat = pass,
        )
    }

    fun generatePlayers(count: Int): List<PlayerRequestDTO> = (1..count).map { generatePlayer() }

    // strips non-ASCII letters (apostrophes, hyphens, accented chars, etc.)
    private fun String.letters(fallback: String): String = replace(Regex("[^a-zA-Z]"), "").ifEmpty { fallback }
}
