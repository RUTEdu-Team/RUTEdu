package prz.rutedu.app.data

import prz.rutedu.app.math.mathEngineAvailable
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.ExpressionTypeAnswer
import prz.rutedu.app.models.Question.SelectFromList

private fun String.format(vararg args: Any): String {
    var result = this
    for (arg in args) {
        val indexS = result.indexOf("%s")
        val indexD = result.indexOf("%d")
        val index = when {
            indexS == -1 -> indexD
            indexD == -1 -> indexS
            else -> minOf(indexS, indexD)
        }
        if (index != -1) result = result.substring(0, index) + arg.toString() + result.substring(index + 2)
    }
    return result
}

/**
 * Generates algebra quiz questions.
 *
 * On Android ([mathEngineAvailable] = true): returns [ExpressionTypeAnswer] questions where
 * the student types an algebraic expression verified via [prz.rutedu.app.math.MathEngine].
 *
 * On iOS ([mathEngineAvailable] = false): returns [SelectFromList] fallback questions with
 * hardcoded choices, so no CAS engine is needed.
 *
 * ## Adding a new algebra lesson
 * 1. Add the lesson to [prz.rutedu.app.data.SubjectRepository] with an id starting with `"algebra_"`.
 * 2. Implement `private fun algebra_X_Y_android()` and `private fun algebra_X_Y_ios()` here.
 * 3. Register both in the `when` block inside [generateFor].
 */
object AlgebraQuestionGenerator {

    private fun s(key: String) = GeneratorStrings.algebra(key)

    private fun t(pl: String, en: String): String =
        if (GeneratorStrings.algebra("word.and") == "i") pl else en

    /**
     * Generates a list of algebra questions for the specified [lessonId].
     *
     * Depending on whether the Math Eclipse engine is available (on Android), it returns
     * either typed algebraic expression questions or fallback multiple-choice select questions.
     *
     * @param lessonId   The identifier of the algebra lesson (e.g. `"algebra_1_1"`).
     * @param seed       The random seed to ensure deterministic question generation.
     * @param excludeIds The set of question IDs to exclude from the generated list.
     * @return List of generated algebra [Question]s.
     */
    fun generateFor(lessonId: String, seed: Long, excludeIds: Set<Int> = emptySet()): List<Question> {
        val all: List<Question> = if (mathEngineAvailable) {
            when (lessonId) {
                "algebra_1_1" -> algebra_1_1_android()
                "algebra_1_2" -> algebra_1_2_android()
                "algebra_1_3" -> algebra_1_3_android()
                "algebra_2_1" -> algebra_2_1_android()
                "algebra_2_2" -> algebra_2_2_android()
                else -> emptyList()
            }
        } else {
            when (lessonId) {
                "algebra_1_1" -> algebra_1_1_ios()
                "algebra_1_2" -> algebra_1_2_ios()
                "algebra_1_3" -> algebra_1_3_ios()
                "algebra_2_1" -> algebra_2_1_ios()
                "algebra_2_2" -> algebra_2_2_ios()
                else -> emptyList()
            }
        }
        return if (excludeIds.isEmpty()) all else all.filter { it.id !in excludeIds }
    }

    /**
     * Returns the total number of questions available for the specified [lessonId].
     *
     * @param lessonId The identifier of the algebra lesson.
     * @return The count of generated questions.
     */
    fun totalFor(lessonId: String): Int = generateFor(lessonId, seed = 0L).size

    // -- algebra_1_1: Upraszczanie wyrażeń ---------------------------------------------------

