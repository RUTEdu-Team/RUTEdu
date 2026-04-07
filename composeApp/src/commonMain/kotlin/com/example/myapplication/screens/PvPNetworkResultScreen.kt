package com.example.myapplication.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.myapplication.multiplayer.model.PlayerFinalResult
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val OrangeResult = Color(0xFFF47B20)
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)

@Composable
fun PvPNetworkResultScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    val state = MultiplayerSessionManager.state
    val ranking = state.finalRanking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = GoldColor,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Koniec gry!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        ranking.firstOrNull()?.let { winner ->
            Text(
                text = if (winner.nickname == state.myNickname) "Wygrałeś! 🎉" else "Wygrał: ${winner.nickname}",
                fontSize = 16.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        // Podium
        if (ranking.size >= 2) {
            PodiumSection(ranking = ranking, myNickname = state.myNickname)
        }

        Spacer(Modifier.height(32.dp))

        // Full ranking list
        if (ranking.size > 3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F6FA))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ranking.drop(3).forEach { result ->
                    RankingRow(result = result, myNickname = state.myNickname)
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val mode = state.gameMode
                val subjectId = state.selectedSubjectId
                val topicId = state.selectedTopicId
                MultiplayerSessionManager.cleanup()
                navController.navigate(Screen.PvPSetup.createRoute(mode.name)) {
                    popUpTo(Screen.MultiplayerMenu.route)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeResult)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Zagraj ponownie", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                MultiplayerSessionManager.cleanup()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Menu główne", fontSize = 15.sp)
        }

        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}

@Composable
private fun PodiumSection(ranking: List<PlayerFinalResult>, myNickname: String) {
    // Show top 3 as podium (2nd | 1st | 3rd)
    val first = ranking.getOrNull(0)
    val second = ranking.getOrNull(1)
    val third = ranking.getOrNull(2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        second?.let { PodiumBlock(result = it, height = 90.dp, medalColor = SilverColor, myNickname = myNickname) }
        Spacer(Modifier.width(8.dp))
        // 1st place
        first?.let { PodiumBlock(result = it, height = 120.dp, medalColor = GoldColor, myNickname = myNickname) }
        Spacer(Modifier.width(8.dp))
        // 3rd place
        third?.let { PodiumBlock(result = it, height = 70.dp, medalColor = BronzeColor, myNickname = myNickname) }
    }
}

@Composable
private fun PodiumBlock(
    result: PlayerFinalResult,
    height: Dp,
    medalColor: Color,
    myNickname: String
) {
    val isMe = result.nickname == myNickname
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isMe) OrangeResult else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                result.nickname.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = if (isMe) Color.White else Color(0xFF9E9E9E),
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = result.nickname.take(8),
            fontSize = 12.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = if (isMe) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${result.totalScore} pkt",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(medalColor.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${result.rank}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RankingRow(result: PlayerFinalResult, myNickname: String) {
    val isMe = result.nickname == myNickname
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${result.rank}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = if (isMe) "Ty" else result.nickname,
            fontSize = 14.sp,
            fontWeight = if (isMe) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isMe) OrangeResult else Color(0xFF1A1A1A),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${result.totalScore} pkt",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )
    }
}
