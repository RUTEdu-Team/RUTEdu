package prz.rutedu.app.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Android implementation of the [LocalAppLocale] expect object.
 *
 * Reads system locales and handles subtree configuration injection using Compose
 * Platform components like [LocalConfiguration].
 */
actual object LocalAppLocale {
    private var default: Locale? = null
    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    /**
     * Resolves a subtree locale override on Android.
     *
     * Configures the system resources configuration with the target language code,
     * allowing string resources to reload.
     *
     * @param value The language tag (e.g. `"pl"`, `"en"`) or `null` to use system language.
     * @return Compose [ProvidedValue] containing the updated local environment state.
     */
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = Configuration(LocalConfiguration.current)

        if (default == null) {
            default = Locale.getDefault()
        }

        val new = when(value) {
            null -> default!!
            else -> Locale(value)
        }
        Locale.setDefault(new)
        configuration.setLocale(new)

        val resources = LocalContext.current.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        return LocalConfiguration.provides(configuration)
    }
}
