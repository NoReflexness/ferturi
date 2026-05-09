package com.noreflexness.ferturi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.noreflexness.ferturi.data.Calibration
import com.noreflexness.ferturi.domain.MixCalculator

@Composable
fun CalibrationsSection(
    calibrations: List<Calibration>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onUpsert: (Calibration) -> Unit,
    onDelete: (String) -> Unit,
) {
    var editing by remember { mutableStateOf<Calibration?>(null) }
    var creating by remember { mutableStateOf(false) }

    SectionCard(title = "Venturi calibrations", icon = Icons.Default.Speed) {
        if (calibrations.isEmpty()) {
            Text(
                "No calibrations yet. Run water through the venturi at a known valve setting, " +
                    "measure how much main-line output and how much fluid was drawn from the container.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        calibrations.sortedBy { it.valveSetting }.forEach { cal ->
            CalibrationRow(
                cal = cal,
                selected = cal.id == selectedId,
                onSelect = { onSelect(cal.id) },
                onEdit = { editing = cal },
                onDelete = { onDelete(cal.id) },
            )
        }
        FilledTonalButton(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add calibration", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (creating) {
        CalibrationDialog(
            initial = null,
            onDismiss = { creating = false },
            onSave = { c -> onUpsert(c); creating = false },
        )
    }
    editing?.let { c ->
        CalibrationDialog(
            initial = c,
            onDismiss = { editing = null },
            onSave = { saved -> onUpsert(saved); editing = null },
        )
    }
}

@Composable
private fun CalibrationRow(
    cal: Calibration,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val draw = MixCalculator.drawRatio(cal)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cal.label.ifBlank { "Valve ${fmt(cal.valveSetting, 2)}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    text = "Valve ${fmt(cal.valveSetting, 2)} · " +
                        "${fmtVolumeL(cal.mixDrawnLiters)} drawn / ${fmtVolumeL(cal.outputLiters)} output",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Draw ratio ${fmt(draw * 100.0, 3)} %" +
                        (MixCalculator.mainFlowRateLpm(cal)?.let { " · ${fmt(it, 2)} L/min main flow" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun CalibrationDialog(
    initial: Calibration?,
    onDismiss: () -> Unit,
    onSave: (Calibration) -> Unit,
) {
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var valve by remember { mutableStateOf(initial?.let { fmt(it.valveSetting, 2) } ?: "") }
    var mixDrawn by remember { mutableStateOf(initial?.let { fmt(it.mixDrawnLiters, 3) } ?: "") }
    var output by remember { mutableStateOf(initial?.let { fmt(it.outputLiters, 3) } ?: "") }
    var time10L by remember { mutableStateOf(initial?.secondsPer10L?.let { fmt(it, 2) } ?: "") }

    val valid = listOf(valve, mixDrawn, output).all { it.toEuropeanDoubleOrNull()?.let { v -> v > 0 } == true }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New calibration" else "Edit calibration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = valve,
                    onValueChange = { valve = it },
                    label = { Text("Valve setting (0–5)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = mixDrawn,
                    onValueChange = { mixDrawn = it },
                    label = { Text("Mix drawn from container (L)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = output,
                    onValueChange = { output = it },
                    label = { Text("Total main-line output (L)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = time10L,
                    onValueChange = { time10L = it },
                    label = { Text("Seconds to fill 10 L (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val cal = (initial ?: Calibration(
                        label = "",
                        valveSetting = 0.0,
                        mixDrawnLiters = 0.0,
                        outputLiters = 0.0,
                    )).copy(
                        label = label.trim(),
                        valveSetting = valve.toEuropeanDoubleOrNull() ?: 0.0,
                        mixDrawnLiters = mixDrawn.toEuropeanDoubleOrNull() ?: 0.0,
                        outputLiters = output.toEuropeanDoubleOrNull() ?: 0.0,
                        secondsPer10L = time10L.toEuropeanDoubleOrNull(),
                    )
                    onSave(cal)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
