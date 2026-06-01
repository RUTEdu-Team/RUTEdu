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
import prz.rutedu.app.components.TopicCard
import prz.rutedu.app.data.LessonProgressStore
import prz.rutedu.app.data.SubjectRepository

/**
 * Topic list for a given subject.
 *
 * Displays all topics belonging to [subjectId] as [TopicCard]s in a scrollable list.
 * Progress for each topic is read from [LessonProgressStore] and refreshed every time the user
 * navigates back here (via `LaunchedEffect(navBackStackEntry)`), ensuring that completing a
 * lesson inside a topic is immediately reflected in the topic card's progress bar.
 *
 * Locked topics render a non-clickable grey card. Unlocked topics with at least one lesson
 * navigate to [Screen.TopicDetail] on tap.
 *
 * @param subjectId   ID of the subject whose topics are shown. If not found in [SubjectRepository],
 *                    the composable returns early (renders nothing).
 * @param navController Used to navigate to [Screen.TopicDetail] and to pop back on back-press.
 * @param driver       SQLite driver for reading lesson progress from [LessonProgressStore].
 * @param bottomPadding System navigation bar height padding from `App`.
 */
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    navController: NavController,
    driver: SqlDriver,
    bottomPadding: Dp = 0.dp
) {
    val subject = SubjectRepository.getById(subjectId) ?: return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var topicProgresses by remember {
        mutableStateOf(
            subject.topics.associate { topic ->
                topic.id to LessonProgressStore.topicFraction(driver, topic.lessons.map { it.id })
            }
        )
    }
    LaunchedEffect(navBackStackEntry) {
        topicProgresses = subject.topics.associate { topic ->
            topic.id to LessonProgressStore.topicFraction(driver, topic.lessons.map { it.id })
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
                text = stringResource(subject.getNameRes()),
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
            items(subject.topics) { topic ->
                val realProgress = topicProgresses[topic.id] ?: topic.progress
                TopicCard(
                    topic = topic.copy(progress = realProgress),
                    onClick = {
                        if (!topic.isLocked && topic.lessons.isNotEmpty())
                            navController.navigate(Screen.TopicDetail.createRoute(subjectId, topic.id))
                    }
                )
            }
        }
    }
}
