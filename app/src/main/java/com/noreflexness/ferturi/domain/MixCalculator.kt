package com.noreflexness.ferturi.domain

import com.noreflexness.ferturi.data.Calibration
import kotlin.math.abs

/**
 * Pure-Kotlin core that turns calibration measurements into a mix recipe.
 *
 * Definitions
 * -----------
 *  * `targetRatio` — recommended dilution of *raw* fertilizer in the final
 *    main-line water. Dimensionless (e.g. 0.005 = 5 mL of raw per L of output).
 *  * For a calibration row taken at valve setting V we measured how much of
 *    the output came from the container:
 *
 *        drawRatio(V) = mixDrawn / output
 *
 *  * The container holds a *diluted* mix at concentration
 *
 *        C = rawInContainer / containerVolume
 *
 *  * The venturi blends container fluid into the main line, so the final
 *    main-line concentration is `C * drawRatio(V)`.
 *
 *  * Solving for the container concentration we need:
 *
 *        C = targetRatio / drawRatio(V)
 *
 *    Feasible when 0 < C ≤ 1 (i.e. you'd need at most a container full of pure
 *    raw fertilizer).
 */
object MixCalculator {

    /** Practical sweet spot for container concentration. */
    private const val SWEET_SPOT_LOW = 0.01
    private const val SWEET_SPOT_HIGH = 0.5

    /** drawRatio = mixDrawn / output, guarded against bad input. */
    fun drawRatio(c: Calibration): Double {
        if (c.outputLiters <= 0.0) return 0.0
        return c.mixDrawnLiters / c.outputLiters
    }

    /** Optional flow rate (L/min) from the seconds-per-10L field. */
    fun mainFlowRateLpm(c: Calibration): Double? {
        val s = c.secondsPer10L ?: return null
        if (s <= 0.0) return null
        return 600.0 / s
    }

    /**
     * Compute the recipe at a given calibration point for a target ratio and
     * container volume. Returns a [MixResult] that includes feasibility and
     * an optional [alternative] suggestion when the preferred valve doesn't
     * fit.
     */
    fun calculate(
        targetRatio: Double,
        containerVolumeL: Double,
        preferred: Calibration,
        allCalibrations: List<Calibration>,
    ): MixResult {
        require(targetRatio > 0.0) { "targetRatio must be > 0" }
        require(containerVolumeL > 0.0) { "containerVolumeL must be > 0" }

        val recipe = recipeFor(preferred, targetRatio, containerVolumeL)

        val alternative = if (!recipe.feasible) {
            findAlternative(targetRatio, containerVolumeL, allCalibrations)
        } else {
            null
        }

        return MixResult(
            preferred = recipe,
            alternative = alternative,
        )
    }

    private fun recipeFor(
        cal: Calibration,
        targetRatio: Double,
        containerVolumeL: Double,
    ): Recipe {
        val r = drawRatio(cal)
        if (r <= 0.0) {
            return Recipe(
                calibration = cal,
                drawRatio = 0.0,
                containerConcentration = Double.POSITIVE_INFINITY,
                rawFertilizerLiters = Double.POSITIVE_INFINITY,
                waterLiters = 0.0,
                feasible = false,
                note = "Calibration has no draw ratio yet.",
            )
        }
        val c = targetRatio / r
        val raw = c * containerVolumeL
        val water = containerVolumeL - raw
        val feasible = c in 1e-9..1.0
        val note = when {
            c > 1.0 -> "Valve draw is too weak — container would need to be more than 100% raw fertilizer."
            c < SWEET_SPOT_LOW ->
                "Valve draw is strong; the container would be very dilute (good if you have plenty of fertilizer)."
            c > SWEET_SPOT_HIGH ->
                "Container will be quite concentrated — handle with care."
            else -> null
        }
        return Recipe(
            calibration = cal,
            drawRatio = r,
            containerConcentration = c,
            rawFertilizerLiters = raw.coerceAtLeast(0.0),
            waterLiters = water.coerceAtLeast(0.0),
            feasible = feasible,
            note = note,
        )
    }

    /**
     * Pick or interpolate a valve setting where the recipe lands inside the
     * sweet-spot band. Returns null when only one calibration row is available
     * (so we can't interpolate) or no setting can fit.
     */
    private fun findAlternative(
        targetRatio: Double,
        containerVolumeL: Double,
        cals: List<Calibration>,
    ): Recipe? {
        if (cals.size < 1) return null

        // First, check if any *existing* calibration is feasible.
        cals.forEach { cal ->
            val r = recipeFor(cal, targetRatio, containerVolumeL)
            if (r.feasible) return r
        }

        if (cals.size < 2) return null

        // Need drawRatio R such that targetRatio/R is in (0, 1] — i.e. R >= targetRatio.
        // Aim for the sweet spot mid-point so the mix isn't crazy concentrated.
        val targetDrawRatio = targetRatio / 0.2 // aim for C = 0.2 (20% raw in container)

        val sorted = cals.sortedBy { it.valveSetting }
        // Walk adjacent pairs and look for a bracket containing targetDrawRatio.
        for (i in 0 until sorted.size - 1) {
            val a = sorted[i]
            val b = sorted[i + 1]
            val ra = drawRatio(a)
            val rb = drawRatio(b)
            val (lo, hi) = if (ra <= rb) ra to rb else rb to ra
            if (targetDrawRatio in lo..hi) {
                // Linear interpolate the valve setting.
                val v = if (abs(rb - ra) < 1e-12) {
                    (a.valveSetting + b.valveSetting) / 2.0
                } else {
                    a.valveSetting + (targetDrawRatio - ra) * (b.valveSetting - a.valveSetting) / (rb - ra)
                }
                val interp = Calibration(
                    id = "interp",
                    label = "Suggested",
                    valveSetting = v,
                    mixDrawnLiters = targetDrawRatio,
                    outputLiters = 1.0,
                    secondsPer10L = null,
                )
                return recipeFor(interp, targetRatio, containerVolumeL)
                    .copy(note = "Interpolated between valve %.2f and %.2f.".format(a.valveSetting, b.valveSetting))
            }
        }

        // No bracket — pick the calibration with the highest drawRatio (strongest pull)
        // since infeasibility is usually "valve too weak".
        val strongest = sorted.maxBy { drawRatio(it) }
        return recipeFor(strongest, targetRatio, containerVolumeL).copy(
            note = "Even the strongest measured valve setting can't reach this target. Try a stronger setting and recalibrate.",
        )
    }
}

data class Recipe(
    val calibration: Calibration,
    val drawRatio: Double,
    val containerConcentration: Double,
    val rawFertilizerLiters: Double,
    val waterLiters: Double,
    val feasible: Boolean,
    val note: String? = null,
)

data class MixResult(
    val preferred: Recipe,
    val alternative: Recipe? = null,
)
