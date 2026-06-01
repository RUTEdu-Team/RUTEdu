package prz.rutedu.app.data

import app.cash.sqldelight.db.SqlDriver
import prz.rutedu.app.data.LessonProgressStore.load

/**
 * Persists and retrieves the student's progress through a lesson across app sessions.
 *
 * Progress is stored in the `appSettings` key-value table (SQLite) using the pattern:
 * - **Key:** `lp_<lessonId>` (e.g. `"lp_mat_1_1"`)
 * - **Value:** `"<currentIndex>/<correctCount>/<totalCount>"` (e.g. `"3/2/12"`)
 *
 * This store is used by `LessonGameScreen` to resume a lesson where the student left off.
 * On screen disposal (navigation away), the current state is automatically saved.
 *
 * @see [LessonProgressStore.load]
 * @see [LessonProgressStore.save]
 */
object LessonProgressStore {

    /**
     * Snapshot of a student's progress through one lesson.
     *
     * @property currentIndex  Zero-based index of the next question to present.
     *                         When equal to [totalCount] the lesson is complete.
     * @property correctCount  Number of questions answered correctly in this session.
     * @property totalCount    Total number of questions in the configured question set.
     */
    data class Progress(
        val currentIndex: Int,
        val correctCount: Int,
        val totalCount: Int
    ) {
        /**
         * Completion fraction computed as `currentIndex / totalCount`, clamped to `0f..1f`.
         * Returns `0f` when [totalCount] is zero to avoid division by zero.
         */
        val fraction: Float
            get() = if (totalCount == 0) 0f else (currentIndex.toFloat() / totalCount).coerceIn(0f, 1f)
    }

    private fun key(lessonId: String) = "lp_$lessonId"

    /**
     * Loads the saved [Progress] for [lessonId] from the database.
     *
     * @param driver   The SQLite driver for the current platform.
     * @param lessonId The lesson identifier (e.g. `"mat_1_1"`).
     * @return The persisted [Progress], or `null` if no progress has been saved yet
     *         or if the stored value cannot be parsed.
     */
    fun load(driver: SqlDriver, lessonId: String): Progress? {
        val raw = driver.executeQuery(
            identifier = null,
            sql = "SELECT setting_value FROM appSettings WHERE setting_key = ?",
            mapper = { cursor ->
                app.cash.sqldelight.db.QueryResult.Value(
                    if (cursor.next().value) cursor.getString(0) else null
                )
            },
            parameters = 1,
            binders = { bindString(0, key(lessonId)) }
        ).value ?: return null

        val parts = raw.split("/")
        if (parts.size != 3) return null
        return runCatching {
            Progress(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull()
    }

    /**
     * Persists [progress] for [lessonId], overwriting any previously stored value.
     *
     * @param driver    The SQLite driver for the current platform.
     * @param lessonId  The lesson identifier.
     * @param progress  The progress snapshot to store.
     */
    fun save(driver: SqlDriver, lessonId: String, progress: Progress) {
        val value = "${progress.currentIndex}/${progress.correctCount}/${progress.totalCount}"
        driver.execute(
            identifier = null,
            sql = "INSERT OR REPLACE INTO appSettings (setting_key, setting_value) VALUES (?, ?)",
            parameters = 2,
            binders = {
                bindString(0, key(lessonId))
                bindString(1, value)
            }
        )
    }

    /**
     * Returns the saved completion fraction (`0f..1f`) for [lessonId], or `0f` if the
     * lesson has not been started.
     *
     * Convenience wrapper around [load] for use when only the fraction is needed
     * (e.g. drawing progress bars on lesson cards).
     *
     * @param driver   The SQLite driver for the current platform.
     * @param lessonId The lesson identifier.
     */
    fun lessonFraction(driver: SqlDriver, lessonId: String): Float =
        load(driver, lessonId)?.fraction ?: 0f

    /**
     * Returns the average completion fraction across all lessons in a topic.
     *
     * Used by `TopicDetailScreen` to show an aggregate progress bar for a topic.
     *
     * @param driver     The SQLite driver for the current platform.
     * @param lessonIds  All lesson identifiers belonging to the topic.
     * @return Average fraction in `0f..1f`, or `0f` if [lessonIds] is empty.
     */
    fun topicFraction(driver: SqlDriver, lessonIds: List<String>): Float =
        if (lessonIds.isEmpty()) 0f
        else lessonIds.map { lessonFraction(driver, it) }.average().toFloat()
}
