package com.neonforge.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/**
 * PHASE 2 SCAFFOLD PLACEHOLDER.
 * Real dashboard / navigation / design system arrive in the next Phase 2 chunk.
 * No system operations are performed here.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldPlaceholder()
        }
    }
}

@Composable
fun ScaffoldPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "NEONFORGE — Phase 2 scaffold. UI integration pending.",
            color = Color(0xFFF4F7FF),
            fontSize = 14.sp
        )
    }
}
