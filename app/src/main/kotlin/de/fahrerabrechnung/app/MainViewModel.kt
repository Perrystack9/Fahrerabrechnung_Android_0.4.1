package de.fahrerabrechnung.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.settings.AppPreferencesRepository
import de.fahrerabrechnung.data.violation.DrivingCheckRepository
import de.fahrerabrechnung.data.violation.DrivingCheckState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: AppPreferencesRepository,
    drivingChecks: DrivingCheckRepository,
) : ViewModel() {
    // Read-only M2/M3/M4 calculation stays active while the app is open. User-facing results follow in M5.
    val drivingCheckState: StateFlow<DrivingCheckState> = drivingChecks.observe().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DrivingCheckState.Preparing,
    )

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    val onboardingComplete: StateFlow<Boolean?> = preferences.onboardingComplete.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun finishOnboarding(latitude: Double?, longitude: Double?, onSaved: () -> Unit) {
        viewModelScope.launch {
            preferences.finishOnboarding(latitude, longitude)
            onSaved()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }
}
