package prz.rutedu.app.data

import app.cash.sqldelight.db.SqlDriver
import kotlin.random.Random

/**
 * Manages per-session state for chemistry lessons that use procedural question generation.
 *
 * Chemistry lessons (IDs starting with `"chemia_"`) generate their questions dynamically
 * from a random seed rather than using a fixed list. This store persists two pieces of
 * state in the `appSettings` key-value table:
 *
 * 1. **Session seed** (`chseed_<lessonId>`) - a `Long` used to initialise the random
 *    number generator in [ChemistryQuestionGenerator]. The same seed always produces the
 *    same shuffled question order, so the session can be resumed consistently across
 *    app restarts.
 *
 * 2. **Answered IDs** (`chansw_<lessonId>`) - a comma-separated list of question [Int]
 *    IDs that have already been answered correctly. On the next session, answered questions
 *    are excluded from the generated list so the student always sees fresh material.
 *
 * Both keys are created automatically the first time a chemistry lesson is opened.
 */
object ChemistrySessionStore {

    private fun seedKey(lessonId: String) = "chseed_$lessonId"
    private fun answeredKey(lessonId: String) = "chansw_$lessonId"

    /**
     * Returns the existing session seed for [lessonId], or creates and persists a new
     * random seed if none exists yet.
     *
     * The seed is stable for the lifetime of one "session" (the student keeps answering
     * from the same shuffled list until all questions are answered, at which point the
     * answered set should be cleared to start a fresh session).
     *
     * @param driver    Platform SQLite driver.
     * @param lessonId  Chemistry lesson identifier (e.g. `"chemia_1_1"`).
     * @return A `Long` seed value.
     */
    fun getOrCreateSeed(driver: SqlDriver, lessonId: String): Long {
        val existing = loadString(driver, seedKey(lessonId))
        if (existing != null) return existing.toLongOrNull() ?: createSeed(driver, lessonId)
        return createSeed(driver, lessonId)
    }

    private fun createSeed(driver: SqlDriver, lessonId: String): Long {
        val seed = Random.nextLong()
        saveString(driver, seedKey(lessonId), seed.toString())
        return seed
    }

    /**
     * Returns the set of question IDs that have already been answered in previous sessions
     * for [lessonId]. The [ChemistryQuestionGenerator] filters these out so they are not
     * shown again until the student resets or finishes the full pool.
     *
     * @param driver   Platform SQLite driver.
     * @param lessonId Chemistry lesson identifier.
     * @return Set of answered question IDs, or an empty set if none have been recorded.
     */
    fun getAnsweredIds(driver: SqlDriver, lessonId: String): Set<Int> {
        val raw = loadString(driver, answeredKey(lessonId)) ?: return emptySet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    /**
     * Records [questionId] as answered for [lessonId], adding it to the persisted set.
     *
     * Called by `LessonGameScreen` each time the student advances past a correct answer
     * in a chemistry lesson.
     *
     * @param driver     Platform SQLite driver.
     * @param lessonId   Chemistry lesson identifier.
     * @param questionId The [prz.rutedu.app.models.Question.id] of the answered question.
     */
    fun markAnswered(driver: SqlDriver, lessonId: String, questionId: Int) {
        val current = getAnsweredIds(driver, lessonId)
        val updated = (current + questionId).joinToString(",")
        saveString(driver, answeredKey(lessonId), updated)
    }

    private fun loadString(driver: SqlDriver, key: String): String? =
        driver.executeQuery(
            identifier = null,
            sql = "SELECT setting_value FROM appSettings WHERE setting_key = ?",
            mapper = { cursor ->
                app.cash.sqldelight.db.QueryResult.Value(
                    if (cursor.next().value) cursor.getString(0) else null
                )
            },
            parameters = 1,
            binders = { bindString(0, key) }
        ).value

    private fun saveString(driver: SqlDriver, key: String, value: String) {
        driver.execute(
            identifier = null,
            sql = "INSERT OR REPLACE INTO appSettings (setting_key, setting_value) VALUES (?, ?)",
            parameters = 2,
            binders = {
                bindString(0, key)
                bindString(1, value)
            }
        )
    }
}
