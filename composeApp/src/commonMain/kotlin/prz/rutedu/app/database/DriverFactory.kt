package prz.rutedu.app.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific factory for creating the SQLite driver used by [prz.rutedu.app.Database].
 *
 * This is an `expect` class - each target platform provides its own `actual` implementation:
 * - **Android:** `DriverFactory.android.kt` - uses `AndroidSqliteDriver` backed by the system SQLite.
 * - **iOS:** `DriverFactory.ios.kt` - uses `NativeSqliteDriver` backed by SQLite.framework.
 *
 * Both implementations convert the async SQLDelight schema to synchronous (`.synchronous()`)
 * because the app accesses the database on the main thread inside composables.
 *
 * **Android usage (MainActivity.kt):**
 * ```
 * val driver = DriverFactory(applicationContext).createDriver()
 * ```
 *
 * **iOS usage (MainViewController.kt):**
 * ```
 * val driver = DriverFactory().createDriver()
 * ```
 */
expect class DriverFactory {
    /**
     * Creates and returns a platform-specific [SqlDriver] connected to the app database.
     * The schema is applied (or migrated) automatically on first open.
     */
    fun createDriver(): SqlDriver

    /**
     * Deletes the underlying database file from the device storage.
     *
     * Called by [prz.rutedu.app.DatabaseErrorScreen] when the user opts to reset progress after
     * an unrecoverable database error. Returns `true` on success, `false` if the delete failed.
     */
    fun deleteDatabase(): Boolean
}
