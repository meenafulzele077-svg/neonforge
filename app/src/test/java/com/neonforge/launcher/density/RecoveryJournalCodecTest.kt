package com.neonforge.launcher.density

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryJournalCodecTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun journal_roundTrip() {
        val journal = RecoveryJournal(
            sessionActive = true,
            gamePackage = "com.test.game",
            gameLabel = "Test Game",
            physicalDensity = 440,
            baselineOverride = null,
            targetDensity = 512,
            startedAt = 123456789L
        )
        val decoded = json.decodeFromString<RecoveryJournal>(json.encodeToString(journal))
        assertEquals(journal, decoded)
    }

    @Test
    fun journal_roundTrip_withOverrideBaseline() {
        val journal = RecoveryJournal(
            sessionActive = true,
            gamePackage = "com.test.game2",
            gameLabel = "Test Game 2",
            physicalDensity = 440,
            baselineOverride = 480,
            targetDensity = 320,
            startedAt = 987654321L
        )
        val decoded = json.decodeFromString<RecoveryJournal>(json.encodeToString(journal))
        assertEquals(journal, decoded)
    }
}
