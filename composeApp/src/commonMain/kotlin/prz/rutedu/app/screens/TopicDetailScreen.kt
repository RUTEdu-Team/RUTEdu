package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.cash.sqldelight.db.SqlDriver
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.Screen
import prz.rutedu.app.locale.getNameRes
import prz.rutedu.app.components.LessonCard
import prz.rutedu.app.data.LessonProgressStore
import prz.rutedu.app.data.QuestionBank
import prz.rutedu.app.data.SubjectRepository

/**
 * Lesson list for a given topic.
 *
 * Displays all lessons belonging to [topicId] as [LessonCard]s in a scrollable list.
 * Progress is read from [LessonProgressStore] and refreshed on every back-navigation, mirroring
 * the same pattern used in [SubjectDetailScreen].
 *
 * Before navigating to [LessonGameScreen], the click handler verifies that the lesson is not
 * locked **and** that [QuestionBank.questionsFor] returns at least one question. This prevents
 * navigating to a lesson whose content hasn't been written yet.
 *
 * @param subjectId   Parent subject ID (passed through to [Screen.LessonGame.createRoute]).
 * @param topicId     ID of the topic whose lessons are shown. Returns early if not found.
 * @param navController Navigation controller for back-press and forward navigation to lessons.
 * @param driver       SQLite driver for [LessonProgressStore] reads.
 * @param bottomPadding System navigation bar height padding from `App`.
 */
@Composable
fun TopicDetailScreen(
    subjectId: String,
    topicId: String,
    navController: NavController,
    driver: SqlDriver,
    bottomPadding: Dp = 0.dp
) {
    val topic = SubjectRepository.getTopicById(subjectId, topicId) ?: return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var lessonProgresses by remember {
        mutableStateOf(
            topic.lessons.associate { lesson ->
                lesson.id to LessonProgressStore.lessonFraction(driver, lesson.id)
            }
        )
    }
    LaunchedEffect(navBackStackEntry) {
        lessonProgresses = topic.lessons.associate { lesson ->
            lesson.id to LessonProgressStore.lessonFraction(driver, lesson.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(topic.getNameRes()),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topic.lessons) { lesson ->
                // Inject live DB progress while preserving the static isLocked value from the data model.
                val realProgress = lessonProgresses[lesson.id] ?: lesson.progress
                LessonCard(
                    lesson = lesson.copy(progress = realProgress),
                    onClick = {
                        if (!lesson.isLocked && QuestionBank.questionsFor(lesson.id).isNotEmpty())
                            navController.navigate(Screen.LessonGame.createRoute(subjectId, topicId, lesson.id))
                    }
                )
            }
        }
    }
}
