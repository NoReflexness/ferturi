package com.noreflexness.ferturi.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A real-world measurement of how the venturi behaves at a given valve setting.
 *
 * `mixDrawnLiters` is how much fluid was sucked from the mix container while
 * `outputLiters` came out of the main line during the same test. The ratio of
 * those two is the venturi's "draw ratio" at this valve setting.
 */
@Serializable
data class Calibration(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val valveSetting: Double,
    val mixDrawnLiters: Double,
    val outputLiters: Double,
    val secondsPer10L: Double? = null,
)

/**
 * A fertilizer product with a vendor-recommended dilution ratio.
 *
 * `recommendedRatio` is dimensionless (e.g. 0.005 = 5 mL of raw fertilizer per
 * litre of final output). The UI accepts the input in the most useful unit
 * for the user (mL per L) and converts.
 */
@Serializable
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val recommendedRatio: Double,
    val notes: String = "",
)

@Serializable
data class FerturiState(
    val containerVolumeL: Double = 20.0,
    val products: List<Product> = emptyList(),
    val calibrations: List<Calibration> = emptyList(),
    val selectedProductId: String? = null,
    val selectedCalibrationId: String? = null,
)
