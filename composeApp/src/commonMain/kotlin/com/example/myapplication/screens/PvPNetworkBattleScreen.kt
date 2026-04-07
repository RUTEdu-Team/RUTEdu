package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.model.GamePhase
import com.example.myapplication.multiplayer.model.PvPQuestionType
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val OrangeBattle = Color(0xFFF47B20)
private val GreenCorrect = Color(0xFF4CAF50)
private val RedWrong = Color(0xFFD32F2F)

@Composable
fun PvPNetworkBattleScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    val state = MultiplayerSessionManager.state

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.GAME_OVER) {
            navController.navigate(Screen.PvPNetworkResult.route) {
                popUpTo(Screen.PvPNetworkBattle.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Mini scoreboard
        MiniScoreboard(
            players = state.players,
            scores = state.scores,
            myNickname = state.myNickname
        )

        // Round + timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Runda ${state.currentRound} / ${state.totalRounds}",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.Medium
            )
            CircularTimer(
                timeLeftMs = state.timeLeftMs,
                totalMs = state.timePerRoundMs
            )
        }

        Spacer(Modifier.height(8.dp))

        // Question
        val question = state.currentQuestion
        if (question != null && state.phase == GamePhase.IN_ROUND) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F6FA))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = question.questionText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            val answered = state.answeredThisRound
            when (question.questionType) {
                PvPQuestionType.SELECT, PvPQuestionType.ELEMENT_CARD, PvPQuestionType.FIND_OPERATOR -> {
                    MultipleChoiceInput(
                        options = question.options,
                        answered = answered,
                        selectedAnswer = state.myAnswer,
                        onAnswer = { MultiplayerSessionManager.submitAnswer(it) }
                    )
                }
                PvPQuestionType.FIND_ANSWER, PvPQuestionType.TYPE_ANSWER -> {
                    NumericInput(
                        answered = answered,
                        currentAnswer = state.myAnswer,
                        onAnswer = { MultiplayerSessionManager.submitAnswer(it) }
                    )
                }
            }

            if (answered) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Odpowiedź wysłana! Czekam na innych...", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                }
            }
        }

        // Round result overlay
        if (state.phase == GamePhase.ROUND_RESULT) {
            RoundResultPanel(
                results = state.roundResults,
                correctAnswer = state.lastCorrectAnswer,
                myNickname = state.myNickname
            )
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}

@Composable
private fun MiniScoreboard(
    players: List<String>,
    scores: Map<String, Int>,
    myNickname: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F6FA))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        players.forEach { nickname ->
            val score = scores[nickname] ?: 0
            val isMe = nickname == myNickname
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isMe) OrangeBattle else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nickname.take(1).uppercase(),
                        color = if (isMe) Color.White else Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$score",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMe) OrangeBattle else Color(0xFF1A1A1A)
                )
                Text(
                    text = nickname.take(6),
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CircularTimer(timeLeftMs: Long, totalMs: Long) {
    val fraction = if (totalMs > 0) (timeLeftMs.toFloat() / totalMs).coerceIn(0f, 1f) else 0f
    val seconds = (timeLeftMs / 1000).coerceAtLeast(0)
    val color = when {
        fraction > 0.5f -> GreenCorrect
        fraction > 0.25f -> OrangeBattle
        else -> RedWrong
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(3.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$seconds",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MultipleChoiceInput(
    options: List<String>,
    answered: Boolean,
    selectedAnswer: String,
    onAnswer: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        itemsIndexed(options) { index, option ->
            val isSelected = selectedAnswer == index.toString()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.2f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) OrangeBattle
                        else Color(0xFFF5F6FA)
                    )
                    .then(if (!answered) Modifier.clickable { onAnswer(index.toString()) } else Modifier)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) Color.White else Color(0xFF1A1A1A),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun NumericInput(
    answered: Boolean,
    currentAnswer: String,
    onAnswer: (String) -> Unit
) {
    var localInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF5F6FA))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (localInput.isEmpty()) "?" else localInput,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (localInput.isEmpty()) Color(0xFFBDBDBD) else Color(0xFF1A1A1A)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("-", "0", "⌫")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F6FA))
                                .then(if (!answered) Modifier.clickable {
                                    when (key) {
                                        "⌫" -> localInput = localInput.dropLast(1)
                                        "-" -> if (localInput.isEmpty()) localInput = "-" else if (!localInput.startsWith("-")) localInput = "-$localInput"
                                        else -> if (localInput.length < 6) localInput += key
                                    }
                                } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                        }
                    }
                }
            }
            // Submit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!answered && localInput.isNotEmpty()) OrangeBattle else Color(0xFFE0E0E0))
                    .then(if (!answered && localInput.isNotEmpty()) Modifier.clickable { onAnswer(localInput) } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text("Zatwierdź", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun RoundResultPanel(
    results: Map<String, com.example.myapplication.multiplayer.model.PlayerRoundResult>,
    correctAnswer: String,
    myNickname: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF5F6FA))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Poprawna odpowiedź: $correctAnswer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GreenCorrect
                )
                results.entries.sortedByDescending { it.value.totalScore }.forEach { (nickname, result) ->
                    val isMe = nickname == myNickname
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMe) OrangeBattle.copy(alpha = 0.08f) else Color.White)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMe) "Ty ($nickname)" else nickname,
                            fontWeight = if (isMe) FontWeight.SemiBold else FontWeight.Normal,
                            color = Color(0xFF1A1A1A),
                            fontSize = 14.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val resultColor = if (result.correct) GreenCorrect else RedWrong
                            Text(
                                text = result.answer.ifEmpty { "—" },
                                fontSize = 13.sp,
                                color = resultColor
                            )
                            Text(
                                text = "+${result.pointsEarned}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.pointsEarned > 0) GreenCorrect else Color(0xFF9E9E9E)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OrangeBattle.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${result.totalScore} pkt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeBattle
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
