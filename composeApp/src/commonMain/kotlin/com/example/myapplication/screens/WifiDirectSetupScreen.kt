package com.example.myapplication.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager
import com.example.myapplication.multiplayer.network.WifiDirectController
import com.example.myapplication.multiplayer.network.WifiPeerDevice

private val Orange = Color(0xFFF47B20)
private val TextPrimary = Color(0xFF1A1A1A)
private val LightBg = Color(0xFFF5F6FA)

@Composable
fun WifiDirectSetupScreen(
    isHost: Boolean,
    nickname: String,
    navController: NavController,
    bottomPadding: Dp = 0.dp
) {
    val controller = remember { WifiDirectController() }
    var statusText by remember {
        mutableStateOf(if (isHost) "Tworzenie grupy WiFi Direct..." else "Szukanie urządzeń...")
    }
    var peers by remember { mutableStateOf<List<WifiPeerDevice>>(emptyList()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    DisposableEffect(isHost) {
        if (isHost) {
            controller.createGroup(
                onReady = { statusText = "Gotowy — czekam na gracza..." },
                onError = { err -> errorText = err }
            )
        } else {
            controller.discoverPeers(
                onFound = { found ->
                    peers = found
                    statusText = if (found.isEmpty()) "Szukam urządzeń..." else "Znaleziono ${found.size} urządzeń"
                },
                onError = { err -> errorText = err }
            )
        }
        onDispose { controller.stop() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                controller.stop()
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("WiFi Direct", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "alpha"
            )
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Orange.copy(alpha = alpha * 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Wifi, null,
                    tint = Orange.copy(alpha = alpha),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(statusText, fontSize = 16.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.Medium)

            errorText?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(err, fontSize = 14.sp, color = Color(0xFFE53935))
            }

            if (!isHost && peers.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    "Urządzenia w pobliżu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9E9E9E)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(peers, key = { it.address }) { peer ->
                        PeerDeviceRow(
                            peer = peer,
                            isConnecting = isConnecting,
                            onClick = {
                                if (!isConnecting) {
                                    isConnecting = true
                                    statusText = "Łączę z ${peer.name}..."
                                    controller.connectToPeer(
                                        peer = peer,
                                        onConnected = { goIp ->
                                            MultiplayerSessionManager.initAsClientDirect(nickname, goIp)
                                            navController.navigate(Screen.PvPLobbyClient.route) {
                                                popUpTo(Screen.MultiplayerMenu.route)
                                            }
                                        },
                                        onError = { err ->
                                            isConnecting = false
                                            errorText = err
                                            statusText = "Błąd — spróbuj ponownie"
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerDeviceRow(peer: WifiPeerDevice, isConnecting: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightBg)
            .clickable(enabled = !isConnecting) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Wifi, null, tint = Orange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(peer.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Text(peer.address, fontSize = 12.sp, color = Color(0xFF9E9E9E))
        }
        Text(
            if (isConnecting) "..." else "Połącz",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Orange
        )
    }
}
