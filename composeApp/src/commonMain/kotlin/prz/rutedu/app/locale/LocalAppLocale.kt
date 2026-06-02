package prz.rutedu.app.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import prz.rutedu.app.getSystemLanguage


/**
 * Returns the currently active language code, checking [customAppLocale]
 * first and falling back to the detected system language.
 *
 * Checks if the language is supported by [prz.rutedu.app.data.QuestionBank], falling back to `"en"`.
 */
fun getCurrentLanguage(): String {
    val lang = customAppLocale ?: getSystemLanguage()
    return if (lang in prz.rutedu.app.data.QuestionBank.SUPPORTED_QUESTION_LANGS) lang else "en"
}


/**
 * Global mutable state that holds the user's selected language code
 * (e.g. `"pl"`, `"en"`), or `null` when the system default is in use.
 *
 * This is written by `Settings.kt` when the user switches language and read by
 * [AppLocaleProvider] to force a full recomposition of the UI subtree. It is also
 * persisted to and loaded from the database via `appSettings` on app startup in `App.kt`.
 */
var customAppLocale by mutableStateOf<String?>(null)

/**
 * Platform-specific composition local that exposes the current locale string to the
 * composable tree.
 *
 * This is an `expect` object - each platform provides its own `actual` implementation
 * that reads the system locale (e.g. `Locale.getDefault().language` on Android).
 * When [customAppLocale] is non-null it overrides the system value.
 *
 * Composables that need to adapt to the current language can read:
 * ```
 * val locale = LocalAppLocale.current // e.g. "pl" or "en"
 * ```
 */
expect object LocalAppLocale {
    /** The current locale string available inside the composition. */
    val current: String @Composable get
    /**
     * Returns a [ProvidedValue] that overrides the locale for a subtree.
     * Pass `null` to fall back to the system locale.
     */
    @Composable infix fun provides(value: String?): ProvidedValue<*>
}

/**
 * Wraps [content] in a [CompositionLocalProvider] that injects [customAppLocale] into
 * the composition and forces a full recomposition of [content] whenever the locale changes.
 *
 * This composable must wrap the entire UI (placed around [prz.rutedu.app.App]'s
 * `MaterialTheme`) so that all locale-dependent composables react to language switches.
 *
 * The `key(customAppLocale)` block ensures every composable in the subtree is recreated -
 * not just recomposed - when the locale changes, which is necessary for composables that
 * read locale at initialisation time rather than on each recomposition.
 *
 * @param content The UI subtree that should respect [customAppLocale].
 */
@Composable
fun AppLocaleProvider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppLocale provides customAppLocale,
    ) {
        androidx.compose.runtime.key(customAppLocale) {
            content()
        }
    }
}
