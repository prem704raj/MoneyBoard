package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

@Composable
fun SkylineIllustration(
    modifier: Modifier = Modifier,
    tint: Color = MoneyBoardColors.SkylineStroke.copy(alpha = 0.45f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val stroke = Stroke(width = 1.2.dp.toPx())

        // Horizontal base baseline
        drawLine(
            color = tint,
            start = Offset(0f, h - 2.dp.toPx()),
            end = Offset(w, h - 2.dp.toPx()),
            strokeWidth = 1.2.dp.toPx()
        )

        // Repeatable architectural silhouettes across width
        val segmentWidth = 180.dp.toPx()
        val numSegments = (w / segmentWidth).toInt() + 2

        for (i in 0 until numSegments) {
            val startX = i * segmentWidth
            drawSkylineSegment(startX, h - 2.dp.toPx(), tint, stroke)
        }
    }
}

private fun DrawScope.drawSkylineSegment(
    startX: Float,
    baseY: Float,
    tint: Color,
    stroke: Stroke
) {
    // Shaniwar Wada gate/temple style silhouette
    val p1 = Path().apply {
        moveTo(startX + 10f, baseY)
        lineTo(startX + 10f, baseY - 28f)
        lineTo(startX + 18f, baseY - 38f)
        lineTo(startX + 18f, baseY - 48f)
        // Shikhara dome
        lineTo(startX + 23f, baseY - 58f)
        lineTo(startX + 28f, baseY - 48f)
        lineTo(startX + 28f, baseY - 38f)
        lineTo(startX + 36f, baseY - 28f)
        lineTo(startX + 36f, baseY)
    }
    drawPath(p1, tint, style = stroke)

    // Tower with arches
    val p2 = Path().apply {
        moveTo(startX + 40f, baseY)
        lineTo(startX + 40f, baseY - 45f)
        lineTo(startX + 55f, baseY - 45f)
        lineTo(startX + 55f, baseY)
    }
    drawPath(p2, tint, style = stroke)

    // Spire
    val p3 = Path().apply {
        moveTo(startX + 47.5f, baseY - 45f)
        lineTo(startX + 47.5f, baseY - 62f)
    }
    drawPath(p3, tint, style = stroke)

    // Modern IT tower building with grid/windows
    val p4 = Path().apply {
        moveTo(startX + 60f, baseY)
        lineTo(startX + 60f, baseY - 55f)
        lineTo(startX + 85f, baseY - 55f)
        lineTo(startX + 85f, baseY)
    }
    drawPath(p4, tint, style = stroke)

    // Windows in IT building
    for (step in 1..4) {
        drawLine(
            color = tint,
            start = Offset(startX + 65f, baseY - step * 10f),
            end = Offset(startX + 80f, baseY - step * 10f),
            strokeWidth = 1f
        )
    }

    // Historical dome / memorial
    val p5 = Path().apply {
        moveTo(startX + 90f, baseY)
        lineTo(startX + 90f, baseY - 32f)
        cubicTo(
            startX + 90f, baseY - 50f,
            startX + 115f, baseY - 50f,
            startX + 115f, baseY - 32f
        )
        lineTo(startX + 115f, baseY)
    }
    drawPath(p5, tint, style = stroke)

    // Small minaret/pillar
    val p6 = Path().apply {
        moveTo(startX + 102.5f, baseY - 48f)
        lineTo(startX + 102.5f, baseY - 60f)
    }
    drawPath(p6, tint, style = stroke)

    // City high-rise
    val p7 = Path().apply {
        moveTo(startX + 120f, baseY)
        lineTo(startX + 120f, baseY - 40f)
        lineTo(startX + 130f, baseY - 50f)
        lineTo(startX + 140f, baseY - 40f)
        lineTo(startX + 140f, baseY)
    }
    drawPath(p7, tint, style = stroke)

    // Small bridge / trees
    val p8 = Path().apply {
        moveTo(startX + 145f, baseY)
        cubicTo(
            startX + 152f, baseY - 18f,
            startX + 168f, baseY - 18f,
            startX + 175f, baseY
        )
    }
    drawPath(p8, tint, style = stroke)
}

