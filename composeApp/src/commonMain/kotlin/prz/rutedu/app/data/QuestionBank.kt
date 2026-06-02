package prz.rutedu.app.data

import app.cash.sqldelight.db.SqlDriver
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import rutedu.composeapp.generated.resources.Res
import prz.rutedu.app.Database
import prz.rutedu.app.models.Question
import prz.rutedu.app.locale.getCurrentLanguage
import prz.rutedu.app.math.mathEngineAvailable

/**
 * Central registry of all quiz questions.
 *
 * Questions are persisted in the SQLite database partitioned by subject and loaded into
 * the in-memory [cachedQuestions] map on demand. The database is seeded on first launch
 * from bundled JSON assets organised as:
 *
 * ```
 * files/math/questions_<lang>.json   – mat_* and algebra_* lessons
 * files/geo/questions_<lang>.json    – geo_* lessons
 * files/chem/questions_<lang>.json   – chemia_3_1 and chemia_3_2
 * ```
 *
 * where `<lang>` is one of the codes in [SUPPORTED_QUESTION_LANGS].
 *
 * ## Adding a new question language
 *
 * 1. Create the three asset files (`files/math/questions_<code>.json`, etc.) with translated content.
 * 2. Add the language code to [SUPPORTED_QUESTION_LANGS].
 * 3. Bump [SEED_VERSION] to force re-seeding on next launch.
 *
 * A lightweight versioning mechanism (`questions_db_version` in `appSettings`) ensures that
 * the database is automatically re-seeded whenever the bundled JSON content or [SEED_VERSION] changes.
 *
 * Chemistry lessons that are **not** in [staticLessons] (i.e. anything other than
 * `chemia_3_1` and `chemia_3_2`) are generated dynamically by [ChemistryQuestionGenerator].
 */
object QuestionBank {

    /**
     * Two-letter ISO 639-1 language codes for which question translations are bundled as JSON assets.
     *
     * Adding a new language requires:
     * 1. Providing the three per-subject JSON files (`files/math/questions_<code>.json`, etc.).
     * 2. Adding the code here.
     * 3. Bumping [SEED_VERSION] so the database is re-seeded with the new data.
     */
    val SUPPORTED_QUESTION_LANGS = listOf(
        "pl", "en", "cs", "de", "el", "es", "fr", "hu", "it", "nl",
        "pt", "sk", "uk", "bg", "hr", "sr", "sv", "da", "no", "is",
        "fi", "et", "lv", "lt", "ro", "sl", "ga", "mt"
    )

    /**
     * Increment whenever the bundled JSON question assets change content or new languages are added.
     * A version mismatch causes the stored questions to be fully cleared and re-seeded on next launch.
     */
    private const val SEED_VERSION = "4"

    /**
     * Lesson identifiers whose questions are stored in the database.
     *
     * All other lesson IDs are routed to the appropriate generator:
     * - `chemia_*` → [ChemistryQuestionGenerator]
     * - `mat_*` (outside this list) → [MathQuestionGenerator]
     * - `algebra_*` (outside this list) → [AlgebraQuestionGenerator]
     */
    val staticLessons = listOf(
        "mat_1_1", "mat_1_2", "mat_1_3", "mat_2_1", "mat_2_2",
        "mat_3_1", "mat_4_1", "mat_5_1",
        "geo_1_1", "geo_4_1", "geo_4_2", "geo_4_3", "geo_4_4",
        "chemia_3_1", "chemia_3_2",
        "algebra_1_1", "algebra_1_2", "algebra_1_3", "algebra_2_1", "algebra_2_2"
    )

    /**
     * Maps each lesson in [staticLessons] to its subject bucket.
     *
     * The bucket name matches the sub-directory of the bundled JSON assets and the
     * `subject` column in the `storedQuestion` table.
     */
    private fun subjectForLesson(lessonId: String): String = when {
        lessonId.startsWith("mat_") || lessonId.startsWith("algebra_") -> "math"
        lessonId.startsWith("geo_") -> "geo"
        lessonId.startsWith("chemia_") -> "chem"
        else -> "unknown"
    }

    private var cachedQuestions: Map<String, List<Question>> = emptyMap()

