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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prz.rutedu.app.math.MathEngine
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme

/**
 * Question content for [Question.ExpressionTypeAnswer].
 *
 * The student types an algebraic expression (e.g. `"6*x+2"` or `"6x+2"`).
 * On "Sprawdź", a [MathEngine] is constructed on [Dispatchers.Default] to check
 * mathematical equivalence via `Simplify(userExpr - correctExpr) == 0`.
 * A loading indicator is shown during the check.
 *
 * When [MathEngine.isAvailable] is false (iOS), falls back to normalized string comparison.
 */
@Composable
internal fun ExpressionTypeAnswerContent(
    question: Question.ExpressionTypeAnswer,
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
                engine.areEquivalent(input.trim(), question.correctExpr) == true
            } else {
                // iOS fallback: normalized string comparison
                input.trim().replace(" ", "").lowercase() ==
                    question.correctExpr.replace(" ", "").lowercase()
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
            androidx.compose.material3.TextField(
                value = input,
                onValueChange = { v ->
                    if (v.length <= 40) {
                        input = v
                        isWrong = false
                    }
                },
                placeholder = {
                    Text(stringResource(Res.string.placeholder_algebra), color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { check() }),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (checking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
            }
        }

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
                Text(
                    text = question.inlineHint,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
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
