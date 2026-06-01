package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.answer_submitted
import rutedu.composeapp.generated.resources.back_to_menu
import rutedu.composeapp.generated.resources.battle_game_ended
import rutedu.composeapp.generated.resources.both_correct
import rutedu.composeapp.generated.resources.both_wrong
import rutedu.composeapp.generated.resources.check
import rutedu.composeapp.generated.resources.correct
import rutedu.composeapp.generated.resources.its_a_tie
import rutedu.composeapp.generated.resources.no
import rutedu.composeapp.generated.resources.play_again
import rutedu.composeapp.generated.resources.player_wins
import rutedu.composeapp.generated.resources.round_counter
import rutedu.composeapp.generated.resources.waiting
import rutedu.composeapp.generated.resources.wrong
import rutedu.composeapp.generated.resources.yes

/**
 * A single round question in the two-player PvP battle.
 *
 * @property question      The arithmetic expression shown to both players (e.g. `"7 + 5"`).
 * @property correctAnswer The expected integer result.
 */
data class PvPQuestion(
    val question: String,
    val correctAnswer: Int
)

/**
 * Two-player head-to-head arithmetic battle screen.
 *
 * The screen is split horizontally into two [PlayerGameArea]s. Player 2's half is rotated 180°
 * so both players face each other on the same device. Each of the 10 rounds shows the same
 * arithmetic question simultaneously; the player who is the only one correct scores a point
 * (both correct -> both score; both wrong -> neither scores).
 *
 * @param navController Navigation controller for the post-game "Back to menu" pop.
 * @param player1Name   Display name for player 1 (green, bottom, normal orientation).
 * @param player2Name   Display name for player 2 (blue, top, rotated 180°).
 */
