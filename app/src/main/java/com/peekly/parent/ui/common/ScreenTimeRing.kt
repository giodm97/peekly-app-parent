package com.peekly.parent.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peekly.parent.ui.theme.AmberAccent
import com.peekly.parent.ui.theme.GreenAccent
import com.peekly.parent.ui.theme.RedAccent

@Composable
fun ScreenTimeRing(
    rating: String,
    size: Dp = 52.dp,
    strokeWidth: Dp = 5.dp
) {
    val (progress, color, label) = when (rating.lowercase()) {
        "low"    -> Triple(0.25f, GreenAccent, "25%")
        "medium" -> Triple(0.60f, AmberAccent, "60%")
        "high"   -> Triple(0.90f, RedAccent,   "90%")
        else     -> Triple(0f,    GreenAccent, "—")
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val diameter = this.size.minDimension - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color      = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft    = topLeft,
                size       = arcSize
            )
            drawArc(
                color      = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter  = false,
                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft    = topLeft,
                size       = arcSize
            )
        }
        Text(
            text  = label,
            color = color,
            fontSize = (size.value * 0.22f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
