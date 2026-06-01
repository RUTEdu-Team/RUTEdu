package prz.rutedu.app.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.cash.sqldelight.db.SqlDriver
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.Screen
import prz.rutedu.app.components.SubjectCard
import prz.rutedu.app.data.LessonProgressStore
import prz.rutedu.app.data.SubjectRepository



/**
 * Home screen - the first screen the user sees after launch.
 *
 * Displays a 2-column grid of [SubjectCard]s (one per subject defined in [SubjectRepository]).
 * Each card shows live progress by reading [LessonProgressStore] for all lessons in that subject.
 *
 * **Progress refresh:** progress is re-read every time the user navigates back to this screen
 * (via `LaunchedEffect(navBackStackEntry)`). This ensures that after completing a lesson the
 * home screen immediately reflects the new progress without requiring a full recomposition restart.
 *
 * **Header:** a branded logo row with the app name and a settings icon that navigates to
 * [Screen.ConfigList] (per-subject question-count configuration).
 *
 * @param navController Navigation controller used to push [Screen.SubjectDetail] when a card is tapped.
 * @param driver         SQLite driver forwarded to [LessonProgressStore] for progress reads.
 * @param bottomPadding  Extra bottom padding equal to the system navigation bar height, passed down
 *                       by `App` via `Scaffold` padding values so the grid content is not clipped.
 */
@Composable
fun MainScreen(
    navController: NavController,
    driver: SqlDriver,
    bottomPadding: Dp = 0.dp
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var subjects by remember {
        mutableStateOf(
            SubjectRepository.subjects.map { subject ->
                val allLessonIds = subject.topics.flatMap { it.lessons }.map { it.id }
                subject.copy(progress = LessonProgressStore.topicFraction(driver, allLessonIds))
            }
        )
    }
    LaunchedEffect(navBackStackEntry) {
        subjects = SubjectRepository.subjects.map { subject ->
            val allLessonIds = subject.topics.flatMap { it.lessons }.map { it.id }
            subject.copy(progress = LessonProgressStore.topicFraction(driver, allLessonIds))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Logo header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "RUTEdu",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate(Screen.ConfigList.route) }) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.menu_settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(Res.string.main_good_morning),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = stringResource(Res.string.main_what_to_learn),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        val uriHandler = LocalUriHandler.current

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            items(subjects) { subject ->
                SubjectCard(
                    subject = subject,
                    onClick = { navController.navigate(Screen.SubjectDetail.createRoute(subject.id)) }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
//                    Text(
//                        text = "Aplikacja powstała dzięki",
//                        fontSize = 11.sp,
//                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C4189))
                                .clickable { uriHandler.openUri("https://prz.edu.pl/") }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.logo_prz),
                                contentDescription = "Politechnika Rzeszowska",
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1C4189))
                                .clickable { uriHandler.openUri("https://weii.prz.edu.pl/") }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.weii_header),
                                contentDescription = stringResource(Res.string.weii_description),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
