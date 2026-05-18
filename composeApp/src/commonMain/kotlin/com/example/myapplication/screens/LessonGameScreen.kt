package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.myapplication.data.ChemistrySessionStore
import com.example.myapplication.data.LessonProgressStore
import com.example.myapplication.data.QuestionBank
import com.example.myapplication.data.SubjectConfigStore
import com.example.myapplication.data.SubjectRepository
import com.example.myapplication.models.Question
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────
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

    val latestIndex = rememberUpdatedState(currentIndex)
    val latestCorrect = rememberUpdatedState(correctCount)
    DisposableEffect(Unit) {
        onDispose {
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

    val safeIndex = currentIndex.coerceIn(0, totalCount - 1)
    val question = questions[safeIndex]
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Wróć", tint = Color(0xFF1A1A1A))
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
