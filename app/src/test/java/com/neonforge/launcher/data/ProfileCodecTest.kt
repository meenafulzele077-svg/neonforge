package com.neonforge.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileCodecTest {
    @Test
    fun encodeDecode_roundTrip() {
        val profiles = listOf(
            DpiProfile("com.test.game", "Test Game", 480, true, 100L, 100L, "NONE", "Notes")
        )
        val json = ProfileCodec.encode(profiles)
        val decoded = ProfileCodec.decode(json)
        assertEquals(profiles, decoded)
    }

    @Test
    fun decode_handlesBlank() {
        assertEquals(emptyList<DpiProfile>(), ProfileCodec.decode(""))
    }
    
    @Test
    fun decode_handlesInvalidJson() {
        assertEquals(emptyList<DpiProfile>(), ProfileCodec.decode("{invalid json"))
    }
}
