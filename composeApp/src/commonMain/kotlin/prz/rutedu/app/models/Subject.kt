package prz.rutedu.app.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level educational subject shown on the home screen (e.g. "Matematyka", "Chemia").
 *
 * A [Subject] owns an ordered list of [Topic]s. Topics are displayed as cards on the
 * subject-detail screen. Progress tracking is stored separately at runtime via
 * [prz.rutedu.app.data.LessonProgressStore]; the [progress] field here is a
 * static default used until real progress data is loaded.
 *
 * **How to add a new subject:**
 * Open `SubjectRepository.kt` and append a `Subject(...)` entry to the `subjects` list.
 * Then define at least one [Topic] with at least one [Lesson] inside it, and register
 * the lesson's questions in `QuestionBank.kt` or `ChemistryQuestionGenerator.kt`.
 *
 * @property id              Unique string identifier used for navigation routes
 *                           (e.g. `"matematyka"`, `"chemia"`, `"geografia"`).
 * @property name            Display name shown on subject cards (Polish).
 * @property lessonCount     Total number of lessons across all topics - shown as metadata
 *                           on the subject card. Keep this in sync with the actual number
 *                           of [Lesson] entries in [topics].
 * @property progress        Overall completion fraction in the range `0f..1f`.
 *                           This is a static placeholder; actual progress is computed
 *                           from [prz.rutedu.app.data.LessonProgressStore] at runtime.
 * @property color           Primary accent colour for this subject (progress bars, icons).
 * @property backgroundColor Lighter tint used as the card background. Should be derived
 *                           from [color] with high lightness.
 * @property icon            Icon displayed inside the subject card. Use `Icons.Default.*`
 *                           from the `materialIconsExtended` dependency.
 * @property topics          Ordered list of [Topic]s that belong to this subject.
 */
data class Subject(
    val id: String,
    val name: String,
    val lessonCount: Int,
    val progress: Float,
    val color: Color,
    val backgroundColor: Color,
    val icon: ImageVector,
    val topics: List<Topic> = emptyList()
)
