package de.fahrerabrechnung.core.design

import androidx.compose.ui.graphics.Color

internal object LightTokens {
    val Primary = Color(0xFF174A6E)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD2E5F4)
    val OnPrimaryContainer = Color(0xFF0B2638)
    val Secondary = Color(0xFF526777)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFD6E4EE)
    val OnSecondaryContainer = Color(0xFF152A38)
    val Tertiary = Color(0xFF5F5E7A)
    val OnTertiary = Color(0xFFFFFFFF)
    val Background = Color(0xFFF7F9FC)
    val OnBackground = Color(0xFF1A1C1E)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1C1E)
    val SurfaceVariant = Color(0xFFE1E7ED)
    val OnSurfaceVariant = Color(0xFF41474C)
    val Outline = Color(0xFF71787E)
    val OutlineVariant = Color(0xFFC1C7CD)
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)
}

internal object DarkTokens {
    val Primary = Color(0xFF9BCBEE)
    val OnPrimary = Color(0xFF00344F)
    val PrimaryContainer = Color(0xFF0E4869)
    val OnPrimaryContainer = Color(0xFFCBE8FA)
    val Secondary = Color(0xFFB9C9D5)
    val OnSecondary = Color(0xFF24333E)
    val SecondaryContainer = Color(0xFF3A4A56)
    val OnSecondaryContainer = Color(0xFFD5E5F0)
    val Tertiary = Color(0xFFC8C4EA)
    val OnTertiary = Color(0xFF302E4B)
    val Background = Color(0xFF101417)
    val OnBackground = Color(0xFFE1E3E6)
    val Surface = Color(0xFF161A1D)
    val OnSurface = Color(0xFFE1E3E6)
    val SurfaceVariant = Color(0xFF41484D)
    val OnSurfaceVariant = Color(0xFFC1C7CD)
    val Outline = Color(0xFF8B9297)
    val OutlineVariant = Color(0xFF41484D)
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)
}

data class StatusColors(
    val positive: Color,
    val warning: Color,
    val critical: Color,
    val info: Color,
    val neutral: Color,
)

val LightStatusColors = StatusColors(
    positive = Color(0xFF2E6B4F),
    warning = Color(0xFF9A6700),
    critical = Color(0xFFBA1A1A),
    info = Color(0xFF376A8A),
    neutral = Color(0xFF6F767C),
)

val DarkStatusColors = StatusColors(
    positive = Color(0xFF8CD3AC),
    warning = Color(0xFFF4C55B),
    critical = Color(0xFFFFB4AB),
    info = Color(0xFF9CCBEB),
    neutral = Color(0xFFABB1B6),
)
