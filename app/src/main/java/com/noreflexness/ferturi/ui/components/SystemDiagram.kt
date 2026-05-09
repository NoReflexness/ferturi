package com.noreflexness.ferturi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Schematic of the system: fertilizer container → siphon hose → venturi in the
 * main water line. Used both as the small logo in the top app bar (compact)
 * and as a labelled infographic banner at the top of the screen.
 */
@Composable
fun SystemDiagram(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val pipeColor = MaterialTheme.colorScheme.primary
    val pipeFill = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val containerStroke = MaterialTheme.colorScheme.outline
    // Fixed fertilizer green so it reads "fertilizer" regardless of the
    // device's dynamic-color palette.
    val liquid = FertilizerGreen
    val liquidLight = FertilizerGreen.copy(alpha = 0.35f)
    val venturiColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val measurer = if (compact) null else rememberTextMeasurer()
    val labelStyle = remember(labelColor) {
        TextStyle(
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }

    Canvas(modifier = modifier) {
        drawSystem(
            compact = compact,
            pipeColor = pipeColor,
            pipeFill = pipeFill,
            containerStroke = containerStroke,
            liquid = liquid,
            liquidLight = liquidLight,
            venturiColor = venturiColor,
            measurer = measurer,
            labelStyle = labelStyle,
        )
    }
}

/**
 * Wraps [SystemDiagram] in a tinted rounded card so it reads as a banner at
 * the top of the screen.
 */
@Composable
fun SystemDiagramBanner(modifier: Modifier = Modifier) {
    val tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(color = tint, size = size)
        }
        SystemDiagram(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp))
    }
}

