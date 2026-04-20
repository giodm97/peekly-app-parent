package com.peekly.parent.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

@Composable
fun ScreenTimeChip(rating: String) {
    val (label, color) = when (rating.lowercase()) {
        "low"    -> "Low"    to Color(0xFF2E7D32)
        "medium" -> "Medium" to Color(0xFFE65100)
        "high"   -> "High"   to Color(0xFFC62828)
        else     -> rating   to MaterialTheme.colorScheme.outline
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
