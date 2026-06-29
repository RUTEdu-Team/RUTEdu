package prz.rutedu.app

import android.os.Build
import kotlin.system.exitProcess

/** Android implementation of [Platform]. Reports `"Android <SDK_INT>"` as the platform name. */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/**
 * Instantiates the Android platform info provider.
 *
 * @return An instance of [AndroidPlatform] reporting the current Android SDK version.
 */
actual fun getPlatform(): Platform = AndroidPlatform()

actual fun writeTextToFile(path: String, text: String) {
    try {
        val file = java.io.File(path)
        val parent = file.parentFile
        if (parent != null && parent.exists()) {
            file.writeText(text, charset = Charsets.UTF_8)
        }
    } catch (_: Exception) {}
}

actual fun readTextFromFile(path: String): String {
    return try {
        val file = java.io.File(path)
        if (file.exists()) file.readText(charset = Charsets.UTF_8) else ""
    } catch (_: Exception) {
        ""
    }
}

actual fun exitApp() {
    exitProcess(0)
}

actual fun getSystemLanguage(): String {
    return java.util.Locale.getDefault().language
}