private fun DrawScope.drawSystem(
    compact: Boolean,
    pipeColor: Color,
    pipeFill: Color,
    containerStroke: Color,
    liquid: Color,
    liquidLight: Color,
    venturiColor: Color,
    measurer: TextMeasurer?,
    labelStyle: TextStyle,
) {
    val w = size.width
    val h = size.height
    // Reserve room above (for venturi / main-line labels) and below (for the
    // container label) in non-compact mode.
    val topLabelRoom = if (compact) 0f else 14.dp.toPx()
    val bottomLabelRoom = if (compact) 0f else 14.dp.toPx()
    val artTop = topLabelRoom
    val artH = h - topLabelRoom - bottomLabelRoom

    // Layout: main line on top, container suspended below, hose rising up
    // into the venturi waist.
    val pipeStrokeWidth = (if (compact) 1.5f else 2.5f).dp.toPx()
    val pipeTopY = artTop + artH * 0.10f
    val pipeBotY = artTop + artH * 0.32f

    // Venturi waist (narrows inward).
    val venturiCenterX = w * 0.55f
    val venturiHalfWidth = w * 0.08f
    val venturiThroatTopY = artTop + artH * 0.16f
    val venturiThroatBotY = artTop + artH * 0.26f

    // Container box (lower-left, below the pipe).
    val containerLeft = w * 0.04f
    val containerRight = w * 0.22f
    val containerTop = artTop + artH * 0.50f
    val containerBot = artTop + artH * 0.95f
    val containerCornerR = (if (compact) 2f else 4f).dp.toPx()

    // Container neck (a small lid on top, where the siphon enters).
    val neckLeft = containerLeft + (containerRight - containerLeft) * 0.30f
    val neckRight = containerRight - (containerRight - containerLeft) * 0.30f
    val neckTop = containerTop - artH * 0.04f
    val neckBot = containerTop

    // Liquid level (about 70% full, measured from the bottom).
    val liquidTop = containerTop + (containerBot - containerTop) * 0.30f

    // Siphon hose: container top-center → up → across (above container) →
    // up into the bottom of the venturi waist.
    val hoseStartX = (containerLeft + containerRight) / 2f
    val hoseStartY = neckTop
    val hoseRunY = (pipeBotY + containerTop) / 2f - artH * 0.02f

    // ---- Pipe outline path with venturi waist ----
    val pipeTop = Path().apply {
        moveTo(0f, pipeTopY)
        lineTo(venturiCenterX - venturiHalfWidth, pipeTopY)
        lineTo(venturiCenterX, venturiThroatTopY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeTopY)
        lineTo(w, pipeTopY)
    }
    val pipeBottom = Path().apply {
        moveTo(0f, pipeBotY)
        lineTo(venturiCenterX - venturiHalfWidth, pipeBotY)
        lineTo(venturiCenterX, venturiThroatBotY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeBotY)
        lineTo(w, pipeBotY)
    }
    val pipeFillPath = Path().apply {
        moveTo(0f, pipeTopY)
        lineTo(venturiCenterX - venturiHalfWidth, pipeTopY)
        lineTo(venturiCenterX, venturiThroatTopY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeTopY)
        lineTo(w, pipeTopY)
        lineTo(w, pipeBotY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeBotY)
        lineTo(venturiCenterX, venturiThroatBotY)
        lineTo(venturiCenterX - venturiHalfWidth, pipeBotY)
        lineTo(0f, pipeBotY)
        close()
    }
    drawPath(pipeFillPath, color = pipeFill)
    drawPath(pipeTop, color = pipeColor, style = Stroke(width = pipeStrokeWidth))
    drawPath(pipeBottom, color = pipeColor, style = Stroke(width = pipeStrokeWidth))

    // Highlight the venturi waist with a slightly thicker accent stroke.
    val venturiAccent = Path().apply {
        moveTo(venturiCenterX - venturiHalfWidth, pipeTopY)
        lineTo(venturiCenterX, venturiThroatTopY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeTopY)
    }
    val venturiAccentBot = Path().apply {
        moveTo(venturiCenterX - venturiHalfWidth, pipeBotY)
        lineTo(venturiCenterX, venturiThroatBotY)
        lineTo(venturiCenterX + venturiHalfWidth, pipeBotY)
    }
    drawPath(venturiAccent, color = venturiColor, style = Stroke(width = pipeStrokeWidth * 1.4f))
    drawPath(venturiAccentBot, color = venturiColor, style = Stroke(width = pipeStrokeWidth * 1.4f))

    // ---- Container ----
    // Lid.
    drawRoundRect(
        color = containerStroke.copy(alpha = 0.20f),
        topLeft = Offset(neckLeft, neckTop),
        size = Size(neckRight - neckLeft, neckBot - neckTop),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(containerCornerR / 2f, containerCornerR / 2f),
    )
    drawRoundRect(
        color = containerStroke,
        topLeft = Offset(neckLeft, neckTop),
        size = Size(neckRight - neckLeft, neckBot - neckTop),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(containerCornerR / 2f, containerCornerR / 2f),
        style = Stroke(width = pipeStrokeWidth),
    )
    // Body, faint fill.
    drawRoundRect(
        color = liquidLight.copy(alpha = 0.18f),
        topLeft = Offset(containerLeft, containerTop),
        size = Size(containerRight - containerLeft, containerBot - containerTop),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(containerCornerR, containerCornerR),
    )
    // Liquid (clipped to body bottom).
    drawRect(
        color = liquid,
        topLeft = Offset(containerLeft + pipeStrokeWidth / 2f, liquidTop),
        size = Size(
            (containerRight - containerLeft) - pipeStrokeWidth,
            containerBot - liquidTop - pipeStrokeWidth / 2f,
        ),
    )
    // Body outline.
    drawRoundRect(
        color = containerStroke,
        topLeft = Offset(containerLeft, containerTop),
        size = Size(containerRight - containerLeft, containerBot - containerTop),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(containerCornerR, containerCornerR),
        style = Stroke(width = pipeStrokeWidth),
    )

    // ---- Siphon hose: container neck → up → across → up into venturi waist (from below) ----
    val hoseEndX = venturiCenterX
    val hoseEndY = venturiThroatBotY
    val hose = Path().apply {
        moveTo(hoseStartX, hoseStartY)
        lineTo(hoseStartX, hoseRunY)
        lineTo(hoseEndX, hoseRunY)
        lineTo(hoseEndX, hoseEndY)
    }
    drawPath(
        hose,
        color = liquid,
        style = Stroke(width = pipeStrokeWidth * 1.2f),
    )

    // ---- Flow arrows (skipped in compact mode) ----
    if (!compact) {
        // Main-line water flowing left → right, at the centerline of the pipe.
        val centerY = (pipeTopY + pipeBotY) / 2f
        drawArrowRight(Offset(w * 0.10f, centerY), pipeColor, length = w * 0.05f)
        drawArrowRight(Offset(w * 0.85f, centerY), pipeColor, length = w * 0.05f)
        // Tiny up-arrow on the hose just below the venturi waist showing the
        // venturi sucking fertilizer up out of the container.
        drawArrowUp(Offset(venturiCenterX, hoseEndY + artH * 0.05f), liquid, length = artH * 0.07f)
    }

    // ---- Labels ----
    if (measurer != null) {
        val containerLayout = measurer.measure("Container", labelStyle)
        val venturiLayout = measurer.measure("Venturi", labelStyle)
        val mainLineLayout = measurer.measure("Main line", labelStyle)

        // Top strip: venturi (centred above waist) and main-line (right edge).
        val topStripCenterY = (topLabelRoom - venturiLayout.size.height) / 2f
        drawText(
            textLayoutResult = venturiLayout,
            topLeft = Offset(
                (venturiCenterX - venturiLayout.size.width / 2f).coerceAtLeast(0f),
                topStripCenterY.coerceAtLeast(0f),
            ),
        )
        drawText(
            textLayoutResult = mainLineLayout,
            topLeft = Offset(
                (w - mainLineLayout.size.width - 2f).coerceAtLeast(0f),
                topStripCenterY.coerceAtLeast(0f),
            ),
        )

        // Bottom strip: container label centred under container body.
        val containerCx = (containerLeft + containerRight) / 2f - containerLayout.size.width / 2f
        drawText(
            textLayoutResult = containerLayout,
            topLeft = Offset(
                containerCx.coerceAtLeast(0f),
                artTop + artH + (bottomLabelRoom - containerLayout.size.height) / 2f,
            ),
        )
    }
}

