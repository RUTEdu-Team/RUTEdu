package prz.rutedu.app.database

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import prz.rutedu.app.Database

/**
 * Android implementation of [DriverFactory].
 *
 * Uses [AndroidSqliteDriver] which wraps the platform SQLite database and stores
 * the database file in the app's private data directory (managed by Android).
 * The explicit [AndroidSqliteDriver.Callback] ensures the schema is created and
 * migrations are run when the database is first opened or upgraded.
 *
 * @param context Application context required by [AndroidSqliteDriver] to locate
 *                the database file.
 */
actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val schema = Database.Schema.synchronous()
        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = "test.db",
            callback = AndroidSqliteDriver.Callback(
                schema = schema
            )
        )
    }
}