    /**
     * Seeds the question database if the bundled JSON content has changed since the last seed.
     *
     * The seeding version is stored in `appSettings` under the key `questions_db_version`.
     * When the stored value differs from [SEED_VERSION] the database is cleared and
     * re-populated from the per-subject asset files for every code in [SUPPORTED_QUESTION_LANGS].
     *
     * This method is safe to call on every app launch; it is a no-op when already up-to-date.
     *
     * @param driver The platform-specific SQLite driver.
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun seedDatabaseIfNeeded(driver: SqlDriver) {
        val db = Database(driver)

        val dbVersion = try {
            db.databaseQueries.getSetting("questions_db_version").executeAsOneOrNull()
        } catch (_: Exception) {
            null
        }

        if (dbVersion == SEED_VERSION) return

        db.transaction {
            try {
                db.databaseQueries.clearStoredQuestions()
            } catch (_: Exception) {}

            for (subject in listOf("math", "geo", "chem")) {
                for (lang in SUPPORTED_QUESTION_LANGS) {
                    seedSubject(db, subject, lang)
                }
            }

            db.databaseQueries.upsertSetting("questions_db_version", SEED_VERSION)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun seedSubject(db: Database, subject: String, lang: String) {
        try {
            val bytes = Res.readBytes("files/$subject/questions_$lang.json")
            val jsonText = bytes.decodeToString()
            val format = Json { ignoreUnknownKeys = true }
            val lessonsList = format.decodeFromString<List<LessonQuestionsDto>>(jsonText)

            for (lessonQuestions in lessonsList) {
                for (qDto in lessonQuestions.questions) {
                    val qJson = format.encodeToString(QuestionDto.serializer(), qDto)
                    db.databaseQueries.upsertQuestion(
                        lesson_id = lessonQuestions.lessonId,
                        question_id = qDto.id.toLong(),
                        language = lang,
                        subject = subject,
                        type = qDto::class.simpleName ?: "Unknown",
                        data_json = qJson
                    )
                }
            }
        } catch (e: Exception) {
            // Do not print stack trace for missing translation files
        }
    }

    /**
     * Loads questions for all lessons in [staticLessons] from the database into the
     * in-memory cache for the given [languageCode].
     *
     * Falls back to `"en"` when [languageCode] is not present in [SUPPORTED_QUESTION_LANGS]. Call this:
     * - Once synchronously during the first composition (to avoid UI flashes).
     * - Again inside `LaunchedEffect` whenever [prz.rutedu.app.locale.customAppLocale] changes.
     *
     * @param driver       Platform-specific SQLite driver.
     * @param languageCode Two-letter ISO 639-1 code; must be in [SUPPORTED_QUESTION_LANGS].
     */
    fun loadQuestions(driver: SqlDriver, languageCode: String) {
        val db = Database(driver)
        val lang = if (languageCode in SUPPORTED_QUESTION_LANGS) languageCode else "en"

        val newCache = mutableMapOf<String, List<Question>>()
        val format = Json { ignoreUnknownKeys = true }

        for (lessonId in staticLessons) {
            var dbRows = db.databaseQueries.getQuestionsForLesson(lessonId, lang).executeAsList()
            if (dbRows.isEmpty() && lang != "en") {
                dbRows = db.databaseQueries.getQuestionsForLesson(lessonId, "en").executeAsList()
            }
            val models = dbRows.mapNotNull { row ->
                try {
                    format.decodeFromString<QuestionDto>(row.data_json).toModel()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            newCache[lessonId] = models
        }
        cachedQuestions = newCache
    }

    /**
     * Returns the ordered list of questions for [lessonId].
     *
     * Routing:
     * - Lessons in [staticLessons] are served from [cachedQuestions] (populated by [loadQuestions]).
     *   `algebra_*` lessons additionally filter by platform capability ([mathEngineAvailable]).
     * - `chemia_*` lessons not in [staticLessons] are delegated to [ChemistryQuestionGenerator].
     * - All other IDs are delegated to [MathQuestionGenerator] or [AlgebraQuestionGenerator].
     *
     * @param lessonId   Lesson identifier (e.g. `"mat_1_1"`, `"geo_4_1"`, `"chemia_3_2"`).
     * @param seed       Random seed for shuffling / procedural generation.
     * @param excludeIds Question IDs to omit (already-answered questions in a session).
     * @return Ordered list of questions to present, or an empty list when unrecognised.
     */
    fun questionsFor(lessonId: String, seed: Long = 0L, excludeIds: Set<Int> = emptySet()): List<Question> {
        val rawQuestions = when {
            lessonId in staticLessons -> {
                val cached = cachedQuestions[lessonId] ?: emptyList()
                val platformFiltered = if (lessonId.startsWith("algebra_")) {
                    if (mathEngineAvailable) {
                        cached.filterIsInstance<Question.ExpressionTypeAnswer>()
                    } else {
                        cached.filterIsInstance<Question.SelectFromList>()
                    }
                } else {
                    cached
                }
                val shuffled = if (seed != 0L) {
                    platformFiltered.shuffled(kotlin.random.Random(seed))
                } else {
                    platformFiltered
                }
                if (excludeIds.isEmpty()) shuffled else shuffled.filter { it.id !in excludeIds }
            }
            lessonId.startsWith("chemia_") ->
                ChemistryQuestionGenerator.generateFor(lessonId, seed, excludeIds)
            lessonId.startsWith("algebra_") ->
                AlgebraQuestionGenerator.generateFor(lessonId, seed, excludeIds)
            lessonId.startsWith("mat_") ->
                MathQuestionGenerator.questionsFor(lessonId, seed, excludeIds)
            else ->
                emptyList()
        }

        return rawQuestions
    }
}
