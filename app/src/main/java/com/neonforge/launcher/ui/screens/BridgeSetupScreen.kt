package com.neonforge.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.privilege.BridgeConfig
import com.neonforge.launcher.ui.components.NfCard
import kotlinx.coroutines.launch

@Composable
fun BridgeSetupScreen(
    bridgeConfig: BridgeConfig,
    onBack: () -> Unit,
    onTestConnection: () -> Unit
) {
    val storedToken by bridgeConfig.token.collectAsState(initial = "")
    val storedPort by bridgeConfig.port.collectAsState(initial = "38471")
    val scope = rememberCoroutineScope()

    var tokenInput by remember { mutableStateOf(storedToken) }
    var portInput by remember { mutableStateOf(storedPort) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text("Bridge Setup", style = MaterialTheme.typography.headlineMedium)

        NfCard {
            Text(
                "Termux Bridge Configuration",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Termux Bridge Configuration\n\nPaste the bridge token from Termux:\n\ncat ~/neonforge_bridge/.token",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                label = { Text("Bridge Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = portInput,
                onValueChange = { portInput = it },
                label = { Text("Bridge Port") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        bridgeConfig.setToken(tokenInput)
                        bridgeConfig.setPort(portInput)
                        onTestConnection()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE & TEST CONNECTION")
            }
        }

        NfCard {
            Text("Prerequisites", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "1. Termux bridge must be running: ~/neonforge_bridge/nf_up.sh\n" +
                "2. Copy the token from: cat ~/neonforge_bridge/.token\n" +
                "3. Paste it above and save",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
