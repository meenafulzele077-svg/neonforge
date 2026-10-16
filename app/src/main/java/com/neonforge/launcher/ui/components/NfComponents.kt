package com.neonforge.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neonforge.launcher.ui.theme.NfCyan
import com.neonforge.launcher.ui.theme.NfError
import com.neonforge.launcher.ui.theme.NfOutline
import com.neonforge.launcher.ui.theme.NfSuccess
import com.neonforge.launcher.ui.theme.NfTextSecondary
import com.neonforge.launcher.ui.theme.NfWarning

@Composable
fun NfCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NfOutline, MaterialTheme.shapes.medium)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun NfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NfCyan, contentColor = Color.Black)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

enum class NfStatusTone { NEUTRAL, SUCCESS, WARNING, ERROR }

@Composable
fun NfStatusChip(label: String, tone: NfStatusTone) {
    val color = when (tone) {
        NfStatusTone.NEUTRAL -> NfTextSecondary
        NfStatusTone.SUCCESS -> NfSuccess
        NfStatusTone.WARNING -> NfWarning
        NfStatusTone.ERROR -> NfError
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
