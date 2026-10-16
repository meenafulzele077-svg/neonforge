package com.neonforge.launcher.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone

@Composable
fun DiagnosticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineMedium)

        NfCard {
            Text("Device Environment", style = MaterialTheme.typography.titleMedium)
            InfoRow("Model", Build.MODEL)
            InfoRow("Android", Build.VERSION.RELEASE)
            InfoRow("SDK", Build.VERSION.SDK_INT.toString())
            InfoRow("Manufacturer", Build.MANUFACTURER)
            InfoRow("Fingerprint", Build.FINGERPRINT)
        }

        NfCard {
            Text("System Access", style = MaterialTheme.typography.titleMedium)
            InfoRow("Shizuku Bridge", "NOT INTEGRATED")
            InfoRow("Current Density", "UNREADABLE")
            InfoRow("Baseline Density", "NONE SAVED")
        }
        
        NfCard {
            Text("Session State", style = MaterialTheme.typography.titleMedium)
            NfStatusChip("IDLE", NfStatusTone.NEUTRAL)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
