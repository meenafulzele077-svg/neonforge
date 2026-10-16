package com.neonforge.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.data.DpiProfile
import com.neonforge.launcher.data.ProfileRepository
import com.neonforge.launcher.density.DensityPolicy
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfPrimaryButton
import com.neonforge.launcher.ui.theme.NfError
import com.neonforge.launcher.ui.theme.NfMidnight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    packageName: String,
    profileRepo: ProfileRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLabel = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        }.getOrDefault(packageName)
    }

    val profiles by profileRepo.profiles.collectAsState(initial = emptyList())
    val existingProfile = profiles.firstOrNull { it.packageName == packageName }

    var displayName by remember { mutableStateOf(appLabel) }
    var densityInput by remember { mutableStateOf("480") }
    var enabled by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            displayName = it.displayName
            densityInput = it.requestedDensity.toString()
            enabled = it.enabled
            notes = it.notes
        }
    }

    Scaffold(
        containerColor = NfMidnight,
        topBar = {
            TopAppBar(
                title = { Text(if (existingProfile != null) "Edit Profile" else "New Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existingProfile != null) {
                        IconButton(onClick = {
                            scope.launch {
                                profileRepo.delete(packageName)
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NfMidnight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            NfCard {
                Text("Display Name", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            NfCard {
                Text("Target Density (DPI)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = densityInput,
                    onValueChange = { 
                        if (it.all { c -> c.isDigit() }) {
                            densityInput = it
                            validationError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter DPI") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Valid range: ${DensityPolicy.MIN_DENSITY} - ${DensityPolicy.MAX_DENSITY}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Quick presets:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DensityPolicy.PRESETS.take(4).forEach { preset ->
                        FilledTonalButton(
                            onClick = { densityInput = preset.toString(); validationError = null },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("$preset", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                if (validationError != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(validationError!!, style = MaterialTheme.typography.labelSmall, color = NfError)
                }
            }

            NfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Profile", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Apply this density when the game launches",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }

            NfCard {
                Text("Notes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )
            }

            NfPrimaryButton(
                text = if (existingProfile != null) "UPDATE PROFILE" else "SAVE PROFILE",
                onClick = {
                    val result = DensityPolicy.validate(densityInput)
                    if (result.isFailure) {
                        validationError = result.exceptionOrNull()?.message
                    } else {
                        val now = System.currentTimeMillis()
                        val profile = DpiProfile(
                            packageName = packageName,
                            displayName = displayName.ifBlank { appLabel },
                            requestedDensity = result.getOrNull()!!,
                            enabled = enabled,
                            createdAt = existingProfile?.createdAt ?: now,
                            updatedAt = now,
                            notes = notes
                        )
                        scope.launch {
                            profileRepo.upsert(profile)
                            onBack()
                        }
                    }
                }
            )
        }
    }
}
