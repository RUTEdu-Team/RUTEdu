package prz.rutedu.app.screens

import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import androidx.compose.foundation.background
import prz.rutedu.app.locale.getNameRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                stringResource(Res.string.config_subject_title, stringResource(subject.getNameRes())),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            // Per-topic lesson sliders
            unlockedTopics.forEachIndexed { topicIndex, (topic, lessons) ->
                if (topicIndex > 0) Spacer(Modifier.height(24.dp))

                Text(
                    stringResource(topic.getNameRes()).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                stringResource(lesson.getNameRes()),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
                                            stringResource(Res.string.config_suggested),
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
                                Text(stringResource(Res.string.config_tasks_count, 5), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(Res.string.config_tasks_count, maxQ), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Progress management
            Text(
                stringResource(Res.string.config_progress_mgmt),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE53935).copy(alpha = 0.15f)),
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
                                stringResource(Res.string.config_reset_progress),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                stringResource(Res.string.config_reset_desc),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                .background(Color(0xFF3DBD7D).copy(alpha = 0.15f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(Res.string.config_reset_done),
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
                                stringResource(Res.string.config_reset_progress),
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
                            Text(stringResource(Res.string.config_reset_confirm), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Save
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
            Text(stringResource(Res.string.save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
