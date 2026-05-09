package com.noreflexness.ferturi

import com.noreflexness.ferturi.data.Calibration
import com.noreflexness.ferturi.domain.MixCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MixCalculatorTest {

    private val example = Calibration(
        valveSetting = 3.0,
        mixDrawnLiters = 2.1,
        outputLiters = 70.0,
    )

    @Test
    fun draw_ratio_matches_definition() {
        assertEquals(2.1 / 70.0, MixCalculator.drawRatio(example), 1e-9)
    }

    @Test
    fun feasible_recipe_for_5_ml_per_litre() {
        val r = MixCalculator.calculate(
            targetRatio = 0.005,
            containerVolumeL = 20.0,
            preferred = example,
            allCalibrations = listOf(example),
        )
        assertTrue("should be feasible", r.preferred.feasible)
        // C = 0.005 / (2.1/70.0) = 0.005 / 0.03 = 0.1666...
        assertEquals(0.1666666, r.preferred.containerConcentration, 1e-4)
        // raw = C * 20 = ~3.333 L
        assertEquals(3.333, r.preferred.rawFertilizerLiters, 1e-2)
        // water = 20 - raw
        assertEquals(20.0 - 3.333, r.preferred.waterLiters, 1e-2)
        assertNull(r.alternative)
    }

    @Test
    fun infeasible_when_target_too_high_for_valve() {
        // Tiny draw ratio + huge target ⇒ C > 1
        val weak = example.copy(mixDrawnLiters = 0.01)
        val r = MixCalculator.calculate(
            targetRatio = 0.05,
            containerVolumeL = 20.0,
            preferred = weak,
            allCalibrations = listOf(weak),
        )
        assertTrue("preferred should be infeasible", !r.preferred.feasible)
        // Only one calibration ⇒ no interpolation possible.
        assertNull(r.alternative)
    }

    @Test
    fun finds_existing_feasible_alternative_when_preferred_is_too_weak() {
        val low = Calibration(valveSetting = 1.0, mixDrawnLiters = 0.5, outputLiters = 100.0) // 0.005
        val mid = Calibration(valveSetting = 3.0, mixDrawnLiters = 2.1, outputLiters = 70.0)  // 0.030
        val high = Calibration(valveSetting = 5.0, mixDrawnLiters = 6.0, outputLiters = 60.0) // 0.100
        val r = MixCalculator.calculate(
            targetRatio = 0.02, // 20 mL/L target
            containerVolumeL = 10.0,
            preferred = low, // very weak — needs C = 0.02/0.005 = 4.0, infeasible
            allCalibrations = listOf(low, mid, high),
        )
        assertTrue(!r.preferred.feasible)
        assertNotNull("should find an alternative", r.alternative)
        assertTrue("alternative must be feasible", r.alternative!!.feasible)
        // C * drawRatio should equal the target ratio
        assertEquals(0.02, r.alternative.containerConcentration * r.alternative.drawRatio, 1e-6)
    }

    @Test
    fun flow_rate_from_seconds_per_10L() {
        val cal = example.copy(secondsPer10L = 30.0)
        // 30 s for 10 L ⇒ 20 L/min
        assertEquals(20.0, MixCalculator.mainFlowRateLpm(cal)!!, 1e-9)
    }
}
