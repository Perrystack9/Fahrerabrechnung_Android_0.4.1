package de.fahrerabrechnung.core.design

import androidx.compose.ui.graphics.Color

object BrandColors {
    val Navy = Color(0xFF0F2D4A)
    val Petrol = Color(0xFF008B8B)
    val LightGray = Color(0xFFE5E7EB)
    val White = Color(0xFFFFFFFF)
}

internal object LightTokens {
    val Primary = Color(0xFF0F2D4A)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE8EFF5)
    val OnPrimaryContainer = Color(0xFF0F2D4A)
    val Secondary = Color(0xFF008B8B)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE0F2F1)
    val OnSecondaryContainer = Color(0xFF0F2D4A)
    val Tertiary = Color(0xFF006B6B)
    val OnTertiary = Color(0xFFFFFFFF)
    val Background = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF0F2D4A)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF0F2D4A)
    val SurfaceVariant = Color(0xFFE5E7EB)
    val OnSurfaceVariant = Color(0xFF526271)
    val Outline = Color(0xFF71787E)
    val OutlineVariant = Color(0xFFD0D8DE)
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)
}

internal object DarkTokens {
    val Primary = Color(0xFFBAD6ED)
    val OnPrimary = Color(0xFF0F2D4A)
    val PrimaryContainer = Color(0xFF21435F)
    val OnPrimaryContainer = Color(0xFFCBE8FA)
    val Secondary = Color(0xFF73D4D1)
    val OnSecondary = Color(0xFF003B3A)
    val SecondaryContainer = Color(0xFF005150)
    val OnSecondaryContainer = Color(0xFFB8EFEC)
    val Tertiary = Color(0xFF73D4D1)
    val OnTertiary = Color(0xFF003B3A)
    val Background = Color(0xFF091B2B)
    val OnBackground = Color(0xFFF0F4F7)
    val Surface = Color(0xFF0F2D4A)
    val OnSurface = Color(0xFFF0F4F7)
    val SurfaceVariant = Color(0xFF294459)
    val OnSurfaceVariant = Color(0xFFD0D8DE)
    val Outline = Color(0xFF8B9297)
    val OutlineVariant = Color(0xFF294459)
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
