package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import de.fahrerabrechnung.core.design.FahrerBrand
import de.fahrerabrechnung.feature.onboarding.OnboardingDraft
import de.fahrerabrechnung.feature.onboarding.OnboardingScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.fahrerabrechnung.core.design.AppScaffold
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.CardActivityImportRepository
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.feature.analytics.AnalyticsScreen
import de.fahrerabrechnung.feature.calendar.CalendarScreen
import de.fahrerabrechnung.feature.common.LocalScreenScrollState
import de.fahrerabrechnung.feature.detail.DayReportScreen
import de.fahrerabrechnung.feature.detail.SettingsSubpagePlaceholderScreen
import de.fahrerabrechnung.feature.detail.VehicleDetailPlaceholderScreen
import de.fahrerabrechnung.feature.payroll.PayrollScreen
import de.fahrerabrechnung.feature.settings.SettingsScreen
import de.fahrerabrechnung.feature.start.StartScreen

private enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    START("start", "Start", Icons.Rounded.Home),
    CALENDAR("calendar", "Kalender", Icons.Rounded.CalendarMonth),
    ANALYTICS("analytics", "Auswertung", Icons.Rounded.Analytics),
    PAYROLL("payroll", "Abrechnung", Icons.Rounded.ReceiptLong),
    SETTINGS("settings", "Einstellungen", Icons.Rounded.Settings),
}

private val navigationLabels = TopLevelDestination.entries.map { it.label }

private object DetailRoutes {
    const val ONBOARDING = "onboarding"
    const val DAY_REPORT = "day_report"
    const val CARD_IMPORT = "card_import"
    const val VEHICLE_DETAIL = "vehicle_detail"
    const val SETTINGS_SUBPAGE = "settings_subpage"
}

