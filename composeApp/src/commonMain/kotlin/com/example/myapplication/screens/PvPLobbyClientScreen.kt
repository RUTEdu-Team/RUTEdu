package com.example.myapplication.screens

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.myapplication.BackHandlerEffect
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.model.DiscoveredHost
import com.example.myapplication.multiplayer.model.GamePhase
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val OrangeClient = Color(0xFFF47B20)

@Composable
fun PvPLobbyClientScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    val state = MultiplayerSessionManager.state

    // Password dialog state
    var pendingHost by remember { mutableStateOf<DiscoveredHost?>(null) }
    var enteredPassword by remember { mutableStateOf("") }

    fun cancelAndGoBack() {
        MultiplayerSessionManager.cleanup()
        navController.popBackStack(Screen.MultiplayerMenu.route, false)
    }

    BackHandlerEffect { cancelAndGoBack() }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentPhase by rememberUpdatedState(state.phase)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (currentPhase != GamePhase.IN_ROUND &&
                    currentPhase != GamePhase.COUNTDOWN &&
                    currentPhase != GamePhase.ROUND_RESULT &&
                    currentPhase != GamePhase.GAME_OVER
                ) {
                    MultiplayerSessionManager.cleanup()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.phase) {
        when (state.phase) {
            GamePhase.IN_ROUND -> navController.navigate(Screen.PvPNetworkBattle.route) {
                popUpTo(Screen.PvPLobbyClient.route) { inclusive = true }
            }
            GamePhase.IDLE -> navController.popBackStack(Screen.MultiplayerMenu.route, false)
            else -> {}
        }
    }

    // Password dialog
    pendingHost?.let { host ->
        AlertDialog(
            onDismissRequest = {
                pendingHost = null
                enteredPassword = ""
            },
            title = { Text("Hasło wymagane", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Lobby \"${host.name}\" jest chronione hasłem.",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = { enteredPassword = it },
                        label = { Text("Hasło") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pw = enteredPassword
                        pendingHost = null
                        enteredPassword = ""
                        MultiplayerSessionManager.joinHost(host, pw)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeClient)
                ) { Text("Dołącz") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingHost = null
                    enteredPassword = ""
                }) { Text("Anuluj") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        when (state.phase) {
            GamePhase.LOBBY_CLIENT -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            color = OrangeClient,
                            strokeWidth = 4.dp
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Połączono!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Czekam aż host rozpocznie grę...",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        if (state.players.size > 1) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Gracze: ${state.players.joinToString(", ")}",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }

            GamePhase.DISCOVERING -> {
                Spacer(Modifier.height(24.dp))
                Text("Szukaj gry", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(
                    "Upewnij się, że jesteś w tej samej sieci co host.",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E)
                )
                Spacer(Modifier.height(32.dp))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = OrangeClient,
                        strokeWidth = 4.dp
                    )
                }
                Spacer(Modifier.height(12.dp))
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
                            if (host.hasPassword) {
                                pendingHost = host
                                enteredPassword = ""
                            } else {
                                MultiplayerSessionManager.joinHost(host)
                            }
                        }
                    }
                }
            }

            else -> {
                Box(modifier = Modifier.weight(1f))
            }
        }

        state.errorMessage?.let { err ->
            Text(err, color = Color(0xFFD32F2F), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        androidx.compose.material3.OutlinedButton(
            onClick = { cancelAndGoBack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(host.name, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A), fontSize = 15.sp)
                if (host.hasPassword) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Hasło wymagane",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
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
