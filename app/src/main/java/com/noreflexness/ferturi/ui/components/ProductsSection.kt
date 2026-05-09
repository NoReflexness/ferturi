package com.noreflexness.ferturi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
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
import com.noreflexness.ferturi.data.Product

@Composable
fun ProductsSection(
    products: List<Product>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onUpsert: (Product) -> Unit,
    onDelete: (String) -> Unit,
) {
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }

    SectionCard(title = "Fertilizer products", icon = Icons.Default.Science) {
        if (products.isEmpty()) {
            Text(
                "No products yet — add one with its recommended dilution.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        products.forEach { product ->
            ProductRow(
                product = product,
                selected = product.id == selectedId,
                onSelect = { onSelect(product.id) },
                onEdit = { editing = product },
                onDelete = { onDelete(product.id) },
            )
        }
        FilledTonalButton(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add product", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (creating) {
        ProductDialog(
            initial = null,
            onDismiss = { creating = false },
            onSave = { p -> onUpsert(p); creating = false },
        )
    }
    editing?.let { p ->
        ProductDialog(
            initial = p,
            onDismiss = { editing = null },
            onSave = { saved -> onUpsert(saved); editing = null },
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
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
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    text = "Recommended ${fmtRatioMlPerL(product.recommendedRatio)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (product.notes.isNotBlank()) {
                    Text(
                        text = product.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
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
private fun ProductDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var ratioMlPerL by remember {
        mutableStateOf(if (initial != null) fmt(initial.recommendedRatio * 1000.0, 4) else "")
    }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New product" else "Edit product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = ratioMlPerL,
                    onValueChange = { ratioMlPerL = it },
                    label = { Text("Recommended dilution (mL per L)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && ratioMlPerL.toEuropeanDoubleOrNull()?.let { it > 0 } == true,
                onClick = {
                    val mlPerL = ratioMlPerL.toEuropeanDoubleOrNull() ?: return@TextButton
                    val product = (initial ?: Product(name = "", recommendedRatio = 0.0)).copy(
                        name = name.trim(),
                        recommendedRatio = mlPerL / 1000.0,
                        notes = notes.trim(),
                    )
                    onSave(product)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