@Composable
fun AppRoot(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    cardActivityImports: CardActivityImportRepository? = null,
    cardVehicleImports: CardVehicleImportRepository? = null,
    onboardingComplete: Boolean = true,
    onFinishOnboarding: (Double?, Double?, () -> Unit) -> Unit = { _, _, done -> done() },
) {
    val navController = rememberNavController()
    var draft by rememberSaveable(stateSaver = OnboardingDraft.Saver) { mutableStateOf(OnboardingDraft()) }
    var settingsTitle by rememberSaveable { mutableStateOf("Einstellung") }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: if (onboardingComplete) TopLevelDestination.START.route else DetailRoutes.ONBOARDING
    // NavHost retains outgoing entries during its AnimatedContent transition, even with
    // Enter/ExitTransition.None. Render only the current entry to avoid two screens at once.
    val visibleRoute = rememberUpdatedState(currentRoute)
    // Keep each tab's scroll position alive while its outgoing NavHost content is hidden.
    val tabScrollStates = TopLevelDestination.entries.associate { it.route to rememberScrollState() }
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }
    val title = if (topLevel == TopLevelDestination.START) FahrerBrand.NAME else topLevel?.label ?: when (currentRoute) {
        DetailRoutes.ONBOARDING -> FahrerBrand.NAME
        DetailRoutes.DAY_REPORT -> "Tagesübersicht"
        DetailRoutes.CARD_IMPORT -> "Fahrerkarte"
        DetailRoutes.VEHICLE_DETAIL -> "Fahrzeug"
        DetailRoutes.SETTINGS_SUBPAGE -> settingsTitle
        else -> FahrerBrand.NAME
    }

    AppScaffold(
        title = title,
        navigationIcon = {
            if (topLevel == null && (onboardingComplete || currentRoute != DetailRoutes.ONBOARDING)) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.testTag("back")) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Zurück")
                }
            }
        },
        bottomBar = {
            if (topLevel != null && onboardingComplete) {
                BoxWithConstraints {
                    val density = LocalDensity.current
                    val direction = LocalLayoutDirection.current
                    val insets = NavigationBarDefaults.windowInsets
                    val horizontalInsets = insets.getLeft(density, direction) + insets.getRight(density, direction)
                    // Reserve 8 dp between items and round down once for all five labels.
                    val itemWidth = (constraints.maxWidth - horizontalInsets - with(density) { 32.dp.roundToPx() }) / 5
                    val labelStyle = navigationLabelStyle(navigationLabels, itemWidth - with(density) { 2.dp.roundToPx() })
                    // Reset Material's animated selection state when the destination changes.
                    key(currentRoute) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                            TopLevelDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = currentRoute == destination.route,
                                    onClick = {
                                        if (currentRoute != destination.route) navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(destination.icon, contentDescription = null) },
                                    label = { NavigationLabel(destination.label, destination.route, labelStyle) },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_${destination.route}"),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingComplete) TopLevelDestination.START.route else DetailRoutes.ONBOARDING,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(TopLevelDestination.START.route) {
                if (visibleRoute.value == TopLevelDestination.START.route) {
                    CompositionLocalProvider(LocalScreenScrollState provides tabScrollStates.getValue(TopLevelDestination.START.route)) {
                        StartScreen(padding, onOpenCardImport = { navController.navigate(DetailRoutes.CARD_IMPORT) }, onOpenOnboarding = { navController.navigate(DetailRoutes.ONBOARDING) })
                    }
                }
            }
            composable(TopLevelDestination.CALENDAR.route) {
                if (visibleRoute.value == TopLevelDestination.CALENDAR.route) {
                    CompositionLocalProvider(LocalScreenScrollState provides tabScrollStates.getValue(TopLevelDestination.CALENDAR.route)) {
                        CalendarScreen(padding, onOpenExampleDay = { navController.navigate(DetailRoutes.DAY_REPORT) })
                    }
                }
            }
            composable(TopLevelDestination.ANALYTICS.route) {
                if (visibleRoute.value == TopLevelDestination.ANALYTICS.route) {
                    CompositionLocalProvider(LocalScreenScrollState provides tabScrollStates.getValue(TopLevelDestination.ANALYTICS.route)) {
                        AnalyticsScreen(padding)
                    }
                }
            }
            composable(TopLevelDestination.PAYROLL.route) {
                if (visibleRoute.value == TopLevelDestination.PAYROLL.route) {
                    CompositionLocalProvider(LocalScreenScrollState provides tabScrollStates.getValue(TopLevelDestination.PAYROLL.route)) {
                        PayrollScreen(padding)
                    }
                }
            }
            composable(TopLevelDestination.SETTINGS.route) {
                if (visibleRoute.value == TopLevelDestination.SETTINGS.route) {
                    CompositionLocalProvider(LocalScreenScrollState provides tabScrollStates.getValue(TopLevelDestination.SETTINGS.route)) {
                        SettingsScreen(
                            padding = padding,
                            themeMode = themeMode,
                            onThemeModeChanged = onThemeModeChanged,
                            onOpenSubpage = {
                                if (it == "Fahrerkarte") navController.navigate(DetailRoutes.CARD_IMPORT)
                                else { settingsTitle = it; navController.navigate(DetailRoutes.SETTINGS_SUBPAGE) }
                            },
                            onOpenOnboarding = { navController.navigate(DetailRoutes.ONBOARDING) },
                        )
                    }
                }
            }
            composable(DetailRoutes.ONBOARDING) {
                if (visibleRoute.value == DetailRoutes.ONBOARDING) {
                    OnboardingScreen(padding, draft, onDraftChange = { draft = it }, onFinish = {
                        onFinishOnboarding(draft.homeLatitude.toDoubleOrNull(), draft.homeLongitude.toDoubleOrNull()) {
                            navController.navigate(TopLevelDestination.START.route) {
                                popUpTo(DetailRoutes.ONBOARDING) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    })
                }
            }
            composable(DetailRoutes.DAY_REPORT) {
                if (visibleRoute.value == DetailRoutes.DAY_REPORT) DayReportScreen(padding)
            }
            composable(DetailRoutes.CARD_IMPORT) {
                if (visibleRoute.value == DetailRoutes.CARD_IMPORT) CardHardwareTestScreen(padding, cardActivityImports, cardVehicleImports)
            }
            composable(DetailRoutes.VEHICLE_DETAIL) {
                if (visibleRoute.value == DetailRoutes.VEHICLE_DETAIL) VehicleDetailPlaceholderScreen(padding)
            }
            composable(DetailRoutes.SETTINGS_SUBPAGE) {
                if (visibleRoute.value == DetailRoutes.SETTINGS_SUBPAGE) SettingsSubpagePlaceholderScreen(padding, settingsTitle)
            }
        }
    }
}
