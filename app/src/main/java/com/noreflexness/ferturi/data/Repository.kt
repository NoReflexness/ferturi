package com.noreflexness.ferturi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "ferturi")
private val STATE_KEY = stringPreferencesKey("state_json")

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
    encodeDefaults = true
}

class FerturiRepository(private val appContext: Context) {

    val state: Flow<FerturiState> = appContext.dataStore.data.map { prefs ->
        val raw = prefs[STATE_KEY] ?: return@map seed()
        try {
            json.decodeFromString<FerturiState>(raw)
        } catch (_: SerializationException) {
            seed()
        }
    }

    suspend fun update(transform: (FerturiState) -> FerturiState) {
        appContext.dataStore.edit { prefs ->
            val current = prefs[STATE_KEY]?.let {
                try { json.decodeFromString<FerturiState>(it) } catch (_: SerializationException) { seed() }
            } ?: seed()
            val next = transform(current)
            prefs[STATE_KEY] = json.encodeToString(next)
        }
    }

    private fun seed(): FerturiState = FerturiState(
        containerVolumeL = 20.0,
        products = listOf(
            Product(name = "Generic 5 mL/L", recommendedRatio = 0.005, notes = "Common all-round dilution."),
        ),
        calibrations = listOf(
            Calibration(
                label = "Example",
                valveSetting = 3.0,
                mixDrawnLiters = 2.1,
                outputLiters = 70.0,
                secondsPer10L = null,
            ),
        ),
    )
}
