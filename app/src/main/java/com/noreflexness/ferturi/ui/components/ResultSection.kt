package com.noreflexness.ferturi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noreflexness.ferturi.domain.MixCalculator
import com.noreflexness.ferturi.domain.Recipe
import com.noreflexness.ferturi.ui.UiState

@Composable
fun ResultSection(ui: UiState) {
    SectionCard(title = "Mix recipe", icon = Icons.Default.Calculate) {
        val state = ui.state
        val product = state.products.firstOrNull { it.id == state.selectedProductId }
            ?: state.products.firstOrNull()
        val result = ui.result

        if (product == null) {
            EmptyHint("Add a fertilizer product below to get a recipe.")
            return@SectionCard
        }
        if (state.calibrations.isEmpty()) {
            EmptyHint("Add at least one calibration measurement to get a recipe.")
            return@SectionCard
        }
        if (result == null) {
            EmptyHint("Set a container volume above 0 L.")
            return@SectionCard
        }

        Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Target: ${fmtRatioMlPerL(product.recommendedRatio)} of raw fertilizer in main-line water",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        RecipeBlock(
            heading = "At valve ${fmt(result.preferred.calibration.valveSetting, 2)}",
            recipe = result.preferred,
            containerVolumeL = state.containerVolumeL,
            tone = if (result.preferred.feasible) Tone.Good else Tone.Warning,
        )

        result.alternative?.let { alt ->
            RecipeBlock(
                heading = if (alt.calibration.id == "interp") {
                    "Suggested valve ${fmt(alt.calibration.valveSetting, 2)}"
                } else {
                    "Try valve ${fmt(alt.calibration.valveSetting, 2)} instead"
                },
                recipe = alt,
                containerVolumeL = state.containerVolumeL,
                tone = Tone.Info,
            )
        }
    }
}

private enum class Tone { Good, Warning, Info }

@Composable
private fun RecipeBlock(
    heading: String,
    recipe: Recipe,
    containerVolumeL: Double,
    tone: Tone,
) {
    val bg = when (tone) {
        Tone.Good -> MaterialTheme.colorScheme.primaryContainer
        Tone.Warning -> MaterialTheme.colorScheme.errorContainer
        Tone.Info -> MaterialTheme.colorScheme.secondaryContainer
    }
    val fg = when (tone) {
        Tone.Good -> MaterialTheme.colorScheme.onPrimaryContainer
        Tone.Warning -> MaterialTheme.colorScheme.onErrorContainer
        Tone.Info -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
        if (recipe.feasible) {
            BigStat(
                label = "Raw fertilizer into ${fmtVolumeL(containerVolumeL)} container",
                value = fmtVolumeL(recipe.rawFertilizerLiters),
                valueColor = fg,
            )
            Text(
                text = "+ ${fmtVolumeL(recipe.waterLiters)} water → container concentration ${fmt(recipe.containerConcentration * 100.0, 2)} %",
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
            )
            Text(
                text = "Venturi draw at this setting: ${fmt(recipe.drawRatio * 100.0, 3)} % of main-line output",
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
            )
            MixCalculator.mainFlowRateLpm(recipe.calibration)?.let { lpm ->
                val containerEmptyMin = if (recipe.drawRatio > 0) {
                    containerVolumeL / (lpm * recipe.drawRatio)
                } else 0.0
                Text(
                    text = "Main-line flow ≈ ${fmt(lpm, 2)} L/min · container drains in ≈ ${fmt(containerEmptyMin, 1)} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = fg,
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = fg)
                Text(
                    text = "Not achievable here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = fg,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        recipe.note?.let {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = fg)
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = fg,
                )
            }
        }
    }
}

@Composable
private fun BigStat(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
        )
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
