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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.ShizukuUiState
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
    shizukuState: ShizukuUiState,
    session: SessionSnapshot,
    journal: RecoveryJournal?,
    lastOp: OpRecord?,
    launchError: String?,
    onReadDensity: () -> Unit,
    onRequestPermission: () -> Unit,
    onRefreshStatus: () -> Unit,
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
    val guidance = when (shizukuState) {
        ShizukuUiState.NOT_INSTALLED -> "Install Shizuku from the official source, then return here."
        ShizukuUiState.NOT_RUNNING -> "Start Shizuku (Wireless debugging) in the Shizuku app, then return here or tap REFRESH STATUS."
        ShizukuUiState.NOT_AUTHORIZED -> "Tap GRANT ACCESS and choose Allow in the Shizuku dialog."
        ShizukuUiState.READY -> "Privileged bridge ready. Every operation is verified by re-reading the system."
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
            Text("System access", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NfStatusChip(
                    when (shizukuState) {
                        ShizukuUiState.NOT_INSTALLED -> "SHIZUKU: NOT INSTALLED"
                        ShizukuUiState.NOT_RUNNING -> "SHIZUKU: NOT RUNNING"
                        ShizukuUiState.NOT_AUTHORIZED -> "SHIZUKU: NOT AUTHORIZED"
                        ShizukuUiState.READY -> "SHIZUKU: READY"
                    },
                    when (shizukuState) {
                        ShizukuUiState.NOT_INSTALLED -> NfStatusTone.ERROR
                        ShizukuUiState.NOT_RUNNING -> NfStatusTone.WARNING
                        ShizukuUiState.NOT_AUTHORIZED -> NfStatusTone.WARNING
                        ShizukuUiState.READY -> NfStatusTone.SUCCESS
                    }
                )
                NfStatusChip("SESSION: ${session.phase.name}", phaseTone)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                guidance,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                session.currentDensity?.let {
                    "Current density: $it (physical ${session.physicalDensity})"
                } ?: "Current density: not read yet or unreadable",
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
                FilledTonalButton(onClick = onReadDensity, enabled = !busy) {
                    Text("READ DENSITY")
                }
                FilledTonalButton(onClick = onRefreshStatus) {
                    Text("REFRESH STATUS")
                }
                if (shizukuState == ShizukuUiState.NOT_AUTHORIZED) {
                    FilledTonalButton(onClick = onRequestPermission) {
                        Text("GRANT ACCESS")
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
                    enabled = shizukuState == ShizukuUiState.READY && !busy
                )
                if (session.phase == SessionPhase.IN_SESSION) {
                    Spacer(Modifier.height(8.dp))
                    NfPrimaryButton("RESTORE ORIGINAL DENSITY", onClick = onRestore)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Launch is enabled only with Shizuku READY. Every apply/restore is verified by re-reading the system.",
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
