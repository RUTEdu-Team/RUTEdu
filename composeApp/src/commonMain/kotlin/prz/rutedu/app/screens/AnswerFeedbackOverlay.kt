package prz.rutedu.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * The three possible feedback states for a quiz answer.
 *
 * - [NONE] - no overlay shown (default between questions).
 * - [CORRECT] - green check card shown for 950 ms, then `onCorrectDismiss` fires.
 * - [WRONG] - red cross card shown for 750 ms, then `onWrongDismiss` fires.
 */
enum class FeedbackState { NONE, CORRECT, WRONG }

/**
 * Full-screen overlay that briefly displays a correct / wrong feedback card after the user
 * submits an answer.
 *
 * The card appears with a spring-bounce scale-in animation and fades out on dismissal. Timing:
 * - **Correct:** 950 ms visible, then [onCorrectDismiss] is called (which advances to the next question).
 * - **Wrong:** 750 ms visible, then [onWrongDismiss] is called (which hides the overlay so the user
 *   can retry the same question).
 *
 * **`displayState` latch:** a separate `displayState` variable remembers the last non-NONE state
 * so the exit animation always shows the right icon/text while `AnimatedVisibility` fades out.
 * Without this latch, switching `state` to NONE before the animation finishes would show a blank card.
 *
 * This composable is stateless with respect to timing - [LessonGameScreen] owns [FeedbackState]
 * and provides the dismiss callbacks.
 *
 * @param state           Current feedback state driven by [LessonGameScreen].
 * @param accentColor     Subject accent color (unused in the overlay itself, present for future use).
 * @param onCorrectDismiss Called after the 950 ms correct delay - should advance to the next question.
 * @param onWrongDismiss   Called after the 750 ms wrong delay - should reset state to [FeedbackState.NONE].
 */
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        text = if (isCorrect) stringResource(Res.string.feedback_correct) else stringResource(Res.string.feedback_wrong),
                        fontSize = if (isCorrect) 22.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
