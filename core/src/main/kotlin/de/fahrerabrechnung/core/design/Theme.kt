package de.fahrerabrechnung.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import de.fahrerabrechnung.core.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fahrerabrechnung.core.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = LightTokens.Primary,
    onPrimary = LightTokens.OnPrimary,
    primaryContainer = LightTokens.PrimaryContainer,
    onPrimaryContainer = LightTokens.OnPrimaryContainer,
    secondary = LightTokens.Secondary,
    onSecondary = LightTokens.OnSecondary,
    secondaryContainer = LightTokens.SecondaryContainer,
    onSecondaryContainer = LightTokens.OnSecondaryContainer,
    tertiary = LightTokens.Tertiary,
    onTertiary = LightTokens.OnTertiary,
    background = LightTokens.Background,
    onBackground = LightTokens.OnBackground,
    surface = LightTokens.Surface,
    surfaceTint = LightTokens.Primary,
    surfaceContainer = LightTokens.Surface,
    surfaceContainerLow = androidx.compose.ui.graphics.Color(0xFFF7F9FB),
    surfaceContainerHigh = LightTokens.SurfaceVariant,
    surfaceContainerHighest = LightTokens.SurfaceVariant,
    surfaceContainerLowest = LightTokens.Surface,
    onSurface = LightTokens.OnSurface,
    surfaceVariant = LightTokens.SurfaceVariant,
    onSurfaceVariant = LightTokens.OnSurfaceVariant,
    outline = LightTokens.Outline,
    outlineVariant = LightTokens.OutlineVariant,
    error = LightTokens.Error,
    onError = LightTokens.OnError,
    errorContainer = LightTokens.ErrorContainer,
    onErrorContainer = LightTokens.OnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = DarkTokens.Primary,
    onPrimary = DarkTokens.OnPrimary,
    primaryContainer = DarkTokens.PrimaryContainer,
    onPrimaryContainer = DarkTokens.OnPrimaryContainer,
    secondary = DarkTokens.Secondary,
    onSecondary = DarkTokens.OnSecondary,
    secondaryContainer = DarkTokens.SecondaryContainer,
    onSecondaryContainer = DarkTokens.OnSecondaryContainer,
    tertiary = DarkTokens.Tertiary,
    onTertiary = DarkTokens.OnTertiary,
    background = DarkTokens.Background,
    onBackground = DarkTokens.OnBackground,
    surface = DarkTokens.Surface,
    surfaceTint = DarkTokens.Primary,
    surfaceContainer = DarkTokens.Surface,
    surfaceContainerLow = DarkTokens.Surface,
    surfaceContainerHigh = DarkTokens.SurfaceVariant,
    surfaceContainerHighest = DarkTokens.SurfaceVariant,
    surfaceContainerLowest = DarkTokens.Background,
    onSurface = DarkTokens.OnSurface,
    surfaceVariant = DarkTokens.SurfaceVariant,
    onSurfaceVariant = DarkTokens.OnSurfaceVariant,
    outline = DarkTokens.Outline,
    outlineVariant = DarkTokens.OutlineVariant,
    error = DarkTokens.Error,
    onError = DarkTokens.OnError,
    errorContainer = DarkTokens.ErrorContainer,
    onErrorContainer = DarkTokens.OnErrorContainer,
)

@OptIn(ExperimentalTextApi::class)
private val Inter = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(R.font.inter_variable, weight = weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
    },
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Inter, fontSize = 48.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = Inter, fontSize = 40.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.Medium),
    displaySmall = TextStyle(fontFamily = Inter, fontSize = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineLarge = TextStyle(fontFamily = Inter, fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontFamily = Inter, fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontFamily = Inter, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = Inter, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = Inter, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

object FahrerThemeTokens {
    val spacing: AppSpacing @Composable get() = LocalAppSpacing.current
    val statusColors: StatusColors @Composable get() = LocalStatusColors.current
}

@Composable
fun FahrerTagebuchTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing(),
        LocalStatusColors provides if (dark) DarkStatusColors else LightStatusColors,
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
