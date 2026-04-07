package com.example.myapplication.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.model.DiscoveredHost
import com.example.myapplication.multiplayer.model.GamePhase
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val OrangeClient = Color(0xFFF47B20)

@Composable
fun PvPLobbyClientScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    val state = MultiplayerSessionManager.state

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.IN_ROUND) {
            navController.navigate(Screen.PvPNetworkBattle.route) {
                popUpTo(Screen.PvPLobbyClient.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Szukaj gry", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        Text("Upewnij się, że jesteś w tej samej sieci WiFi co host.", fontSize = 14.sp, color = Color(0xFF9E9E9E))

        Spacer(Modifier.height(32.dp))

        if (state.phase == GamePhase.DISCOVERING) {
            ScanningIndicator()
            Spacer(Modifier.height(8.dp))
            Text(
                "Skanowanie...",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )
            Spacer(Modifier.height(32.dp))

            Text(
                "Znalezione gry (${state.discoveredHosts.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9E9E9E)
            )
            Spacer(Modifier.height(12.dp))

            if (state.discoveredHosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F6FA))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Brak gier w pobliżu.\nUpewnij się, że host uruchomił grę.",
                        color = Color(0xFF9E9E9E),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.discoveredHosts) { host ->
                    DiscoveredHostRow(host = host) {
                        MultiplayerSessionManager.joinHost(host)
                    }
                }
            }
        }

        if (state.phase == GamePhase.LOBBY_CLIENT) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ScanningIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Połączono! Czekam na start...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Gracze: ${state.players.joinToString(", ")}",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }

        state.errorMessage?.let { err ->
            Text(err, color = Color(0xFFD32F2F), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

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
private fun ScanningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(OrangeClient.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SignalWifi4Bar,
                contentDescription = null,
                tint = OrangeClient,
                modifier = Modifier.size(36.dp).rotate(rotation)
            )
        }
    }
}

@Composable
private fun DiscoveredHostRow(host: DiscoveredHost, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F6FA))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OrangeClient.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = host.name.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = OrangeClient,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(host.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A), fontSize = 15.sp)
            Text(
                "${host.currentPlayers}/${host.maxPlayers} graczy · ${host.mode}",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeClient)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Dołącz", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
