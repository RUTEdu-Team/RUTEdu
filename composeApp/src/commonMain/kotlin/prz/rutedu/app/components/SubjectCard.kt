package prz.rutedu.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.models.Subject
import prz.rutedu.app.theme.themeBackgroundColor
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.locale.getNameRes

/**
 * Grid tile that represents a single [Subject] on the home screen.
 *
 * The card fills whatever width is given by the caller (typically half the screen in a 2-column
 * `LazyVerticalGrid`) and renders, top to bottom:
 * 1. A rounded icon box in the subject's accent [Subject.color] with a white icon inside.
 * 2. The subject name in bold.
 * 3. A lesson-count subtitle (e.g. "12 lekcji") in the accent color.
 * 4. A custom progress bar - a thin capsule where the filled portion equals [Subject.progress].
 *    The bar uses a custom `Box`-in-`Box` approach instead of Material3's `LinearProgressIndicator`
 *    to avoid the built-in stop-indicator dot and animation lag that appears when progress = 1f.
 *
 * The entire card background is [Subject.backgroundColor] (a desaturated tint of the accent color),
 * which gives each subject a distinct but harmonious look without requiring explicit theming.
 *
 * This composable is stateless - it delegates navigation to [onClick].
 *
 * @param subject  The subject whose data is rendered on this card.
 * @param onClick  Called when the user taps the card. The caller (usually `MainScreen`) navigates
 *                 to the subject detail screen.
 * @param modifier Optional [Modifier] forwarded to the outer `Box`. Use it to control size/weight
 *                 inside a grid cell.
 */
@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(subject.themeBackgroundColor())
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            val subjectName = stringResource(subject.getNameRes())
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(subject.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = subjectName,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subjectName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(Res.string.subject_lessons_count, subject.lessonCount),
                fontSize = 13.sp,
                color = subject.color,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Custom two-layer Box progress bar: outer = track at 30% alpha, inner = fill.
            // Avoids Material3 LinearProgressIndicator which adds an unwanted stop-dot at 100%.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(subject.color.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(subject.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(subject.color)
                )
            }
        }
    }
}
