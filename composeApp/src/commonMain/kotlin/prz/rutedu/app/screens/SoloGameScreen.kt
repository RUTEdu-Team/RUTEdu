package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.Database
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.back
import rutedu.composeapp.generated.resources.check
import rutedu.composeapp.generated.resources.fragment_modes_add_sub
import rutedu.composeapp.generated.resources.fragment_modes_divisibility
import rutedu.composeapp.generated.resources.fragment_modes_mul_div
import rutedu.composeapp.generated.resources.fragment_modes_table
import rutedu.composeapp.generated.resources.fragment_modes_units
import rutedu.composeapp.generated.resources.game_over
import rutedu.composeapp.generated.resources.no
import rutedu.composeapp.generated.resources.play_again
import rutedu.composeapp.generated.resources.question_counter
import rutedu.composeapp.generated.resources.result_excellent
import rutedu.composeapp.generated.resources.result_good
import rutedu.composeapp.generated.resources.result_great
import rutedu.composeapp.generated.resources.result_practice
import rutedu.composeapp.generated.resources.score_saved
import rutedu.composeapp.generated.resources.yes
import kotlin.random.Random

/**
 * The solo mini-game mode selected from [SelectionScreen].
 * Each entry maps to a distinct question generator.
 */
enum class GameMode {
    /** Addition and subtraction with operands in 1–50. */
    ADD_SUBTRACT,
    /** Multiplication and division with factors 1–12. */
    MULTIPLY_DIVIDE,
    /** Divisibility-rule questions requiring a YES or NO answer. */
    DIVISIBILITY,
    /** Unit-conversion questions (hours<->minutes, km<->m, kg<->g, etc.). */
    UNIT_CONVERSION,
    /** Multiplication-table questions with factors 1–12. */
    MULTIPLICATION_TABLE
}

/**
 * A single question produced by one of the solo-game generator functions.
 *
 * @property questionText  The expression shown to the player (e.g. `"7 x 8 = ?"`).
 * @property correctAnswer The expected answer as a string (e.g. `"56"`, `"YES"`, `"NO"`).
 * @property answerType    Input method - numeric pad or YES/NO buttons; defaults to [AnswerType.NUMBER].
 */
data class GameQuestion(
    val questionText: String,
    val correctAnswer: String,
    val answerType: AnswerType = AnswerType.NUMBER
)

/**
 * Determines which input widget is shown in [GameScreen].
 * - [NUMBER] - full-screen number pad ([FullScreenNumberPad]).
 * - [YES_NO] - two large YES/NO buttons ([YesNoButtons]).
 */
enum class AnswerType {
    /** Student types a numeric answer. */
    NUMBER,
    /** Student taps YES or NO (used for divisibility questions). */
    YES_NO
}

/** Returns a random addition or subtraction question with operands in 1–50. */
fun generateAddSubtractQuestion(): GameQuestion {
    val a = (1..50).random()
    val b = (1..50).random()
    return if (listOf(true, false).random()) {
        GameQuestion("$a + $b = ?", (a + b).toString())
    } else {
        val larger = maxOf(a, b)
        val smaller = minOf(a, b)
        GameQuestion("$larger - $smaller = ?", (larger - smaller).toString())
    }
}

/** Returns a random multiplication or division question with factors 1–12. */
fun generateMultiplyDivideQuestion(): GameQuestion {
    val a = (1..12).random()
    val b = (1..12).random()
    return if (listOf(true, false).random()) {
        GameQuestion("$a × $b = ?", (a * b).toString())
    } else {
        val product = a * b
        GameQuestion("$product ÷ $a = ?", b.toString())
    }
}

/** Returns a random divisibility-rule question ("Is N divisible by D?") with a YES/NO answer. */
fun generateDivisibilityQuestion(): GameQuestion {

    val divisor = (2..10).random()

    val multiplier = (2..20).random()

    val isDivisible = Random.nextBoolean()

    val number = (divisor * multiplier) + if (isDivisible) 0 else 1

    return GameQuestion(
        "Is $number divisible by $divisor?",
        if (isDivisible) "YES" else "NO",
        AnswerType.YES_NO
    )
}

/** Returns a random unit-conversion question (e.g. `"3 hours = ? minutes"`). */
fun generateUnitConversionQuestion(): GameQuestion {
    val conversions = listOf(
        Triple("hours", "minutes", 60),
        Triple("minutes", "seconds", 60),
        Triple("meters", "centimeters", 100),
        Triple("kilometers", "meters", 1000),
        Triple("kilograms", "grams", 1000),
        Triple("days", "hours", 24)
    )
    
    val (fromUnit, toUnit, factor) = conversions.random()
    val value = (1..10).random()
    
    return GameQuestion(
        "$value $fromUnit = ? $toUnit",
        (value * factor).toString()
    )
}

/** Returns a random multiplication-table question with factors 1–12. */
fun generateMultiplicationTableQuestion(): GameQuestion {
    val a = (1..12).random()
    val b = (1..12).random()
    return GameQuestion("$a × $b = ?", (a * b).toString())
}

