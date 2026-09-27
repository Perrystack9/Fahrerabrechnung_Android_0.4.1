package de.fahrerabrechnung.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.fahrerabrechnung.core.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val themeMode: Flow<ThemeMode> = context.appSettingsDataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    val onboardingComplete: Flow<Boolean> = context.appSettingsDataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appSettingsDataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun finishOnboarding(latitude: Double?, longitude: Double?) {
        context.appSettingsDataStore.edit { prefs ->
            if (latitude != null && longitude != null && latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                prefs[HOME_LATITUDE] = latitude
                prefs[HOME_LONGITUDE] = longitude
            }
            prefs[ONBOARDING_COMPLETE] = true
        }
    }

    val homePoint: Flow<Pair<Double, Double>?> = context.appSettingsDataStore.data.map { prefs ->
        val lat = prefs[HOME_LATITUDE]
        val lon = prefs[HOME_LONGITUDE]
        if (lat != null && lon != null) lat to lon else null
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HOME_LATITUDE = doublePreferencesKey("home_latitude")
        val HOME_LONGITUDE = doublePreferencesKey("home_longitude")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
}
