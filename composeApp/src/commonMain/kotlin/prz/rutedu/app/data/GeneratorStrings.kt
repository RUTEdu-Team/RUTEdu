package prz.rutedu.app.data

import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import rutedu.composeapp.generated.resources.Res

/**
 * Lazy-loaded localisation tables for text used inside procedurally-generated questions.
 *
 * Static quiz questions (lessons stored in the SQLite database via [QuestionBank]) already
 * carry their localised text inside the bundled JSON assets.  Dynamic generators
 * ([AlgebraQuestionGenerator], [ChemistryQuestionGenerator]) build [prz.rutedu.app.models.Question]
 * objects at runtime and therefore cannot use Compose resource strings - they need their own
 * language-keyed lookup tables.
 *
 * ## File layout
 *
 * ```
 * files/algebra/strings_<lang>.json  – texts for [AlgebraQuestionGenerator]
 * files/chem/strings_<lang>.json     – texts for [ChemistryQuestionGenerator]
 * files/math/strings_<lang>.json     – texts for [MathQuestionGenerator]
 * ```
 *
 * Both files are flat `Map<String, String>` JSON objects.  When a key is absent from the
 * language-specific file the English value is used; when the language file itself is missing
 * the whole map falls back to English silently.
 *
 * ## Adding a new language
 *
 * 1. Create `files/algebra/strings_<code>.json` and `files/chem/strings_<code>.json`.
 * 2. Call [load] with the new language code - no other changes required.
 *
 * The files are automatically included in the app binary as Compose multiplatform resources.
 */
object GeneratorStrings {

    private var algebraMap: Map<String, String> = emptyMap()
    private var chemMap:    Map<String, String> = emptyMap()
    private var mathMap:    Map<String, String> = emptyMap()
    private var algebraEn:  Map<String, String> = emptyMap()
    private var chemEn:     Map<String, String> = emptyMap()
    private var mathEn:     Map<String, String> = emptyMap()

    private var loadedLang: String = ""

    /**
     * Loads algebra, chemistry, and math string tables for [lang].
     *
     * English values serve as the base; language-specific entries override them, so any
     * key missing from `strings_<lang>.json` automatically falls back to English.
     *
     * This method is idempotent — calling it twice with the same [lang] is a no-op.
     * Must be called from a coroutine (e.g. a [androidx.compose.runtime.LaunchedEffect])
     * before any generator function is invoked.
     *
     * @param lang Two-letter ISO 639-1 code (e.g. `"pl"`, `"de"`).
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(lang: String) {
        if (lang == loadedLang) return
        val format = Json { ignoreUnknownKeys = true }
        if (algebraEn.isEmpty()) algebraEn = readFile(format, "algebra", "en")
        if (chemEn.isEmpty())    chemEn    = readFile(format, "chem",    "en")
        if (mathEn.isEmpty())    mathEn    = readFile(format, "math",    "en")
        algebraMap = if (lang == "en") algebraEn else algebraEn + readFile(format, "algebra", lang)
        chemMap    = if (lang == "en") chemEn    else chemEn    + readFile(format, "chem",    lang)
        mathMap    = if (lang == "en") mathEn    else mathEn    + readFile(format, "math",    lang)
        loadedLang = lang
    }

    /** Returns the algebra generator string for [key]; falls back to the raw key when not found. */
    fun algebra(key: String): String = algebraMap[key] ?: key

    /** Returns the chemistry generator string for [key]; falls back to the raw key when not found. */
    fun chem(key: String): String = chemMap[key] ?: key

    /** Returns the math generator string for [key]; falls back to the raw key when not found. */
    fun math(key: String): String = mathMap[key] ?: key

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(format: Json, subject: String, lang: String): Map<String, String> =
        try {
            format.decodeFromString<Map<String, String>>(
                Res.readBytes("files/$subject/strings_$lang.json").decodeToString()
            )
        } catch (_: Exception) {
            emptyMap()
        }
}
