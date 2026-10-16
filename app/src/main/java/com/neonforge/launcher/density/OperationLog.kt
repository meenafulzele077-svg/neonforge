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

@Serializable
data class OpRecord(
    val timestamp: Long,
    val operation: String,
    val exitCode: Int,
    val detail: String,
    val success: Boolean
)

private val Context.opStore by preferencesDataStore(name = "nf_oplog")

class OperationLog(private val context: Context) {
    private val key = stringPreferencesKey("oplog_json")
    private val json = Json { ignoreUnknownKeys = true }

    val records: Flow<List<OpRecord>> = context.opStore.data.map { p ->
        val raw = p[key] ?: ""
        if (raw.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<OpRecord>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun record(operation: String, exitCode: Int, detail: String, success: Boolean) {
        context.opStore.edit { p ->
            val raw = p[key] ?: ""
            val current = if (raw.isBlank()) mutableListOf()
            else runCatching {
                json.decodeFromString<List<OpRecord>>(raw).toMutableList()
            }.getOrDefault(mutableListOf())
            current.add(0, OpRecord(System.currentTimeMillis(), operation, exitCode, detail, success))
            p[key] = json.encodeToString(current.take(20))
        }
    }
}
