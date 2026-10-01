package de.fahrerabrechnung.app

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import de.fahrerabrechnung.core.model.ThemeMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.data.card.CardActivityImportRepository
import de.fahrerabrechnung.data.card.CardSourcePairRepository
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.DriverCardImportRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var cardActivityImports: CardActivityImportRepository
    @Inject lateinit var cardSourcePairs: CardSourcePairRepository
    @Inject lateinit var cardVehicleImports: CardVehicleImportRepository
    @Inject lateinit var driverCardImports: DriverCardImportRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val onboardingComplete = viewModel.onboardingComplete.collectAsStateWithLifecycle().value
            val themeMode = viewModel.themeMode.collectAsStateWithLifecycle().value
            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            SideEffect {
                val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            FahrerTagebuchTheme(themeMode = themeMode) {
                if (onboardingComplete != null) AppRoot(
                    cardActivityImports = cardActivityImports,
                    cardVehicleImports = cardVehicleImports,
                    cardSourcePairs = cardSourcePairs,
                    driverCardImports = driverCardImports,
                    onboardingComplete = onboardingComplete,
                    onFinishOnboarding = viewModel::finishOnboarding,
                    themeMode = themeMode,
                    onThemeModeChanged = viewModel::setThemeMode,
                )
            }
        }
    }
}
