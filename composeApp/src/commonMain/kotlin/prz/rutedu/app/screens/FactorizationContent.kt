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
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.math.MathEngine
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.placeholder_factorize
import rutedu.composeapp.generated.resources.your_answer


/**
 * Question content for [Question.Factorization] - the student enters a simplified/factorized algebraic expression.
 *
 * Renders the expression, accepts text input using the ASCII keyboard, and uses [MathEngine] (if available)
 * to evaluate equivalence between the user's expression and the simplified expression.
 *
 * @param question      The question details: expression to simplify and hint.
 * @param accentColor   Subject accent color.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect     Called when the input is mathematically equivalent to the correct solution.
 * @param onWrong       Called when the checked input is incorrect.
 */
@Composable
internal fun FactorizationContent(
    question: Question.Factorization,
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

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    if (showHint) {
        HintBottomSheet(hint = question.hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    fun check() {
        if (checking || input.isBlank()) return
        checking = true
        isWrong = false
        scope.launch(Dispatchers.Default) {
            val engine = MathEngine()
            val correct = if (engine.isAvailable) {
                engine.areEquivalent(input.trim(), engine.simplify(question.expression).toString()) == true
            } else {
                input.trim().replace(" ", "").lowercase() ==
                        question.expression.replace(" ", "").lowercase()
            }
            withContext(Dispatchers.Main) {
                checking = false
                if (correct) onCorrect() else {
                    isWrong = true
                    onWrong()
                }
            }
        }
    }

    LaunchedEffect(question.id) {
        focusRequester.requestFocus()
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
            text = question.expression,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.your_answer),
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
            isWrong -> Color(0xFFE53935)
            input.isNotEmpty() -> accentColor
            else -> if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0)
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
                    if (v.length <= 40) {
                        input = v
                        isWrong = false
                    }
                },
                placeholder = {
                    Text(
                        stringResource(Res.string.placeholder_factorize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        check()
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (checking) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
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
            checkEnabled = input.isNotEmpty() && !checking
        )

        Spacer(Modifier.height(16.dp))
    }
}