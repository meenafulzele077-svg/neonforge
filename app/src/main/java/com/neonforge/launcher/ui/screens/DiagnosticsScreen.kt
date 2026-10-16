package com.neonforge.launcher.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.ShizukuUiState
import com.neonforge.launcher.density.OpRecord
import com.neonforge.launcher.density.SessionPhase
import com.neonforge.launcher.density.SessionSnapshot
import com.neonforge.launcher.shizuku.BinderDeliveryInfo
import com.neonforge.launcher.shizuku.ShizukuBridge
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    shizukuState: ShizukuUiState,
    binderAlive: Boolean,
    permissionGranted: Boolean,
    session: SessionSnapshot,
    ops: List<OpRecord>,
    delivery: BinderDeliveryInfo
) {
    val timeFormat = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.US) }
    val serverVersion = remember { ShizukuBridge.serverVersion() }

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
            InfoRow("Shizuku state", shizukuState.name)
            InfoRow("Binder alive", binderAlive.toString())
            InfoRow("Permission granted", permissionGranted.toString())
            InfoRow("Shizuku server version", serverVersion?.toString() ?: "unknown")
        }

        NfCard {
            Text("Binder Delivery Chain", style = MaterialTheme.typography.titleMedium)
            InfoRow("Provider class in APK", delivery.providerClassPresent.toString())
            InfoRow("Own provider registered", delivery.ownProviderRegistered.toString())
            InfoRow("Shizuku provider visible", delivery.serverProviderVisible.toString())
            InfoRow("Active pull result", delivery.requestInfo)
        }

        NfCard {
            Text("Session State", style = MaterialTheme.typography.titleMedium)
            NfStatusChip(
                session.phase.name,
                when (session.phase) {
                    SessionPhase.IDLE -> NfStatusTone.NEUTRAL
                    SessionPhase.APPLYING, SessionPhase.RESTORING -> NfStatusTone.WARNING
                    SessionPhase.IN_SESSION -> NfStatusTone.SUCCESS
                    SessionPhase.ERROR -> NfStatusTone.ERROR
                }
            )
            if (session.message.isNotBlank()) {
                Spacer(Modifier.padding(top = 4.dp))
                Text(session.message, style = MaterialTheme.typography.bodyMedium)
            }
        }

        NfCard {
            Text("Operation Log (real results)", style = MaterialTheme.typography.titleMedium)
            if (ops.isEmpty()) {
                Text(
                    "No operations recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ops.forEach { op ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                timeFormat.format(Date(op.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            NfStatusChip(
                                if (op.success) "OK" else "FAIL exit=${op.exitCode}",
                                if (op.success) NfStatusTone.SUCCESS else NfStatusTone.ERROR
                            )
                        }
                        Text("${op.operation}: ${op.detail}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
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
