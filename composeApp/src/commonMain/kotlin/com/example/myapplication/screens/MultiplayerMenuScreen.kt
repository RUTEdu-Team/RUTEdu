package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Screen
import com.example.myapplication.multiplayer.model.MultiplayerMode

private val OrangeMenu = Color(0xFFF47B20)
private val BlueMenu = Color(0xFF4A80F0)

@Composable
fun MultiplayerMenuScreen(navController: NavController, bottomPadding: Dp = 0.dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = Color(0xFF1A1A1A))
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Wieloosobowy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Graj z innymi bez internetu — przez WiFi lub hotspot",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E),
            lineHeight = 21.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        // 1v1 card
        ModeCard(
            title = "1 vs 1",
            subtitle = "Pojedynek z jednym znajomym.\n10 rund, pytania z wybranej lekcji.",
            icon = Icons.Default.Person,
            color = OrangeMenu,
            backgroundColor = OrangeMenu.copy(alpha = 0.08f)
        ) {
            navController.navigate(Screen.PvPSetup.createRoute(MultiplayerMode.ONE_VS_ONE.name))
        }

        Spacer(Modifier.height(16.dp))

        // Tournament card
        ModeCard(
            title = "Turniej",
            subtitle = "2–4 graczy, każdy ze swoim telefonem.\nNajszybsza poprawna odpowiedź zdobywa więcej punktów.",
            icon = Icons.Default.EmojiEvents,
            color = BlueMenu,
            backgroundColor = BlueMenu.copy(alpha = 0.08f)
        ) {
            navController.navigate(Screen.PvPSetup.createRoute(MultiplayerMode.TOURNAMENT.name))
        }

        Spacer(Modifier.height(32.dp))

        // Info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F6FA))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = OrangeMenu,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Jak zacząć?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "1. Jeden gracz tworzy grę (host)\n" +
                        "2. Pozostali łączą się do tej samej sieci WiFi lub hotspotu\n" +
                        "3. Pozostali wybierają 'Dołącz' i tapują hosta z listy",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E),
                        lineHeight = 21.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(bottomPadding + 16.dp))
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF9E9E9E), lineHeight = 20.sp)
            }
        }
    }
}
