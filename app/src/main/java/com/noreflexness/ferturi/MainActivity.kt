package com.noreflexness.ferturi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.noreflexness.ferturi.ui.FerturiViewModel
import com.noreflexness.ferturi.ui.screen.FerturiApp
import com.noreflexness.ferturi.ui.theme.FerturiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FerturiViewModel by viewModels { FerturiViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FerturiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val ui by viewModel.ui.collectAsState()
                    FerturiApp(
                        ui = ui,
                        onSelectProduct = viewModel::selectProduct,
                        onSelectCalibration = viewModel::selectCalibration,
                        onContainerVolumeChange = viewModel::setContainerVolume,
                        onUpsertProduct = viewModel::upsertProduct,
                        onDeleteProduct = viewModel::deleteProduct,
                        onUpsertCalibration = viewModel::upsertCalibration,
                        onDeleteCalibration = viewModel::deleteCalibration,
                    )
                }
            }
        }
    }
}
