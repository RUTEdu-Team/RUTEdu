package com.example.myapplication.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class FeedbackState { NONE, CORRECT, WRONG }

@Composable
internal fun AnswerFeedbackOverlay(
    state: FeedbackState,
    accentColor: Color,
    onCorrectDismiss: () -> Unit,
    onWrongDismiss: () -> Unit
) {
    LaunchedEffect(state) {
        when (state) {
            FeedbackState.CORRECT -> { delay(950); onCorrectDismiss() }
            FeedbackState.WRONG   -> { delay(750); onWrongDismiss() }
            FeedbackState.NONE    -> {}
        }
    }

    // Track last visible state so exit animation shows correct icon/text, not wrong
    var displayState by remember { mutableStateOf(FeedbackState.CORRECT) }
    if (state != FeedbackState.NONE) displayState = state

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = state != FeedbackState.NONE,
            enter = fadeIn() + scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)
            ),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            val isCorrect = displayState == FeedbackState.CORRECT
            val green = Color(0xFF3DBD7D)
            val red   = Color(0xFFE53935)
            val iconColor = if (isCorrect) green else red

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.width(200.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(iconColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Text(
                        text = if (isCorrect) "Dobrze!" else "Spróbuj jeszcze raz",
                        fontSize = if (isCorrect) 22.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
