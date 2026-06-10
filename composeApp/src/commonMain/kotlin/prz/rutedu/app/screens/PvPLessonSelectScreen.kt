package prz.rutedu.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.Screen
import prz.rutedu.app.data.QuestionBank
import prz.rutedu.app.data.SubjectRepository
import prz.rutedu.app.locale.getNameRes
import prz.rutedu.app.models.Question

/**
 * Returns `true` when the question requires the full periodic table UI, which does not fit
 * in a half-screen player area of the PvP battle. Lessons containing such questions are
 * excluded from the duel picker and the questions are filtered out of battle sets.
 */
internal fun isPeriodicTableQuestion(question: Question): Boolean =
    question is Question.PeriodicTableQuiz ||
        question is Question.PeriodicTableByShell ||
        question is Question.PeriodicTableByName

/**
 * Lesson picker for the two-player battle (Pojedynek).
 *
 * Lists every subject from [SubjectRepository] as an expandable card. Expanding a subject
 * shows its unlocked topics as section labels with their unlocked lessons as tappable rows.
 * Tapping a lesson navigates to [Screen.PvPBattle] for that lesson.
 *
 * Lessons whose question set contains periodic-table questions are excluded - the zoomable
 * table needs the whole screen and is unplayable in a split-screen half.
 *
 * @param navController Navigation controller for the back pop and battle navigation.
 * @param bottomPadding System navigation bar height; applied as list content padding.
 */
@Composable
fun PvPLessonSelectScreen(
    navController: NavController,
    bottomPadding: Dp = 0.dp
) {
    var expandedSubjectId by remember { mutableStateOf<String?>(null) }

    // Lesson IDs playable in the duel: unlocked and free of periodic-table questions.
    val playableLessonIds = remember {
        SubjectRepository.subjects
            .flatMap { it.topics }
            .filter { !it.isLocked }
            .flatMap { it.lessons }
            .filter { !it.isLocked }
            .filter { lesson -> QuestionBank.questionsFor(lesson.id).none(::isPeriodicTableQuestion) }
            .map { it.id }
            .toSet()
    }

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
                stringResource(Res.string.menu_pvp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    stringResource(Res.string.fragment_modes_instruction),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            items(SubjectRepository.subjects, key = { it.id }) { subject ->
                val playableTopics = subject.topics.filter { topic ->
                    !topic.isLocked && topic.lessons.any { it.id in playableLessonIds }
                }
                if (playableTopics.isEmpty()) return@items
                val isExpanded = expandedSubjectId == subject.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSubjectId = if (isExpanded) null else subject.id
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(subject.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = subject.icon,
                                    contentDescription = null,
                                    tint = subject.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                stringResource(subject.getNameRes()),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                playableTopics.forEach { topic ->
                                    Text(
                                        stringResource(topic.getNameRes()).uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                    topic.lessons.filter { it.id in playableLessonIds }.forEach { lesson ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navController.navigate(
                                                        Screen.PvPBattle.createRoute(subject.id, lesson.id)
                                                    )
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(subject.color)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                stringResource(lesson.getNameRes()),
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
