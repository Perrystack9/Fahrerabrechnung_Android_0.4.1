package de.fahrerabrechnung.app

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
import androidx.compose.runtime.getValue
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
import de.fahrerabrechnung.feature.analytics.AnalyticsScreen
import de.fahrerabrechnung.feature.calendar.CalendarScreen
import de.fahrerabrechnung.feature.detail.CardImportPlaceholderScreen
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

private object DetailRoutes {
    const val DAY_REPORT = "day_report"
    const val CARD_IMPORT = "card_import"
    const val VEHICLE_DETAIL = "vehicle_detail"
    const val SETTINGS_SUBPAGE = "settings_subpage"
}

@Composable
fun AppRoot(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TopLevelDestination.START.route
    val topLevel = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }
    val title = topLevel?.label ?: when (currentRoute) {
        DetailRoutes.DAY_REPORT -> "Tagesreport"
        DetailRoutes.CARD_IMPORT -> "Fahrerkarte"
        DetailRoutes.VEHICLE_DETAIL -> "Fahrzeug"
        DetailRoutes.SETTINGS_SUBPAGE -> "Einstellung"
        else -> "Fahrerabrechnung"
    }

    AppScaffold(
        title = title,
        navigationIcon = {
            if (topLevel == null) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.testTag("back")) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Zurück")
                }
            }
        },
        bottomBar = {
            if (topLevel != null) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                            modifier = Modifier.testTag("nav_${destination.route}"),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = TopLevelDestination.START.route) {
            composable(TopLevelDestination.START.route) {
                StartScreen(padding, onOpenCardImport = { navController.navigate(DetailRoutes.CARD_IMPORT) })
            }
            composable(TopLevelDestination.CALENDAR.route) {
                CalendarScreen(padding, onOpenExampleDay = { navController.navigate(DetailRoutes.DAY_REPORT) })
            }
            composable(TopLevelDestination.ANALYTICS.route) { AnalyticsScreen(padding) }
            composable(TopLevelDestination.PAYROLL.route) { PayrollScreen(padding) }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsScreen(
                    padding = padding,
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged,
                    onOpenSubpage = { navController.navigate(DetailRoutes.SETTINGS_SUBPAGE) },
                )
            }
            composable(DetailRoutes.DAY_REPORT) { DayReportScreen(padding) }
            composable(DetailRoutes.CARD_IMPORT) { CardImportPlaceholderScreen(padding) }
            composable(DetailRoutes.VEHICLE_DETAIL) { VehicleDetailPlaceholderScreen(padding) }
            composable(DetailRoutes.SETTINGS_SUBPAGE) { SettingsSubpagePlaceholderScreen(padding, "Einstellungsdetail") }
        }
    }
}
