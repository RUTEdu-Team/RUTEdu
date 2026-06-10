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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import rutedu.composeapp.generated.resources.correct
import rutedu.composeapp.generated.resources.its_a_tie
import rutedu.composeapp.generated.resources.lesson_no_questions
import rutedu.composeapp.generated.resources.play_again
import rutedu.composeapp.generated.resources.player_wins
import rutedu.composeapp.generated.resources.round_counter
import rutedu.composeapp.generated.resources.waiting
import rutedu.composeapp.generated.resources.wrong
import prz.rutedu.app.data.QuestionBank
import prz.rutedu.app.data.SubjectRepository
import prz.rutedu.app.models.Question
import kotlin.random.Random

/** Per-round answer state of a single player. */
private enum class PlayerRoundState { ANSWERING, CORRECT, WRONG }

private val Player1Color = Color(0xFF4CAF50)
private val Player2Color = Color(0xFF2196F3)

/**
 * Two-player head-to-head battle on any lesson from the app (Pojedynek).
 *
 * The screen is split horizontally into two player areas. Player 2's half is rotated 180°
 * so both players face each other on the same device. Each round, both players get their own
 * interactive instance of the same [Question] (rendered via the shared [QuestionContent]
 * dispatcher, so every question type from every subject works). A player's half locks with a
 * "Waiting..." overlay once they answer; when both have answered, each correct answer scores
 * a point and the next round starts.
 *
 * Rounds are capped at [MAX_ROUNDS]. No solo progress is saved (LessonProgressStore untouched).
 *
 * @param subjectId     Subject the lesson belongs to (drives the accent color).
 * @param lessonId      Lesson whose question set is used for the battle.
 * @param navController Navigation controller for the post-game "Back to menu" pop.
 * @param player1Name   Display name for player 1 (green, bottom, normal orientation).
 * @param player2Name   Display name for player 2 (blue, top, rotated 180°).
 */
@Composable
fun PvPBattleScreen(
    subjectId: String,
    lessonId: String,
    navController: NavController,
    player1Name: String = "Player 1",
    player2Name: String = "Player 2"
) {
    val subject = SubjectRepository.getById(subjectId)
    val accentColor = subject?.color ?: Color(0xFF4A80F0)

    var seed by remember { mutableStateOf(Random.Default.nextLong()) }
    val questions = remember(seed) {
        // Periodic-table questions are filtered out as a safety net - the picker already
        // excludes such lessons, but generated sets vary with the seed.
        QuestionBank.questionsFor(lessonId, seed)
            .filterNot(::isPeriodicTableQuestion)
            .shuffled(Random(seed))
            .take(MAX_ROUNDS)
    }
    val totalRounds = questions.size

    var roundIndex by remember { mutableStateOf(0) }
    var player1Score by remember { mutableStateOf(0) }
    var player2Score by remember { mutableStateOf(0) }
    var player1State by remember { mutableStateOf(PlayerRoundState.ANSWERING) }
    var player2State by remember { mutableStateOf(PlayerRoundState.ANSWERING) }
    var showResult by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    // Round resolution: when both players answered, score, briefly show results, then advance.
    LaunchedEffect(player1State, player2State) {
        if (player1State != PlayerRoundState.ANSWERING && player2State != PlayerRoundState.ANSWERING) {
            showResult = true
            if (player1State == PlayerRoundState.CORRECT) player1Score++
            if (player2State == PlayerRoundState.CORRECT) player2Score++
            delay(2000)
            if (roundIndex >= totalRounds - 1) {
                gameOver = true
            } else {
                roundIndex++
                player1State = PlayerRoundState.ANSWERING
                player2State = PlayerRoundState.ANSWERING
                showResult = false
            }
        }
    }

    if (questions.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(Res.string.lesson_no_questions),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text(stringResource(Res.string.back_to_menu))
            }
        }
        return
    }

    if (gameOver) {
        BattleResultScreen(
            player1Name = player1Name,
            player2Name = player2Name,
            player1Score = player1Score,
            player2Score = player2Score,
            onPlayAgain = {
                seed = Random.Default.nextLong()
                roundIndex = 0
                player1Score = 0
                player2Score = 0
                player1State = PlayerRoundState.ANSWERING
                player2State = PlayerRoundState.ANSWERING
                showResult = false
                gameOver = false
            },
            onBack = { navController.popBackStack() }
        )
        return
    }

    val question = questions[roundIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Player 2 area (top, rotated 180° so the opponent can play face-to-face)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(180f)
                .background(Player2Color.copy(alpha = 0.05f))
        ) {
            PlayerBattleArea(
                playerName = player2Name,
                playerColor = Player2Color,
                score = player2Score,
                roundIndex = roundIndex,
                totalRounds = totalRounds,
                question = question,
                accentColor = accentColor,
                state = player2State,
                showResult = showResult,
                onAnswered = { correct ->
                    player2State = if (correct) PlayerRoundState.CORRECT else PlayerRoundState.WRONG
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
        )

        // Player 1 area (bottom, normal orientation)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Player1Color.copy(alpha = 0.05f))
        ) {
            PlayerBattleArea(
                playerName = player1Name,
                playerColor = Player1Color,
                score = player1Score,
                roundIndex = roundIndex,
                totalRounds = totalRounds,
                question = question,
                accentColor = accentColor,
                state = player1State,
                showResult = showResult,
                onAnswered = { correct ->
                    player1State = if (correct) PlayerRoundState.CORRECT else PlayerRoundState.WRONG
                }
            )
        }
    }
}

