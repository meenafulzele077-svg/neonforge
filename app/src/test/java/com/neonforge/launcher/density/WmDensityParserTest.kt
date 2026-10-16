package com.neonforge.launcher.density

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WmDensityParserTest {
    @Test
    fun parse_physicalOnly() {
        val state = WmDensityParser.parse("Physical density: 440")
        assertEquals(440, state?.physical)
        assertNull(state?.override)
        assertEquals(440, state?.effective)
    }

    @Test
    fun parse_physicalAndOverride() {
        val state = WmDensityParser.parse("Physical density: 440\nOverride density: 512")
        assertEquals(440, state?.physical)
        assertEquals(512, state?.override)
        assertEquals(512, state?.effective)
    }

    @Test
    fun parse_garbageReturnsNull() {
        assertNull(WmDensityParser.parse("cmd: Can't find service: window"))
        assertNull(WmDensityParser.parse(""))
        assertNull(WmDensityParser.parse("Physical density: abc"))
    }
}