private fun DrawScope.drawArrowRight(tipAnchor: Offset, color: Color, length: Float) {
    val head = length * 0.45f
    val shaftEnd = Offset(tipAnchor.x + length / 2f, tipAnchor.y)
    val shaftStart = Offset(tipAnchor.x - length / 2f, tipAnchor.y)
    val sw = 1.6.dp.toPx()
    drawLine(color, shaftStart, shaftEnd, strokeWidth = sw)
    drawLine(color, shaftEnd, Offset(shaftEnd.x - head * 0.6f, shaftEnd.y - head * 0.5f), strokeWidth = sw)
    drawLine(color, shaftEnd, Offset(shaftEnd.x - head * 0.6f, shaftEnd.y + head * 0.5f), strokeWidth = sw)
}

private fun DrawScope.drawArrowDown(tip: Offset, color: Color, length: Float) {
    val head = length * 0.5f
    val sw = 1.6.dp.toPx()
    val shaftStart = Offset(tip.x, tip.y - length)
    drawLine(color, shaftStart, tip, strokeWidth = sw)
    drawLine(color, tip, Offset(tip.x - head * 0.45f, tip.y - head * 0.6f), strokeWidth = sw)
    drawLine(color, tip, Offset(tip.x + head * 0.45f, tip.y - head * 0.6f), strokeWidth = sw)
}

private fun DrawScope.drawArrowUp(tip: Offset, color: Color, length: Float) {
    val head = length * 0.5f
    val sw = 1.6.dp.toPx()
    val shaftStart = Offset(tip.x, tip.y + length)
    drawLine(color, shaftStart, tip, strokeWidth = sw)
    drawLine(color, tip, Offset(tip.x - head * 0.45f, tip.y + head * 0.6f), strokeWidth = sw)
    drawLine(color, tip, Offset(tip.x + head * 0.45f, tip.y + head * 0.6f), strokeWidth = sw)
}

/** Material-style green so the fertilizer reads "fertilizer" in any palette. */
private val FertilizerGreen = Color(0xFF388E3C)

@Suppress("unused")
private fun DrawScope.rotated(degrees: Float, pivot: Offset, block: DrawScope.() -> Unit) {
    rotate(degrees, pivot) { block() }
}
