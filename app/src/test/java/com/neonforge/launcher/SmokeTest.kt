package com.neonforge.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

/** Toolchain smoke test only. Real density/parser tests arrive in Phase 3. */
class SmokeTest {
    @Test
    fun arithmeticSanity() {
        assertEquals(4, 2 + 2)
    }
}
