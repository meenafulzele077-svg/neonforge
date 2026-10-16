package com.neonforge.launcher.privilege

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bridgeStore by preferencesDataStore(name = "nf_bridge_config")

class BridgeConfig(private val context: Context) {
    private val keyToken = stringPreferencesKey("bridge_token")
    private val keyPort = stringPreferencesKey("bridge_port")

    val token: Flow<String> = context.bridgeStore.data.map { it[keyToken] ?: "" }
    val port: Flow<String> = context.bridgeStore.data.map { it[keyPort] ?: "38471" }

    suspend fun setToken(value: String) {
        context.bridgeStore.edit { it[keyToken] = value }
    }

    suspend fun setPort(value: String) {
        context.bridgeStore.edit { it[keyPort] = value }
    }
}
