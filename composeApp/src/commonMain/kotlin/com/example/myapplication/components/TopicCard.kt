package com.example.myapplication.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.Lesson
import com.example.myapplication.models.Topic
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
// TopicCard  –  used in SubjectDetailScreen
// Status label: "X% Ukończono"
// ─────────────────────────────────────────────
@Composable
fun TopicCard(
    topic: Topic,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusText = if (topic.isLocked) "Zablokowane"
    else "${(topic.progress * 100).roundToInt()}% Ukończono"

    ItemCard(
        name = topic.name,
        description = topic.description,
        progress = topic.progress,
        isLocked = topic.isLocked,
        color = topic.color,
        icon = topic.icon,
        statusText = statusText,
        onClick = onClick,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
// LessonCard  –  used in TopicDetailScreen
// Status label: "Kontynuuj – X%" / "Rozpocznij – 0%"
// ─────────────────────────────────────────────
@Composable
fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusText = when {
        lesson.isLocked -> "Zablokowane"
        lesson.progress > 0f -> "Kontynuuj – ${(lesson.progress * 100).roundToInt()}%"
        else -> "Rozpocznij – 0%"
    }

    ItemCard(
        name = lesson.name,
        description = lesson.description,
        progress = lesson.progress,
        isLocked = lesson.isLocked,
        color = lesson.color,
        icon = lesson.icon,
        statusText = statusText,
        onClick = onClick,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────
// Shared internal card – single source of truth
// ─────────────────────────────────────────────
@Composable
private fun ItemCard(
    name: String,
    description: String,
    progress: Float,
    isLocked: Boolean,
    color: Color,
    icon: ImageVector,
    statusText: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val iconBg = if (isLocked) Color(0xFFE2E6ED) else color.copy(alpha = 0.13f)
    val iconTint = if (isLocked) Color(0xFFADB5BD) else color
    val displayIcon = if (isLocked) Icons.Default.Lock else icon
    val titleColor = if (isLocked) Color(0xFFADB5BD) else Color(0xFF1A1A1A)
    val subtitleColor = if (isLocked) Color(0xFFBCC1CA) else Color(0xFF9E9E9E)
    val statusColor = if (isLocked) Color(0xFFADB5BD) else color

    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (!isLocked) Modifier.clickable { onClick() } else Modifier)

    if (isLocked) {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0xFFDDE1E9))
        ) {
            CardContent(
                name, description, progress, isLocked, displayIcon,
                iconBg, iconTint, titleColor, subtitleColor, statusText, statusColor
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CardContent(
                name, description, progress, isLocked, displayIcon,
                iconBg, iconTint, titleColor, subtitleColor, statusText, statusColor
            )
        }
    }
}

@Composable
private fun CardContent(
    name: String,
    description: String,
    progress: Float,
    isLocked: Boolean,
    displayIcon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    titleColor: Color,
    subtitleColor: Color,
    statusText: String,
    statusColor: Color,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = name,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = subtitleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE8EBF0))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusColor.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(statusColor)
                )
            }
        }
    }
}
