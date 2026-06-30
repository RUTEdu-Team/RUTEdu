package prz.rutedu.app.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import prz.rutedu.app.Database

/**
 * iOS implementation of [DriverFactory].
 *
 * Uses [NativeSqliteDriver] which stores the database in the app's Documents directory
 * (the standard location for user data on iOS). No context is required because iOS
 * resolves the database path from the app bundle environment automatically.
 */
actual class DriverFactory {
    /**
     * Creates and configures the native SQLite database driver for iOS.
     *
     * The database file is created inside the standard application sandbox Documents directory.
     *
     * @return A thread-safe [SqlDriver] ready to run migrations and queries.
     */
    actual fun createDriver(): SqlDriver {
        val schema = Database.Schema.synchronous()
        return NativeSqliteDriver(
            schema = schema,
            name = "test.db"
        )
    }

    actual fun deleteDatabase(): Boolean {
        return try {
            co.touchlab.sqliter.DatabaseFileContext.deleteDatabase("test.db", null)
            true
        } catch (e: Exception) {
            println("DriverFactory: Failed to delete native database: ${e.message}")
            false
        }
    }
}