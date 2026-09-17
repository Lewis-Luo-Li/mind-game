package com.idroid.stuido.mind_game.core.ui.primitives

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.idroid.stuido.mind_game.core.ui.theme.AppThemeScheme

/**
 * Brand / semantic color roles (Material3).
 * Every [com.idroid.stuido.mind_game.core.ui.theme.AppThemeScheme] ships a full light+dark [ColorScheme] pair.
 */
private val LilacLight = lightColorScheme(
    primary = Color(0xFF6650A4), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF), onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8), onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4), onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFEF7FF), onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFEF7FF), onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC), onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E), outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
)

private val LilacDark = darkColorScheme(
    primary = Color(0xFFD0BCFF), onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B), onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC), onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458), onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8), onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48), onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF141218), onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218), onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F), onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99), outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
)

private val OceanLight = lightColorScheme(
    primary = Color(0xFF00639B), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBE6FF), onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF50606F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E5F6), onSecondaryContainer = Color(0xFF0C1D2A),
    tertiary = Color(0xFF66587A), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFECDDFF), onTertiaryContainer = Color(0xFF211633),
    background = Color(0xFFF8FDFF), onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF8FDFF), onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFDDE3EA), onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E), outlineVariant = Color(0xFFC1C7CE),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)

private val OceanDark = darkColorScheme(
    primary = Color(0xFF8CCDFF), onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B70), onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFFB7C9DA), onSecondary = Color(0xFF22323F),
    secondaryContainer = Color(0xFF384957), onSecondaryContainer = Color(0xFFD3E5F6),
    tertiary = Color(0xFFCFBFF3), onTertiary = Color(0xFF372B49),
    tertiaryContainer = Color(0xFF4E4161), onTertiaryContainer = Color(0xFFECDDFF),
    background = Color(0xFF101418), onBackground = Color(0xFFE0E2E6),
    surface = Color(0xFF101418), onSurface = Color(0xFFE0E2E6),
    surfaceVariant = Color(0xFF41484D), onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B939A), outlineVariant = Color(0xFF41484D),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

private val ForestLight = lightColorScheme(
    primary = Color(0xFF3B653D), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBCECBA), onPrimaryContainer = Color(0xFF002105),
    secondary = Color(0xFF53634F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8CF), onSecondaryContainer = Color(0xFF111F10),
    tertiary = Color(0xFF3B6470), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBEEAF6), onTertiaryContainer = Color(0xFF001F27),
    background = Color(0xFFF7FBF0), onBackground = Color(0xFF181D16),
    surface = Color(0xFFF7FBF0), onSurface = Color(0xFF181D16),
    surfaceVariant = Color(0xFFDEE5D9), onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF73796E), outlineVariant = Color(0xFFC2C8BC),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)

private val ForestDark = darkColorScheme(
    primary = Color(0xFFA1D09F), onPrimary = Color(0xFF0B3A10),
    primaryContainer = Color(0xFF245126), onPrimaryContainer = Color(0xFFBCECBA),
    secondary = Color(0xFFBBCCB4), onSecondary = Color(0xFF263423),
    secondaryContainer = Color(0xFF3C4B38), onSecondaryContainer = Color(0xFFD6E8CF),
    tertiary = Color(0xFFA2CDDA), onTertiary = Color(0xFF013640),
    tertiaryContainer = Color(0xFF214C57), onTertiaryContainer = Color(0xFFBEEAF6),
    background = Color(0xFF10150E), onBackground = Color(0xFFE1E4DA),
    surface = Color(0xFF10150E), onSurface = Color(0xFFE1E4DA),
    surfaceVariant = Color(0xFF424940), onSurfaceVariant = Color(0xFFC2C8BC),
    outline = Color(0xFF8C9388), outlineVariant = Color(0xFF424940),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

private val SunsetLight = lightColorScheme(
    primary = Color(0xFF9C4120), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCC), onPrimaryContainer = Color(0xFF360D00),
    secondary = Color(0xFF77574B), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCC), onSecondaryContainer = Color(0xFF2C160D),
    tertiary = Color(0xFF6C5C2E), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E0A5), onTertiaryContainer = Color(0xFF231A00),
    background = Color(0xFFFFF8F6), onBackground = Color(0xFF221A17),
    surface = Color(0xFFFFF8F6), onSurface = Color(0xFF221A17),
    surfaceVariant = Color(0xFFF4DED5), onSurfaceVariant = Color(0xFF52433D),
    outline = Color(0xFF85736C), outlineVariant = Color(0xFFD7C2BA),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)

private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB59A), onPrimary = Color(0xFF531E06),
    primaryContainer = Color(0xFF7A2F12), onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFE7BEB0), onSecondary = Color(0xFF442A1E),
    secondaryContainer = Color(0xFF5D4033), onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = Color(0xFFD8C38B), onTertiary = Color(0xFF3A2F04),
    tertiaryContainer = Color(0xFF52451A), onTertiaryContainer = Color(0xFFF5E0A5),
    background = Color(0xFF1E1511), onBackground = Color(0xFFF1DFDA),
    surface = Color(0xFF1E1511), onSurface = Color(0xFFF1DFDA),
    surfaceVariant = Color(0xFF52433D), onSurfaceVariant = Color(0xFFD7C2BA),
    outline = Color(0xFFA08C85), outlineVariant = Color(0xFF52433D),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

fun schemeFor(theme: AppThemeScheme, dark: Boolean): ColorScheme = when (theme) {
    AppThemeScheme.LILAC -> if (dark) LilacDark else LilacLight
    AppThemeScheme.OCEAN -> if (dark) OceanDark else OceanLight
    AppThemeScheme.FOREST -> if (dark) ForestDark else ForestLight
    AppThemeScheme.SUNSET -> if (dark) SunsetDark else SunsetLight
}
