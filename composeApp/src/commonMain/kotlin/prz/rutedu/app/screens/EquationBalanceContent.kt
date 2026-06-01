package prz.rutedu.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import prz.rutedu.app.components.NumberKeypad
import prz.rutedu.app.models.Question

/**
 * Question content for [Question.EquationBalance] - the student fills in stoichiometric
 * coefficients to balance a chemical equation.
 *
 * The equation is displayed as `reactant₁ + reactant₂ -> product₁ + product₂` with horizontally
 * scrollable overflow. Terms with a non-null `fixedCoefficient` are read-only; terms with
 * `fixedCoefficient == null` render as interactive [EquationTermCell]s backed by a [NumberKeypad].
 *
 * ## Multi-slot editing
 * The composable tracks an `activePos` index that determines which blank is currently receiving
 * keypad input. Tapping any blank cell sets it as active. The first blank is pre-selected on
 * question load. Input is limited to 2 characters per coefficient (maximum value 99).
 *
 * ## Correctness check
 * [isAnswerCorrect] validates by finding a scaling factor `k` from the first term and verifying
 * that every other term satisfies `userValue == correctValue * k`. This accepts multiples of the
 * canonical balanced coefficients (e.g., `2H₂ + O₂ -> 2H₂O` and `4H₂ + 2O₂ -> 4H₂O` both pass).
 *
 * "Sprawdź" is enabled only when all blanks are non-empty.
 *
 * @param question     The question: instruction, sub-instruction, reactants, products, hint.
 * @param accentColor  Subject accent color for active cells and the check button.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect    Called when all coefficients are correct (including valid multiples).
 * @param onWrong      Called when the submitted coefficients are incorrect.
 */
