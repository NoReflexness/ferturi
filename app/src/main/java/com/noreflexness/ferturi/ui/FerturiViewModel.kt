package com.noreflexness.ferturi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.noreflexness.ferturi.data.Calibration
import com.noreflexness.ferturi.data.FerturiRepository
import com.noreflexness.ferturi.data.FerturiState
import com.noreflexness.ferturi.data.Product
import com.noreflexness.ferturi.domain.MixCalculator
import com.noreflexness.ferturi.domain.MixResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val state: FerturiState = FerturiState(),
    val result: MixResult? = null,
)

class FerturiViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FerturiRepository(app.applicationContext)

    val ui: StateFlow<UiState> = repo.state
        .map { state ->
            UiState(state = state, result = computeResult(state))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    private fun computeResult(state: FerturiState): MixResult? {
        val product = state.products.firstOrNull { it.id == state.selectedProductId }
            ?: state.products.firstOrNull()
            ?: return null
        val calibration = state.calibrations.firstOrNull { it.id == state.selectedCalibrationId }
            ?: state.calibrations.firstOrNull()
            ?: return null
        if (state.containerVolumeL <= 0.0 || product.recommendedRatio <= 0.0) return null
        return runCatching {
            MixCalculator.calculate(
                targetRatio = product.recommendedRatio,
                containerVolumeL = state.containerVolumeL,
                preferred = calibration,
                allCalibrations = state.calibrations,
                availableRawLiters = product.availableRawLiters,
            )
        }.getOrNull()
    }

    fun setContainerVolume(value: Double) = launch { it.copy(containerVolumeL = value) }
    fun selectProduct(id: String) = launch { it.copy(selectedProductId = id) }
    fun selectCalibration(id: String) = launch { it.copy(selectedCalibrationId = id) }

    fun upsertProduct(product: Product) = launch { state ->
        val list = state.products.toMutableList()
        val idx = list.indexOfFirst { it.id == product.id }
        if (idx >= 0) list[idx] = product else list.add(product)
        state.copy(products = list, selectedProductId = product.id)
    }

    fun deleteProduct(id: String) = launch { state ->
        state.copy(
            products = state.products.filterNot { it.id == id },
            selectedProductId = state.selectedProductId.takeUnless { it == id },
        )
    }

    fun upsertCalibration(cal: Calibration) = launch { state ->
        val list = state.calibrations.toMutableList()
        val idx = list.indexOfFirst { it.id == cal.id }
        if (idx >= 0) list[idx] = cal else list.add(cal)
        state.copy(calibrations = list, selectedCalibrationId = cal.id)
    }

    fun deleteCalibration(id: String) = launch { state ->
        state.copy(
            calibrations = state.calibrations.filterNot { it.id == id },
            selectedCalibrationId = state.selectedCalibrationId.takeUnless { it == id },
        )
    }

    private fun launch(block: (FerturiState) -> FerturiState) {
        viewModelScope.launch { repo.update(block) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                return FerturiViewModel(app) as T
            }
        }
    }
}
