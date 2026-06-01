package prz.rutedu.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.cash.sqldelight.db.SqlDriver
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.components.BottomNavBar
import prz.rutedu.app.components.NavTab
import prz.rutedu.app.data.SubjectRepository
import prz.rutedu.app.locale.AppLocaleProvider
import prz.rutedu.app.locale.customAppLocale
import prz.rutedu.app.screens.ConfigListScreen
import prz.rutedu.app.screens.GameMode
import prz.rutedu.app.screens.GameScreen
import prz.rutedu.app.screens.LessonGameScreen
import prz.rutedu.app.screens.MainScreen
import prz.rutedu.app.screens.PlayerSelectionScreen
import prz.rutedu.app.screens.PvPBattleScreen
import prz.rutedu.app.screens.SelectionScreen
import prz.rutedu.app.screens.Settings
import prz.rutedu.app.screens.SubjectConfigScreen
import prz.rutedu.app.screens.SubjectDetailScreen
import prz.rutedu.app.screens.TopicDetailScreen
import prz.rutedu.app.theme.RUTEduTheme
import prz.rutedu.app.theme.ThemeMode
import prz.rutedu.app.theme.customAppThemeMode
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*

/**
 * All navigation destinations in the app, expressed as a sealed class hierarchy.
 *
 * Each object holds a [route] string that must match a `composable(route)` declaration in
 * the [App] `NavHost`. Destinations with path parameters expose a `createRoute(...)` factory
 * so the caller never constructs URL strings by hand.
 *
 * The three bottom-nav tabs (START, NAUKA, ĆWICZENIA) map to [Home], [SubjectDetail] (last
 * visited), and [LessonGame] (last visited) respectively - see [BottomNavBar] and [App].
 *
 * @property route The navigation route string used in [NavHost] composable declarations.
 */
sealed class Screen(val route: String) {
    /** Home screen - 2-column subject grid with settings shortcut. */
    object Home : Screen("home")
    /** Game-mode selection after player pick. */
    object Selection : Screen("selection")
    /** Leaderboard player picker before a game session. */
    object PlayerSelection : Screen("player-selection")
    /** Two-player PvP battle screen. */
    object PvP : Screen("pvp")
    /** Language / app settings. */
    object Settings : Screen("settings")
    /** Language settings. */
    object LanguageSettings : Screen("language-settings")
    /** Placeholder shown when NAUKA tab is tapped with no last-visited subject. */
    object Nauka : Screen("nauka")
    /** Placeholder shown when ĆWICZENIA tab is tapped with no last-visited lesson. */
    object Cwiczenia : Screen("cwiczenia")
    /** Solo arithmetic mini-game: addition and subtraction. */
    object GameAddSubtract : Screen("game-add-subtract")
    /** Solo arithmetic mini-game: multiplication and division. */
    object GameMultiplyDivide : Screen("game-multiply-divide")
    /** Solo arithmetic mini-game: divisibility rules. */
    object GameDivisibility : Screen("game-divisibility")
    /** Solo arithmetic mini-game: unit conversion. */
    object GameUnitConversion : Screen("game-unit-conversion")
    /** Solo arithmetic mini-game: multiplication table. */
    object GameMultiplicationTable : Screen("game-multiplication-table")

    /** List of all subjects for per-subject question-count configuration. */
    object ConfigList : Screen("config-list")

    /** Per-subject question-count configurator. Requires `subjectId` path argument. */
    object SubjectConfig : Screen("subject-config/{subjectId}") {
        /** @return A concrete route string, e.g. `"subject-config/matematyka"`. */
        fun createRoute(subjectId: String) = "subject-config/$subjectId"
    }

    /** Topic list for a subject. Requires `subjectId` path argument. */
    object SubjectDetail : Screen("subject/{subjectId}") {
        /** @return A concrete route string, e.g. `"subject/geografia"`. */
        fun createRoute(subjectId: String) = "subject/$subjectId"
    }

    /** Lesson list for a topic. Requires `subjectId` and `topicId` path arguments. */
    object TopicDetail : Screen("topic/{subjectId}/{topicId}") {
        /** @return A concrete route string, e.g. `"topic/chemia/kwasy"`. */
        fun createRoute(subjectId: String, topicId: String) = "topic/$subjectId/$topicId"
    }

    /**
     * In-lesson quiz screen. Requires `subjectId`, `topicId`, and `lessonId` path arguments.
     *
     * The `lessonId` determines which question set is loaded:
     * - IDs starting with `"chemia_"` -> `ChemistryQuestionGenerator` (procedural).
     * - All other IDs -> static `QuestionBank`.
     */
    object LessonGame : Screen("lesson/{subjectId}/{topicId}/{lessonId}") {
        /** @return A concrete route string, e.g. `"lesson/chemia/kwasy/chemia_3_2"`. */
        fun createRoute(subjectId: String, topicId: String, lessonId: String) =
            "lesson/$subjectId/$topicId/$lessonId"
    }
}

/** The database row ID of the player chosen on the player-selection screen. `null` when no player
 *  is active (e.g. first launch before any profile has been created). */
var selectedPlayerId by mutableStateOf<Long?>(null)

