package com.neonforge.launcher.density

data class DensityState(val physical: Int, val override: Int?) {
    val effective: Int get() = override ?: physical
}

/**
 * Strict parser for `wm density` output.
 * Expected lines: "Physical density: N" and optional "Override density: M".
 * Returns null when the physical line is missing or non-numeric (never guesses).
 */
object WmDensityParser {
    fun parse(output: String): DensityState? {
        var physical: Int? = null
        var override: Int? = null
        for (raw in output.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("Physical density:") ->
                    physical = line.substringAfter(':').trim().toIntOrNull()
                line.startsWith("Override density:") ->
                    override = line.substringAfter(':').trim().toIntOrNull()
            }
        }
        return physical?.let { DensityState(it, override) }
    }
}
