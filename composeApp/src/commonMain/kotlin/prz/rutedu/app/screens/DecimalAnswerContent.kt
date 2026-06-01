package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

@Composable
internal fun DecimalAnswerContent(
    question: Question.DecimalAnswer,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var input by remember(question.id) { mutableStateOf("") }
    var isWrong by remember(question.id) { mutableStateOf(false) }
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

        Spacer(Modifier.height(40.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (question.labelBefore != null) {
                Text(
                    text = question.labelBefore,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            val isDark = isAppInDarkTheme()
            val borderColor = if (isWrong) Color(0xFFE53935) else if (input.isNotEmpty()) accentColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0))

            TextField(
                value = input,
                onValueChange = { v ->
                    if (v.all { it.isDigit() || it == '.' || it == ',' } && v.count { it == '.' || it == ',' } <= 1) {
                        input = v
                        isWrong = false
                    }
                },
                placeholder = { Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (checkDecimal(input, question.correctAnswer, question.precision)) onCorrect()
                    else { isWrong = true; onWrong() }
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(120.dp)
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            )
        }

        if (question.inlineHint != null) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.07f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = accentColor, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = question.inlineHint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
            }
        }

        Spacer(Modifier.height(32.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = {
                if (checkDecimal(input, question.correctAnswer, question.precision)) onCorrect()
                else { isWrong = true; onWrong() }
            },
            checkEnabled = input.isNotEmpty()
        )
        Spacer(Modifier.height(16.dp))
    }
}

private fun checkDecimal(input: String, correct: Double, precision: Int): Boolean {
    val normalized = input.replace(',', '.')
    val userVal = normalized.toDoubleOrNull() ?: return false
    val epsilon = 10.0.pow(-precision.toDouble()) / 2.0
    return abs(userVal - correct) < epsilon
}
