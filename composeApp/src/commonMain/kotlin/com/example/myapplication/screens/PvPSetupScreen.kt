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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.data.SubjectRepository
import com.example.myapplication.multiplayer.model.MultiplayerMode
import com.example.myapplication.multiplayer.network.MultiplayerSessionManager

private val Orange = Color(0xFFF47B20)
private val LightBg = Color(0xFFF5F6FA)
private val TextPrimary = Color(0xFF1A1A1A)

@Composable
fun PvPSetupScreen(
    mode: MultiplayerMode,
    navController: NavController,
    bottomPadding: Dp = 0.dp
) {
    var nickname by remember { mutableStateOf(com.example.myapplication.lastMultiplayerNickname) }
    var lobbyPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf("") }
    var selectedTopicId by remember { mutableStateOf("") }
    var timePerRound by remember { mutableStateOf(15) }
    var rounds by remember { mutableStateOf(10) }
    var isHost by remember { mutableStateOf<Boolean?>(null) }

    val subjects = remember { SubjectRepository.subjects }
    val topics = remember(selectedSubjectId) {
        SubjectRepository.getById(selectedSubjectId)?.topics ?: emptyList()
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (mode == MultiplayerMode.ONE_VS_ONE) "1 vs 1" else "Turniej",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Nickname
            item {
                SectionLabel("Twój pseudonim")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(16) },
                    placeholder = { Text("Wpisz nick...", color = Color(0xFF9E9E9E)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Role
            item {
                SectionLabel("Twoja rola")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoleChip(
                        label = "Stwórz grę",
                        icon = { Icon(Icons.Default.Person, null, Modifier.size(18.dp), tint = if (isHost == true) Color.White else Orange) },
                        selected = isHost == true,
                        modifier = Modifier.weight(1f)
                    ) { isHost = true }
                    RoleChip(
                        label = "Dołącz",
                        icon = { Icon(Icons.Default.Groups, null, Modifier.size(18.dp), tint = if (isHost == false) Color.White else Orange) },
                        selected = isHost == false,
                        modifier = Modifier.weight(1f)
                    ) { isHost = false }
                }
            }

            // Host-only fields
            if (isHost == true) {
                // Optional lobby password
                item {
                    SectionLabel("Hasło lobby (opcjonalne)")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lobbyPassword,
                        onValueChange = { lobbyPassword = it.take(16) },
                        placeholder = { Text("Zostaw puste = otwarte lobby", color = Color(0xFF9E9E9E)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    )
                }

                item {
                    SectionLabel("Przedmiot")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.forEach { subject ->
                            SelectableRow(
                                label = subject.name,
                                selected = selectedSubjectId == subject.id,
                                color = subject.color
                            ) {
                                selectedSubjectId = subject.id
                                selectedTopicId = ""
                            }
                        }
                    }
                }

                if (selectedSubjectId.isNotEmpty()) {
                    item {
                        SectionLabel("Temat")
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topics.forEach { topic ->
                                SelectableRow(
                                    label = topic.name,
                                    selected = selectedTopicId == topic.id,
                                    color = Orange
                                ) { selectedTopicId = topic.id }
                            }
                        }
                    }
                }

                item {
                    SectionLabel("Czas na odpowiedź")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(10, 15, 20).forEach { sec ->
                            TimeChip(
                                label = "${sec}s",
                                selected = timePerRound == sec,
                                modifier = Modifier.weight(1f)
                            ) { timePerRound = sec }
                        }
                    }
                }

                item {
                    SectionLabel("Liczba rund")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15).forEach { r ->
                            TimeChip(
                                label = "$r",
                                selected = rounds == r,
                                modifier = Modifier.weight(1f)
                            ) { rounds = r }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        val canProceed = nickname.isNotBlank() && isHost != null &&
                (isHost == false || (selectedSubjectId.isNotEmpty() && selectedTopicId.isNotEmpty()))

        Button(
            onClick = {
                val nick = nickname.trim()
                com.example.myapplication.lastMultiplayerNickname = nick
                if (isHost == true) {
                    MultiplayerSessionManager.initAsHost(
                        nickname = nick,
                        mode = mode,
                        subjectId = selectedSubjectId,
                        topicId = selectedTopicId,
                        rounds = rounds,
                        timePerRoundSec = timePerRound,
                        password = lobbyPassword
                    )
                    navController.navigate(Screen.PvPLobbyHost.route)
                } else {
                    MultiplayerSessionManager.initAsClient(nick)
                    navController.navigate(Screen.PvPLobbyClient.route)
                }
            },
            enabled = canProceed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = bottomPadding + 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Text(
                text = if (isHost == true) "Stwórz grę" else "Szukaj gier",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E))
}

@Composable
private fun RoleChip(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Orange else LightBg)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else TextPrimary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color.copy(alpha = 0.12f) else LightBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) color else TextPrimary,
            fontSize = 15.sp
        )
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TimeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Orange else LightBg)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextPrimary,
            fontSize = 15.sp
        )
    }
}