@Composable
fun PvPBattleScreen(
    navController: NavController,
    player1Name: String = "Player 1",
    player2Name: String = "Player 2"
) {
    var player1Score by remember { mutableStateOf(0) }
    var player2Score by remember { mutableStateOf(0) }
    var currentQuestion by remember { mutableStateOf<PvPQuestion?>(null) }
    var player1Answer by remember { mutableStateOf("") }
    var player2Answer by remember { mutableStateOf("") }
    var player1Answered by remember { mutableStateOf(false) }
    var player2Answered by remember { mutableStateOf(false) }
    var roundNumber by remember { mutableStateOf(1) }
    var gameOver by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var lastRoundWinner by remember { mutableStateOf<Int?>(null) }
    
    val totalRounds = 10
    
    val gameEndedText = stringResource(Res.string.battle_game_ended)
    val yesText = stringResource(Res.string.yes)
    val noText = stringResource(Res.string.no)
    val playAgainText = stringResource(Res.string.play_again)
    val backToMenuText = stringResource(Res.string.back_to_menu)
    val itsATieText = stringResource(Res.string.its_a_tie)
    val bothCorrectText = stringResource(Res.string.both_correct)
    val bothWrongText = stringResource(Res.string.both_wrong)
    val waitingText = stringResource(Res.string.waiting)
    val correctText = stringResource(Res.string.correct)
    val wrongText = stringResource(Res.string.wrong)
    val answerSubmittedText = stringResource(Res.string.answer_submitted)
    val checkText = stringResource(Res.string.check)
    
    fun generateNewQuestion(): PvPQuestion {
        val a = (1..12).random()
        val b = (1..12).random()
        val operators = listOf("+", "-", "×")
        val operator = operators.random()
        
        return when (operator) {
            "+" -> PvPQuestion("$a + $b", a + b)
            "-" -> PvPQuestion("$a - $b", a - b)
            "×" -> PvPQuestion("$a × $b", a * b)
            else -> PvPQuestion("$a + $b", a + b)
        }
    }
    
    // Initialize first question
    LaunchedEffect(Unit) {
        currentQuestion = generateNewQuestion()
    }
    
    // Check if both players answered
    LaunchedEffect(player1Answered, player2Answered) {
        if (player1Answered && player2Answered && currentQuestion != null) {
            showResult = true
            
            val p1Correct = player1Answer.toIntOrNull() == currentQuestion!!.correctAnswer
            val p2Correct = player2Answer.toIntOrNull() == currentQuestion!!.correctAnswer
            
            when {
                p1Correct && !p2Correct -> {
                    player1Score++
                    lastRoundWinner = 1
                }
                !p1Correct && p2Correct -> {
                    player2Score++
                    lastRoundWinner = 2
                }
                p1Correct && p2Correct -> {
                    // Both correct - both get a point
                    player1Score++
                    player2Score++
                    lastRoundWinner = 0
                }
                else -> {
                    lastRoundWinner = -1 // Both wrong
                }
            }
            
            delay(2000)
            
            if (roundNumber >= totalRounds) {
                gameOver = true
            } else {
                roundNumber++
                currentQuestion = generateNewQuestion()
                player1Answer = ""
                player2Answer = ""
                player1Answered = false
                player2Answered = false
                showResult = false
                lastRoundWinner = null
            }
        }
    }
    
    if (gameOver) {
        // Game Over Screen - mimics solo game style
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉 $gameEndedText 🎉",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = when {
                    player1Score > player2Score -> stringResource(Res.string.player_wins, player1Name)
                    player2Score > player1Score -> stringResource(Res.string.player_wins, player2Name)
                    else -> itsATieText
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = player1Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "$player1Score",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = player2Name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "$player2Score",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = {
                    player1Score = 0
                    player2Score = 0
                    roundNumber = 1
                    gameOver = false
                    player1Answer = ""
                    player2Answer = ""
                    player1Answered = false
                    player2Answered = false
                    currentQuestion = generateNewQuestion()
                },
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text(playAgainText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(backToMenuText, fontSize = 18.sp)
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Player 2 Area (Top - Rotated 180 degrees)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF2196F3).copy(alpha = 0.05f))
                .rotate(180f)
        ) {
            PlayerGameArea(
                playerName = player2Name,
                score = player2Score,
                question = currentQuestion?.question ?: "",
                answer = player2Answer,
                hasAnswered = player2Answered,
                showResult = showResult,
                isCorrect = player2Answer.toIntOrNull() == currentQuestion?.correctAnswer,
                onNumberClick = { if (!player2Answered) player2Answer += it },
                onDelete = { if (!player2Answered && player2Answer.isNotEmpty()) player2Answer = player2Answer.dropLast(1) },
                onClear = { if (!player2Answered) player2Answer = "" },
                onSubmit = { player2Answered = true },
                playerColor = Color(0xFF2196F3)
            )
        }
        
        // Divider with round info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$player2Score",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.round_counter, roundNumber, totalRounds),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (showResult && lastRoundWinner != null) {
                        Text(
                            text = when (lastRoundWinner) {
                                1 -> "$player1Name ✓"
                                2 -> "$player2Name ✓"
                                0 -> bothCorrectText
                                else -> bothWrongText
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (lastRoundWinner) {
                                1 -> Color(0xFF4CAF50)
                                2 -> Color(0xFF2196F3)
                                0 -> Color(0xFF4CAF50)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
                
                Text(
                    text = "$player1Score",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
        
        // Player 1 Area (Bottom - Normal orientation)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF4CAF50).copy(alpha = 0.05f))
        ) {
            PlayerGameArea(
                playerName = player1Name,
                score = player1Score,
                question = currentQuestion?.question ?: "",
                answer = player1Answer,
                hasAnswered = player1Answered,
                showResult = showResult,
                isCorrect = player1Answer.toIntOrNull() == currentQuestion?.correctAnswer,
                onNumberClick = { if (!player1Answered) player1Answer += it },
                onDelete = { if (!player1Answered && player1Answer.isNotEmpty()) player1Answer = player1Answer.dropLast(1) },
                onClear = { if (!player1Answered) player1Answer = "" },
                onSubmit = { player1Answered = true },
                playerColor = Color(0xFF4CAF50)
            )
        }
    }
}

/**
 * One player's half of the [PvPBattleScreen]: name, score, question, answer display, and keypad.
 *
 * Replaces the keypad with a "waiting" label once [hasAnswered] is `true`, then shows
 * correct/wrong feedback when [showResult] flips to `true`.
 *
 * @param playerName    Display name shown at the top.
 * @param score         Current accumulated score for this player.
 * @param question      Arithmetic expression string to solve.
 * @param answer        The answer the player has typed so far.
 * @param hasAnswered   Whether the player has already submitted an answer this round.
 * @param showResult    Whether to reveal the correct/wrong result (after both players answer).
 * @param isCorrect     Whether this player's submitted answer matched the correct answer.
 * @param onNumberClick Called with a digit string when a digit key is tapped.
 * @param onDelete      Called on backspace tap.
 * @param onClear       Called on clear tap.
 * @param onSubmit      Called when the Submit/Check button is tapped.
 * @param playerColor   Accent color for this player's UI elements.
 */
@Composable
fun PlayerGameArea(
    playerName: String,
    score: Int,
    question: String,
    answer: String,
    hasAnswered: Boolean,
    showResult: Boolean,
    isCorrect: Boolean,
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    playerColor: Color
) {
    val waitingText = stringResource(Res.string.waiting)
    val correctText = stringResource(Res.string.correct)
    val wrongText = stringResource(Res.string.wrong)
    val answerSubmittedText = stringResource(Res.string.answer_submitted)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header: Player name and score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium,
                color = playerColor,
                fontWeight = FontWeight.Bold
            )
            if (hasAnswered && !showResult) {
                Text(
                    text = waitingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        // Question display - similar to solo game
        Text(
            text = question,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        // Answer display - similar to solo game
        Text(
            text = if (answer.isEmpty()) "?" else answer,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = when {
                showResult && isCorrect -> Color(0xFF4CAF50)
                showResult && !isCorrect -> Color(0xFFF44336)
                else -> playerColor
            }
        )
        
        // Result indicator
        if (showResult) {
            Text(
                text = if (isCorrect) correctText else wrongText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
        
        // Number pad - full width like solo game
        if (!hasAnswered) {
            PvPNumberPad(
                onNumberClick = onNumberClick,
                onDelete = onDelete,
                onClear = onClear,
                onSubmit = onSubmit,
                canSubmit = answer.isNotEmpty(),
                accentColor = playerColor
            )
        } else if (!showResult) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = answerSubmittedText,
                    style = MaterialTheme.typography.titleMedium,
                    color = playerColor
                )
            }
        }
    }
}

/**
 * Compact 5-row numeric keypad for [PlayerGameArea] in the split-screen battle layout.
 *
 * Mirrors [FullScreenNumberPad] but uses 40 dp keys to fit each player's half of the screen.
 *
 * @param onNumberClick Called with a digit string when a digit key is tapped.
 * @param onDelete      Called on backspace (⌫) tap.
 * @param onClear       Called on clear (C) tap.
 * @param onSubmit      Called when the Submit/Check button is tapped.
 * @param canSubmit     Whether the Submit button should be enabled.
 * @param accentColor   Player accent color applied to the Submit button background.
 */
@Composable
fun PvPNumberPad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean,
    accentColor: Color
) {
    val checkText = stringResource(Res.string.check)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Row 1: 1 2 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PvPKeyButton("1", Modifier.weight(1f)) { onNumberClick("1") }
            PvPKeyButton("2", Modifier.weight(1f)) { onNumberClick("2") }
            PvPKeyButton("3", Modifier.weight(1f)) { onNumberClick("3") }
        }
        
        // Row 2: 4 5 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PvPKeyButton("4", Modifier.weight(1f)) { onNumberClick("4") }
            PvPKeyButton("5", Modifier.weight(1f)) { onNumberClick("5") }
            PvPKeyButton("6", Modifier.weight(1f)) { onNumberClick("6") }
        }
        
        // Row 3: 7 8 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PvPKeyButton("7", Modifier.weight(1f)) { onNumberClick("7") }
            PvPKeyButton("8", Modifier.weight(1f)) { onNumberClick("8") }
            PvPKeyButton("9", Modifier.weight(1f)) { onNumberClick("9") }
        }
        
        // Row 4: C 0 ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PvPKeyButton("C", Modifier.weight(1f), backgroundColor = Color(0xFFFF9800), contentColor = Color.White) { onClear() }
            PvPKeyButton("0", Modifier.weight(1f)) { onNumberClick("0") }
            PvPKeyButton("⌫", Modifier.weight(1f), backgroundColor = Color(0xFFF44336), contentColor = Color.White) { onDelete() }
        }
        
        // Row 5: Minus and Submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PvPKeyButton("-", Modifier.weight(1f)) { onNumberClick("-") }
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .weight(2f)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = checkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * A single 40 dp tall key button used inside [PvPNumberPad].
 *
 * @param text            Label shown on the key.
 * @param modifier        Modifier applied to the button (typically `Modifier.weight(1f)`).
 * @param backgroundColor Key background; defaults to `primaryContainer`.
 * @param contentColor    Key text color; defaults to `onPrimaryContainer`.
 * @param onClick         Click handler.
 */
@Composable
fun PvPKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
