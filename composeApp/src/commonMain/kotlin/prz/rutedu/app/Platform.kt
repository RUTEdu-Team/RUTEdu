package prz.rutedu.app

/**
 * Abstraction over the target platform's identity.
 *
 * Currently used for debug/diagnostic purposes only. Each platform's `actual`
 * implementation (in `androidMain` / `iosMain`) returns a human-readable name
 * such as `"Android 16"` or `"iOS 26.5"`.
 */
interface Platform {
    /** Human-readable platform name and version string. */
    val name: String
}

/**
 * Returns the [Platform] instance for the currently running platform.
 *
 * This is an `expect` function - each target provides its own `actual` implementation:
 * - **Android:** `Platform.android.kt` -> `AndroidPlatform`
 * - **iOS:** `Platform.ios.kt` -> `IOSPlatform`
 */
expect fun getPlatform(): Platform

/**
 * Writes [text] to the file at [path], creating or overwriting it.
 * No-op on platforms without arbitrary local file access (e.g. iOS sandbox).
 */
expect fun writeTextToFile(path: String, text: String)

/**
 * Reads the contents of the file at [path] as a UTF-8 string.
 * Returns an empty string if the file does not exist or cannot be read.
 */
expect fun readTextFromFile(path: String): String

/**
 * Terminates the application process immediately.
 *
 * Shown on the [DatabaseErrorScreen] as a safe alternative to resetting progress
 * when the user wants to keep their data and wait for a fix.
 */
expect fun exitApp()

/**
 * Returns the two-letter ISO 639-1 language code of the device system locale (e.g. `"pl"`, `"en"`).
 * Used as a fallback when no custom app locale has been stored in the database yet.
 */
expect fun getSystemLanguage(): String
