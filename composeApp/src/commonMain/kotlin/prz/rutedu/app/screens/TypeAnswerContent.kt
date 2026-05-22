package prz.rutedu.app.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme

/**
 * Question content for [Question.TypeAnswer] - the student types a numeric answer into a text field.
 *
 * Unlike [FindAnswerContent] (which uses `NumberKeypad`), this composable uses a system keyboard
 * so the student can submit with the IME "Done" action. Input is restricted to digits only, up
 * to 4 characters, enforced in `onValueChange`.
 *
 * Optional visual elements (rendered only when the question data is non-null/non-empty):
 * - **Triangle diagram** (`question.triangleAngles != null`): a [TriangleCanvas] showing two
 *   known angles and a `?` at the apex, drawn via the Compose `Canvas` API.
 * - **Unit suffix** (`question.unit.isNotEmpty()`): a greyed-out unit label (e.g. `"°"`, `"cm"`)
 *   appended inside the text field so the student doesn't type the unit.
 * - **Inline hint** (`question.inlineHint != null`): a small lightbulb-styled tip shown permanently
 *   below the input field (as opposed to the full [HintBottomSheet] which requires a tap).
 *
 * @param question     Question data: prompt, correct answer, optional triangle, unit, and inline hint.
 * @param accentColor  Subject accent color for borders, the inline hint background, and buttons.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect    Called when `input.toIntOrNull() == question.correctAnswer`.
 * @param onWrong      Called when the input does not match the answer.
 */
@Composable
internal fun TypeAnswerContent(
    question: Question.TypeAnswer,
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

        Spacer(Modifier.height(20.dp))

        // Triangle visual
        if (question.triangleAngles != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                TriangleCanvas(
                    angle1 = question.triangleAngles.first,
                    angle2 = question.triangleAngles.second,
                    color = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(20.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // "Twoja odpowiedź" label
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

        // Input field
        val isDark = isAppInDarkTheme()
        val borderColor = if (isWrong) Color(0xFFE53935) else if (input.isNotEmpty()) accentColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFFE8EAF0))
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
                    if (v.all { it.isDigit() } && v.length <= 4) {
                        input = v
                        isWrong = false
                    }
                },
                placeholder = { Text("Wpisz wynik...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (input.toIntOrNull() == question.correctAnswer) onCorrect()
                    else onWrong()
                }),
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
            if (question.unit.isNotEmpty()) {
                Text(
                    text = question.unit,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Inline always-visible hint
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
 * Draws a fixed-layout triangle diagram with two labeled known angles and a `?` at the apex.
 *
 * The triangle vertices are positioned at fixed proportions of the canvas:
 * - Bottom-left (A): `(8%, 88%)`
 * - Bottom-right (B): `(92%, 88%)`
 * - Apex (C): `(50%, 5%)`
 *
 * The apex angle is shown with a dashed arc (unknown) while the two base angles get solid arcs.
 * Labels are placed near each arc:
 * - `angle1°` to the upper-right of bottom-left
 * - `angle2°` to the upper-left of bottom-right
 * - `?` below the apex arc
 *
 * This is a pure drawing composable - it does not interact with any state.
 *
 * @param angle1 The known angle at the bottom-left vertex, in degrees.
 * @param angle2 The known angle at the bottom-right vertex, in degrees.
 * @param color  Stroke and text color (usually the subject accent color).
 * @param modifier Layout modifier applied to the underlying [Canvas].
 */
@Composable
internal fun TriangleCanvas(
    angle1: Int,
    angle2: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bLeft  = Offset(w * 0.08f, h * 0.88f)
        val bRight = Offset(w * 0.92f, h * 0.88f)
        val apex   = Offset(w * 0.50f, h * 0.05f)

        val stroke = Stroke(width = 2.5.dp.toPx())

        // Triangle sides
        val triPath = Path().apply {
            moveTo(bLeft.x, bLeft.y)
            lineTo(apex.x, apex.y)
            lineTo(bRight.x, bRight.y)
            lineTo(bLeft.x, bLeft.y)
        }
        drawPath(triPath, color = color, style = stroke)

        // Dashed arc at apex (unknown angle)
        val apexArcR = 28.dp.toPx()
        val apexArcPath = Path().apply {
            addArc(
                oval = Rect(
                    center = apex,
                    radius = apexArcR
                ),
                startAngleDegrees = 100f,
                sweepAngleDegrees = -80f  // approximate visual arc between the two sides
            )
        }
        drawPath(
            apexArcPath,
            color = color.copy(alpha = 0.6f),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
        )

        // Small angle arcs at base corners
        val baseArcR = 22.dp.toPx()
        val arc1Path = Path().apply {
            addArc(
                oval = Rect(center = bLeft, radius = baseArcR),
                startAngleDegrees = -60f,
                sweepAngleDegrees = 45f
            )
        }
        drawPath(arc1Path, color = color, style = Stroke(width = 1.5.dp.toPx()))

        val arc2Path = Path().apply {
            addArc(
                oval = Rect(center = bRight, radius = baseArcR),
                startAngleDegrees = 180f + 15f,
                sweepAngleDegrees = 45f
            )
        }
        drawPath(arc2Path, color = color, style = Stroke(width = 1.5.dp.toPx()))

        // Text labels
        val labelStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        val questionStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        val a1Text = textMeasurer.measure("${angle1}°", labelStyle)
        val a2Text = textMeasurer.measure("${angle2}°", labelStyle)
        val qText  = textMeasurer.measure("?", questionStyle)

        // angle1 bottom-left
        drawText(a1Text, topLeft = Offset(bLeft.x - 10.dp.toPx(), bLeft.y - 36.dp.toPx()))
        // angle2 bottom-right
        drawText(a2Text, topLeft = Offset(bRight.x - a2Text.size.width - 6.dp.toPx(), bRight.y - 36.dp.toPx()))
        // ? at apex
        drawText(qText, topLeft = Offset(apex.x - qText.size.width / 2f, apex.y + 32.dp.toPx()))
    }
}
