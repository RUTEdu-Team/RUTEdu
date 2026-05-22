package prz.rutedu.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import prz.rutedu.app.models.Subject

/**
 * Represents the theme configurations supported by the application.
 */
enum class ThemeMode {
    /**
     * Follows the system-wide light/dark theme preference.
     */
    SYSTEM,

    /**
     * Forces the light color scheme.
     */
    LIGHT,

    /**
     * Forces the dark color scheme.
     */
    DARK;

    companion object {
        /**
         * Parses a string value into a [ThemeMode].
         *
         * Performs a case-insensitive lookup. If the string is null, empty, or does not
         * match any known mode, it defaults to [SYSTEM].
         *
         * @param value The string representation of the theme mode (e.g., from settings storage).
         * @return The corresponding [ThemeMode] enum constant.
         */
        fun fromString(value: String?): ThemeMode {
            return when (value?.lowercase()) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> SYSTEM
            }
        }
    }
}

/**
 * Global state holding the currently selected [ThemeMode] of the application.
 *
 * This is backed by Compose's [mutableStateOf], meaning any composable referencing it
 * or functions like [isAppInDarkTheme] will automatically recompose when this value changes.
 */
var customAppThemeMode by mutableStateOf(ThemeMode.SYSTEM)

/**
 * The brand's primary color, a vibrant orange accent, used across light and dark themes
 * to emphasize active elements, primary buttons, and key highlights.
 */
val PrimaryColor = Color(0xFFF47B20)

/**
 * The Material 3 Light Color Scheme configuration for the application.
 *
 * Implements a bright, clean aesthetic with:
 * - [PrimaryColor] (orange) for active indicators and accents.
 * - Soft cream/light-gray backgrounds to reduce eye strain.
 * - Solid white surfaces for elevated containers (cards, sheets).
 * - Slate/cool-gray secondary tones for supporting layout elements.
 */
val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEFE3), // Soft orange-white tint
    onPrimaryContainer = Color(0xFFE05300), // High contrast orange
    secondary = Color(0xFF5C6B73), // Cool gray/slate secondary
    onSecondary = Color.White,
    background = Color(0xFFF5F6FA), // Cream/light gray background
    onBackground = Color(0xFF1A1A1A), // Charcoal text
    surface = Color.White, // White cards / sheets
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE8EBF0), // Muted card background (e.g. for locked cards)
    onSurfaceVariant = Color(0xFF8F9BB3), // Gray subtitle
    outline = Color(0xFFDDE1E9), // Light outline gray
    outlineVariant = Color(0xFFE2E6ED)
)

/**
 * The Material 3 Dark Color Scheme configuration for the application.
 *
 * Designed for comfortable viewing in low-light environments, using:
 * - A near-black background to conserve device battery and reduce eye strain.
 * - Dark charcoal surfaces to create depth and separate cards from the background.
 * - Accessible high-contrast orange containers and light orange text to remain readable.
 * - Slate and muted gray tones for less important UI outlines and secondary elements.
 */
val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C2B00), // Dark orange tint container
    onPrimaryContainer = Color(0xFFFFB076), // High contrast light orange
    secondary = Color(0xFF3E4E56), // Dark slate secondary
    onSecondary = Color(0xFFE2E6ED),
    background = Color(0xFF0F1012), // Near-black background
    onBackground = Color(0xFFE2E6ED), // Off-white text
    surface = Color(0xFF1A1C1E), // Dark charcoal surfaces for cards
    onSurface = Color(0xFFE2E6ED),
    surfaceVariant = Color(0xFF25282B), // Alternative dark card surface
    onSurfaceVariant = Color(0xFF8F9BB3), // Gray subtitle
    outline = Color(0xFF373E47), // Muted dark gray outline
    outlineVariant = Color(0xFF2E333D)
)

/**
 * The main theme composable wrapper for the RUTEdu application.
 *
 * Configures the [MaterialTheme] using either the light or dark Material 3 color scheme
 * based on the provided [themeMode].
 *
 * **Usage:**
 * ```
 * RUTEduTheme(themeMode = ThemeMode.DARK) {
 *     // App content here
 * }
 * ```
 *
 * @param themeMode The [ThemeMode] to apply. Defaults to the global [customAppThemeMode].
 * @param content The composable content hierarchy to apply this theme to.
 */
@Composable
fun RUTEduTheme(
    themeMode: ThemeMode = customAppThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Checks if the application is currently rendered in dark theme.
 *
 * Resolves the configuration by evaluating the active [customAppThemeMode] and checking the system-wide
 * dark theme setting via [isSystemInDarkTheme] if the mode is set to [ThemeMode.SYSTEM].
 *
 * @return `true` if the theme is dark, `false` otherwise.
 */
@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean {
    return when (customAppThemeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

/**
 * Resolves the subject's card background color in a theme-aware way.
 *
 * - **Light Mode:** Uses the static, pre-defined [Subject.backgroundColor].
 * - **Dark Mode:** Derives a color by applying a 15% opacity to the subject's main [Subject.color].
 *
 * @return The resolved [Color] to be used as the background for subject-related cards or headers.
 */
@Composable
@ReadOnlyComposable
fun Subject.themeBackgroundColor(): Color {
    return if (isAppInDarkTheme()) {
        this.color.copy(alpha = 0.15f)
    } else {
        this.backgroundColor
    }
}
