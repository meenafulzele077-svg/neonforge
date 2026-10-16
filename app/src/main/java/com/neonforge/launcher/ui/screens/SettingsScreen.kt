package com.neonforge.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.data.NfSettings
import com.neonforge.launcher.data.SettingsRepository
import com.neonforge.launcher.ui.components.NfCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsRepo: SettingsRepository) {
    val settings by settingsRepo.settings.collectAsState(initial = NfSettings())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        NfCard {
            Text("Feedback", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Haptic Feedback")
                    Text("Vibrate on interactions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.hapticsEnabled, onCheckedChange = { scope.launch { settingsRepo.setHaptics(it) } })
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Sound Effects")
                    Text("Play UI sounds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.soundEnabled, onCheckedChange = { scope.launch { settingsRepo.setSound(it) } })
            }
        }

        NfCard {
            Text("Motion & Animation", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Reduced Motion")
                    Text("Minimize cinematic transitions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.reducedMotion, onCheckedChange = { scope.launch { settingsRepo.setReducedMotion(it) } })
            }
            Spacer(Modifier.height(16.dp))
            Text("Animation Intensity: ${settings.animationIntensity}%")
            Slider(
                value = settings.animationIntensity.toFloat(),
                onValueChange = { scope.launch { settingsRepo.setAnimationIntensity(it.toInt()) } },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        NfCard {
            Text("Background Monitoring", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Text("Check interval: ${settings.monitoringIntervalSec}s")
            Text("Lower values use more battery. (Active monitoring pending Phase 4)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = settings.monitoringIntervalSec.toFloat(),
                onValueChange = { scope.launch { settingsRepo.setMonitoringInterval(it.toInt()) } },
                valueRange = 2f..60f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