/** Subject ID of the most recently visited subject detail screen.
 *  The NAUKA tab uses this to jump directly back rather than landing on the placeholder. */
var lastVisitedSubjectId by mutableStateOf<String?>(null)

/** Full navigation route of the most recently opened lesson (e.g. `"lesson/chemia/kwasy/chemia_3_2"`).
 *  The ĆWICZENIA tab uses this to resume the last lesson immediately. */
var lastVisitedLessonRoute by mutableStateOf<String?>(null)

/**
 * Returns `true` for routes where the [BottomNavBar] should be visible.
 *
 * The bottom bar is hidden on game screens (e.g. `"pvp"`, `"game-add-subtract"`) and settings
 * screens to avoid cluttering full-screen experiences.
 */
private fun showBottomNav(route: String?): Boolean =
    route == Screen.Home.route ||
        route == Screen.Nauka.route ||
        route == Screen.Cwiczenia.route ||
        route?.startsWith("subject/") == true ||
        route?.startsWith("topic/") == true ||
        route?.startsWith("lesson/") == true

/**
 * Root composable and single entry point for the entire app.
 *
 * Responsibilities:
 * 1. Creates the `NavController` and [Database] instances for the session.
 * 2. Restores the persisted language preference from the database on first launch
 *    (writes into [customAppLocale] so [AppLocaleProvider] reacts immediately).
 * 3. Wraps the whole UI in [MaterialTheme] and [AppLocaleProvider] so every descendant
 *    reacts to locale changes without needing to be individually locale-aware.
 * 4. Provides a [Scaffold] with a context-sensitive [BottomNavBar]:
 *    - The bar is hidden on non-navigable screens (see [showBottomNav]).
 *    - The active accent color tracks the current subject for visual continuity.
 *    - Tab clicks use [lastVisitedSubjectId] / [lastVisitedLessonRoute] to restore the
 *      user's position rather than landing on placeholder screens.
 * 5. Hosts the [NavHost] with all application routes registered as `composable()` blocks.
 *    All nav transitions are instant (no enter/exit animation) to keep the UI snappy.
 *
 * **Adding a new screen:** declare an `object` in [Screen], add a `composable()` block here,
 * and add the route to [showBottomNav] if the bottom bar should be visible there.
 *
 * @param driver Platform-specific SQLite driver created by `DriverFactory` in the platform entry
 *               point (Android `MainActivity` / iOS `MainViewController`). Must outlive this
 *               composable - do not create a new driver inside `App`.
 */
@Composable
@Preview
fun App(driver: SqlDriver) {
    val navController = rememberNavController()
    val db = remember { Database(driver) }

    // Load initial settings synchronously during the first composition to prevent flashes
    remember(db) {
        try {
            val savedLanguage = db.databaseQueries.getLanguage().executeAsOneOrNull()
            if (savedLanguage != null) {
                customAppLocale = savedLanguage
            }
        } catch (_: Exception) {}
        try {
            val savedTheme = db.databaseQueries.getThemeMode().executeAsOneOrNull()
            if (savedTheme != null) {
                customAppThemeMode = ThemeMode.fromString(savedTheme)
            }
        } catch (_: Exception) {}
        Unit
    }

    RUTEduTheme {
        AppLocaleProvider {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route

            // Read the subjectId path arg from the back stack (null on non-subject routes).
            // Used to match the bottom nav accent color to the subject the user is currently in.
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
                                // NAUKA/ĆWICZENIA tabs restore the last-visited destination instead of
                                // landing on placeholder screens. Falls back to the placeholder route
                                // when no subject/lesson has been visited yet in this session.
                                val destination = when (tab) {
                                    NavTab.NAUKA -> lastVisitedSubjectId
                                        ?.let { Screen.SubjectDetail.createRoute(it) }
                                        ?: Screen.Nauka.route
                                    NavTab.CWICZENIA -> lastVisitedLessonRoute
                                        ?: Screen.Cwiczenia.route
                                    else -> tab.route
                                }
                                // popUpTo HOME (non-inclusive) clears intermediate back-stack entries.
                                // saveState/restoreState was intentionally removed - it caused
                                // the HOME tab to stop responding after repeated tab switches.
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
                            label = stringResource(Res.string.placeholder_learn),
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.Cwiczenia.route) {
                        PlaceholderScreen(
                            label = stringResource(Res.string.placeholder_exercises),
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
                        Settings(
                            navController = navController,
                            database = db,
                            bottomPadding = effectiveBottomPadding
                        )
                    }

                    composable(Screen.LanguageSettings.route) {
                        prz.rutedu.app.screens.LanguageSettings(
                            navController = navController,
                            database = db,
                            bottomPadding = effectiveBottomPadding
                        )
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
                }
            }
        }
    }
}

/**
 * Temporary stand-in screen shown for tabs (NAUKA, ĆWICZENIA) that have not yet been
 * fleshed out, or when the user reaches a tab with no prior navigation history.
 *
 * Renders a centered label on a white background. Replace this with real content once
 * the corresponding tab feature is implemented.
 */
@Composable
private fun PlaceholderScreen(label: String, bottomPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
