package com.neonforge.launcher.density

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Durable record written BEFORE any density change.
 * Survives app kills and reboots. Cleared only after a verified restore.
 */
@Serializable
data class RecoveryJournal(
    val sessionActive: Boolean,
    val gamePackage: String,
    val gameLabel: String,
    val physicalDensity: Int,
    val baselineOverride: Int?,
    val targetDensity: Int,
    val startedAt: Long
)

private val Context.journalStore by preferencesDataStore(name = "nf_journal")

class RecoveryJournalRepo(private val context: Context) {
    private val key = stringPreferencesKey("journal_json")
    private val json = Json { ignoreUnknownKeys = true }

    val journal: Flow<RecoveryJournal?> = context.journalStore.data.map { p ->
        val raw = p[key] ?: ""
        if (raw.isBlank()) null
        else runCatching { json.decodeFromString<RecoveryJournal>(raw) }.getOrNull()
    }

    suspend fun begin(journal: RecoveryJournal) {
        context.journalStore.edit { it[key] = json.encodeToString(journal) }
    }

    suspend fun clear() {
        context.journalStore.edit { it.remove(key) }
    }
}