    private fun algebra_1_1_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = s("prompt.expand") + " x*(x + 2)",
            correctExpr = "x^2+2*x", displayCorrect = "x² + 2x",
            hint = Hint(s("hint.multiply_each_by_x"),
                steps = listOf("x*(x+2)", "= x*x + x*2", "= x² + 2x"))),
        ExpressionTypeAnswer(1,
            prompt = s("prompt.expand") + " (x + 1)*(x + 1)",
            correctExpr = "x^2+2*x+1", displayCorrect = "x² + 2x + 1",
            hint = Hint(s("hint.short_mult_sq_sum"),
                sectionTitle = s("section.short_mult"),
                items = listOf("(a+b)² = a² + 2ab + b²"),
                steps = listOf("(x+1)² = x² + 2·x·1 + 1²", "= x² + 2x + 1"))),
        ExpressionTypeAnswer(2,
            prompt = s("prompt.expand") + " (x + 3)*(x - 3)",
            correctExpr = "x^2-9", displayCorrect = "x² - 9",
            hint = Hint(s("hint.short_mult_diff_sq"),
                steps = listOf("(x+3)(x-3) = x² - 3²", "= x² - 9"))),
        ExpressionTypeAnswer(3,
            prompt = s("prompt.expand") + " 2*x*(x + 4)",
            correctExpr = "2*x^2+8*x", displayCorrect = "2x² + 8x",
            hint = Hint(s("hint.multiply_2x"),
                steps = listOf("2x*(x+4)", "= 2x*x + 2x*4", "= 2x² + 8x"))),
        ExpressionTypeAnswer(4,
            prompt = s("prompt.expand") + " (x + 2)*(x + 3)",
            correctExpr = "x^2+5*x+6", displayCorrect = "x² + 5x + 6",
            hint = Hint(s("hint.cross_multiply"),
                steps = listOf("(x+2)(x+3)", "= x²+3x+2x+6", "= x² + 5x + 6"))),
        ExpressionTypeAnswer(5,
            prompt = s("prompt.expand") + " (x - 4)*(x + 4)",
            correctExpr = "x^2-16", displayCorrect = "x² - 16",
            hint = Hint(s("hint.formula_diff_sq"),
                steps = listOf("(x-4)(x+4) = x² - 4²", "= x² - 16"))),
        ExpressionTypeAnswer(6,
            prompt = s("prompt.expand") + " (x - 2)²",
            correctExpr = "x^2-4*x+4", displayCorrect = "x² - 4x + 4",
            hint = Hint(s("hint.formula_sq_diff"),
                steps = listOf("(x-2)² = x² - 2·x·2 + 2²", "= x² - 4x + 4"))),
        ExpressionTypeAnswer(7,
            prompt = s("prompt.expand") + " 3*x*(2*x - 1)",
            correctExpr = "6*x^2-3*x", displayCorrect = "6x² - 3x",
            hint = Hint(s("hint.multiply_3x"),
                steps = listOf("3x*(2x-1)", "= 3x*2x - 3x*1", "= 6x² - 3x"))),
        ExpressionTypeAnswer(8,
            prompt = s("prompt.expand") + " (2*x + 1)²",
            correctExpr = "4*x^2+4*x+1", displayCorrect = "4x² + 4x + 1",
            hint = Hint(s("hint.formula_sq_sum_2x1"),
                steps = listOf("(2x)² = 4x²", "2*(2x)*1 = 4x", "1² = 1", s("step.result") + " 4x²+4x+1"))),
        ExpressionTypeAnswer(9,
            prompt = s("prompt.expand") + " (x + 5)*(x - 2)",
            correctExpr = "x^2+3*x-10", displayCorrect = "x² + 3x - 10",
            hint = Hint(s("hint.cross_multiply_pair"),
                steps = listOf("x*x + x*(-2) + 5*x + 5*(-2)", "= x² - 2x + 5x - 10", "= x² + 3x - 10")))
    )

    private fun algebra_1_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, s("fmt.ios_what_is").format("x*(x+2)"),
            listOf("x²+2x", "x²+2", "2x+2", "x²-2x"), setOf(0),
            hint = Hint(s("hint.multiply_each_by_x"))),
        SelectFromList(1, s("fmt.ios_what_is").format("(x+1)²"),
            listOf("x²+2x+1", "x²+1", "x²-2x+1", "2x+1"), setOf(0),
            hint = Hint(s("hint.short_mult_sq_sum"))),
        SelectFromList(2, s("fmt.ios_what_is").format("(x+3)(x-3)"),
            listOf("x²-9", "x²+9", "x²-3", "x²+6x-9"), setOf(0),
            hint = Hint(s("hint.short_mult_diff_sq"))),
        SelectFromList(3, s("fmt.ios_what_is").format("2x*(x+4)"),
            listOf("2x²+8x", "2x+8", "x²+8x", "2x²+4"), setOf(0)),
        SelectFromList(4, s("fmt.ios_what_is").format("(x+2)(x+3)"),
            listOf("x²+5x+6", "x²+6x+5", "x²+5x+5", "x²+6"), setOf(0)),
        SelectFromList(5, s("fmt.ios_what_is").format("(x-4)(x+4)"),
            listOf("x²-16", "x²+16", "x²-8x-16", "x²-8"), setOf(0),
            hint = Hint(s("hint.formula_diff_sq"))),
        SelectFromList(6, s("fmt.ios_what_is").format("(x-2)²"),
            listOf("x²-4x+4", "x²+4x+4", "x²-4", "x²-2x+4"), setOf(0)),
        SelectFromList(7, s("fmt.ios_what_is").format("3x*(2x-1)"),
            listOf("6x²-3x", "6x-3x", "6x²-1", "5x²-3x"), setOf(0)),
        SelectFromList(8, s("fmt.ios_what_is").format("(2x+1)²"),
            listOf("4x²+4x+1", "2x²+4x+1", "4x²+1", "4x²+2x+1"), setOf(0)),
        SelectFromList(9, s("fmt.ios_what_is").format("(x+5)(x-2)"),
            listOf("x²+3x-10", "x²-3x-10", "x²+3x+10", "x²-10"), setOf(0))
    )

    // -- algebra_1_2: Pochodne ---------------------------------------------------------------

    private fun derivativeHint() = Hint(
        mainText = s("hint.derivative_rule"),
        boldPart = "d/dx(xⁿ) = n·xⁿ⁻¹",
        sectionTitle = s("section.differentiation"),
        items = listOf(
            "d/dx(xⁿ) = n·xⁿ⁻¹",
            "d/dx(c·f(x)) = c·f'(x)",
            "d/dx(f(x)+g(x)) = f'(x)+g'(x)",
            s("item.derivative_constant")
        )
    )

    private fun algebra_1_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = s("prompt.derivative") + " x²",
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint("d/dx(x²) = 2·x²⁻¹ = 2x",
                steps = listOf("f(x) = x²", s("step.n2_so"), "= 2x"))),
        ExpressionTypeAnswer(1,
            prompt = s("prompt.derivative") + " x³",
            correctExpr = "3*x^2", displayCorrect = "3x²",
            hint = Hint("d/dx(x³) = 3x²",
                steps = listOf(s("step.n3_so"), "= 3x²"))),
        ExpressionTypeAnswer(2,
            prompt = s("prompt.derivative") + " 3*x²",
            correctExpr = "6*x", displayCorrect = "6x",
            hint = Hint(s("hint.constant_factor_3"),
                steps = listOf("f'(x) = 3·d/dx(x²)", "= 3·2x", "= 6x"))),
        ExpressionTypeAnswer(3,
            prompt = s("prompt.derivative") + " 2*x³",
            correctExpr = "6*x^2", displayCorrect = "6x²",
            hint = Hint(s("hint.constant_factor_2"),
                steps = listOf("f'(x) = 2·3x²", "= 6x²"))),
        ExpressionTypeAnswer(4,
            prompt = s("prompt.derivative") + " x² + 2*x",
            correctExpr = "2*x+2", displayCorrect = "2x + 2",
            hint = Hint(s("hint.differentiate_each"),
                steps = listOf("d/dx(x²) = 2x", "d/dx(2x) = 2", "f'(x) = 2x + 2"))),
        ExpressionTypeAnswer(5,
            prompt = s("prompt.derivative") + " 3*x² + 4*x",
            correctExpr = "6*x+4", displayCorrect = "6x + 4",
            hint = derivativeHint().copy(steps = listOf("d/dx(3x²) = 6x", "d/dx(4x) = 4", "f'(x) = 6x + 4"))),
        ExpressionTypeAnswer(6,
            prompt = s("prompt.derivative") + " x² - 5",
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint(s("hint.derivative_constant_zero"),
                steps = listOf("d/dx(x²) = 2x", "d/dx(-5) = 0", "f'(x) = 2x"))),
        ExpressionTypeAnswer(7,
            prompt = s("prompt.derivative") + " 4*x² + 2*x + 1",
            correctExpr = "8*x+2", displayCorrect = "8x + 2",
            hint = derivativeHint().copy(steps = listOf("d/dx(4x²)=8x", "d/dx(2x)=2", "d/dx(1)=0", "f'(x)=8x+2"))),
        ExpressionTypeAnswer(8,
            prompt = s("prompt.derivative") + " x³ + x²",
            correctExpr = "3*x^2+2*x", displayCorrect = "3x² + 2x",
            hint = derivativeHint().copy(steps = listOf("d/dx(x³)=3x²", "d/dx(x²)=2x", "f'(x)=3x²+2x"))),
        ExpressionTypeAnswer(9,
            prompt = s("prompt.derivative") + " 2*x³ + 3*x",
            correctExpr = "6*x^2+3", displayCorrect = "6x² + 3",
            hint = derivativeHint().copy(steps = listOf("d/dx(2x³)=6x²", "d/dx(3x)=3", "f'(x)=6x²+3")))
    )

    private fun algebra_1_2_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, s("fmt.ios_derivative").format("x²"),
            listOf("2x", "x", "2", "x²"), setOf(0),
            hint = Hint(s("hint.derivative_rule"))),
        SelectFromList(1, s("fmt.ios_derivative").format("x³"),
            listOf("3x²", "x²", "3x", "3"), setOf(0)),
        SelectFromList(2, s("fmt.ios_derivative").format("3x²"),
            listOf("6x", "3x", "6x²", "6"), setOf(0),
            hint = Hint(s("hint.constant_factor_3_short"))),
        SelectFromList(3, s("fmt.ios_derivative").format("x²+2x"),
            listOf("2x+2", "2x+1", "x+2", "2x²+2"), setOf(0)),
        SelectFromList(4, s("fmt.ios_derivative").format("3x²+4x"),
            listOf("6x+4", "3x+4", "6x+3", "6x²+4"), setOf(0)),
        SelectFromList(5, s("fmt.ios_derivative").format("x²-5"),
            listOf("2x", "2x-5", "2x-0", "x"), setOf(0),
            hint = Hint(s("hint.derivative_constant_generic"))),
        SelectFromList(6, s("fmt.ios_derivative").format("4x²+2x+1"),
            listOf("8x+2", "4x+2", "8x+2x", "8x"), setOf(0)),
        SelectFromList(7, s("fmt.ios_derivative").format("x³+x²"),
            listOf("3x²+2x", "x²+x", "3x+2x", "3x²"), setOf(0)),
        SelectFromList(8, s("fmt.ios_derivative").format("2x³+3x"),
            listOf("6x²+3", "6x+3", "2x²+3", "6x²"), setOf(0)),
        SelectFromList(9, s("fmt.ios_derivative").format("5x²"),
            listOf("10x", "5x", "10x²", "5"), setOf(0))
    )

    // -- algebra_1_3: Całki ------------------------------------------------------------------

    private fun integralHint() = Hint(
        mainText = s("hint.integral_rule"),
        boldPart = "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
        sectionTitle = s("section.integration"),
        items = listOf(
            "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
            "∫c·f(x) dx = c·∫f(x) dx",
            "∫(f+g) dx = ∫f dx + ∫g dx"
        )
    )

    private fun algebra_1_3_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = s("prompt.integral") + " ∫2x dx",
            correctExpr = "x^2", displayCorrect = "x²",
            inlineHint = s("input.no_constant"),
            hint = Hint("∫2x dx = 2·x²/2 = x²",
                steps = listOf("∫2x dx = 2·∫x dx", "= 2·x²/2", "= x²"))),
        ExpressionTypeAnswer(1,
            prompt = s("prompt.integral") + " ∫3x² dx",
            correctExpr = "x^3", displayCorrect = "x³",
            inlineHint = s("input.no_constant"),
            hint = Hint("∫3x² dx = 3·x³/3 = x³",
                steps = listOf("∫3x² dx = 3·x³/3", "= x³"))),
        ExpressionTypeAnswer(2,
            prompt = s("prompt.integral") + " ∫x dx",
            correctExpr = "x^2/2", displayCorrect = "x²/2",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫x dx = x^(1+1)/(1+1)", "= x²/2"))),
        ExpressionTypeAnswer(3,
            prompt = s("prompt.integral") + " ∫4x³ dx",
            correctExpr = "x^4", displayCorrect = "x⁴",
            inlineHint = s("input.no_constant"),
            hint = Hint("∫4x³ dx = 4·x⁴/4 = x⁴",
                steps = listOf("4·x^(3+1)/(3+1)", "= 4·x⁴/4", "= x⁴"))),
        ExpressionTypeAnswer(4,
            prompt = s("prompt.integral") + " ∫(2x + 3) dx",
            correctExpr = "x^2+3*x", displayCorrect = "x² + 3x",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫2x dx = x²", "∫3 dx = 3x", s("step.result") + " x²+3x"))),
        ExpressionTypeAnswer(5,
            prompt = s("prompt.integral") + " ∫(6x² + 2x) dx",
            correctExpr = "2*x^3+x^2", displayCorrect = "2x³ + x²",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫6x² dx = 6·x³/3 = 2x³", "∫2x dx = x²", s("step.result") + " 2x³+x²"))),
        ExpressionTypeAnswer(6,
            prompt = s("prompt.integral") + " ∫5 dx",
            correctExpr = "5*x", displayCorrect = "5x",
            inlineHint = s("input.no_constant"),
            hint = Hint(s("hint.integral_constant"), steps = listOf("∫5 dx = 5x"))),
        ExpressionTypeAnswer(7,
            prompt = s("prompt.integral") + " ∫(x² + 1) dx",
            correctExpr = "x^3/3+x", displayCorrect = "x³/3 + x",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫x² dx = x³/3", "∫1 dx = x", s("step.result") + " x³/3+x"))),
        ExpressionTypeAnswer(8,
            prompt = s("prompt.integral") + " ∫(3x² - 2x) dx",
            correctExpr = "x^3-x^2", displayCorrect = "x³ - x²",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫3x² dx = x³", "∫2x dx = x²", s("step.result") + " x³-x²"))),
        ExpressionTypeAnswer(9,
            prompt = s("prompt.integral") + " ∫(4x + 6) dx",
            correctExpr = "2*x^2+6*x", displayCorrect = "2x² + 6x",
            inlineHint = s("input.no_constant"),
            hint = integralHint().copy(steps = listOf("∫4x dx = 2x²", "∫6 dx = 6x", s("step.result") + " 2x²+6x")))
    )

    private fun algebra_1_3_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, s("fmt.ios_integral").format("∫2x dx"),
            listOf("x²", "x²/2", "2x²", "x"), setOf(0)),
        SelectFromList(1, s("fmt.ios_integral").format("∫3x² dx"),
            listOf("x³", "x²", "3x³", "x³/3"), setOf(0)),
        SelectFromList(2, s("fmt.ios_integral").format("∫x dx"),
            listOf("x²/2", "x²", "2x", "x/2"), setOf(0)),
        SelectFromList(3, s("fmt.ios_integral").format("∫4x³ dx"),
            listOf("x⁴", "4x⁴", "x³", "x⁴/4"), setOf(0)),
        SelectFromList(4, s("fmt.ios_integral").format("∫(2x+3) dx"),
            listOf("x²+3x", "x²+3", "2x²+3x", "x+3x"), setOf(0)),
        SelectFromList(5, s("fmt.ios_integral").format("∫5 dx"),
            listOf("5x", "5", "x/5", "5x²"), setOf(0)),
        SelectFromList(6, s("fmt.ios_integral").format("∫(x²+1) dx"),
            listOf("x³/3+x", "x³+x", "x²/2+x", "x³/3+1"), setOf(0)),
        SelectFromList(7, s("fmt.ios_integral").format("∫(3x²-2x) dx"),
            listOf("x³-x²", "3x³-x²", "x³-2x²", "x²-x"), setOf(0)),
        SelectFromList(8, s("fmt.ios_integral").format("∫(4x+6) dx"),
            listOf("2x²+6x", "4x²+6x", "2x+6", "4x²+6"), setOf(0)),
        SelectFromList(9, s("fmt.ios_integral").format("∫6x² dx"),
            listOf("2x³", "6x³", "x³", "6x²/2"), setOf(0))
    )

    // -- algebra_2_1: Równania liniowe -------------------------------------------------------

    private fun linearHint() = Hint(
        mainText = s("hint.linear_method"),
        sectionTitle = s("section.solving_method"),
        items = listOf(
            s("item.linear_step1"),
            s("item.linear_step2"),
            s("item.linear_step3")
        )
    )

    private fun algebra_2_1_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, s("prompt.solve") + " 2x + 4 = 0",
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = s("input.type_number_ex"),
            hint = linearHint().copy(steps = listOf("2x = -4", "x = -4/2", "x = -2"))),
        ExpressionTypeAnswer(1, s("prompt.solve") + " 3x - 6 = 0",
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("3x = 6", "x = 6/3", "x = 2"))),
        ExpressionTypeAnswer(2, s("prompt.solve") + " x + 5 = 10",
            correctExpr = "5", displayCorrect = "x = 5",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("x = 10-5", "x = 5"))),
        ExpressionTypeAnswer(3, s("prompt.solve") + " 4x = 12",
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("x = 12/4", "x = 3"))),
        ExpressionTypeAnswer(4, s("prompt.solve") + " 2x - 8 = 0",
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("2x = 8", "x = 4"))),
        ExpressionTypeAnswer(5, s("prompt.solve") + " 5x + 10 = 0",
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("5x = -10", "x = -10/5", "x = -2"))),
        ExpressionTypeAnswer(6, s("prompt.solve") + " 3x + 9 = 0",
            correctExpr = "-3", displayCorrect = "x = -3",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("3x = -9", "x = -3"))),
        ExpressionTypeAnswer(7, s("prompt.solve") + " x - 7 = 3",
            correctExpr = "10", displayCorrect = "x = 10",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("x = 3+7", "x = 10"))),
        ExpressionTypeAnswer(8, s("prompt.solve") + " 6x = 18",
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("x = 18/6", "x = 3"))),
        ExpressionTypeAnswer(9, s("prompt.solve") + " 2x + 6 = 14",
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("2x = 14-6 = 8", "x = 4"))),
        ExpressionTypeAnswer(10, s("prompt.solve") + " 7x - 14 = 0",
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("7x = 14", "x = 2"))),
        ExpressionTypeAnswer(11, s("prompt.solve") + " x + 3 = -1",
            correctExpr = "-4", displayCorrect = "x = -4",
            inlineHint = s("input.type_number"),
            hint = linearHint().copy(steps = listOf("x = -1-3", "x = -4")))
    )

    private fun algebra_2_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, s("prompt.solve_short") + " 2x + 4 = 0",
            listOf("x = -2", "x = 2", "x = -4", "x = 4"), setOf(0)),
        SelectFromList(1, s("prompt.solve_short") + " 3x - 6 = 0",
            listOf("x = 2", "x = -2", "x = 6", "x = 3"), setOf(0)),
        SelectFromList(2, s("prompt.solve_short") + " x + 5 = 10",
            listOf("x = 5", "x = 15", "x = -5", "x = 10"), setOf(0)),
        SelectFromList(3, s("prompt.solve_short") + " 4x = 12",
            listOf("x = 3", "x = 4", "x = 8", "x = 12"), setOf(0)),
        SelectFromList(4, s("prompt.solve_short") + " 5x + 10 = 0",
            listOf("x = -2", "x = 2", "x = -10", "x = 10"), setOf(0)),
        SelectFromList(5, s("prompt.solve_short") + " 6x = 18",
            listOf("x = 3", "x = 6", "x = 12", "x = 2"), setOf(0)),
        SelectFromList(6, s("prompt.solve_short") + " x - 7 = 3",
            listOf("x = 10", "x = -4", "x = 4", "x = 7"), setOf(0)),
        SelectFromList(7, s("prompt.solve_short") + " 2x + 6 = 14",
            listOf("x = 4", "x = 10", "x = 3", "x = 7"), setOf(0)),
        SelectFromList(8, s("prompt.solve_short") + " 3x + 9 = 0",
            listOf("x = -3", "x = 3", "x = -9", "x = 9"), setOf(0)),
        SelectFromList(9, s("prompt.solve_short") + " 7x - 14 = 0",
            listOf("x = 2", "x = -2", "x = 7", "x = 14"), setOf(0))
    )

    // -- algebra_2_2: Równania kwadratowe ---------------------------------------------------

    private fun quadraticHint() = Hint(
        mainText = "x² + bx + c = 0. Delta = b²-4c. Pierwiastki: (-b±√Δ)/2",
        boldPart = "Delta = b²-4c",
        sectionTitle = s("section.formulas"),
        items = listOf(
            "Δ = b² - 4ac",
            "x₁ = (-b - √Δ) / 2a",
            "x₂ = (-b + √Δ) / 2a",
            s("item.delta_zero")
        )
    )

    private fun algebra_2_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, s("prompt.solve") + " x² - 5x + 6 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "2", displayCorrect = "x₁ = 2",
            inlineHint = s("input.smaller_root"),
            hint = quadraticHint().copy(steps = listOf("Δ=25-24=1", "x₁=(5-1)/2=2", "x₂=(5+1)/2=3"))),
        ExpressionTypeAnswer(1, s("prompt.solve") + " x² - 5x + 6 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = s("input.larger_root"),
            hint = quadraticHint().copy(steps = listOf("Δ=1", "x₁=2, x₂=3"))),
        ExpressionTypeAnswer(2, s("prompt.solve") + " x² + x - 6 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = s("input.smaller_root"),
            hint = quadraticHint().copy(steps = listOf("Δ=1+24=25", "x₁=(-1-5)/2=-3", "x₂=(-1+5)/2=2"))),
        ExpressionTypeAnswer(3, s("prompt.solve") + " x² + x - 6 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = s("input.larger_root"),
            hint = quadraticHint().copy(steps = listOf("x₁=-3, x₂=2"))),
        ExpressionTypeAnswer(4, s("prompt.solve") + " x² - 4 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "-2", displayCorrect = "x₁ = -2",
            inlineHint = s("input.smaller_root"),
            hint = Hint(s("hint.quad_simple"), steps = listOf("x²=4", "x₁=-2, x₂=2"))),
        ExpressionTypeAnswer(5, s("prompt.solve") + " x² - 4 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = s("input.larger_root")),
        ExpressionTypeAnswer(6, s("prompt.solve") + " x² - 3x + 2 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "1", displayCorrect = "x₁ = 1",
            inlineHint = s("input.smaller_root"),
            hint = quadraticHint().copy(steps = listOf("Δ=9-8=1", "x₁=(3-1)/2=1", "x₂=2"))),
        ExpressionTypeAnswer(7, s("prompt.solve") + " x² - 3x + 2 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = s("input.larger_root")),
        ExpressionTypeAnswer(8, s("prompt.solve") + " x² + 2x - 8 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "-4", displayCorrect = "x₁ = -4",
            inlineHint = s("input.smaller_root"),
            hint = quadraticHint().copy(steps = listOf("Δ=4+32=36", "x₁=(-2-6)/2=-4", "x₂=2"))),
        ExpressionTypeAnswer(9, s("prompt.solve") + " x² + 2x - 8 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = s("input.larger_root")),
        ExpressionTypeAnswer(10, s("prompt.solve") + " x² - 9 = 0  (" + s("input.smaller_root_paren") + ")",
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = s("input.smaller_root")),
        ExpressionTypeAnswer(11, s("prompt.solve") + " x² - 9 = 0  (" + s("input.larger_root_paren") + ")",
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = s("input.larger_root"))
    )

    private fun algebra_2_2_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, s("fmt.ios_roots_of").format("x² - 5x + 6 = 0"),
            listOf(s("fmt.ios_roots_pair").format("2", "3"), s("fmt.ios_roots_pair").format("-2", "-3"), s("fmt.ios_roots_pair").format("1", "6"), s("fmt.ios_roots_pair").format("-1", "6")), setOf(0),
            hint = quadraticHint()),
        SelectFromList(1, s("fmt.ios_roots_of").format("x² + x - 6 = 0"),
            listOf(s("fmt.ios_roots_pair").format("-3", "2"), s("fmt.ios_roots_pair").format("3", "-2"), s("fmt.ios_roots_pair").format("-1", "6"), s("fmt.ios_roots_pair").format("1", "-6")), setOf(0)),
        SelectFromList(2, s("fmt.ios_roots_of").format("x² - 4 = 0"),
            listOf(s("fmt.ios_roots_pair").format("-2", "2"), s("fmt.ios_roots_pair").format("2", "2"), s("fmt.ios_roots_pair").format("-4", "4"), s("fmt.ios_roots_pair").format("1", "4")), setOf(0)),
        SelectFromList(3, s("fmt.ios_roots_of").format("x² - 3x + 2 = 0"),
            listOf(s("fmt.ios_roots_pair").format("1", "2"), s("fmt.ios_roots_pair").format("-1", "-2"), s("fmt.ios_roots_pair").format("1", "3"), s("fmt.ios_roots_pair").format("2", "3")), setOf(0)),
        SelectFromList(4, s("fmt.ios_roots_of").format("x² + 2x - 8 = 0"),
            listOf(s("fmt.ios_roots_pair").format("-4", "2"), s("fmt.ios_roots_pair").format("4", "-2"), s("fmt.ios_roots_pair").format("-4", "-2"), s("fmt.ios_roots_pair").format("4", "2")), setOf(0)),
        SelectFromList(5, s("fmt.ios_roots_of").format("x² - 9 = 0"),
            listOf(s("fmt.ios_roots_pair").format("-3", "3"), s("fmt.ios_roots_pair").format("3", "3"), s("fmt.ios_roots_pair").format("-9", "9"), s("fmt.ios_roots_pair").format("1", "9")), setOf(0)),
        SelectFromList(6, s("fmt.ios_roots_of").format("x² + 4x + 4 = 0"),
            listOf(s("fmt.ios_root_double").format("-2"), s("fmt.ios_roots_pair").format("2", "-2"), s("fmt.ios_root_double").format("2"), s("fmt.ios_roots_pair").format("-4", "-1")), setOf(0),
            hint = Hint(s("hint.quad_delta_zero"))),
        SelectFromList(7, s("fmt.ios_how_many_roots").format("x² + 1 = 0"),
            listOf(s("word.no_real_roots"), "1", "2", s("word.infinitely_many")), setOf(0),
            hint = Hint(s("hint.quad_no_real")))
    )
}
