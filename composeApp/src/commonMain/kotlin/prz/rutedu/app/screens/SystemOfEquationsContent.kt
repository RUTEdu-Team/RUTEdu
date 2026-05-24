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
internal fun SystemOfEquationsContent(
    question: Question.SystemOfEquations,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {

    var inputs by remember(question.id) {
        mutableStateOf(List(question.variables.size) { "" })
    }

    var checking by remember(question.id) { mutableStateOf(false) }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    if (showHint) {
        HintBottomSheet(
            hint = question.hint,
            accentColor = accentColor,
            onDismiss = { showHint = false }
        )
    }

    fun parseSymjaNumber(input: String): Double? {
        return try {
            when {
                "/" in input -> {
                    val parts = input.split("/")
                    if (parts.size != 2) return null
                    parts[0].toDouble() / parts[1].toDouble()
                }
                else -> input.toDouble()
            }
        } catch (e: Exception) {
            null
        }
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
                    parts[0].toDouble() / parts[1].toDouble()
                }
                else -> input.toDouble()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun systemSolver(): Pair<String, String> {

        val equationsList = question.equations.map { eq ->
            "(${eq.left}) == (${eq.right})"
        }

        val system = "{${equationsList.joinToString(", ")}}"
        val variables = "{${question.variables.joinToString(", ")}}"

        return system to variables
    }

    fun check() {
        if (checking || inputs.any { it.isBlank() }) return

        checking = true
        isWrong = false
        focusManager.clearFocus()

        scope.launch(Dispatchers.Default) {

            val engine = MathEngine()

            val (system, variables) = systemSolver()

            val solution = engine.solve(system, variables)
                ?.toString()
                ?.replace(" ", "")

            println(solution)

            val parsed = Regex("([a-zA-Z]+)->(-?\\d+(?:/\\d+)?|\\d+(?:\\.\\d+)?)")
                .findAll(solution ?: "")
                .associate {
                    val variable = it.groupValues[1]
                    val value = it.groupValues[2]
                    variable to parseSymjaNumber(value)
                }

            val correct = if (parsed.isEmpty()) {
                false
            } else {
                question.variables.indices.all { i ->
                    val varName = question.variables[i]
                    val userValue = parseToNumber(
                        normalizeLinearAnswer(inputs[i])
                    )

                    val correctValue = parsed[varName]

                    userValue != null &&
                            correctValue != null &&
                            kotlin.math.abs(userValue - correctValue) < 0.0001
                }
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

        question.equations.forEach {
            Text(
                text = "${it.left} = ${it.right}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        Column {
            question.variables.forEachIndexed { i, v ->
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text("$v =")

                    Spacer(Modifier.width(8.dp))

                    TextField(
                        value = inputs[i],
                        onValueChange = {
                            inputs = inputs.toMutableList().also { list ->
                                list[i] = it
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .width(70.dp)
                            .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                    )
                }

                if (i != question.variables.lastIndex) {
                    Spacer(Modifier.width(16.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = { check() },
            checkEnabled = inputs.all { it.isNotBlank() } && !checking
        )
    }
}