@Composable
internal fun EquationBalanceContent(
    question: Question.EquationBalance,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    val blankPositions = remember(question.id) {
        buildList {
            question.reactants.forEachIndexed { i, t ->
                if (t.fixedCoefficient == null) add(i to (t.correctCoefficient ?: 1))
            }
            val offset = question.reactants.size
            question.products.forEachIndexed { i, t ->
                if (t.fixedCoefficient == null) add((offset + i) to (t.correctCoefficient ?: 1))
            }
        }
    }

    var inputs by remember(question.id) { mutableStateOf(mapOf<Int, String>()) }
    var activePos by remember(question.id) { mutableStateOf(blankPositions.firstOrNull()?.first ?: -1) }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }

    val allFilled = blankPositions.all { (pos, _) -> inputs[pos]?.isNotEmpty() == true }

    if (showHint) {
        HintBottomSheet(hint = question.hint, accentColor = accentColor, onDismiss = { showHint = false })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scrollable top area - instruction + equation
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = question.instruction,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = question.subInstruction,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    question.reactants.forEachIndexed { i, term ->
                        if (i > 0) Text("+", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        val pos = i
                        EquationTermCell(
                            formula = term.formula,
                            coefficient = if (term.fixedCoefficient == null) inputs[pos] ?: "" else term.fixedCoefficient.toString(),
                            isBlank = term.fixedCoefficient == null,
                            isActive = pos == activePos,
                            isWrong = isWrong && term.fixedCoefficient == null,
                            accentColor = accentColor,
                            onClick = if (term.fixedCoefficient == null) { { activePos = pos; isWrong = false } } else null
                        )
                    }

                    Spacer(Modifier.width(8.dp))
                    Text("→", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))

                    question.products.forEachIndexed { i, term ->
                        if (i > 0) Text("+", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        val pos = question.reactants.size + i
                        EquationTermCell(
                            formula = term.formula,
                            coefficient = if (term.fixedCoefficient == null) inputs[pos] ?: "" else term.fixedCoefficient.toString(),
                            isBlank = term.fixedCoefficient == null,
                            isActive = pos == activePos,
                            isWrong = isWrong && term.fixedCoefficient == null,
                            accentColor = accentColor,
                            onClick = if (term.fixedCoefficient == null) { { activePos = pos; isWrong = false } } else null
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            NumberKeypad(
                onDigit = { digit ->
                    if (activePos >= 0) {
                        val current = inputs[activePos] ?: ""
                        if (current.length < 2) {
                            inputs = inputs + (activePos to current + digit)
                        }
                    }
                },
                onClear = { if (activePos >= 0) inputs = inputs + (activePos to "") },
                onBackspace = {
                    if (activePos >= 0) {
                        val current = inputs[activePos] ?: ""
                        inputs = inputs + (activePos to current.dropLast(1))
                    }
                }
            )

            Spacer(Modifier.height(16.dp))
        }

        // Przycisk zawsze widoczny na dole
        BottomButtons(
            accentColor = accentColor,
            onHint = { showHint = true },
            onCheck = {
                if (isAnswerCorrect(question, inputs)) onCorrect()
                else onWrong()
            },
            checkEnabled = allFilled
        )

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * A single term in the balance equation: a coefficient circle above the formula label.
 *
 * Visual states:
 * - **Fixed** (`isBlank = false`): read-only, white circle with a grey border; never clickable.
 * - **Blank, idle** (`isBlank = true`, not active): neutral grey border, shows `?` placeholder.
 * - **Blank, active** (`isBlank = true`, `isActive = true`): accent-colored border and background,
 *   shows the typed coefficient or `?` if still empty.
 * - **Wrong** (`isWrong = true`): red border and light red background, immediately resets when
 *   the user taps any blank.
 *
 * @param formula     Chemical formula label (e.g. `"H₂O"`, `"O₂"`).
 * @param coefficient The coefficient string to display (empty string renders as `?` for blanks).
 * @param isBlank     Whether this term accepts user input.
 * @param isActive    Whether this term is currently receiving keypad input.
 * @param isWrong     Whether the submitted answer was wrong (turns the cell red).
 * @param accentColor Subject accent color for the active state.
 * @param onClick     Click callback - `null` for fixed (read-only) terms.
 */
@Composable
internal fun EquationTermCell(
    formula: String,
    coefficient: String,
    isBlank: Boolean,
    isActive: Boolean,
    isWrong: Boolean,
    accentColor: Color,
    onClick: (() -> Unit)?
) {
    val borderColor = when {
        isWrong -> MaterialTheme.colorScheme.error
        isActive -> accentColor
        isBlank -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val bgColor = when {
        isWrong -> MaterialTheme.colorScheme.errorContainer
        isActive -> accentColor.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.5.dp, borderColor, CircleShape)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isBlank && coefficient.isEmpty()) "?" else coefficient,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isBlank && coefficient.isEmpty() -> MaterialTheme.colorScheme.outline
                    isActive -> accentColor
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
        Text(text = formula, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * Validates whether the student's coefficient inputs correctly balance the equation.
 *
 * Accepts any valid multiple of the canonical coefficients - e.g. if the canonical solution is
 * `1H₂ + 1Cl₂ -> 2HCl`, then `2H₂ + 2Cl₂ -> 4HCl` (k=2) is also accepted.
 *
 * **Algorithm:**
 * 1. Build a list of `(userValue, correctValue)` pairs for all terms.
 *    Fixed-coefficient terms contribute their fixed value as both user and correct.
 * 2. Determine the scaling factor `k = firstUser / firstCorrect` from the first term.
 * 3. Verify that every term satisfies `userValue == correctValue * k`.
 * 4. If any blank cannot be parsed as an integer, return `false` immediately.
 *
 * @return `true` if all inputs are a consistent k-multiple of the correct coefficients.
 */
private fun isAnswerCorrect(question: Question.EquationBalance, inputs: Map<Int, String>): Boolean {
    // Build list of (userValue, correctValue) for all terms.
    // Fixed terms use their fixedCoefficient as both user and correct value.
    // Blank terms use the user's input and the correctCoefficient.
    val allTerms = buildList {
        question.reactants.forEachIndexed { i, t ->
            val userVal = t.fixedCoefficient ?: inputs[i]?.toIntOrNull() ?: return false
            val correctVal = t.fixedCoefficient ?: (t.correctCoefficient ?: 1)
            add(userVal to correctVal)
        }
        val offset = question.reactants.size
        question.products.forEachIndexed { i, t ->
            val userVal = t.fixedCoefficient ?: inputs[offset + i]?.toIntOrNull() ?: return false
            val correctVal = t.fixedCoefficient ?: (t.correctCoefficient ?: 1)
            add(userVal to correctVal)
        }
    }

    if (allTerms.isEmpty()) return false

    // Determine the multiplier k from the first term.
    val (firstUser, firstCorrect) = allTerms.first()
    if (firstUser <= 0 || firstUser % firstCorrect != 0) return false
    val k = firstUser / firstCorrect

    // All terms must satisfy userValue == correctValue * k.
    return allTerms.all { (user, correct) -> user == correct * k }
}
