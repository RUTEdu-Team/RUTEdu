package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.models.Hint
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*

/**
 * Modal bottom sheet that displays a structured [Hint] when the user taps the "Podpowiedź" button.
 *
 * The sheet is scrollable and renders up to four distinct visual sections derived from the [Hint]
 * model, each rendered only when its data is non-empty:
 *
 * 1. **Main text block** (`hint.mainText`) - a bordered card with a colored left stripe.
 *    If `hint.boldPart` is set, that substring is rendered in `FontWeight.Bold` using
 *    `AnnotatedString` so no raw HTML or markup is needed in question data.
 * 2. **Section header** (`hint.sectionTitle`) - a small-caps label above the item list.
 * 3. **Item list** (`hint.items`) - pill-shaped rows; the second item (index 1) gets a light
 *    accent background to highlight the "key" answer. Dot colors cycle through three shades.
 * 4. **Step list** (`hint.steps`) - a grey rounded box with a "Krok po kroku:" header and
 *    bullet-separated step lines.
 *
 * @param hint        The hint data object from the `Question` being answered.
 * @param accentColor Subject accent color applied to the left border stripe, header icon, and button.
 * @param onDismiss   Called when the user taps "Rozumiem, wracam do zadania" or the x close button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HintBottomSheet(
    hint: Hint,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(Res.string.button_hint),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main text with left border and optional bold
            if (hint.mainText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.07f))
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(accentColor)
                    )
                    val annotated = buildAnnotatedString {
                        val text = hint.mainText
                        val bold = hint.boldPart
                        if (bold != null) {
                            val idx = text.indexOf(bold)
                            if (idx >= 0) {
                                append(text.substring(0, idx))
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
                                append(text.substring(idx + bold.length))
                            } else append(text)
                        } else append(text)
                    }
                    Text(
                        text = annotated,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Section title + items
            if (hint.sectionTitle != null) {
                Text(
                    text = hint.sectionTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            if (hint.items.isNotEmpty()) {
                hint.items.forEachIndexed { index, item ->
                    val dotColor = when (index % 3) {
                        0 -> MaterialTheme.colorScheme.outline
                        1 -> accentColor
                        else -> accentColor.copy(alpha = 0.5f)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (index == 1) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(text = item, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Steps
            if (hint.steps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(Res.string.hint_step_by_step),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        hint.steps.forEach { step ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "•",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp, top = 1.dp)
                                )
                                Text(text = step, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(stringResource(Res.string.hint_dismiss_button), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Renders a single large equation token (a number, operator symbol, or `"?"`) at [fontSize].
 *
 * Shared across [FindAnswerContent], [FindOperatorContent], and [EquationBalanceContent] so that
 * all equation-style questions use a consistent extra-bold monospace-like style.
 *
 * @param text     The token string to display (e.g. `"12"`, `"+"`, `"?"`).
 * @param color    Text color - typically the subject accent color for the answer slot, or
 *                 `Color(0xFF1A1A1A)` for known values.
 * @param fontSize Token size; defaults to 48 sp for primary equation display.
 */
@Composable
internal fun EquationText(text: String, color: Color, fontSize: TextUnit = 48.sp) {
    Text(text = text, fontSize = fontSize, fontWeight = FontWeight.ExtraBold, color = color)
}

/**
 * The standard two-button action row used at the bottom of most question content composables.
 *
 * Layout: `[Podpowiedź (outline)] [Sprawdź (filled, accent)]`
 *
 * - The hint button is always enabled and opens [HintBottomSheet].
 * - The check button is enabled only when [checkEnabled] is `true` (e.g. the user has
 *   selected an answer or entered non-empty text).
 *
 * @param accentColor  Subject accent color for button borders and the filled check button.
 * @param onHint       Called when "Podpowiedź" is tapped - should show [HintBottomSheet].
 * @param onCheck      Called when "Sprawdź" is tapped - should validate the current answer.
 * @param checkEnabled Whether the check button is interactive.
 */
@Composable
internal fun BottomButtons(
    accentColor: Color,
    onHint: () -> Unit,
    onCheck: () -> Unit,
    checkEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onHint,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(Res.string.button_hint), color = accentColor, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onCheck,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            enabled = checkEnabled
        ) {
            Text(stringResource(Res.string.button_check), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

