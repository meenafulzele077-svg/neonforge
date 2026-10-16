package com.neonforge.launcher.data

import kotlinx.serialization.Serializable

@Serializable
data class DpiProfile(
    val packageName: String,
    val displayName: String,
    val requestedDensity: Int,
    val enabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val lastKnownResult: String = "NONE",
    val notes: String = ""
)