private const val MAX_ROUNDS = 10

/**
 * One player's half of the battle screen: a header with name/score/round counter and an
 * independent [QuestionContent] instance. Once the player answers (or skips - counted as
 * wrong), the area is covered by a waiting/result overlay until the round resolves.
 */
@Composable
private fun PlayerBattleArea(
    playerName: String,
    playerColor: Color,
    score: Int,
    roundIndex: Int,
    totalRounds: Int,
    question: Question,
    accentColor: Color,
    state: PlayerRoundState,
    showResult: Boolean,
    onAnswered: (correct: Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(playerColor.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = playerName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = playerColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(Res.string.round_counter, roundIndex + 1, totalRounds),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = playerColor
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            // key() resets the content's internal answer state on every new round.
            key(roundIndex) {
                QuestionContent(
                    question = question,
                    accentColor = accentColor,
                    bottomPadding = 0.dp,
                    onCorrect = { if (state == PlayerRoundState.ANSWERING) onAnswered(true) },
                    onWrong = { if (state == PlayerRoundState.ANSWERING) onAnswered(false) },
                    onSkip = { if (state == PlayerRoundState.ANSWERING) onAnswered(false) }
                )
            }

            if (state != PlayerRoundState.ANSWERING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (showResult) {
                            Text(
                                text = if (state == PlayerRoundState.CORRECT)
                                    stringResource(Res.string.correct)
                                else
                                    stringResource(Res.string.wrong),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state == PlayerRoundState.CORRECT) Player1Color else Color(0xFFE53935)
                            )
                        } else {
                            Text(
                                text = stringResource(Res.string.answer_submitted),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = playerColor
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.waiting),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Post-battle results: winner banner, both scores, replay and back-to-menu actions. */
@Composable
private fun BattleResultScreen(
    player1Name: String,
    player2Name: String,
    player1Score: Int,
    player2Score: Int,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
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
            text = "🎉 ${stringResource(Res.string.battle_game_ended)} 🎉",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = when {
                player1Score > player2Score -> stringResource(Res.string.player_wins, player1Name)
                player2Score > player1Score -> stringResource(Res.string.player_wins, player2Name)
                else -> stringResource(Res.string.its_a_tie)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
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
                    color = Player1Color
                )
                Text(
                    text = "$player1Score",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Player1Color
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = player2Name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Player2Color
                )
                Text(
                    text = "$player2Score",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Player2Color
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Player1Color,
                contentColor = Color.White
            )
        ) {
            Text(stringResource(Res.string.play_again), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(stringResource(Res.string.back_to_menu), fontSize = 18.sp)
        }
    }
}
