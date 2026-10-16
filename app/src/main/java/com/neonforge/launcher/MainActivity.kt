package com.neonforge.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.ui.components.NfCard
import com.neonforge.launcher.ui.components.NfStatusChip
import com.neonforge.launcher.ui.components.NfStatusTone
import com.neonforge.launcher.ui.theme.NeonForgeTheme

/**
 * PHASE 2 STEP 2-A: design system active.
 * Navigation + real screens arrive in Step 2-B. No system operations here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonForgeTheme {
                ScaffoldPlaceholder()
            }
        }
    }
}

@Composable
fun ScaffoldPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("NEONFORGE", style = MaterialTheme.typography.displaySmall)
        Text(
            "Phase 2 design system active. Screens arrive next.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        NfCard {
            Text("Privilege bridge", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            NfStatusChip(label = "SHIZUKU: NOT INTEGRATED YET", tone = NfStatusTone.WARNING)
        }
    }
}
