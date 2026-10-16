package com.neonforge.launcher.density

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DensityPolicyTest {
    @Test
    fun validate_acceptsValidRange() {
        assertTrue(DensityPolicy.validate("480").isSuccess)
        assertEquals(480, DensityPolicy.validate("480").getOrNull())
    }

    @Test
    fun validate_rejectsNonDigits() {
        assertTrue(DensityPolicy.validate("480dpi").isFailure)
        assertTrue(DensityPolicy.validate("abc").isFailure)
    }

    @Test
    fun validate_rejectsOutOfBounds() {
        assertTrue(DensityPolicy.validate("100").isFailure)
        assertTrue(DensityPolicy.validate("2000").isFailure)
    }
    
    @Test
    fun validate_rejectsEmpty() {
        assertTrue(DensityPolicy.validate("").isFailure)
    }
}
