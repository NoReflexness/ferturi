package com.noreflexness.ferturi.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noreflexness.ferturi.data.Calibration
import com.noreflexness.ferturi.data.Product
import com.noreflexness.ferturi.ui.UiState
import com.noreflexness.ferturi.ui.components.CalibrationsSection
import com.noreflexness.ferturi.ui.components.InputsSection
import com.noreflexness.ferturi.ui.components.ProductsSection
import com.noreflexness.ferturi.ui.components.ResultSection
import com.noreflexness.ferturi.ui.components.SystemDiagramBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FerturiApp(
    ui: UiState,
    onSelectProduct: (String) -> Unit,
    onSelectCalibration: (String) -> Unit,
    onContainerVolumeChange: (Double) -> Unit,
    onUpsertProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpsertCalibration: (Calibration) -> Unit,
    onDeleteCalibration: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ferturi",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SystemDiagramBanner()
            }
            item {
                ResultSection(ui = ui)
            }
            item {
                InputsSection(
                    ui = ui,
                    onSelectProduct = onSelectProduct,
                    onSelectCalibration = onSelectCalibration,
                    onContainerVolumeChange = onContainerVolumeChange,
                )
            }
            item {
                ProductsSection(
                    products = ui.state.products,
                    selectedId = ui.state.selectedProductId ?: ui.state.products.firstOrNull()?.id,
                    onSelect = onSelectProduct,
                    onUpsert = onUpsertProduct,
                    onDelete = onDeleteProduct,
                )
            }
            item {
                CalibrationsSection(
                    calibrations = ui.state.calibrations,
                    selectedId = ui.state.selectedCalibrationId ?: ui.state.calibrations.firstOrNull()?.id,
                    onSelect = onSelectCalibration,
                    onUpsert = onUpsertCalibration,
                    onDelete = onDeleteCalibration,
                )
            }
        }
    }
}
