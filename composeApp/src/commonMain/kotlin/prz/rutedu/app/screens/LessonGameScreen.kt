package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import prz.rutedu.app.data.ChemistrySessionStore
import prz.rutedu.app.data.LessonProgressStore
import prz.rutedu.app.data.QuestionBank
import prz.rutedu.app.data.SubjectConfigStore
import prz.rutedu.app.data.SubjectRepository
import prz.rutedu.app.models.Question
import kotlin.math.roundToInt

/**
 * The core quiz engine that drives all lesson gameplay.
 *
 * ## Startup sequence
 * 1. Looks up the `Subject`, `Topic`, and `Lesson` objects from [SubjectRepository].
 * 2. Determines whether this is a chemistry lesson (`lessonId.startsWith("chemia_")`):
 *    - **Chemistry:** creates or restores a session seed via [ChemistrySessionStore], then loads
 *      a procedurally generated question list excluding already-answered IDs.
 *    - **Static:** loads the question list from [QuestionBank].
 * 3. Trims the question list to the user-configured count from [SubjectConfigStore]
 *    (defaults to the full list if no config is saved).
 * 4. Restores the saved progress index and correct-answer count from [LessonProgressStore].
 *    For chemistry lessons, the index always starts at 0 (the unanswered set may have shrunk).
 *
 * ## Progress saving
 * Progress is saved eagerly on every question advance and also on disposal (`DisposableEffect`)
 * so that navigating away mid-lesson (back button, tab switch) never loses progress.
 *
 * ## Question dispatch
 * A `when` expression on the sealed [Question] type routes each question to its dedicated
 * content composable (e.g. [FindAnswerContent], [MapQuizContent], [EquationBalanceContent]).
 * Adding a new question type requires:
 * 1. A new subtype in [Question].
 * 2. A new content composable.
 * 3. A new `is Question.NewType ->` branch in the `when` block below.
 *
 * ## Answer feedback
 * Content composables call `onAnsweredCorrectly` / `onWrongAnswer` callbacks which set
 * [FeedbackState]. The [AnswerFeedbackOverlay] reads this state and auto-dismisses after a
 * fixed delay (950 ms correct, 750 ms wrong). On correct dismissal, `advanceQuestion` fires.
 *
 * @param subjectId    ID of the parent subject (e.g. `"chemia"`).
 * @param topicId      ID of the parent topic (e.g. `"kwasy"`).
 * @param lessonId     ID of the lesson being played. Drives question-set routing and storage keys.
 * @param navController Used to pop back to the topic detail screen on back-press or lesson completion.
 * @param driver        SQLite driver for progress, session, and config stores.
 * @param bottomPadding System navigation bar height padding passed from `App`.
 */
