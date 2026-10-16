package com.neonforge.launcher.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.data.DpiProfile
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone
import com.neonforge.launcher.ui.rememberAppIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LaunchableApp(val packageName: String, val label: String)

@Composable
fun LibraryScreen(profiles: List<DpiProfile>, onOpenProfile: (String) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<LaunchableApp>>(emptyList()) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = runCatching {
            withContext(Dispatchers.Default) {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(intent, 0)
                    .map { it.activityInfo }
                    .filter { it.packageName != context.packageName }
                    .map { LaunchableApp(it.packageName, it.loadLabel(pm).toString()) }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }
            }
        }
        result.onSuccess { apps = it }.onFailure { loadError = true }
    }

    val filtered = apps.filter {
        query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Game Library", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search installed apps") },
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        when {
            loadError -> Text(
                "Could not query launchable apps on this firmware. See diagnostics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            apps.isEmpty() -> Text(
                "Loading installed launchable apps...",
                style = MaterialTheme.typography.bodyMedium
            )
            filtered.isEmpty() -> Text(
                "No apps match the current search.",
                style = MaterialTheme.typography.bodyMedium
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    val profile = profiles.firstOrNull { it.packageName == app.packageName }
                    LibraryRow(app = app, profile = profile, onClick = { onOpenProfile(app.packageName) })
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(app: LaunchableApp, profile: DpiProfile?, onClick: () -> Unit) {
    val icon = rememberAppIcon(app.packageName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(44.dp))
        } else {
            Spacer(Modifier.size(44.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (profile != null) {
            NfStatusChip(
                "DPI ${profile.requestedDensity}",
                if (profile.enabled) NfStatusTone.SUCCESS else NfStatusTone.WARNING
            )
        } else {
            NfStatusChip("NO PROFILE", NfStatusTone.NEUTRAL)
        }
    }
}
