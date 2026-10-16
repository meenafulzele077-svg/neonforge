package com.neonforge.launcher.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.profilesStore by preferencesDataStore(name = "nf_profiles")
val Context.settingsStore by preferencesDataStore(name = "nf_settings")
