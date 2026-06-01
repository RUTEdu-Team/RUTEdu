package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prz.rutedu.app.math.MathEngine
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme


@Composable
internal fun LinearEquationContent(
    question: Question.LinearEquation,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {

    var input by remember(question.id) { mutableStateOf("") }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var checking by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    if (showHint) {
        HintBottomSheet(
            hint = question.hint,
            accentColor = accentColor,
            onDismiss = { showHint = false }
        )
    }

    fun normalizeLinearAnswer(input: String): String {
        return input
            .trim()
            .lowercase()
            .replace(" ", "")
            .replace("==", "=")
            .let {
                when {
                    it.startsWith("x=") -> it.removePrefix("x=")
                    it.startsWith("x==") -> it.removePrefix("x==")
                    else -> it
                }
            }
    }

    fun parseToNumber(input: String): Double? {
        return try {
            when {
                "/" in input -> {
                    val parts = input.split("/")
                    if (parts.size != 2) return null
                    val a = parts[0].toDouble()
                    val b = parts[1].toDouble()
                    a / b
                }
                else -> input.toDouble()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun check() {
        if (checking || input.isBlank()) return

        checking = true
        isWrong = false
        focusManager.clearFocus()

        scope.launch(Dispatchers.Default) {
            val engine = MathEngine()

            val correctSolution = engine.solve(question.equation, "x")
                ?.toString()
                ?.replace(" ", "")

            val userRaw = normalizeLinearAnswer(input)
            val userValue = parseToNumber(userRaw)

            val correct = if (correctSolution == null || userValue == null) {
                false
            } else {
                val extracted = Regex("x->(-?\\d+(\\.\\d+)?)")
                    .find(correctSolution)
                    ?.groupValues
                    ?.get(1)
                    ?.toDoubleOrNull()

                extracted != null && kotlin.math.abs(extracted - userValue) < 0.0001
            }

            withContext(Dispatchers.Main) {
                checking = false
                if (correct) onCorrect()
                else {
                    isWrong = true
                    onWrong()
                }
            }
        }
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
            text = question.equationDisplay,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "x =",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.width(12.dp))

            val isDark = isAppInDarkTheme()

            val borderColor = when {
                isWrong -> Color(0xFFE53935)
                input.isNotEmpty() -> accentColor
                else -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
            }

            TextField(
                value = input,
                onValueChange = {
                    input = it
                    isWrong = false
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { check() }
                ),
                placeholder = {
                    Text("…")
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .width(80.dp)
                    .border(
                        1.5.dp,
                        borderColor,
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp)
            )

            if (checking) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = { check() },
            checkEnabled = input.isNotBlank() && !checking
        )

        Spacer(Modifier.height(16.dp))
    }
}