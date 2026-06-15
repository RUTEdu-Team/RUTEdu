package prz.rutedu.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import prz.rutedu.app.models.Question

/**
 * Routes a [Question] to its dedicated content composable.
 *
 * Single dispatch point shared by [LessonGameScreen] (solo lessons) and [PvPBattleScreen]
 * (two-player battles). Adding a new question type requires:
 * 1. A new subtype in [Question].
 * 2. A new content composable.
 * 3. A new `is Question.NewType ->` branch in the `when` block below.
 *
 * @param question      The question to render.
 * @param accentColor   Subject accent color forwarded to the content composable.
 * @param bottomPadding System navigation bar height padding.
 * @param onCorrect     Called by the content when the user answers correctly.
 * @param onWrong       Called by the content when the user answers incorrectly.
 * @param onSkip        Called by content types that support skipping (FindAnswer, FindOperator).
 */
@Composable
internal fun QuestionContent(
    question: Question,
    accentColor: Color,
    bottomPadding: Dp = 0.dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    onSkip: () -> Unit = {}
) {
    when (question) {
        is Question.FindAnswer -> FindAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong,
            onSkip = onSkip
        )
        is Question.FindOperator -> FindOperatorContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong,
            onSkip = onSkip
        )
        is Question.Factorization -> FactorizationContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.LinearEquation -> LinearEquationContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.SystemOfEquations -> SystemOfEquationsContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.SelectFromList -> SelectFromListContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.TypeAnswer -> TypeAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.MapQuiz -> MapQuizContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.PeriodicTableQuiz -> PeriodicTableContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.PeriodicTableByShell -> PeriodicTableByShellContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.PeriodicTableByName -> PeriodicTableByNameContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.EquationBalance -> EquationBalanceContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.ElementCardQuiz -> ElementCardContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.GraphTypeAnswer -> GraphTypeAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.GraphSelectFromList -> GraphSelectFromListContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.ExpressionTypeAnswer -> ExpressionTypeAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.FractionAnswer -> FractionAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.DecimalAnswer -> DecimalAnswerContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
        is Question.ComparisonQuiz -> ComparisonQuizContent(
            question = question,
            accentColor = accentColor,
            bottomPadding = bottomPadding,
            onCorrect = onCorrect,
            onWrong = onWrong
        )
    }
}
