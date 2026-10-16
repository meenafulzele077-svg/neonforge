package com.neonforge.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class NfSettings(
    val soundEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val animationIntensity: Int = 70,
    val monitoringIntervalSec: Int = 5,
    val selectedPackage: String = ""
)

class SettingsRepository(private val context: Context) {
    private val kSound = booleanPreferencesKey("sound_enabled")
    private val kHaptics = booleanPreferencesKey("haptics_enabled")
    private val kReducedMotion = booleanPreferencesKey("reduced_motion")
    private val kAnim = intPreferencesKey("animation_intensity")
    private val kMonitor = intPreferencesKey("monitoring_interval_sec")
    private val kSelected = stringPreferencesKey("selected_package")

    val settings: Flow<NfSettings> = context.settingsStore.data.map { p ->
        NfSettings(
            soundEnabled = p[kSound] ?: false,
            hapticsEnabled = p[kHaptics] ?: true,
            reducedMotion = p[kReducedMotion] ?: false,
            animationIntensity = p[kAnim] ?: 70,
            monitoringIntervalSec = p[kMonitor] ?: 5,
            selectedPackage = p[kSelected] ?: ""
        )
    }

    suspend fun setSound(v: Boolean) = context.settingsStore.edit { it[kSound] = v }
    suspend fun setHaptics(v: Boolean) = context.settingsStore.edit { it[kHaptics] = v }
    suspend fun setReducedMotion(v: Boolean) = context.settingsStore.edit { it[kReducedMotion] = v }
    suspend fun setAnimationIntensity(v: Int) = context.settingsStore.edit { it[kAnim] = v.coerceIn(0, 100) }
    suspend fun setMonitoringInterval(v: Int) = context.settingsStore.edit { it[kMonitor] = v.coerceIn(2, 60) }
    suspend fun setSelectedPackage(pkg: String) = context.settingsStore.edit { it[kSelected] = pkg }
}
