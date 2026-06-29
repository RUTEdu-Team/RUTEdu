package prz.rutedu.app.data

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import rutedu.composeapp.generated.resources.Res
import prz.rutedu.app.Database
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.MapQuiz
import prz.rutedu.app.models.Question.PointMapQuiz
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.Hint
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
     * Runs on [Dispatchers.Default] to offload IO/parsing/database work from the main thread.
     *
     * @param driver The platform-specific SQLite driver.
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun seedDatabaseIfNeeded(driver: SqlDriver) = withContext(Dispatchers.Default) {
        val db = Database(driver)

        val dbVersion = try {
            db.databaseQueries.getSetting("questions_db_version").executeAsOneOrNull()
        } catch (_: Exception) {
            null
        }

        if (dbVersion == SEED_VERSION) return@withContext

        db.transaction {
            try {
                db.databaseQueries.clearStoredQuestions()
            } catch (_: Exception) {}

            for (subject in listOf("math", "geo", "chem")) {
                for (lang in SUPPORTED_QUESTION_LANGS) {
                    // Running seedSubject synchronously inside transaction is fine as we are on Dispatchers.Default
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
     * Must be non-suspend so it can be called synchronously in Composition (remember block) to prevent flashes.
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
     * - `geo_4_5` and `geografia_stolice_woj` lessons are localized and built dynamically.
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
            lessonId == "geo_4_5" -> {
                val cached = getGeo45(getCurrentLanguage())
                val shuffled = if (seed != 0L) {
                    cached.shuffled(kotlin.random.Random(seed))
                } else {
                    cached
                }
                if (excludeIds.isEmpty()) shuffled else shuffled.filter { it.id !in excludeIds }
            }
            lessonId == "geografia_stolice_woj" -> {
                val cached = getGeografiaStoliceWoj(getCurrentLanguage())
                val shuffled = if (seed != 0L) {
                    cached.shuffled(kotlin.random.Random(seed))
                } else {
                    cached
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

    private data class ParkTranslation(
        val id: Int,
        val key: String,
        val namePl: String,
        val nameEn: String,
        val hintPl: String,
        val hintEn: String
    )

    private val parksData = listOf(
        ParkTranslation(4501, "Białowieski Park Narodowy", "Białowieski Park Narodowy", "Białowieża National Park",
            "Białowieski PN leży na granicy z Białorusią, chroni ostatni fragment lasu pierwotnego.",
            "Białowieża National Park lies on the border with Belarus and protects the last remaining fragment of primeval forest."),
        ParkTranslation(4502, "Biebrzański Park Narodowy", "Biebrzański Park Narodowy", "Biebrza National Park",
            "To największy polski park narodowy, obejmuje dolinę rzeki Biebrzy.",
            "It is the largest Polish national park, covering the Biebrza River valley."),
        ParkTranslation(4503, "Wigierski Park Narodowy", "Wigierski Park Narodowy", "Wigry National Park",
            "Park chroni jezioro Wigry oraz liczne mniejsze jeziora (suchary).",
            "The park protects Lake Wigry and numerous smaller humic lakes (suchary)."),
        ParkTranslation(4504, "Park Narodowy Ujście Warty", "Park Narodowy Ujście Warty", "Warta Mouth National Park",
            "Park chroni tereny podmokłe u ujścia rzeki Warty do Odry.",
            "The park protects wetlands at the confluence of the Warta and Oder rivers."),
        ParkTranslation(4505, "Karkonoski Park Narodowy", "Karkonoski Park Narodowy", "Karkonosze National Park",
            "Obejmuje najwyższe pasmo Sudetów – Karkonosze ze Śnieżką.",
            "It covers the highest range of the Sudeten Mountains – the Karkonosze, with the Śnieżka peak."),
        ParkTranslation(4506, "Kampinoski Park Narodowy", "Kampinoski Park Narodowy", "Kampinos National Park",
            "Leży tuż obok Warszawy, chroni Puszczę Kampinoską.",
            "It lies right next to Warsaw and protects the Kampinos Forest."),
        ParkTranslation(4507, "Park Narodowy Gór Stołowych", "Park Narodowy Gór Stołowych", "Stołowe Mountains National Park",
            "Słynie z formacji skalnych, takich jak Szczeliniec Wielki.",
            "Famous for its rock formations, such as Szczeliniec Wielki."),
        ParkTranslation(4508, "Gorczański Park Narodowy", "Gorczański Park Narodowy", "Gorce National Park",
            "Chroni pasmo Gorców w Beskidach Zachodnich.",
            "Protects the Gorce mountain range in the Western Beskids."),
        ParkTranslation(4509, "Pieniński Park Narodowy", "Pieniński Park Narodowy", "Pieniny National Park",
            "Słynie z przełomu Dunajca i szczytu Trzy Korony.",
            "Famous for the Dunajec River Gorge and the Trzy Korony peak."),
        ParkTranslation(4510, "Roztoczański Park Narodowy", "Roztoczański Park Narodowy", "Roztocze National Park",
            "Położony na Roztoczu Środkowym, chroni m.in. konika polskiego.",
            "Located in Central Roztocze, it protects, among others, the Polish Konik pony."),
        ParkTranslation(4511, "Tatrzański Park Narodowy", "Tatrzański Park Narodowy", "Tatra National Park",
            "Chroni najwyższe polskie góry – Tatry.",
            "Protects the highest Polish mountains – the Tatras."),
        ParkTranslation(4512, "Słowiński Park Narodowy", "Słowiński Park Narodowy", "Słowiński National Park",
            "Słynie z ruchomych wydm nad Morzem Bałtyckim.",
            "Famous for its shifting sand dunes on the Baltic Sea coast."),
        ParkTranslation(4513, "Park Narodowy Bory Tucholskie", "Park Narodowy Bory Tucholskie", "Tuchola Forest National Park",
            "Chroni one z największych kompleksów borów sosnowych w Polsce.",
            "Protects one of the largest pine forest complexes in Poland."),
        ParkTranslation(4514, "Świętokrzyski Park Narodowy", "Świętokrzyski Park Narodowy", "Świętokrzyski National Park",
            "Chroni najwyższe pasmo Gór Świętokrzyskich z gołoborzami.",
            "Protects the highest range of the Świętokrzyskie Mountains with its scree fields (gołoborza)."),
        ParkTranslation(4515, "Woliński Park Narodowy", "Woliński Park Narodowy", "Wolin National Park",
            "Leży na wyspie Wolin, słynie z klifowego wybrzeża.",
            "Located on the island of Wolin, famous for its cliff coastline."),
        ParkTranslation(4516, "Drawieński Park Narodowy", "Drawieński Park Narodowy", "Drawa National Park",
            "Obejmuje dolinę rzeki Drawy i Puszczę Drawską.",
            "Covers the Drawa River valley and the Drawa Forest."),
        ParkTranslation(4517, "Bieszczadzki Park Narodowy", "Bieszczadzki Park Narodowy", "Bieszczady National Park",
            "Chroni najwyższe partie polskich Bieszczadów z połoninami.",
            "Protects the highest parts of the Polish Bieszczady Mountains with their mountain pastures (połoniny)."),
        ParkTranslation(4518, "Ojcowski Park Narodowy", "Ojcowski Park Narodowy", "Ojców National Park",
            "To najmniejszy polski park narodowy, słynie z Maczugi Herkulesa.",
            "It is the smallest Polish national park, famous for the Hercules' Club rock formation."),
        ParkTranslation(4519, "Magurski Park Narodowy", "Magurski Park Narodowy", "Magura National Park",
            "Położony w Beskidzie Niskim, chroni m.in. krasowe Diable Kamienie.",
            "Located in the Low Beskids, it protects, among others, the Diable Kamienie rock formations."),
        ParkTranslation(4520, "Poleski Park Narodowy", "Poleski Park Narodowy", "Polesie National Park",
            "Obejmuje liczne torfowiska i bagna na Polesiu Lubelskim.",
            "Covers numerous peatlands and swamps in Lublin Polesie."),
        ParkTranslation(4521, "Wielkopolski Park Narodowy", "Wielkopolski Park Narodowy", "Wielkopolska National Park",
            "Chroni krajobraz polodowcowy w pobliżu Poznania.",
            "Protects the post-glacial landscape near Poznań."),
        ParkTranslation(4522, "Babiogórski Park Narodowy", "Babiogórski Park Narodowy", "Babia Góra National Park",
            "Obejmuje masyw Babiej Góry, najwyższego szczytu Beskidów.",
            "Covers the Babia Góra massif, the highest peak in the Beskids."),
        ParkTranslation(4523, "Narwiański Park Narodowy", "Narwiański Park Narodowy", "Narew National Park",
            "Chroni dolinę „polskiej Amazonii” – wielokorytowej rzeki Narwi.",
            "Protects the valley of the 'Polish Amazon' – the anastomosing Narew River.")
    )

    private fun getGeo45(lang: String): List<Question> {
        val isPl = lang == "pl"
        return parksData.map { p ->
            val prompt = if (isPl) "Wskaż ${p.namePl}" else "Indicate ${p.nameEn}"
            val hintText = if (isPl) p.hintPl else p.hintEn
            MapQuiz(p.id, p.key, prompt, MapRegion.POLAND, "files/polish_national_parks.geojson", Hint(hintText))
        }
    }

    private data class CapitalTranslation(
        val id: Int,
        val targets: List<String>,
        val namePl: String,
        val nameEn: String,
        val provincePl: String,
        val provinceEn: String,
        val isPlural: Boolean = false
    )

    private val capitalsData = listOf(
        CapitalTranslation(4601, listOf("Wrocław"), "Wrocław", "Wrocław", "dolnośląskiego", "Lower Silesian"),
        CapitalTranslation(4602, listOf("Bydgoszcz"), "Bydgoszcz", "Bydgoszcz", "kujawsko-pomorskiego", "Kuyavian-Pomeranian"),
        CapitalTranslation(4603, listOf("Toruń"), "Toruń", "Toruń", "kujawsko-pomorskiego", "Kuyavian-Pomeranian"),
        CapitalTranslation(4604, listOf("Lublin"), "Lublin", "Lublin", "lubelskiego", "Lublin"),
        CapitalTranslation(4605, listOf("Gorzów Wielkopolski"), "Gorzów Wielkopolski", "Gorzów Wielkopolski", "lubuskiego", "Lubusz"),
        CapitalTranslation(4606, listOf("Zielona Góra"), "Zielona Góra", "Zielona Góra", "lubuskiego", "Lubusz"),
        CapitalTranslation(4607, listOf("Łódź"), "Łódź", "Łódź", "łódzkiego", "Łódź"),
        CapitalTranslation(4608, listOf("Kraków"), "Kraków", "Kraków", "małopolskiego", "Lesser Poland"),
        CapitalTranslation(4609, listOf("Warszawa"), "Warszawę", "Warsaw", "mazowieckiego (oraz całego kraju)", "Masovian (and the whole country)"),
        CapitalTranslation(4610, listOf("Opole"), "Opole", "Opole", "opolskiego", "Opole"),
        CapitalTranslation(4611, listOf("Rzeszów"), "Rzeszów", "Rzeszów", "podkarpackiego", "Subcarpathian"),
        CapitalTranslation(4612, listOf("Białystok"), "Białystok", "Białystok", "podlaskiego", "Podlaskie"),
        CapitalTranslation(4613, listOf("Gdańsk"), "Gdańsk", "Gdańsk", "pomorskiego", "Pomeranian"),
        CapitalTranslation(4614, listOf("Katowice"), "Katowice", "Katowice", "śląskiego", "Silesian", isPlural = true),
        CapitalTranslation(4615, listOf("Kielce"), "Kielce", "Kielce", "świętokrzyskiego", "Świętokrzyskie", isPlural = true),
        CapitalTranslation(4616, listOf("Olsztyn"), "Olsztyn", "Olsztyn", "warmińsko-mazurskiego", "Warmian-Masurian"),
        CapitalTranslation(4617, listOf("Poznań"), "Poznań", "Poznań", "wielkopolskiego", "Greater Poland"),
        CapitalTranslation(4618, listOf("Szczecin"), "Szczecin", "Szczecin", "zachodniopomorskiego", "West Pomeranian")
    )

    private fun getGeografiaStoliceWoj(lang: String): List<Question> {
        val isPl = lang == "pl"
        return capitalsData.map { c ->
            val prompt = if (isPl) "Zaznacz na mapie ${c.namePl}" else "Mark ${c.nameEn} on the map"
            
            val hintText = if (isPl) {
                val verb = if (c.isPlural) "są stolicą" else "jest stolicą"
                val prefix = if (c.id in listOf(4602, 4603, 4605, 4606)) "jedną z dwóch stolic" else "stolicą"
                "${c.namePl} ${verb} ${prefix} województwa ${c.provincePl}."
            } else {
                val verb = if (c.isPlural) "are the capital" else "is the capital"
                val prefix = if (c.id in listOf(4602, 4603, 4605, 4606)) "one of the two capitals" else "the capital"
                "${c.nameEn} ${verb} of the ${c.provinceEn} Voivodeship."
            }
            
            PointMapQuiz(c.id, c.targets, prompt, MapRegion.POLAND, "files/polish_provinces_and_capitals.geojson", Hint(hintText))
        }
    }
}
