package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*

/**
 * Question content for [Question.SelectFromList] - the student picks one or more options.
 *
 * Supports two selection modes controlled by [Question.SelectFromList.multiSelect]:
 * - **Single-select** (`multiSelect = false`): radio-button style; tapping a row selects it and
 *   deselects any previous selection. The indicator is a circle.
 * - **Multi-select** (`multiSelect = true`): checkbox style; tapping toggles each option
 *   independently. A hint "Zaznacz wszystkie poprawne odpowiedzi" appears below the prompt.
 *   The indicator is a rounded square.
 *
 * Selected rows are highlighted with a light accent background and an accent-colored border.
 * When the user taps "Sprawdź" with the wrong selection, borders and backgrounds of the
 * incorrectly selected rows turn red; the state resets when any option is tapped again.
 *
 * "Sprawdź" is enabled as soon as at least one option is selected and disabled when none is.
 *
 * @param question    The question: prompt, option strings, correct index set, multi-select flag, hint.
 * @param accentColor Subject accent color for selection indicators and the check button.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect   Called when `selected == question.correctIndices`.
 * @param onWrong     Called when the submitted selection is incorrect.
 */
@Composable
internal fun SelectFromListContent(
    question: Question.SelectFromList,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var selected by remember(question.id) { mutableStateOf(emptySet<Int>()) }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    if (showHint) {
        HintBottomSheet(hint = question.hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = question.prompt,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (question.multiSelect) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.select_all_correct_answers),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        question.options.forEachIndexed { index, option ->
            val isSelected = index in selected
            val isDark = isAppInDarkTheme()
            val borderColor = when {
                isWrong && isSelected -> Color(0xFFE53935)
                isSelected -> accentColor
                else -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
            }
            val bgColor = when {
                isWrong && isSelected -> if (isDark) Color(0xFF422121) else Color(0xFFFFEBEA)
                isSelected -> accentColor.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable {
                        isWrong = false
                        selected = if (question.multiSelect) {
                            if (isSelected) selected - index else selected + index
                        } else {
                            setOf(index)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(if (question.multiSelect) RoundedCornerShape(6.dp) else CircleShape)
                        .background(if (isSelected) accentColor else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (isSelected) accentColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFBCC1CA)),
                            if (question.multiSelect) RoundedCornerShape(6.dp) else CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = option,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = {
                if (selected == question.correctIndices) onCorrect()
                else onWrong()
            },
            checkEnabled = selected.isNotEmpty()
        )
        Spacer(Modifier.height(16.dp))
    }
}