@Composable
fun LessonGameScreen(
    subjectId: String,
    topicId: String,
    lessonId: String,
    navController: NavController,
    driver: SqlDriver,
    bottomPadding: Dp = 0.dp
) {
    val subject = SubjectRepository.getById(subjectId)
    val topic = SubjectRepository.getTopicById(subjectId, topicId)
    val lesson = topic?.lessons?.find { it.id == lessonId }
    val isChemistry = lessonId.startsWith("chemia_")
    val chemSeed = remember {
        if (isChemistry) ChemistrySessionStore.getOrCreateSeed(driver, lessonId) else 0L
    }
    val chemAnswered = remember {
        if (isChemistry) ChemistrySessionStore.getAnsweredIds(driver, lessonId) else emptySet()
    }
    val allQuestions = remember { QuestionBank.questionsFor(lessonId, chemSeed, chemAnswered) }
    val configuredCount = remember {
        if (allQuestions.isEmpty()) 0
        else SubjectConfigStore.load(driver, lessonId)?.questionCount?.coerceIn(1, allQuestions.size)
            ?: allQuestions.size
    }
    val questions = remember(configuredCount) { allQuestions.take(configuredCount) }
    val totalCount = questions.size

    val savedProgress = remember { LessonProgressStore.load(driver, lessonId) }

    var currentIndex by remember {
        mutableStateOf(
            when {
                isChemistry -> 0  // always start from filtered list beginning
                savedProgress == null -> 0
                savedProgress.currentIndex >= totalCount -> totalCount  // complete – keep at end, not 0
                savedProgress.currentIndex >= 0 -> savedProgress.currentIndex
                else -> 0
            }
        )
    }
    var correctCount by remember { mutableStateOf(if (isChemistry) 0 else savedProgress?.correctCount ?: 0) }

    var isComplete by remember {
        mutableStateOf(!isChemistry && savedProgress != null && savedProgress.currentIndex >= totalCount && totalCount > 0)
    }

    // rememberUpdatedState captures the *latest* values without restarting DisposableEffect.
    // Without this, onDispose would close over the stale initial values of currentIndex/correctCount.
    val latestIndex = rememberUpdatedState(currentIndex)
    val latestCorrect = rememberUpdatedState(correctCount)
    DisposableEffect(Unit) {
        onDispose {
            // Save on disposal (back-press, tab switch, or process kill) so no progress is lost.
            if (questions.isNotEmpty()) {
                LessonProgressStore.save(
                    driver, lessonId,
                    LessonProgressStore.Progress(latestIndex.value, latestCorrect.value, totalCount)
                )
            }
        }
    }

    val accentColor = subject?.color ?: Color(0xFF4A80F0)

    if (isComplete) {
        LessonCompleteContent(
            subjectName = subject?.name ?: "",
            lessonName = lesson?.name ?: "",
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onReset = {
                currentIndex = 0
                correctCount = 0
                isComplete = false
                LessonProgressStore.save(driver, lessonId, LessonProgressStore.Progress(0, 0, totalCount))
            },
            onBack = { navController.popBackStack() }
        )
        return
    }

    if (questions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Brak pytań dla tej lekcji", color = Color(0xFF9E9E9E), fontSize = 16.sp)
        }
        return
    }

    // currentIndex can equal totalCount when the lesson is complete, so clamp before indexing.
    val safeIndex = currentIndex.coerceIn(0, totalCount - 1)
    val question = questions[safeIndex]

    // displayQuestion is 1-based and capped at totalCount so the header never shows "11/10".
    val displayQuestion = (currentIndex + 1).coerceAtMost(totalCount)
    val headerProgress = if (totalCount > 0) displayQuestion.toFloat() / totalCount else 0f

    var feedbackState by remember { mutableStateOf(FeedbackState.NONE) }

    val advanceQuestion: () -> Unit = {
        feedbackState = FeedbackState.NONE
        if (isChemistry) {
            ChemistrySessionStore.markAnswered(driver, lessonId, questions[currentIndex.coerceIn(0, totalCount - 1)].id)
        }
        val newCorrect = correctCount + 1
        correctCount = newCorrect
        if (currentIndex < totalCount - 1) {
            val newIndex = currentIndex + 1
            currentIndex = newIndex
            LessonProgressStore.save(driver, lessonId, LessonProgressStore.Progress(newIndex, newCorrect, totalCount))
        } else {
            currentIndex = totalCount
            isComplete = true
            LessonProgressStore.save(driver, lessonId, LessonProgressStore.Progress(totalCount, newCorrect, totalCount))
        }
    }

    val onAnsweredCorrectly: () -> Unit = { feedbackState = FeedbackState.CORRECT }
    val onWrongAnswer: () -> Unit = { feedbackState = FeedbackState.WRONG }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = Color(0xFF1A1A1A))
                }
                Text(
                    text = subject?.name ?: "Lekcja",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(headerProgress * 100).roundToInt()}% ukończone",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$displayQuestion / $totalCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(headerProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(accentColor)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (question) {
                is Question.FindAnswer -> FindAnswerContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer,
                    onSkip = { if (currentIndex < totalCount - 1) currentIndex++ }
                )
                is Question.FindOperator -> FindOperatorContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer,
                    onSkip = { if (currentIndex < totalCount - 1) currentIndex++ }
                )
                is Question.SelectFromList -> SelectFromListContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.TypeAnswer -> TypeAnswerContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.MapQuiz -> MapQuizContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.PeriodicTableQuiz -> PeriodicTableContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.PeriodicTableByShell -> PeriodicTableByShellContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.PeriodicTableByName -> PeriodicTableByNameContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.EquationBalance -> EquationBalanceContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.ElementCardQuiz -> ElementCardContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.GraphTypeAnswer -> GraphTypeAnswerContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
                is Question.GraphSelectFromList -> GraphSelectFromListContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = bottomPadding,
                    onCorrect = onAnsweredCorrectly,
                    onWrong = onWrongAnswer
                )
            }
            AnswerFeedbackOverlay(
                state = feedbackState,
                accentColor = accentColor,
                onCorrectDismiss = advanceQuestion,
                onWrongDismiss = { feedbackState = FeedbackState.NONE }
            )
        }
    }
}
