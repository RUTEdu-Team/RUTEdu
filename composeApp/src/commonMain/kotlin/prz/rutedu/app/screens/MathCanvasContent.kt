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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.math.MathCanvas
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme

/**
 * Question content for [Question.GraphTypeAnswer] - a [MathCanvas] visualization paired with a
 * numeric text-input answer field.
 *
 * Renders a prompt above a card containing a [MathCanvas] (220 dp tall) that displays the
 * mathematical shapes defined in [Question.GraphTypeAnswer.shapes] with the coordinate system
 * from [Question.GraphTypeAnswer.viewport]. The student reads the graph and types their answer.
 *
 * Functionally mirrors [TypeAnswerContent] but uses a graph visualization instead of (or in
 * addition to) a triangle diagram. An optional `question.unit` suffix and `question.inlineHint`
 * are rendered exactly as in [TypeAnswerContent].
 *
 * @param question     The question: prompt, canvas shapes, viewport, correct answer, unit, inline hint, hint.
 * @param accentColor  Subject accent color.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect    Called when `input.toIntOrNull() == question.correctAnswer`.
 * @param onWrong      Called when the input does not match.
 */
@Composable
internal fun GraphTypeAnswerContent(
    question: Question.GraphTypeAnswer,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var input    by remember(question.id) { mutableStateOf("") }
    var isWrong  by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    if (showHint) {
        HintBottomSheet(hint = question.hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = question.prompt,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Math canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            MathCanvas(
                shapes = question.shapes,
                viewport = question.viewport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Twoja odpowiedź",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        val isDark = isAppInDarkTheme()
        val borderColor = when {
            isWrong          -> Color(0xFFE53935)
            input.isNotEmpty() -> accentColor
            else             -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { v ->
                    if (v.all { it.isDigit() } && v.length <= 4) {
                        input = v
                        isWrong = false
                    }
                },
                placeholder = { Text("Wpisz wynik…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (input.toIntOrNull() == question.correctAnswer) onCorrect()
                    else onWrong()
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (question.unit.isNotEmpty()) {
                Text(
                    text = question.unit,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Always-visible inline hint
        if (question.inlineHint != null) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.07f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = question.inlineHint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = {
                if (input.toIntOrNull() == question.correctAnswer) onCorrect()
                else onWrong()
            },
            checkEnabled = input.isNotEmpty()
        )
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Question content for [Question.GraphSelectFromList] - a [MathCanvas] visualization paired with
 * a single-select multiple-choice answer list.
 *
 * Structurally combines the graph panel from [GraphTypeAnswerContent] with the option list from
 * [SelectFromListContent], but always in single-select mode (one answer at a time). Tapping an
 * option deselects any previously selected option.
 *
 * @param question     The question: prompt, canvas shapes, viewport, options, correct index set, hint.
 * @param accentColor  Subject accent color.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect    Called when `selected == question.correctIndices`.
 * @param onWrong      Called when the selection is incorrect.
 */
@Composable
internal fun GraphSelectFromListContent(
    question: Question.GraphSelectFromList,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var selected by remember(question.id) { mutableStateOf(emptySet<Int>()) }
    var isWrong  by remember(question.id) { mutableStateOf(false) }
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

        Spacer(Modifier.height(16.dp))

        // Math canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            MathCanvas(
                shapes = question.shapes,
                viewport = question.viewport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        question.options.forEachIndexed { index, option ->
            val isSelected = index in selected
            val isDark = isAppInDarkTheme()
            val borderColor = when {
                isWrong && isSelected -> Color(0xFFE53935)
                isSelected            -> accentColor
                else                  -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
            }
            val bgColor = when {
                isWrong && isSelected -> if (isDark) Color(0xFF422121) else Color(0xFFFFEBEA)
                isSelected            -> accentColor.copy(alpha = 0.08f)
                else                  -> MaterialTheme.colorScheme.surface
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
                        selected = setOf(index)   // single-select
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) accentColor else Color.Transparent)
                        .border(1.5.dp, if (isSelected) accentColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFBCC1CA)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(text = option, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
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
