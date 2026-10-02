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
import androidx.compose.runtime.LaunchedEffect
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
import de.fahrerabrechnung.data.card.CardSourcePairRepository
import de.fahrerabrechnung.data.card.SavedCardSourcePair
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.SavedCardActivity
import de.fahrerabrechnung.data.card.SavedCardVehicles
import de.fahrerabrechnung.data.card.DriverCardImportRepository
import de.fahrerabrechnung.data.card.DriverCardImportPipeline
import de.fahrerabrechnung.data.card.CardImportUiState
import de.fahrerabrechnung.data.card.CardImportPhase
import de.fahrerabrechnung.feature.analytics.AnalyticsScreen
import de.fahrerabrechnung.feature.calendar.CalendarScreen
import de.fahrerabrechnung.feature.common.LocalScreenScrollState
import de.fahrerabrechnung.feature.detail.DayReportScreen
import de.fahrerabrechnung.feature.detail.SettingsSubpagePlaceholderScreen
import de.fahrerabrechnung.feature.payroll.PayrollScreen
import de.fahrerabrechnung.feature.settings.SettingsScreen
import de.fahrerabrechnung.feature.start.CardFilesStatus
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
    const val CARD_SNAPSHOTS = "card_snapshots"
    const val CARD_ARCHIVE = "card_archive"
    const val CARD_PREVIEW = "card_preview"
    const val VEHICLE_DETAIL = "vehicle_detail"
    const val SETTINGS_SUBPAGE = "settings_subpage"
}

