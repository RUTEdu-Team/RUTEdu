package com.example.myapplication.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.model.GamePhase
import com.example.myapplication.multiplayer.model.MultiplayerMode
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val OrangeLobby = Color(0xFFF47B20)

@Composable
fun PvPLobbyHostScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    val state = MultiplayerSessionManager.state

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.IN_ROUND || state.phase == GamePhase.COUNTDOWN) {
            navController.navigate(Screen.PvPNetworkBattle.route) {
                popUpTo(Screen.PvPLobbyHost.route) { inclusive = true }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Nothing — session managed by user explicitly closing
        }
    }

    val maxPlayers = if (state.gameMode == MultiplayerMode.ONE_VS_ONE) 2 else 4
    val canStart = state.players.size >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text("Lobby hosta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        Text(
            text = if (state.gameMode == MultiplayerMode.ONE_VS_ONE) "Tryb: 1 vs 1" else "Tryb: Turniej (maks. $maxPlayers graczy)",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E)
        )

        Spacer(Modifier.height(32.dp))

        // Waiting animation + info
        WaitingPulse()

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Poczekaj, aż gracze dołączą",
            fontSize = 15.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Gracze (${state.players.size}/$maxPlayers)",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9E9E9E)
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.players) { nickname ->
                PlayerLobbyRow(nickname = nickname, isHost = nickname == state.myNickname)
            }
        }

        state.errorMessage?.let { err ->
            Spacer(Modifier.height(8.dp))
            Text(err, color = Color(0xFFD32F2F), fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { MultiplayerSessionManager.hostStartGame() },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeLobby)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Rozpocznij grę", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                MultiplayerSessionManager.cleanup()
                navController.popBackStack(Screen.MultiplayerMenu.route, false)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Anuluj", fontSize = 15.sp)
        }

        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}

@Composable
private fun WaitingPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(OrangeLobby.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(OrangeLobby),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun PlayerLobbyRow(nickname: String, isHost: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F6FA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OrangeLobby.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nickname.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = OrangeLobby,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(nickname, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A), fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (isHost) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeLobby.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("HOST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeLobby)
            }
        }
    }
}
