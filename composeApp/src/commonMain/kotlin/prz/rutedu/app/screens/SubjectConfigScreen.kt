package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.cash.sqldelight.db.SqlDriver
import prz.rutedu.app.data.LessonProgressStore
import prz.rutedu.app.data.QuestionBank
import prz.rutedu.app.data.SubjectConfigStore
import prz.rutedu.app.data.SubjectRepository
import prz.rutedu.app.models.Lesson
import prz.rutedu.app.models.Topic
import kotlin.math.roundToInt

/**
 * Per-subject lesson configurator: lets the student choose how many questions to practise per lesson.
 *
 * Displays a slider for each unlocked lesson (range 5..max questions, default = min(max, 25)).
 * Config is auto-saved to [SubjectConfigStore] via `rememberUpdatedState` + `DisposableEffect`
 * when the screen is left, and also explicitly on the "Zapisz" (Save) button tap. A "Reset
 * progress" card can wipe [LessonProgressStore] for all lessons in the subject.
 *
 * @param subjectId     ID of the subject to configure (e.g. `"matematyka"`).
 * @param navController Navigation controller for the back-stack pop.
 * @param driver        SQLite driver used by [SubjectConfigStore] and [LessonProgressStore].
 * @param bottomPadding System navigation bar height; applied to the Save button bottom padding.
 */
@Composable
fun SubjectConfigScreen(
    subjectId: String,
    navController: NavController,
    driver: SqlDriver,
    bottomPadding: Dp = 0.dp
) {
    val subject = SubjectRepository.getById(subjectId) ?: return
    val accentColor = subject.color

    // Collect unlocked topics with their unlocked lessons
    val unlockedTopics: List<Pair<Topic, List<Lesson>>> = remember {
        subject.topics
            .filter { !it.isLocked }
            .mapNotNull { topic ->
                val lessons = topic.lessons.filter { !it.isLocked }
                if (lessons.isNotEmpty()) topic to lessons else null
            }
    }

    // Per-lesson max questions
    val maxQuestionsMap: Map<String, Int> = remember {
        unlockedTopics.flatMap { (_, lessons) -> lessons }.associate { lesson ->
            lesson.id to QuestionBank.questionsFor(lesson.id).size.coerceAtLeast(5)
        }
    }

    val suggestedMap: Map<String, Int> = remember {
        maxQuestionsMap.mapValues { (_, max) -> max.coerceAtMost(25) }
    }

    // Load saved config for each lesson; default to suggested
    var questionCounts by remember {
        mutableStateOf(
            unlockedTopics.flatMap { (_, lessons) -> lessons }.associate { lesson ->
                val saved = SubjectConfigStore.load(driver, lesson.id)
                val max = maxQuestionsMap[lesson.id] ?: 5
                lesson.id to (saved?.questionCount?.coerceIn(5, max) ?: (suggestedMap[lesson.id] ?: 5))
            }
        )
    }

    // Auto-save on leave
    val latestCounts = rememberUpdatedState(questionCounts)
    DisposableEffect(Unit) {
        onDispose {
            latestCounts.value.forEach { (lessonId, count) ->
                SubjectConfigStore.save(driver, lessonId, SubjectConfigStore.Config(count))
            }
        }
    }

    var showResetConfirm by remember { mutableStateOf(false) }
    var resetDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = Color(0xFF1A1A1A))
            }
            Text(
                "Konfiguracja: ${subject.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        // ── Scrollable content ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            // ── Per-topic lesson sliders ──────────────────────────────────
            unlockedTopics.forEachIndexed { topicIndex, (topic, lessons) ->
                if (topicIndex > 0) Spacer(Modifier.height(24.dp))

                Text(
                    topic.name.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9E9E9E),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))

                lessons.forEach { lesson ->
                    val lessonId = lesson.id
                    val maxQ = maxQuestionsMap[lessonId] ?: 5
                    val suggested = suggestedMap[lessonId] ?: 5
                    val count = questionCounts[lessonId] ?: suggested

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                lesson.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9E9E9E)
                            )
                            Spacer(Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "$count",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1A1A),
                                    modifier = Modifier.weight(1f)
                                )
                                if (count == suggested) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(accentColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "Sugerowane",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }

                            Slider(
                                value = count.toFloat(),
                                onValueChange = { newVal ->
                                    questionCounts = questionCounts + (lessonId to newVal.roundToInt())
                                },
                                valueRange = 5f..maxQ.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = lesson.color,
                                    activeTrackColor = lesson.color,
                                    inactiveTrackColor = lesson.color.copy(alpha = 0.2f)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("5 ZADAŃ", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                                Text("$maxQ ZADAŃ", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Zarządzanie postępami ─────────────────────────────────────
            Text(
                "Zarządzanie postępami",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Resetuj postęp",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "To trwale usunie Twoje wyniki i historię dla tego przedmiotu. Operacja jest nieodwracalna.",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (resetDone) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F8F0))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Postęp zresetowany ✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3DBD7D)
                            )
                        }
                    } else if (!showResetConfirm) {
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE53935))
                        ) {
                            Text(
                                "Resetuj postęp",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                val lessonIds = subject.topics.flatMap { it.lessons }.map { it.id }
                                lessonIds.forEach { lessonId ->
                                    LessonProgressStore.save(
                                        driver, lessonId,
                                        LessonProgressStore.Progress(0, 0, 0)
                                    )
                                }
                                showResetConfirm = false
                                resetDone = true
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text("Potwierdź reset postępu", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Anuluj", color = Color(0xFF9E9E9E))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Zapisz ──────────────────────────────────────────────────────────
        Button(
            onClick = {
                questionCounts.forEach { (lessonId, count) ->
                    SubjectConfigStore.save(driver, lessonId, SubjectConfigStore.Config(count))
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomPadding + 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Zapisz", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
