package prz.rutedu.app.components

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
import androidx.compose.material3.MaterialTheme
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
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import prz.rutedu.app.locale.getNameRes
import prz.rutedu.app.locale.getDescriptionRes
import prz.rutedu.app.models.Lesson
import prz.rutedu.app.models.Topic
import kotlin.math.roundToInt

/**
 * Card that represents a single [Topic] inside the subject detail screen.
 *
 * Delegates all rendering to the private [ItemCard] / [CardContent] helpers. The only
 * responsibility of this wrapper is computing the human-readable status text:
 * - **Locked** -> "Zablokowane" (grey-out visual, no click handler).
 * - **Unlocked** -> "X% Ukończono" where X is `topic.progress * 100` rounded to the nearest integer.
 *
 * @param topic    The topic to display. Locking, progress, color, and icon are all read from here.
 * @param onClick  Navigation callback; ignored when the topic is locked (the card is not clickable).
 * @param modifier Optional [Modifier] forwarded to the card root.
 */
@Composable
fun TopicCard(
    topic: Topic,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusText = if (topic.isLocked) stringResource(Res.string.status_locked)
    else stringResource(Res.string.status_completed, "${(topic.progress * 100).roundToInt()}%")

    ItemCard(
        name = stringResource(topic.getNameRes()),
        description = stringResource(topic.getDescriptionRes()),
        progress = topic.progress,
        isLocked = topic.isLocked,
        color = topic.color,
        icon = topic.icon,
        statusText = statusText,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Card that represents a single [Lesson] inside the topic detail screen.
 *
 * Shares the private [ItemCard] / [CardContent] rendering pipeline with [TopicCard].
 * The status text logic differs to communicate lesson-specific progress state:
 * - **Locked** -> "Zablokowane".
 * - **In progress** (`progress > 0`) -> "Kontynuuj – X%" (resume prompt).
 * - **Not started** -> "Rozpocznij – 0%" (start prompt).
 *
 * @param lesson   The lesson to display. Locking, progress, color, and icon come from here.
 * @param onClick  Navigation callback leading to `LessonGameScreen`; ignored when locked.
 * @param modifier Optional [Modifier] forwarded to the card root.
 */
@Composable
fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusText = when {
        lesson.isLocked -> stringResource(Res.string.status_locked)
        lesson.progress > 0f -> stringResource(Res.string.status_continue, "${(lesson.progress * 100).roundToInt()}%")
        else -> stringResource(Res.string.status_start)
    }

    ItemCard(
        name = stringResource(lesson.getNameRes()),
        description = stringResource(lesson.getDescriptionRes()),
        progress = lesson.progress,
        isLocked = lesson.isLocked,
        color = lesson.color,
        icon = lesson.icon,
        statusText = statusText,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Shared card shell used by both [TopicCard] and [LessonCard].
 *
 * Handles two distinct visual states:
 * - **Locked:** flat grey card (no elevation, dashed border at `0xFFDDE1E9`) with a lock icon,
 *   muted colors, and `Modifier.clickable` **omitted** - the card cannot be tapped.
 * - **Unlocked:** white elevated card (2 dp shadow) with the subject accent color, subject icon,
 *   and a color-matched progress bar. Tapping fires [onClick].
 *
 * The split into [ItemCard] (shell) + [CardContent] (interior) keeps the locked/unlocked
 * branch minimal - only the `Card` wrapper arguments differ between the two states.
 */
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
    val iconBg = if (isLocked) MaterialTheme.colorScheme.outlineVariant else color.copy(alpha = 0.13f)
    val iconTint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else color
    val displayIcon = if (isLocked) Icons.Default.Lock else icon
    val titleColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val statusColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else color

    val cardModifier = modifier
        .fillMaxWidth()
        .then(if (!isLocked) Modifier.clickable { onClick() } else Modifier)

    if (isLocked) {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CardContent(
                name, description, progress, isLocked, displayIcon,
                iconBg, iconTint, titleColor, subtitleColor, statusText, statusColor
            )
        }
    }
}

/**
 * Renders the internal layout content of the topic or lesson card.
 *
 * Lays out the icon, text descriptions, progress indicator, and completion status.
 *
 * @param name Title of the topic or lesson.
 * @param description Description of what the topic/lesson contains.
 * @param progress Float percentage (0.0 to 1.0) representing completion progress.
 * @param isLocked Whether the card is currently locked/inactive.
 * @param displayIcon Graphic icon showing the subject category.
 * @param iconBg Background color of the icon container.
 * @param iconTint Tint color applied to the icon.
 * @param titleColor Text color for the title name.
 * @param subtitleColor Text color for the description text.
 * @param statusText Human-readable completion status string (e.g. `"Ukończono"`).
 * @param statusColor Text color for the status label.
 */
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
                    .background(MaterialTheme.colorScheme.outlineVariant)
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
