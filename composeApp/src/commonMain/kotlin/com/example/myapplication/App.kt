package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.cash.sqldelight.db.SqlDriver
import com.example.myapplication.components.BottomNavBar
import com.example.myapplication.components.NavTab
import com.example.myapplication.data.SubjectRepository
import com.example.myapplication.locale.AppLocaleProvider
import com.example.myapplication.locale.customAppLocale
import com.example.myapplication.multiplayer.model.MultiplayerMode
import com.example.myapplication.screens.ConfigListScreen
import com.example.myapplication.screens.GameMode
import com.example.myapplication.screens.GameScreen
import com.example.myapplication.screens.MainScreen
import com.example.myapplication.screens.MultiplayerMenuScreen
import com.example.myapplication.screens.PlayerSelectionScreen
import com.example.myapplication.screens.PvPBattleScreen
import com.example.myapplication.screens.PvPLobbyClientScreen
import com.example.myapplication.screens.PvPLobbyHostScreen
import com.example.myapplication.screens.PvPNetworkBattleScreen
import com.example.myapplication.screens.PvPNetworkResultScreen
import com.example.myapplication.screens.PvPSetupScreen
import com.example.myapplication.screens.SelectionScreen
import com.example.myapplication.screens.Settings
import com.example.myapplication.screens.SubjectConfigScreen
import com.example.myapplication.screens.SubjectDetailScreen
import com.example.myapplication.screens.LessonGameScreen
import com.example.myapplication.screens.TopicDetailScreen
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.player1
import rutedu.composeapp.generated.resources.player2
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Selection : Screen("selection")
    object PlayerSelection : Screen("player-selection")
    object PvP : Screen("pvp")
    object Settings : Screen("settings")
    object Nauka : Screen("nauka")
    object Cwiczenia : Screen("cwiczenia")
    object GameAddSubtract : Screen("game-add-subtract")
    object GameMultiplyDivide : Screen("game-multiply-divide")
    object GameDivisibility : Screen("game-divisibility")
    object GameUnitConversion : Screen("game-unit-conversion")
    object GameMultiplicationTable : Screen("game-multiplication-table")

    // Config list screen (settings)
    object ConfigList : Screen("config-list")

    // Multiplayer
    object MultiplayerMenu : Screen("multiplayer-menu")
    object PvPSetup : Screen("pvp-setup/{mode}") {
        fun createRoute(mode: String) = "pvp-setup/$mode"
    }
    object PvPLobbyHost : Screen("pvp-lobby-host")
    object PvPLobbyClient : Screen("pvp-lobby-client")
    object PvPNetworkBattle : Screen("pvp-network-battle")
    object PvPNetworkResult : Screen("pvp-network-result")

    // Subject configurator
    object SubjectConfig : Screen("subject-config/{subjectId}") {
        fun createRoute(subjectId: String) = "subject-config/$subjectId"
    }

    // Subject detail – pass subject id as path arg
    object SubjectDetail : Screen("subject/{subjectId}") {
        fun createRoute(subjectId: String) = "subject/$subjectId"
    }

    // Topic detail – pass both subject id and topic id as path args
    object TopicDetail : Screen("topic/{subjectId}/{topicId}") {
        fun createRoute(subjectId: String, topicId: String) = "topic/$subjectId/$topicId"
    }

    // Lesson game – subject + topic + lesson ids
    object LessonGame : Screen("lesson/{subjectId}/{topicId}/{lessonId}") {
        fun createRoute(subjectId: String, topicId: String, lessonId: String) =
            "lesson/$subjectId/$topicId/$lessonId"
    }
}

// Global state to hold the selected player ID
var selectedPlayerId by mutableStateOf<Long?>(null)

// Remembers last used multiplayer nickname within the session
var lastMultiplayerNickname by mutableStateOf("")

// Remembers the last visited subject so NAUKA tab returns to it
var lastVisitedSubjectId by mutableStateOf<String?>(null)

// Remembers the last opened lesson so ĆWICZENIA tab returns to it
var lastVisitedLessonRoute by mutableStateOf<String?>(null)

// Routes where the bottom nav bar is visible
private fun showBottomNav(route: String?): Boolean =
    route == Screen.Home.route ||
        route == Screen.Nauka.route ||
        route == Screen.Cwiczenia.route ||
        route?.startsWith("subject/") == true ||
        route?.startsWith("topic/") == true ||
        route?.startsWith("lesson/") == true

