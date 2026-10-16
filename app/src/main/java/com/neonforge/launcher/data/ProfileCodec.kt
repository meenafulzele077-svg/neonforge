package com.neonforge.launcher.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ProfileCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(profiles: List<DpiProfile>): String = json.encodeToString(profiles)

    fun decode(raw: String): List<DpiProfile> =
        if (raw.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<DpiProfile>>(raw) }.getOrDefault(emptyList())
}