/**
 * Dispatches to the correct generator based on [mode].
 *
 * @param mode The active [GameMode].
 * @return A fresh [GameQuestion] for that mode.
 */
fun generateQuestionForMode(mode: GameMode): GameQuestion {
    return when (mode) {
        GameMode.ADD_SUBTRACT -> generateAddSubtractQuestion()
        GameMode.MULTIPLY_DIVIDE -> generateMultiplyDivideQuestion()
        GameMode.DIVISIBILITY -> generateDivisibilityQuestion()
        GameMode.UNIT_CONVERSION -> generateUnitConversionQuestion()
        GameMode.MULTIPLICATION_TABLE -> generateMultiplicationTableQuestion()
    }
}

/**
 * Solo 10-question arithmetic mini-game screen.
 *
 * Generates questions via [generateQuestionForMode], tracks score, and saves the result to the
 * leaderboard when [playerId] is non-null. Switches to [GameOverContent] after all 10 questions.
 *
 * @param navController Navigation controller for the back-stack pop on "Back".
 * @param gameMode      Which type of arithmetic questions to generate.
 * @param database      Optional [Database] used to save the score and fetch player name.
 * @param playerId      Optional DB row ID of the active player; required to save the score.
 */
@Composable
fun GameScreen(
    navController: NavController,
    gameMode: GameMode,
    database: Database? = null,
    playerId: Long? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val totalQuestions = 10

    var currentQuestionData by remember { mutableStateOf<GameQuestion?>(null) }
    var userAnswer by remember { mutableStateOf("") }

    var currentQuestion by remember { mutableStateOf(1) }
    var score by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreSaved by remember { mutableStateOf(false) }
    
    var playerName by remember { mutableStateOf<String?>(null) }
    
    // Get localized strings
    val gameModeTitle = when (gameMode) {
        GameMode.ADD_SUBTRACT -> stringResource(Res.string.fragment_modes_add_sub)
        GameMode.MULTIPLY_DIVIDE -> stringResource(Res.string.fragment_modes_mul_div)
        GameMode.DIVISIBILITY -> stringResource(Res.string.fragment_modes_divisibility)
        GameMode.UNIT_CONVERSION -> stringResource(Res.string.fragment_modes_units)
        GameMode.MULTIPLICATION_TABLE -> stringResource(Res.string.fragment_modes_table)
    }
    val questionCounterText = stringResource(Res.string.question_counter, currentQuestion, totalQuestions)
    val yesText = stringResource(Res.string.yes)
    val noText = stringResource(Res.string.no)
    
    // Load player name
    LaunchedEffect(playerId) {
        if (playerId != null && database != null) {
            try {
                val player = database.databaseQueries.getPlayerById(playerId).executeAsOneOrNull()
                playerName = player?.nickname
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun newQuestion() {
        currentQuestionData = generateQuestionForMode(gameMode)
        userAnswer = ""
    }
    
    // Save score when game is over
    fun saveScore() {
        if (!scoreSaved && playerId != null && database != null) {
            coroutineScope.launch {
                try {
                    val player = database.databaseQueries.getPlayerById(playerId).executeAsOneOrNull()
                    if (player != null) {
                        val newHighScore = player.high_score + score.toLong()
                        database.databaseQueries.updatePlayerScore(newHighScore, playerId)
                        scoreSaved = true
                    }
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
        }
    }

    // First question
    if (currentQuestionData == null) {
        newQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header section
        if (playerName != null) {
            Text(
                text = playerName!!,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = gameModeTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { currentQuestion / totalQuestions.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )

        Spacer(Modifier.height(8.dp))

        if (gameOver) {
            LaunchedEffect(gameOver) {
                saveScore()
            }
            
            GameOverContent(
                score = score,
                totalQuestions = totalQuestions,
                playerName = playerName,
                scoreSaved = scoreSaved,
                onPlayAgain = {
                    currentQuestion = 1
                    score = 0
                    gameOver = false
                    scoreSaved = false
                    newQuestion()
                },
                onBack = { navController.popBackStack() }
            )
            return@Column
        }

        // Question display
        Text(
            text = questionCounterText,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(Modifier.height(16.dp))

        // Question text - larger display
        Text(
            text = currentQuestionData?.questionText ?: "",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Answer display
        Text(
            text = if (userAnswer.isEmpty()) "?" else userAnswer,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.weight(1f))

        // Keyboard section - takes remaining space
        if (currentQuestionData?.answerType == AnswerType.YES_NO) {
            YesNoButtons(
                onYes = {
                    userAnswer = "YES"
                    if (currentQuestionData?.correctAnswer == "YES") score++
                    if (currentQuestion == totalQuestions) {
                        gameOver = true
                    } else {
                        currentQuestion++
                        newQuestion()
                    }
                },
                onNo = {
                    userAnswer = "NO"
                    if (currentQuestionData?.correctAnswer == "NO") score++
                    if (currentQuestion == totalQuestions) {
                        gameOver = true
                    } else {
                        currentQuestion++
                        newQuestion()
                    }
                }
            )
        } else {
            FullScreenNumberPad(
                onNumberClick = { userAnswer += it },
                onDelete = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1) },
                onClear = { userAnswer = "" },
                onSubmit = {
                    if (userAnswer.isNotEmpty()) {
                        if (userAnswer == currentQuestionData?.correctAnswer) {
                            score++
                        }
                        if (currentQuestion == totalQuestions) {
                            gameOver = true
                        } else {
                            currentQuestion++
                            newQuestion()
                        }
                    }
                },
                canSubmit = userAnswer.isNotEmpty()
            )
        }
        
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * Post-game results screen shown after all questions are answered in [GameScreen].
 *
 * Displays the final score, a performance label (Excellent / Great / Good / Practice), an
 * optional score-saved confirmation, and Play Again / Back buttons.
 *
 * @param score          Number of correct answers (0–[totalQuestions]).
 * @param totalQuestions Total questions in the session (always 10).
 * @param playerName     Active player's nickname; `null` when no profile is active.
 * @param scoreSaved     Whether the score has already been committed to the database.
 * @param onPlayAgain    Resets all state and starts a new game session.
 * @param onBack         Pops the back-stack to the previous screen.
 */
@Composable
fun GameOverContent(
    score: Int,
    totalQuestions: Int,
    playerName: String?,
    scoreSaved: Boolean,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    val gameOverText = stringResource(Res.string.game_over)
    val resultExcellentText = stringResource(Res.string.result_excellent)
    val resultGreatText = stringResource(Res.string.result_great)
    val resultGoodText = stringResource(Res.string.result_good)
    val resultPracticeText = stringResource(Res.string.result_practice)
    val playAgainText = stringResource(Res.string.play_again)
    val backText = stringResource(Res.string.back)
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = gameOverText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "$score / $totalQuestions",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(8.dp))
        
        val percentage = (score.toFloat() / totalQuestions * 100).toInt()
        Text(
            text = when {
                percentage >= 90 -> resultExcellentText
                percentage >= 70 -> resultGreatText
                percentage >= 50 -> resultGoodText
                else -> resultPracticeText
            },
            style = MaterialTheme.typography.titleLarge
        )
        
        if (scoreSaved && playerName != null) {
            Spacer(Modifier.height(8.dp))
            val scoreSavedText = stringResource(Res.string.score_saved, playerName)
            Text(
                text = scoreSavedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
        ) {
            Text(playAgainText, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(backText, fontSize = 18.sp)
        }
    }
}

/**
 * Two large YES / NO buttons for [AnswerType.YES_NO] questions in [GameScreen].
 *
 * @param onYes Called when the player taps YES.
 * @param onNo  Called when the player taps NO.
 */
@Composable
fun YesNoButtons(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    val yesText = stringResource(Res.string.yes)
    val noText = stringResource(Res.string.no)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onYes,
            modifier = Modifier
                .weight(1f)
                .height(120.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = yesText.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Button(
            onClick = onNo,
            modifier = Modifier
                .weight(1f)
                .height(120.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = noText.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Full-width 5-row numeric keypad used in [GameScreen] for [AnswerType.NUMBER] questions.
 *
 * Rows: `1 2 3` / `4 5 6` / `7 8 9` / `C 0 ⌫` / `- [Check]`.
 * The Submit button is enabled only when [canSubmit] is `true`.
 *
 * @param onNumberClick Called with a digit string when a digit key is tapped.
 * @param onDelete      Called on backspace (⌫) tap.
 * @param onClear       Called on clear (C) tap.
 * @param onSubmit      Called when the Submit/Check button is tapped.
 * @param canSubmit     Whether the Submit button should be enabled.
 */
@Composable
fun FullScreenNumberPad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean
) {
    val checkText = stringResource(Res.string.check)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: 1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FullWidthKeyButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            FullWidthKeyButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            FullWidthKeyButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        
        // Row 2: 4 5 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FullWidthKeyButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            FullWidthKeyButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            FullWidthKeyButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        
        // Row 3: 7 8 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FullWidthKeyButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            FullWidthKeyButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            FullWidthKeyButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        
        // Row 4: Clear 0 Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FullWidthKeyButton("C", Modifier.weight(1f), backgroundColor = Color(0xFFFF9800), contentColor = Color.White) { onClear() }
            FullWidthKeyButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            FullWidthKeyButton("⌫", Modifier.weight(1f), backgroundColor = Color(0xFFF44336), contentColor = Color.White) { onDelete() }
        }
        
        // Row 5: Minus and Submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FullWidthKeyButton("-", Modifier.weight(1f)) { onNumberClick("-") }
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .weight(2f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = checkText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * A single 64 dp tall key button used inside [FullScreenNumberPad].
 *
 * @param text            Label shown on the key.
 * @param modifier        Modifier applied to the button (typically `Modifier.weight(1f)`).
 * @param backgroundColor Key background; defaults to `primaryContainer`.
 * @param contentColor    Key text color; defaults to `onPrimaryContainer`.
 * @param onClick         Click handler.
 */
@Composable
fun FullWidthKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Keep old NumberPad for backwards compatibility
