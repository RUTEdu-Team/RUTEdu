package prz.rutedu.app.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Idempotent database initialisation that guarantees all required tables exist.
 *
 * SQLDelight generates migration code for schema changes, but some edge cases -
 * such as a fresh install on a device that previously had a different schema version,
 * or a corrupted migration run - can leave the database in a partial state. This
 * function creates all tables with `CREATE TABLE IF NOT EXISTS` so it is safe to
 * call on every app launch without risk of data loss or duplicate errors.
 *
 * **Call site:** `MainActivity.onCreate` (Android) / `MainViewController` (iOS), before
 * passing the driver to `App()`.
 *
 * @param driver The platform-specific SQLite driver obtained from [DriverFactory].
 */
fun ensureTablesExist(driver: SqlDriver) {
    // Key-value store for settings and all progress/config data encoded by the app's
    // data layer (LessonProgressStore, ChemistrySessionStore, SubjectConfigStore, ...)
    driver.execute(
        identifier = null,
        sql = """
            CREATE TABLE IF NOT EXISTS appSettings (
                setting_key TEXT PRIMARY KEY NOT NULL,
                setting_value TEXT NOT NULL
            )
        """.trimIndent(),
        parameters = 0
    )

    // Default theme mode setting - inserted only when missing, never overwritten
    driver.execute(
        identifier = null,
        sql = """
            INSERT OR IGNORE INTO appSettings (setting_key, setting_value)
            VALUES ('theme_mode', 'system')
        """.trimIndent(),
        parameters = 0
    )

    // Player profiles used by the arcade/PvP game modes
    driver.execute(
        identifier = null,
        sql = """
            CREATE TABLE IF NOT EXISTS player (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nickname TEXT UNIQUE NOT NULL,
                high_score INTEGER NOT NULL DEFAULT 0,
                games_played INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
            )
        """.trimIndent(),
        parameters = 0
    )
}
