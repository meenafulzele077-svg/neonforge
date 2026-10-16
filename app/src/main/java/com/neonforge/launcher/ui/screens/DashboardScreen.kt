package com.neonforge.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.data.DpiProfile
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfPrimaryButton
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone

@Composable
fun DashboardScreen(
    profiles: List<DpiProfile>,
    selected: DpiProfile?,
    onSelectGame: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("NEONFORGE", style = MaterialTheme.typography.displaySmall)
                Text(
                    "Gaming density command center",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenDiagnostics) {
                Icon(Icons.Default.Info, contentDescription = "Diagnostics")
            }
        }

        NfCard {
            Text("System access", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NfStatusChip("SHIZUKU: NOT INTEGRATED", NfStatusTone.WARNING)
                NfStatusChip("SESSION: IDLE", NfStatusTone.NEUTRAL)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Current density: unreadable until Shizuku integration (Phase 3). No value is guessed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Baseline: none saved yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NfCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Configured profiles", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text("${profiles.size}", style = MaterialTheme.typography.headlineMedium)
            }
        }

        if (selected == null) {
            NfCard {
                Text("No game selected", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Pick an installed app from the library to create its density profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                NfPrimaryButton("OPEN GAME LIBRARY", onSelectGame)
            }
        } else {
            NfCard {
                Text("SELECTED GAME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selected.displayName, style = MaterialTheme.typography.titleLarge)
                Text(selected.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NfStatusChip("TARGET DPI ${selected.requestedDensity}", NfStatusTone.SUCCESS)
                    if (!selected.enabled) {
                        NfStatusChip("PROFILE DISABLED", NfStatusTone.ERROR)
                    }
                }
                Spacer(Modifier.height(10.dp))
                NfPrimaryButton("LAUNCH GAME", onClick = { }, enabled = false)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Launch enables after verified Shizuku density integration (Phase 3).",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "Recent operation: none recorded yet. Real results appear once the density engine is integrated.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
