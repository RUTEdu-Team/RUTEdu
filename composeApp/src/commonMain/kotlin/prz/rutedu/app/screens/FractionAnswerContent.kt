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

@Composable
internal fun FractionAnswerContent(
    question: Question.FractionAnswer,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var numInput by remember(question.id) { mutableStateOf("") }
    var denInput by remember(question.id) { mutableStateOf("") }
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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FractionInputField(
                    value = numInput,
                    onValueChange = { numInput = it; isWrong = false },
                    accentColor = accentColor,
                    isWrong = isWrong
                )
                
                Spacer(Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
                
                Spacer(Modifier.height(8.dp))
                
                FractionInputField(
                    value = denInput,
                    onValueChange = { denInput = it; isWrong = false },
                    accentColor = accentColor,
                    isWrong = isWrong
                )
            }
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
                val uN = numInput.toLongOrNull()
                val uD = denInput.toLongOrNull()
                val cN = question.correctNumerator.toLong()
                val cD = question.correctDenominator.toLong()

                if (uN != null && uD != null && uD != 0L && uN * cD == cN * uD) {
                    onCorrect()
                } else {
                    isWrong = true
                    onWrong()
                }
            },
            checkEnabled = numInput.isNotEmpty() && denInput.isNotEmpty()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FractionInputField(
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    isWrong: Boolean
) {
    val isDark = isAppInDarkTheme()
    val borderColor = if (isWrong) Color(0xFFE53935) else if (value.isNotEmpty()) accentColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0))

    TextField(
        value = value,
        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) onValueChange(it) },
        modifier = Modifier
            .width(80.dp)
            .height(56.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp)),
        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true
    )
}
