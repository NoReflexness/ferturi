package com.noreflexness.ferturi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.noreflexness.ferturi.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsSection(
    ui: UiState,
    onSelectProduct: (String) -> Unit,
    onSelectCalibration: (String) -> Unit,
    onContainerVolumeChange: (Double) -> Unit,
) {
    SectionCard(title = "Inputs", icon = Icons.Default.Tune) {
        var volumeText by remember(ui.state.containerVolumeL) {
            mutableStateOf(fmt(ui.state.containerVolumeL, 3))
        }
        OutlinedTextField(
            value = volumeText,
            onValueChange = { raw ->
                volumeText = raw
                raw.toEuropeanDoubleOrNull()?.let(onContainerVolumeChange)
            },
            label = { Text("Mix container volume (L)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        if (ui.state.products.isNotEmpty()) {
            Text("Product", style = MaterialTheme.typography.labelLarge)
            ChipFlow(
                items = ui.state.products.map { it.id to it.name },
                selectedId = ui.state.selectedProductId ?: ui.state.products.first().id,
                onSelect = onSelectProduct,
            )
        }

        if (ui.state.calibrations.isNotEmpty()) {
            Text("Preferred calibration", style = MaterialTheme.typography.labelLarge)
            ChipFlow(
                items = ui.state.calibrations.map { c ->
                    val label = c.label.ifBlank { "Valve ${fmt(c.valveSetting, 2)}" }
                    c.id to label
                },
                selectedId = ui.state.selectedCalibrationId ?: ui.state.calibrations.first().id,
                onSelect = onSelectCalibration,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipFlow(
    items: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { (id, label) ->
            val selected = id == selectedId
            AssistChip(
                onClick = { onSelect(id) },
                label = { Text(label) },
                colors = if (selected) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    AssistChipDefaults.assistChipColors()
                },
            )
        }
    }
}

/** Accept both 1.5 and 1,5 — Europeans (and the user) use comma as decimal separator. */
fun String.toEuropeanDoubleOrNull(): Double? =
    this.replace(',', '.').trim().toDoubleOrNull()
