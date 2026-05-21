package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Celebration screen shown by [LessonGameScreen] when the student answers all questions correctly.
 *
 * Renders a vertically centered layout with:
 * - A large accent-colored check-mark circle.
 * - "Świetna robota!" headline and the lesson name as subtitle.
 * - A "100% ukończone" pill badge.
 * - Two action buttons:
 *   - **Resetuj i zagraj ponownie** (outline) - calls [onReset] which zeroes the progress and
 *     restarts the question loop without leaving the screen.
 *   - **Wróć do lekcji** (filled) - calls [onBack] which pops the back stack.
 *
 * This composable has no internal state - all actions are delegated to [LessonGameScreen].
 *
 * @param subjectName  Subject name displayed in the top header bar.
 * @param lessonName   Lesson name shown as a subtitle below the headline.
 * @param accentColor  Subject accent color applied to the icon circle and buttons.
 * @param bottomPadding System navigation bar height padding from `App`.
 * @param onReset      Called when the student wants to replay the lesson from the beginning.
 * @param onBack       Called when the student wants to return to the topic detail screen.
 */
@Composable
internal fun LessonCompleteContent(
    subjectName: String,
    lessonName: String,
    accentColor: Color,
    bottomPadding: Dp,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .statusBarsPadding()
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć", tint = Color(0xFF1A1A1A))
            }
            Text(
                text = subjectName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Świetna robota!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lessonName,
            fontSize = 16.sp,
            color = Color(0xFF9E9E9E),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text("100% ukończone", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Resetuj i zagraj ponownie", color = accentColor, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Wróć do lekcji", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