@Composable
fun AppRoot(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    cardActivityImports: CardActivityImportRepository? = null,
    cardVehicleImports: CardVehicleImportRepository? = null,
    cardSourcePairs: CardSourcePairRepository? = null,
    onboardingComplete: Boolean = true,
    onFinishOnboarding: (Double?, Double?, () -> Unit) -> Unit = { _, _, done -> done() },
    driverCardImports: DriverCardImportRepository? = null,
    cardImportPipeline: DriverCardImportPipeline? = null,
    importState: CardImportUiState? = null,
    onConfirmCard: (String) -> Unit = {},
    onDeclineCard: (String) -> Unit = {},
    onRetryImport: () -> Unit = {},
) {
    val navController = rememberNavController()
    var draft by rememberSaveable(stateSaver = OnboardingDraft.Saver) { mutableStateOf(OnboardingDraft()) }
    var settingsTitle by rememberSaveable { mutableStateOf("Einstellung") }
    var vehicleHistoryQuery by rememberSaveable { mutableStateOf("") }
    var vehicleHistoryUseStart by rememberSaveable { mutableStateOf<Long?>(null) }
    var vehicleHistoryNation by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedActivitySnapshot by androidx.compose.runtime.remember {
        mutableStateOf<SavedCardActivity?>(null)
    }
    var selectedVehicleSnapshot by androidx.compose.runtime.remember {
        mutableStateOf<SavedCardVehicles?>(null)
    }
    var selectedSourcePair by androidx.compose.runtime.remember { mutableStateOf<SavedCardSourcePair?>(null) }
    var cardFilesStatus by androidx.compose.runtime.remember { mutableStateOf(CardFilesStatus.CHECKING) }
    var previewIsArchive by androidx.compose.runtime.remember { mutableStateOf(false) }
    var previewLoadError by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: if (onboardingComplete) TopLevelDestination.START.route else DetailRoutes.ONBOARDING
    // NavHost retains outgoing entries during its AnimatedContent transition, even with
    // Enter/ExitTransition.None. Render only the current entry to avoid two screens at once.
    val visibleRoute = rememberUpdatedState(currentRoute)
    // Keep each tab's scroll position alive while its outgoing NavHost content is hidden.
    val tabScrollStates = TopLevelDestination.entries.associate { it.route to rememberScrollState() }
    LaunchedEffect(currentRoute, cardActivityImports, cardVehicleImports, driverCardImports, importState?.lastRun?.id) {
        if (currentRoute == TopLevelDestination.START.route) {
            cardFilesStatus = CardFilesStatus.CHECKING
            cardFilesStatus = try {
                if (cardActivityImports == null || cardVehicleImports == null) CardFilesStatus.ERROR
                else {
                    val bound = driverCardImports?.profile() != null
                    val own = if (bound) driverCardImports?.latest() else null
                    val activity = if (bound) own?.activity != null else cardActivityImports.latest() != null
                    val vehicle = if (bound) own?.vehicles != null else cardVehicleImports.latest() != null
                    when {
                        activity && vehicle -> CardFilesStatus.BOTH
                        activity -> CardFilesStatus.ACTIVITY
                        vehicle -> CardFilesStatus.VEHICLE
                        else -> CardFilesStatus.NONE
                    }
                }
            } catch (_: Exception) { CardFilesStatus.ERROR }
        }
    }
    LaunchedEffect(importState?.phase, onboardingComplete) {
        if (onboardingComplete && importState?.phase in listOf(CardImportPhase.READING, CardImportPhase.SAVING,
                CardImportPhase.CONFIRM_FIRST_CARD, CardImportPhase.CONFIRM_REPLACEMENT,
                CardImportPhase.IMPORTED, CardImportPhase.INCOMPLETE, CardImportPhase.FOREIGN_CARD, CardImportPhase.ERROR)) {
            selectedSourcePair = null; selectedActivitySnapshot = null; selectedVehicleSnapshot = null
            previewLoadError = null
            navController.navigate(DetailRoutes.CARD_IMPORT) { launchSingleTop = true }
        }
    }
    LaunchedEffect(currentRoute, importState?.lastRun?.sessionId) {
        if (currentRoute == DetailRoutes.CARD_PREVIEW && !previewIsArchive && selectedActivitySnapshot == null && selectedVehicleSnapshot == null) {
            try {
                val saved = importState?.lastRun?.sessionId?.let { driverCardImports?.get(it) }
                    ?: driverCardImports?.latest()
                selectedSourcePair = saved?.pair
                selectedActivitySnapshot = saved?.activity
                selectedVehicleSnapshot = saved?.vehicles
                if (saved == null) previewLoadError = "Noch keine gespeicherten Kartendaten vorhanden."
            } catch (_: Exception) { previewLoadError = "Die gespeicherten Kartendaten konnten nicht geöffnet werden." }
        }
    }
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }
    val title = if (topLevel == TopLevelDestination.START) FahrerBrand.NAME else topLevel?.label ?: when (currentRoute) {
        DetailRoutes.ONBOARDING -> FahrerBrand.NAME
        DetailRoutes.DAY_REPORT -> "Tagesübersicht"
        DetailRoutes.CARD_IMPORT -> "Fahrerkarte"
        DetailRoutes.CARD_SNAPSHOTS -> if (cardImportPipeline != null) "Importverlauf" else "Gespeicherte Dateien"
        DetailRoutes.CARD_ARCHIVE -> "Früheres Dateiarchiv"
        DetailRoutes.CARD_PREVIEW -> "Kartendaten"
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
                        StartScreen(padding, onOpenCardImport = { navController.navigate(DetailRoutes.CARD_IMPORT) },
                            onOpenOnboarding = { navController.navigate(DetailRoutes.ONBOARDING) },
                            cardFilesStatus = cardFilesStatus)
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
                if (visibleRoute.value == DetailRoutes.CARD_IMPORT && importState != null) CardImportScreen(
                    padding, importState, onConfirmCard, onDeclineCard, onRetryImport,
                    onOpenHistory = { navController.navigate(DetailRoutes.CARD_SNAPSHOTS) },
                    onOpenLatest = {
                        previewIsArchive = false; previewLoadError = null
                        selectedSourcePair = null; selectedActivitySnapshot = null; selectedVehicleSnapshot = null
                        navController.navigate(DetailRoutes.CARD_PREVIEW)
                    },
                    onOpenVehicles = {
                        selectedVehicleSnapshot = null; selectedSourcePair = null
                        vehicleHistoryQuery = ""; vehicleHistoryUseStart = null; vehicleHistoryNation = null
                        navController.navigate(DetailRoutes.VEHICLE_DETAIL)
                    })
                else if (visibleRoute.value == DetailRoutes.CARD_IMPORT) CardHardwareTestScreen(
                    padding, cardActivityImports, cardVehicleImports,
                    sourcePairs = cardSourcePairs, previewPair = selectedSourcePair,
                    driverImports = driverCardImports,
                    onShowLatestPair = {
                        selectedSourcePair = null
                        selectedActivitySnapshot = null
                        selectedVehicleSnapshot = null
                    },
                    previewSnapshot = selectedActivitySnapshot,
                    onShowLatestSnapshot = {
                        selectedActivitySnapshot = null
                        selectedSourcePair = null
                        if (driverCardImports != null) selectedVehicleSnapshot = null
                    },
                    previewVehicleSnapshot = selectedVehicleSnapshot,
                    onShowLatestVehicleSnapshot = {
                        selectedVehicleSnapshot = null
                        selectedSourcePair = null
                        if (driverCardImports != null) selectedActivitySnapshot = null
                    },
                    onOpenSnapshots = { navController.navigate(DetailRoutes.CARD_SNAPSHOTS) },
                    onOpenVehicleHistory = { query, useStart, nation ->
                        // An explicitly selected archive stays within its own source file.
                        if (driverCardImports == null || selectedVehicleSnapshot == null) {
                            selectedSourcePair = null
                            selectedVehicleSnapshot = null
                        }
                        vehicleHistoryQuery = query.orEmpty()
                        vehicleHistoryUseStart = useStart
                        vehicleHistoryNation = nation
                        navController.navigate(DetailRoutes.VEHICLE_DETAIL)
                    })
            }
            composable(DetailRoutes.CARD_SNAPSHOTS) {
                if (visibleRoute.value == DetailRoutes.CARD_SNAPSHOTS && cardImportPipeline != null && driverCardImports != null)
                    CardImportHistoryScreen(padding, cardImportPipeline, driverCardImports,
                        onPreview = { saved ->
                            previewIsArchive = false; previewLoadError = null
                            selectedSourcePair = saved.pair; selectedActivitySnapshot = saved.activity; selectedVehicleSnapshot = saved.vehicles
                            navController.navigate(DetailRoutes.CARD_PREVIEW)
                        }, onOpenArchive = { navController.navigate(DetailRoutes.CARD_ARCHIVE) })
                else if (visibleRoute.value == DetailRoutes.CARD_SNAPSHOTS)
                    CardSnapshotHistoryScreen(padding, cardActivityImports, cardVehicleImports,
                        pairs = cardSourcePairs,
                        driverImports = driverCardImports,
                        onPreviewPair = { saved ->
                            selectedSourcePair = saved
                            selectedActivitySnapshot = saved.activity
                            selectedVehicleSnapshot = saved.vehicles
                            navController.popBackStack()
                        },
                        onPreviewActivity = { saved ->
                            selectedSourcePair = null
                            selectedActivitySnapshot = saved
                            if (driverCardImports != null) selectedVehicleSnapshot = null
                            navController.popBackStack()
                        },
                        onPreviewVehicle = { saved ->
                            selectedSourcePair = null
                            selectedVehicleSnapshot = saved
                            if (driverCardImports != null) selectedActivitySnapshot = null
                            vehicleHistoryQuery = ""
                            vehicleHistoryUseStart = null
                            vehicleHistoryNation = null
                            navController.navigate(DetailRoutes.VEHICLE_DETAIL)
                        },
                        onPreviewVehicleDay = { saved ->
                            selectedSourcePair = null
                            selectedVehicleSnapshot = saved
                            selectedActivitySnapshot = null
                            navController.popBackStack()
                        })
            }
            composable(DetailRoutes.CARD_ARCHIVE) {
                if (visibleRoute.value == DetailRoutes.CARD_ARCHIVE) CardSnapshotHistoryScreen(padding,
                    cardActivityImports, cardVehicleImports, pairs = cardSourcePairs,
                    onPreviewPair = { saved ->
                        previewIsArchive = true; selectedSourcePair = saved
                        selectedActivitySnapshot = saved.activity; selectedVehicleSnapshot = saved.vehicles
                        navController.navigate(DetailRoutes.CARD_PREVIEW)
                    }, onPreviewActivity = { saved ->
                        previewIsArchive = true; selectedSourcePair = null
                        selectedActivitySnapshot = saved; selectedVehicleSnapshot = null
                        navController.navigate(DetailRoutes.CARD_PREVIEW)
                    }, onPreviewVehicle = { saved ->
                        selectedVehicleSnapshot = saved; vehicleHistoryQuery = ""
                        vehicleHistoryUseStart = null; vehicleHistoryNation = null
                        navController.navigate(DetailRoutes.VEHICLE_DETAIL)
                    }, onPreviewVehicleDay = { saved ->
                        previewIsArchive = true; selectedSourcePair = null
                        selectedActivitySnapshot = null; selectedVehicleSnapshot = saved
                        navController.navigate(DetailRoutes.CARD_PREVIEW)
                    })
            }
            composable(DetailRoutes.CARD_PREVIEW) {
                if (visibleRoute.value == DetailRoutes.CARD_PREVIEW) {
                    if (previewLoadError != null) de.fahrerabrechnung.feature.common.ScreenContent(padding) { Text(checkNotNull(previewLoadError)) }
                    else SavedCardPreviewScreen(padding, selectedActivitySnapshot, selectedVehicleSnapshot,
                        archive = previewIsArchive, onOpenVehicles = { query, useStart, nation ->
                            vehicleHistoryQuery = query.orEmpty(); vehicleHistoryUseStart = useStart; vehicleHistoryNation = nation
                            navController.navigate(DetailRoutes.VEHICLE_DETAIL)
                        })
                }
            }
            composable(DetailRoutes.VEHICLE_DETAIL) {
                if (visibleRoute.value == DetailRoutes.VEHICLE_DETAIL)
                    VehicleHistoryScreen(padding, cardVehicleImports, vehicleHistoryQuery,
                        vehicleHistoryUseStart, vehicleHistoryNation,
                        previewSnapshot = selectedVehicleSnapshot, driverImports = driverCardImports)
            }
            composable(DetailRoutes.SETTINGS_SUBPAGE) {
                if (visibleRoute.value == DetailRoutes.SETTINGS_SUBPAGE) SettingsSubpagePlaceholderScreen(padding, settingsTitle)
            }
        }
    }
}
