package prz.rutedu.app

import platform.UIKit.UIDevice

/** iOS implementation of [Platform]. Reports `"<systemName> <systemVersion>"` as the platform name. */
class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

/**
 * Instantiates the iOS platform info provider.
 *
 * @return An instance of [IOSPlatform] reporting iOS details.
 */
actual fun getPlatform(): Platform = IOSPlatform()

actual fun writeTextToFile(path: String, text: String) {
    // No-op on iOS — app sandbox prevents arbitrary file writes outside designated paths
}

actual fun readTextFromFile(path: String): String = ""

actual fun exitApp() {
    platform.posix.exit(0)
}

actual fun getSystemLanguage(): String {
    val preferred = (platform.Foundation.NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String) ?: "en"
    return preferred.take(2).lowercase()
}