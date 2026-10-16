package com.neonforge.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val context: Context) {
    private val key = stringPreferencesKey("profiles_json")

    val profiles: Flow<List<DpiProfile>> =
        context.profilesStore.data.map { prefs -> ProfileCodec.decode(prefs[key] ?: "") }

    suspend fun upsert(profile: DpiProfile) {
        context.profilesStore.edit { prefs ->
            val current = ProfileCodec.decode(prefs[key] ?: "").toMutableList()
            val idx = current.indexOfFirst { it.packageName == profile.packageName }
            if (idx >= 0) current[idx] = profile else current.add(profile)
            prefs[key] = ProfileCodec.encode(current)
        }
    }

    suspend fun delete(packageName: String) {
        context.profilesStore.edit { prefs ->
            val current = ProfileCodec.decode(prefs[key] ?: "").filterNot { it.packageName == packageName }
            prefs[key] = ProfileCodec.encode(current)
        }
    }
}
