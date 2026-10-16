package com.neonforge.launcher.density

/**
 * Conservative validation rules for logical display density targets.
 * Reused verbatim by the Phase 3 density controller.
 */
object DensityPolicy {
    const val MIN_DENSITY = 160
    const val MAX_DENSITY = 1200

    val PRESETS = listOf(320, 400, 440, 480, 512, 560, 640)

    fun validate(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Density value is empty"))
        }
        if (!trimmed.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Only integer digits are allowed"))
        }
        val value = trimmed.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Value out of integer range"))
        if (value < MIN_DENSITY || value > MAX_DENSITY) {
            return Result.failure(
                IllegalArgumentException("Value must be between $MIN_DENSITY and $MAX_DENSITY")
            )
        }
        return Result.success(value)
    }
}