@Composable
@Preview
fun App(driver: SqlDriver) {
    val navController = rememberNavController()
    val db = Database(driver)

    LaunchedEffect(Unit) {
        try {
            val savedLanguage = db.databaseQueries.getLanguage().executeAsOneOrNull()
            if (savedLanguage != null) customAppLocale = savedLanguage
        } catch (_: Exception) {}
    }

    MaterialTheme {
        AppLocaleProvider {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            // Derive active color from current subject (changes nav color per subject)
            val activeNavColor: Color = run {
                val subjectId = backStackEntry?.arguments?.getString("subjectId")
                if (subjectId != null) SubjectRepository.getById(subjectId)?.color ?: Color(0xFFF47B20)
                else Color(0xFFF47B20)
            }

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomNav(currentRoute)) {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            activeColor = activeNavColor,
                            onTabSelected = { tab ->
                                // NAUKA tab returns to the last opened subject
                                val destination = when (tab) {
                                    NavTab.NAUKA -> lastVisitedSubjectId
                                        ?.let { Screen.SubjectDetail.createRoute(it) }
                                        ?: Screen.Nauka.route
                                    NavTab.CWICZENIA -> lastVisitedLessonRoute
                                        ?: Screen.Cwiczenia.route
                                    else -> tab.route
                                }
                                // Simple pop-to-home then navigate – no saveState/restoreState
                                // which was causing HOME to stop responding
                                navController.navigate(destination) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val effectiveBottomPadding = maxOf(paddingValues.calculateBottomPadding(), navBarPadding)
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable(Screen.Home.route) {
                        MainScreen(
                            navController = navController,
                            driver = driver,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.Nauka.route) {
                        PlaceholderScreen(
                            label = "Nauka",
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.Cwiczenia.route) {
                        PlaceholderScreen(
                            label = "Ćwiczenia",
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.ConfigList.route) {
                        ConfigListScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.SubjectConfig.route) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
                        SubjectConfigScreen(
                            subjectId = subjectId,
                            navController = navController,
                            driver = driver,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.SubjectDetail.route) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
                        LaunchedEffect(subjectId) { lastVisitedSubjectId = subjectId }
                        SubjectDetailScreen(
                            subjectId = subjectId,
                            navController = navController,
                            driver = driver,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.TopicDetail.route) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
                        val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
                        TopicDetailScreen(
                            subjectId = subjectId,
                            topicId = topicId,
                            navController = navController,
                            driver = driver,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.LessonGame.route) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
                        val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
                        val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
                        LaunchedEffect(subjectId, topicId, lessonId) {
                            lastVisitedLessonRoute = Screen.LessonGame.createRoute(subjectId, topicId, lessonId)
                        }
                        LessonGameScreen(
                            subjectId = subjectId,
                            topicId = topicId,
                            lessonId = lessonId,
                            navController = navController,
                            driver = driver,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.Settings.route) {
                        Settings(navController = navController, database = db)
                    }

                    composable(Screen.PlayerSelection.route) {
                        PlayerSelectionScreen(
                            navController = navController,
                            database = db,
                            onPlayerSelected = { player ->
                                selectedPlayerId = player.id
                                navController.navigate(Screen.Selection.route)
                            }
                        )
                    }

                    composable(Screen.PvP.route) {
                        val player1Text = stringResource(Res.string.player1)
                        val player2Text = stringResource(Res.string.player2)
                        PvPBattleScreen(
                            navController = navController,
                            player1Name = player1Text,
                            player2Name = player2Text
                        )
                    }

                    composable(Screen.GameAddSubtract.route) {
                        GameScreen(
                            navController = navController,
                            gameMode = GameMode.ADD_SUBTRACT,
                            database = db,
                            playerId = selectedPlayerId
                        )
                    }

                    composable(Screen.GameMultiplyDivide.route) {
                        GameScreen(
                            navController = navController,
                            gameMode = GameMode.MULTIPLY_DIVIDE,
                            database = db,
                            playerId = selectedPlayerId
                        )
                    }

                    composable(Screen.GameDivisibility.route) {
                        GameScreen(
                            navController = navController,
                            gameMode = GameMode.DIVISIBILITY,
                            database = db,
                            playerId = selectedPlayerId
                        )
                    }

                    composable(Screen.GameUnitConversion.route) {
                        GameScreen(
                            navController = navController,
                            gameMode = GameMode.UNIT_CONVERSION,
                            database = db,
                            playerId = selectedPlayerId
                        )
                    }

                    composable(Screen.GameMultiplicationTable.route) {
                        GameScreen(
                            navController = navController,
                            gameMode = GameMode.MULTIPLICATION_TABLE,
                            database = db,
                            playerId = selectedPlayerId
                        )
                    }

                    composable(Screen.Selection.route) {
                        SelectionScreen(navController = navController)
                    }

                    composable(Screen.MultiplayerMenu.route) {
                        MultiplayerMenuScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.PvPSetup.route) { backStackEntry ->
                        val modeStr = backStackEntry.arguments?.getString("mode") ?: MultiplayerMode.ONE_VS_ONE.name
                        val mode = runCatching { MultiplayerMode.valueOf(modeStr) }.getOrElse { MultiplayerMode.ONE_VS_ONE }
                        PvPSetupScreen(
                            mode = mode,
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.PvPLobbyHost.route) {
                        PvPLobbyHostScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.PvPLobbyClient.route) {
                        PvPLobbyClientScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.PvPNetworkBattle.route) {
                        PvPNetworkBattleScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.PvPNetworkResult.route) {
                        PvPNetworkResultScreen(
                            navController = navController,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                }

            }
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String, bottomPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
    }
}
