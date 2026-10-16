package com.neonforge.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.data.DpiProfile
import com.neonforge.launcher.density.OpRecord
import com.neonforge.launcher.density.RecoveryJournal
import com.neonforge.launcher.density.SessionPhase
import com.neonforge.launcher.density.SessionSnapshot
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfPrimaryButton
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone
import com.neonforge.launcher.ui.theme.NfError
import com.neonforge.launcher.ui.theme.NfSuccess
import com.neonforge.launcher.ui.theme.NfWarning

@Composable
fun DashboardScreen(
    profiles: List<DpiProfile>,
    selected: DpiProfile?,
    channelReady: Boolean,
    channelDescription: String,
    session: SessionSnapshot,
    journal: RecoveryJournal?,
    lastOp: OpRecord?,
    launchError: String?,
    onReadDensity: () -> Unit,
    onRefreshChannel: () -> Unit,
    onOpenBridgeSetup: () -> Unit,
    onLaunch: () -> Unit,
    onRestore: () -> Unit,
    onSelectGame: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val busy = session.phase == SessionPhase.APPLYING || session.phase == SessionPhase.RESTORING
    val phaseTone = when (session.phase) {
        SessionPhase.IDLE -> NfStatusTone.NEUTRAL
        SessionPhase.APPLYING, SessionPhase.RESTORING -> NfStatusTone.WARNING
        SessionPhase.IN_SESSION -> NfStatusTone.SUCCESS
        SessionPhase.ERROR -> NfStatusTone.ERROR
    }

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

        if (journal != null && !busy && session.phase != SessionPhase.IN_SESSION) {
            NfCard {
                Text(
                    "INTERRUPTED SESSION DETECTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = NfWarning
                )
                Text("${journal.gameLabel} - target ${journal.targetDensity}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "A managed session may have left a density override active.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                NfPrimaryButton("RESTORE BASELINE NOW", onRestore)
            }
        }

        NfCard {
            Text("Privilege Channel", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NfStatusChip(
                    if (channelReady) "CHANNEL: READY" else "CHANNEL: OFFLINE",
                    if (channelReady) NfStatusTone.SUCCESS else NfStatusTone.ERROR
                )
                NfStatusChip("SESSION: ${session.phase.name}", phaseTone)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                channelDescription,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                session.currentDensity?.let {
                    "Current density: $it (physical ${session.physicalDensity})"
                } ?: "Current density: not read yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (session.message.isNotBlank()) {
                Text(
                    session.message,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.phase == SessionPhase.ERROR) NfError else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onReadDensity, enabled = !busy && channelReady) {
                    Text("READ DENSITY")
                }
                FilledTonalButton(onClick = onRefreshChannel) {
                    Text("REFRESH")
                }
                if (!channelReady) {
                    FilledTonalButton(onClick = onOpenBridgeSetup) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Text("SETUP")
                    }
                }
            }
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
                Text(
                    "SELECTED GAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(selected.displayName, style = MaterialTheme.typography.titleLarge)
                Text(
                    selected.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NfStatusChip("TARGET DPI ${selected.requestedDensity}", NfStatusTone.SUCCESS)
                    if (!selected.enabled) {
                        NfStatusChip("PROFILE DISABLED", NfStatusTone.ERROR)
                    }
                }
                Spacer(Modifier.height(10.dp))
                NfPrimaryButton(
                    "LAUNCH GAME",
                    onClick = onLaunch,
                    enabled = channelReady && !busy
                )
                if (session.phase == SessionPhase.IN_SESSION) {
                    Spacer(Modifier.height(8.dp))
                    NfPrimaryButton("RESTORE ORIGINAL DENSITY", onClick = onRestore)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Launch is enabled only when channel is READY. Every apply/restore is verified by re-reading the system.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (launchError != null) {
            Text(launchError, style = MaterialTheme.typography.labelSmall, color = NfError)
        }

        lastOp?.let {
            Text(
                "Last operation: ${it.operation} - ${if (it.success) "success" else "failed (exit ${it.exitCode})"}: ${it.detail}",
                style = MaterialTheme.typography.labelSmall,
                color = if (it.success) NfSuccess else NfError
            )
        } ?: Text(
            "Recent operation: none recorded yet.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
