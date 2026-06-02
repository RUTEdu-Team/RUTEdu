package prz.rutedu.app.data

import prz.rutedu.app.math.mathEngineAvailable
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.ExpressionTypeAnswer
import prz.rutedu.app.models.Question.SelectFromList

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
            prompt = t("Rozwiń i uprość: x*(x + 2)", "Expand and simplify: x*(x + 2)"),
            correctExpr = "x^2+2*x", displayCorrect = "x² + 2x",
            hint = Hint(t("Mnożymy każdy składnik nawiasu przez x.", "We multiply each term of the parenthesis by x."),
                steps = listOf("x*(x+2)", "= x*x + x*2", "= x² + 2x"))),
        ExpressionTypeAnswer(1,
            prompt = t("Rozwiń i uprość: (x + 1)*(x + 1)", "Expand and simplify: (x + 1)*(x + 1)"),
            correctExpr = "x^2+2*x+1", displayCorrect = "x² + 2x + 1",
            hint = Hint(t("To wzór skróconego mnożenia: (a+b)² = a²+2ab+b².", "This is a short multiplication formula: (a+b)² = a²+2ab+b²."),
                sectionTitle = t("WZORY SKRÓCONEGO MNOŻENIA", "SHORT MULTIPLICATION FORMULAS"),
                items = listOf("(a+b)² = a² + 2ab + b²"),
                steps = listOf("(x+1)² = x² + 2·x·1 + 1²", "= x² + 2x + 1"))),
        ExpressionTypeAnswer(2,
            prompt = t("Rozwiń i uprość: (x + 3)*(x - 3)", "Expand and simplify: (x + 3)*(x - 3)"),
            correctExpr = "x^2-9", displayCorrect = "x² - 9",
            hint = Hint(t("To wzór skróconego mnożenia: (a+b)(a-b) = a²-b².", "This is a short multiplication formula: (a+b)(a-b) = a²-b²."),
                steps = listOf("(x+3)(x-3) = x² - 3²", "= x² - 9"))),
        ExpressionTypeAnswer(3,
            prompt = t("Rozwiń i uprość: 2*x*(x + 4)", "Expand and simplify: 2*x*(x + 4)"),
            correctExpr = "2*x^2+8*x", displayCorrect = "2x² + 8x",
            hint = Hint(t("Mnożymy 2x przez każdy składnik.", "We multiply 2x by each term."),
                steps = listOf("2x*(x+4)", "= 2x*x + 2x*4", "= 2x² + 8x"))),
        ExpressionTypeAnswer(4,
            prompt = t("Rozwiń i uprość: (x + 2)*(x + 3)", "Expand and simplify: (x + 2)*(x + 3)"),
            correctExpr = "x^2+5*x+6", displayCorrect = "x² + 5x + 6",
            hint = Hint(t("Mnożymy krzyżowo każdą parę składników.", "We cross-multiply each pair of terms."),
                steps = listOf("(x+2)(x+3)", "= x²+3x+2x+6", "= x² + 5x + 6"))),
        ExpressionTypeAnswer(5,
            prompt = t("Rozwiń i uprość: (x - 4)*(x + 4)", "Expand and simplify: (x - 4)*(x + 4)"),
            correctExpr = "x^2-16", displayCorrect = "x² - 16",
            hint = Hint(t("Wzór (a+b)(a-b) = a²-b².", "Formula (a+b)(a-b) = a²-b²."),
                steps = listOf("(x-4)(x+4) = x² - 4²", "= x² - 16"))),
        ExpressionTypeAnswer(6,
            prompt = t("Rozwiń i uprość: (x - 2)²", "Expand and simplify: (x - 2)²"),
            correctExpr = "x^2-4*x+4", displayCorrect = "x² - 4x + 4",
            hint = Hint(t("Wzór (a-b)² = a²-2ab+b².", "Formula (a-b)² = a²-2ab+b²."),
                steps = listOf("(x-2)² = x² - 2·x·2 + 2²", "= x² - 4x + 4"))),
        ExpressionTypeAnswer(7,
            prompt = t("Rozwiń i uprość: 3*x*(2*x - 1)", "Expand and simplify: 3*x*(2*x - 1)"),
            correctExpr = "6*x^2-3*x", displayCorrect = "6x² - 3x",
            hint = Hint(t("Mnożymy 3x przez każdy składnik.", "We multiply 3x by each term."),
                steps = listOf("3x*(2x-1)", "= 3x*2x - 3x*1", "= 6x² - 3x"))),
        ExpressionTypeAnswer(8,
            prompt = t("Rozwiń i uprość: (2*x + 1)²", "Expand and simplify: (2*x + 1)²"),
            correctExpr = "4*x^2+4*x+1", displayCorrect = "4x² + 4x + 1",
            hint = Hint(t("Wzór (a+b)² = a²+2ab+b², gdzie a=2x, b=1.", "Formula (a+b)² = a²+2ab+b², where a=2x, b=1."),
                steps = listOf("(2x)² = 4x²", "2*(2x)*1 = 4x", "1² = 1", t("Wynik: 4x²+4x+1", "Result: 4x²+4x+1")))),
        ExpressionTypeAnswer(9,
            prompt = t("Rozwiń i uprość: (x + 5)*(x - 2)", "Expand and simplify: (x + 5)*(x - 2)"),
            correctExpr = "x^2+3*x-10", displayCorrect = "x² + 3x - 10",
            hint = Hint(t("Mnożymy krzyżowo każdą parę.", "We cross-multiply each pair."),
                steps = listOf("x*x + x*(-2) + 5*x + 5*(-2)", "= x² - 2x + 5x - 10", "= x² + 3x - 10")))
    )

    private fun algebra_1_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, t("Ile wynosi x*(x+2)?", "What is x*(x+2)?"),
            listOf("x²+2x", "x²+2", "2x+2", "x²-2x"), setOf(0),
            hint = Hint(t("Mnożymy x przez każdy składnik: x*x + x*2 = x²+2x.", "We multiply x by each term: x*x + x*2 = x²+2x."))),
        SelectFromList(1, t("Ile wynosi (x+1)²?", "What is (x+1)²?"),
            listOf("x²+2x+1", "x²+1", "x²-2x+1", "2x+1"), setOf(0),
            hint = Hint(t("(a+b)² = a²+2ab+b².", "(a+b)² = a²+2ab+b²."))),
        SelectFromList(2, t("Ile wynosi (x+3)(x-3)?", "What is (x+3)(x-3)?"),
            listOf("x²-9", "x²+9", "x²-3", "x²+6x-9"), setOf(0),
            hint = Hint(t("(a+b)(a-b) = a²-b².", "(a+b)(a-b) = a²-b²."))),
        SelectFromList(3, t("Ile wynosi 2x*(x+4)?", "What is 2x*(x+4)?"),
            listOf("2x²+8x", "2x+8", "x²+8x", "2x²+4"), setOf(0)),
        SelectFromList(4, t("Ile wynosi (x+2)(x+3)?", "What is (x+2)(x+3)?"),
            listOf("x²+5x+6", "x²+6x+5", "x²+5x+5", "x²+6"), setOf(0)),
        SelectFromList(5, t("Ile wynosi (x-4)(x+4)?", "What is (x-4)(x+4)?"),
            listOf("x²-16", "x²+16", "x²-8x-16", "x²-8"), setOf(0),
            hint = Hint(t("(a-b)(a+b) = a²-b².", "(a-b)(a+b) = a²-b²."))),
        SelectFromList(6, t("Ile wynosi (x-2)²?", "What is (x-2)²?"),
            listOf("x²-4x+4", "x²+4x+4", "x²-4", "x²-2x+4"), setOf(0)),
        SelectFromList(7, t("Ile wynosi 3x*(2x-1)?", "What is 3x*(2x-1)?"),
            listOf("6x²-3x", "6x-3x", "6x²-1", "5x²-3x"), setOf(0)),
        SelectFromList(8, t("Ile wynosi (2x+1)²?", "What is (2x+1)²?"),
            listOf("4x²+4x+1", "2x²+4x+1", "4x²+1", "4x²+2x+1"), setOf(0)),
        SelectFromList(9, t("Ile wynosi (x+5)(x-2)?", "What is (x+5)(x-2)?"),
            listOf("x²+3x-10", "x²-3x-10", "x²+3x+10", "x²-10"), setOf(0))
    )

    // -- algebra_1_2: Pochodne ---------------------------------------------------------------

    private val derivativeHint = Hint(
        mainText = t("Reguła: d/dx(xⁿ) = n·xⁿ⁻¹", "Rule: d/dx(xⁿ) = n·xⁿ⁻¹"),
        boldPart = "d/dx(xⁿ) = n·xⁿ⁻¹",
        sectionTitle = t("REGUŁY RÓŻNICZKOWANIA", "DIFFERENTIATION RULES"),
        items = listOf(
            "d/dx(xⁿ) = n·xⁿ⁻¹",
            "d/dx(c·f(x)) = c·f'(x)",
            "d/dx(f(x)+g(x)) = f'(x)+g'(x)",
            t("d/dx(stała) = 0", "d/dx(constant) = 0")
        )
    )

    private fun algebra_1_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = t("Oblicz pochodną f(x) = x²", "Calculate the derivative f(x) = x²"),
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint("d/dx(x²) = 2·x²⁻¹ = 2x",
                steps = listOf("f(x) = x²", t("n=2, więc f'(x) = 2·x^(2-1)", "n=2, so f'(x) = 2·x^(2-1)"), "= 2x"))),
        ExpressionTypeAnswer(1,
            prompt = t("Oblicz pochodną f(x) = x³", "Calculate the derivative f(x) = x³"),
            correctExpr = "3*x^2", displayCorrect = "3x²",
            hint = Hint("d/dx(x³) = 3x²",
                steps = listOf(t("n=3, więc f'(x) = 3·x^(3-1)", "n=3, so f'(x) = 3·x^(3-1)"), "= 3x²"))),
        ExpressionTypeAnswer(2,
            prompt = t("Oblicz pochodną f(x) = 3*x²", "Calculate the derivative f(x) = 3*x²"),
            correctExpr = "6*x", displayCorrect = "6x",
            hint = Hint(t("Stały mnożnik 3 zostaje, różniczkujemy x².", "The constant factor 3 remains, we differentiate x²."),
                steps = listOf("f'(x) = 3·d/dx(x²)", "= 3·2x", "= 6x"))),
        ExpressionTypeAnswer(3,
            prompt = t("Oblicz pochodną f(x) = 2*x³", "Calculate the derivative f(x) = 2*x³"),
            correctExpr = "6*x^2", displayCorrect = "6x²",
            hint = Hint(t("Stały mnożnik 2 zostaje, różniczkujemy x³.", "The constant factor 2 remains, we differentiate x³."),
                steps = listOf("f'(x) = 2·3x²", "= 6x²"))),
        ExpressionTypeAnswer(4,
            prompt = t("Oblicz pochodną f(x) = x² + 2*x", "Calculate the derivative f(x) = x² + 2*x"),
            correctExpr = "2*x+2", displayCorrect = "2x + 2",
            hint = Hint(t("Różniczkujemy każdy składnik osobo.", "We differentiate each term separately."),
                steps = listOf("d/dx(x²) = 2x", "d/dx(2x) = 2", "f'(x) = 2x + 2"))),
        ExpressionTypeAnswer(5,
            prompt = t("Oblicz pochodną f(x) = 3*x² + 4*x", "Calculate the derivative f(x) = 3*x² + 4*x"),
            correctExpr = "6*x+4", displayCorrect = "6x + 4",
            hint = derivativeHint.copy(steps = listOf("d/dx(3x²) = 6x", "d/dx(4x) = 4", "f'(x) = 6x + 4"))),
        ExpressionTypeAnswer(6,
            prompt = t("Oblicz pochodną f(x) = x² - 5", "Calculate the derivative f(x) = x² - 5"),
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint(t("Pochodna stałej (-5) wynosi 0.", "The derivative of a constant (-5) is 0."),
                steps = listOf("d/dx(x²) = 2x", "d/dx(-5) = 0", "f'(x) = 2x"))),
        ExpressionTypeAnswer(7,
            prompt = t("Oblicz pochodną f(x) = 4*x² + 2*x + 1", "Calculate the derivative f(x) = 4*x² + 2*x + 1"),
            correctExpr = "8*x+2", displayCorrect = "8x + 2",
            hint = derivativeHint.copy(steps = listOf("d/dx(4x²)=8x", "d/dx(2x)=2", "d/dx(1)=0", "f'(x)=8x+2"))),
        ExpressionTypeAnswer(8,
            prompt = t("Oblicz pochodną f(x) = x³ + x²", "Calculate the derivative f(x) = x³ + x²"),
            correctExpr = "3*x^2+2*x", displayCorrect = "3x² + 2x",
            hint = derivativeHint.copy(steps = listOf("d/dx(x³)=3x²", "d/dx(x²)=2x", "f'(x)=3x²+2x"))),
        ExpressionTypeAnswer(9,
            prompt = t("Oblicz pochodną f(x) = 2*x³ + 3*x", "Calculate the derivative f(x) = 2*x³ + 3*x"),
            correctExpr = "6*x^2+3", displayCorrect = "6x² + 3",
            hint = derivativeHint.copy(steps = listOf("d/dx(2x³)=6x²", "d/dx(3x)=3", "f'(x)=6x²+3")))
    )

    private fun algebra_1_2_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, "Jaka jest pochodna f(x) = x²?",
            listOf("2x", "x", "2", "x²"), setOf(0),
            hint = Hint("d/dx(xⁿ) = n·xⁿ⁻¹, więc d/dx(x²) = 2x.")),
        SelectFromList(1, "Jaka jest pochodna f(x) = x³?",
            listOf("3x²", "x²", "3x", "3"), setOf(0)),
        SelectFromList(2, "Jaka jest pochodna f(x) = 3x²?",
            listOf("6x", "3x", "6x²", "6"), setOf(0),
            hint = Hint("Mnożnik 3 zostaje: 3·2x = 6x.")),
        SelectFromList(3, "Jaka jest pochodna f(x) = x²+2x?",
            listOf("2x+2", "2x+1", "x+2", "2x²+2"), setOf(0)),
        SelectFromList(4, "Jaka jest pochodna f(x) = 3x²+4x?",
            listOf("6x+4", "3x+4", "6x+3", "6x²+4"), setOf(0)),
        SelectFromList(5, "Jaka jest pochodna f(x) = x²-5?",
            listOf("2x", "2x-5", "2x-0", "x"), setOf(0),
            hint = Hint("Pochodna stałej jest 0.")),
        SelectFromList(6, "Jaka jest pochodna f(x) = 4x²+2x+1?",
            listOf("8x+2", "4x+2", "8x+2x", "8x"), setOf(0)),
        SelectFromList(7, "Jaka jest pochodna f(x) = x³+x²?",
            listOf("3x²+2x", "x²+x", "3x+2x", "3x²"), setOf(0)),
        SelectFromList(8, "Jaka jest pochodna f(x) = 2x³+3x?",
            listOf("6x²+3", "6x+3", "2x²+3", "6x²"), setOf(0)),
        SelectFromList(9, "Jaka jest pochodna f(x) = 5x²?",
            listOf("10x", "5x", "10x²", "5"), setOf(0))
    )

    // -- algebra_1_3: Całki ------------------------------------------------------------------

    private val integralHint = Hint(
        mainText = t("Reguła: ∫xⁿ dx = xⁿ⁺¹/(n+1) + C", "Rule: ∫xⁿ dx = xⁿ⁺¹/(n+1) + C"),
        boldPart = "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
        sectionTitle = t("REGUŁY CAŁKOWANIA", "INTEGRATION RULES"),
        items = listOf(
            "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
            "∫c·f(x) dx = c·∫f(x) dx",
            "∫(f+g) dx = ∫f dx + ∫g dx"
        )
    )

    private fun algebra_1_3_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = t("Oblicz całkę nieoznaczoną: ∫2x dx", "Calculate the indefinite integral: ∫2x dx"),
            correctExpr = "x^2", displayCorrect = "x²",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = Hint("∫2x dx = 2·x²/2 = x²",
                steps = listOf("∫2x dx = 2·∫x dx", "= 2·x²/2", "= x²"))),
        ExpressionTypeAnswer(1,
            prompt = t("Oblicz całkę nieoznaczoną: ∫3x² dx", "Calculate the indefinite integral: ∫3x² dx"),
            correctExpr = "x^3", displayCorrect = "x³",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = Hint("∫3x² dx = 3·x³/3 = x³",
                steps = listOf("∫3x² dx = 3·x³/3", "= x³"))),
        ExpressionTypeAnswer(2,
            prompt = t("Oblicz całkę nieoznaczoną: ∫x dx", "Calculate the indefinite integral: ∫x dx"),
            correctExpr = "x^2/2", displayCorrect = "x²/2",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫x dx = x^(1+1)/(1+1)", "= x²/2"))),
        ExpressionTypeAnswer(3,
            prompt = t("Oblicz całkę nieoznaczoną: ∫4x³ dx", "Calculate the indefinite integral: ∫4x³ dx"),
            correctExpr = "x^4", displayCorrect = "x⁴",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = Hint("∫4x³ dx = 4·x⁴/4 = x⁴",
                steps = listOf("4·x^(3+1)/(3+1)", "= 4·x⁴/4", "= x⁴"))),
        ExpressionTypeAnswer(4,
            prompt = t("Oblicz całkę nieoznaczoną: ∫(2x + 3) dx", "Calculate the indefinite integral: ∫(2x + 3) dx"),
            correctExpr = "x^2+3*x", displayCorrect = "x² + 3x",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫2x dx = x²", "∫3 dx = 3x", t("Wynik: x²+3x", "Result: x²+3x")))),
        ExpressionTypeAnswer(5,
            prompt = t("Oblicz całkę nieoznaczoną: ∫(6x² + 2x) dx", "Calculate the indefinite integral: ∫(6x² + 2x) dx"),
            correctExpr = "2*x^3+x^2", displayCorrect = "2x³ + x²",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫6x² dx = 6·x³/3 = 2x³", "∫2x dx = x²", t("Wynik: 2x³+x²", "Result: 2x³+x²")))),
        ExpressionTypeAnswer(6,
            prompt = t("Oblicz całkę nieoznaczoną: ∫5 dx", "Calculate the indefinite integral: ∫5 dx"),
            correctExpr = "5*x", displayCorrect = "5x",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = Hint(t("∫stała dx = stała·x", "∫constant dx = constant·x"), steps = listOf("∫5 dx = 5x"))),
        ExpressionTypeAnswer(7,
            prompt = t("Oblicz całkę nieoznaczoną: ∫(x² + 1) dx", "Calculate the indefinite integral: ∫(x² + 1) dx"),
            correctExpr = "x^3/3+x", displayCorrect = "x³/3 + x",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫x² dx = x³/3", "∫1 dx = x", t("Wynik: x³/3+x", "Result: x³/3+x")))),
        ExpressionTypeAnswer(8,
            prompt = t("Oblicz całkę nieoznaczoną: ∫(3x² - 2x) dx", "Calculate the indefinite integral: ∫(3x² - 2x) dx"),
            correctExpr = "x^3-x^2", displayCorrect = "x³ - x²",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫3x² dx = x³", "∫2x dx = x²", t("Wynik: x³-x²", "Result: x³-x²")))),
        ExpressionTypeAnswer(9,
            prompt = t("Oblicz całkę nieoznaczoną: ∫(4x + 6) dx", "Calculate the indefinite integral: ∫(4x + 6) dx"),
            correctExpr = "2*x^2+6*x", displayCorrect = "2x² + 6x",
            inlineHint = t("Podaj wynik bez stałej całkowania C", "Provide the result without the integration constant C"),
            hint = integralHint.copy(steps = listOf("∫4x dx = 2x²", "∫6 dx = 6x", t("Wynik: 2x²+6x", "Result: 2x²+6x"))))
    )

    private fun algebra_1_3_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, t("Jaka jest całka ∫2x dx?", "What is the integral ∫2x dx?"),
            listOf("x²", "x²/2", "2x²", "x"), setOf(0)),
        SelectFromList(1, t("Jaka jest całka ∫3x² dx?", "What is the integral ∫3x² dx?"),
            listOf("x³", "x²", "3x³", "x³/3"), setOf(0)),
        SelectFromList(2, t("Jaka jest całka ∫x dx?", "What is the integral ∫x dx?"),
            listOf("x²/2", "x²", "2x", "x/2"), setOf(0)),
        SelectFromList(3, t("Jaka jest całka ∫4x³ dx?", "What is the integral ∫4x³ dx?"),
            listOf("x⁴", "4x⁴", "x³", "x⁴/4"), setOf(0)),
        SelectFromList(4, t("Jaka jest całka ∫(2x+3) dx?", "What is the integral ∫(2x+3) dx?"),
            listOf("x²+3x", "x²+3", "2x²+3x", "x+3x"), setOf(0)),
        SelectFromList(5, t("Jaka jest całka ∫5 dx?", "What is the integral ∫5 dx?"),
            listOf("5x", "5", "x/5", "5x²"), setOf(0)),
        SelectFromList(6, t("Jaka jest całka ∫(x²+1) dx?", "What is the integral ∫(x²+1) dx?"),
            listOf("x³/3+x", "x³+x", "x²/2+x", "x³/3+1"), setOf(0)),
        SelectFromList(7, t("Jaka jest całka ∫(3x²-2x) dx?", "What is the integral ∫(3x²-2x) dx?"),
            listOf("x³-x²", "3x³-x²", "x³-2x²", "x²-x"), setOf(0)),
        SelectFromList(8, t("Jaka jest całka ∫(4x+6) dx?", "What is the integral ∫(4x+6) dx?"),
            listOf("2x²+6x", "4x²+6x", "2x+6", "4x²+6"), setOf(0)),
        SelectFromList(9, t("Jaka jest całka ∫6x² dx?", "What is the integral ∫6x² dx?"),
            listOf("2x³", "6x³", "x³", "6x²/2"), setOf(0))
    )

    // -- algebra_2_1: Równania liniowe -------------------------------------------------------

    private val linearHint = Hint(
        mainText = t("Przenosimy x na lewą stronę, liczby na prawą.", "We move x to the left side, numbers to the right."),
        sectionTitle = t("METODA ROZWIĄZYWANIA", "SOLVING METHOD"),
        items = listOf(
            t("1. Przenieś wyrazy z x na lewą stronę", "1. Move terms with x to the left side"),
            t("2. Przenieś stałe na prawą stronę", "2. Move constants to the right side"),
            t("3. Podziel obie strony przez współczynnik przy x", "3. Divide both sides by the coefficient of x")
        )
    )

    private fun algebra_2_1_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, t("Rozwiąż równanie: 2x + 4 = 0", "Solve the equation: 2x + 4 = 0"),
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = t("Wpisz samą liczbę (np. -2)", "Type only the number (e.g. -2)"),
            hint = linearHint.copy(steps = listOf("2x = -4", "x = -4/2", "x = -2"))),
        ExpressionTypeAnswer(1, t("Rozwiąż równanie: 3x - 6 = 0", "Solve the equation: 3x - 6 = 0"),
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("3x = 6", "x = 6/3", "x = 2"))),
        ExpressionTypeAnswer(2, t("Rozwiąż równanie: x + 5 = 10", "Solve the equation: x + 5 = 10"),
            correctExpr = "5", displayCorrect = "x = 5",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("x = 10-5", "x = 5"))),
        ExpressionTypeAnswer(3, t("Rozwiąż równanie: 4x = 12", "Solve the equation: 4x = 12"),
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("x = 12/4", "x = 3"))),
        ExpressionTypeAnswer(4, t("Rozwiąż równanie: 2x - 8 = 0", "Solve the equation: 2x - 8 = 0"),
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("2x = 8", "x = 4"))),
        ExpressionTypeAnswer(5, t("Rozwiąż równanie: 5x + 10 = 0", "Solve the equation: 5x + 10 = 0"),
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("5x = -10", "x = -10/5", "x = -2"))),
        ExpressionTypeAnswer(6, t("Rozwiąż równanie: 3x + 9 = 0", "Solve the equation: 3x + 9 = 0"),
            correctExpr = "-3", displayCorrect = "x = -3",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("3x = -9", "x = -3"))),
        ExpressionTypeAnswer(7, t("Rozwiąż równanie: x - 7 = 3", "Solve the equation: x - 7 = 3"),
            correctExpr = "10", displayCorrect = "x = 10",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("x = 3+7", "x = 10"))),
        ExpressionTypeAnswer(8, t("Rozwiąż równanie: 6x = 18", "Solve the equation: 6x = 18"),
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("x = 18/6", "x = 3"))),
        ExpressionTypeAnswer(9, t("Rozwiąż równanie: 2x + 6 = 14", "Solve the equation: 2x + 6 = 14"),
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("2x = 14-6 = 8", "x = 4"))),
        ExpressionTypeAnswer(10, t("Rozwiąż równanie: 7x - 14 = 0", "Solve the equation: 7x - 14 = 0"),
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("7x = 14", "x = 2"))),
        ExpressionTypeAnswer(11, t("Rozwiąż równanie: x + 3 = -1", "Solve the equation: x + 3 = -1"),
            correctExpr = "-4", displayCorrect = "x = -4",
            inlineHint = t("Wpisz samą liczbę", "Type only the number"),
            hint = linearHint.copy(steps = listOf("x = -1-3", "x = -4")))
    )

    private fun algebra_2_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, t("Rozwiąż: 2x + 4 = 0", "Solve: 2x + 4 = 0"),
            listOf("x = -2", "x = 2", "x = -4", "x = 4"), setOf(0)),
        SelectFromList(1, t("Rozwiąż: 3x - 6 = 0", "Solve: 3x - 6 = 0"),
            listOf("x = 2", "x = -2", "x = 6", "x = 3"), setOf(0)),
        SelectFromList(2, t("Rozwiąż: x + 5 = 10", "Solve: x + 5 = 10"),
            listOf("x = 5", "x = 15", "x = -5", "x = 10"), setOf(0)),
        SelectFromList(3, t("Rozwiąż: 4x = 12", "Solve: 4x = 12"),
            listOf("x = 3", "x = 4", "x = 8", "x = 12"), setOf(0)),
        SelectFromList(4, t("Rozwiąż: 5x + 10 = 0", "Solve: 5x + 10 = 0"),
            listOf("x = -2", "x = 2", "x = -10", "x = 10"), setOf(0)),
        SelectFromList(5, t("Rozwiąż: 6x = 18", "Solve: 6x = 18"),
            listOf("x = 3", "x = 6", "x = 12", "x = 2"), setOf(0)),
        SelectFromList(6, t("Rozwiąż: x - 7 = 3", "Solve: x - 7 = 3"),
            listOf("x = 10", "x = -4", "x = 4", "x = 7"), setOf(0)),
        SelectFromList(7, t("Rozwiąż: 2x + 6 = 14", "Solve: 2x + 6 = 14"),
            listOf("x = 4", "x = 10", "x = 3", "x = 7"), setOf(0)),
        SelectFromList(8, t("Rozwiąż: 3x + 9 = 0", "Solve: 3x + 9 = 0"),
            listOf("x = -3", "x = 3", "x = -9", "x = 9"), setOf(0)),
        SelectFromList(9, t("Rozwiąż: 7x - 14 = 0", "Solve: 7x - 14 = 0"),
            listOf("x = 2", "x = -2", "x = 7", "x = 14"), setOf(0))
    )

    // -- algebra_2_2: Równania kwadratowe ---------------------------------------------------

    private val quadraticHint = Hint(
        mainText = "x² + bx + c = 0. Delta = b²-4c. Pierwiastki: (-b±√Δ)/2",
        boldPart = "Delta = b²-4c",
        sectionTitle = t("WZORY", "FORMULAS"),
        items = listOf(
            "Δ = b² - 4ac",
            "x₁ = (-b - √Δ) / 2a",
            "x₂ = (-b + √Δ) / 2a",
            t("Jeśli Δ=0, jedno rozwiązanie: x = -b/2a", "If Δ=0, one solution: x = -b/2a")
        )
    )

    private fun algebra_2_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, t("Rozwiąż: x² - 5x + 6 = 0  (podaj mniejszy pierwiastek)", "Solve: x² - 5x + 6 = 0  (provide the smaller root)"),
            correctExpr = "2", displayCorrect = "x₁ = 2",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root"),
            hint = quadraticHint.copy(steps = listOf("Δ=25-24=1", "x₁=(5-1)/2=2", "x₂=(5+1)/2=3"))),
        ExpressionTypeAnswer(1, t("Rozwiąż: x² - 5x + 6 = 0  (podaj większy pierwiastek)", "Solve: x² - 5x + 6 = 0  (provide the larger root)"),
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root"),
            hint = quadraticHint.copy(steps = listOf("Δ=1", "x₁=2, x₂=3"))),
        ExpressionTypeAnswer(2, t("Rozwiąż: x² + x - 6 = 0  (podaj mniejszy pierwiastek)", "Solve: x² + x - 6 = 0  (provide the smaller root)"),
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root"),
            hint = quadraticHint.copy(steps = listOf("Δ=1+24=25", "x₁=(-1-5)/2=-3", "x₂=(-1+5)/2=2"))),
        ExpressionTypeAnswer(3, t("Rozwiąż: x² + x - 6 = 0  (podaj większy pierwiastek)", "Solve: x² + x - 6 = 0  (provide the larger root)"),
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root"),
            hint = quadraticHint.copy(steps = listOf("x₁=-3, x₂=2"))),
        ExpressionTypeAnswer(4, t("Rozwiąż: x² - 4 = 0  (podaj mniejszy pierwiastek)", "Solve: x² - 4 = 0  (provide the smaller root)"),
            correctExpr = "-2", displayCorrect = "x₁ = -2",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root"),
            hint = Hint(t("x²=4, więc x=±2", "x²=4, so x=±2"), steps = listOf("x²=4", "x₁=-2, x₂=2"))),
        ExpressionTypeAnswer(5, t("Rozwiąż: x² - 4 = 0  (podaj większy pierwiastek)", "Solve: x² - 4 = 0  (provide the larger root)"),
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root")),
        ExpressionTypeAnswer(6, t("Rozwiąż: x² - 3x + 2 = 0  (podaj mniejszy pierwiastek)", "Solve: x² - 3x + 2 = 0  (provide the smaller root)"),
            correctExpr = "1", displayCorrect = "x₁ = 1",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root"),
            hint = quadraticHint.copy(steps = listOf("Δ=9-8=1", "x₁=(3-1)/2=1", "x₂=2"))),
        ExpressionTypeAnswer(7, t("Rozwiąż: x² - 3x + 2 = 0  (podaj większy pierwiastek)", "Solve: x² - 3x + 2 = 0  (provide the larger root)"),
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root")),
        ExpressionTypeAnswer(8, t("Rozwiąż: x² + 2x - 8 = 0  (podaj mniejszy pierwiastek)", "Solve: x² + 2x - 8 = 0  (provide the smaller root)"),
            correctExpr = "-4", displayCorrect = "x₁ = -4",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root"),
            hint = quadraticHint.copy(steps = listOf("Δ=4+32=36", "x₁=(-2-6)/2=-4", "x₂=2"))),
        ExpressionTypeAnswer(9, t("Rozwiąż: x² + 2x - 8 = 0  (podaj większy pierwiastek)", "Solve: x² + 2x - 8 = 0  (provide the larger root)"),
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root")),
        ExpressionTypeAnswer(10, t("Rozwiąż: x² - 9 = 0  (podaj mniejszy pierwiastek)", "Solve: x² - 9 = 0  (provide the smaller root)"),
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = t("Wpisz mniejszy pierwiastek", "Type the smaller root")),
        ExpressionTypeAnswer(11, t("Rozwiąż: x² - 9 = 0  (podaj większy pierwiastek)", "Solve: x² - 9 = 0  (provide the larger root)"),
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = t("Wpisz większy pierwiastek", "Type the larger root"))
    )

    private fun algebra_2_2_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, t("Pierwiastki x² - 5x + 6 = 0 to:", "The roots of x² - 5x + 6 = 0 are:"),
            listOf(t("x=2 i x=3", "x=2 and x=3"), t("x=-2 i x=-3", "x=-2 and x=-3"), t("x=1 i x=6", "x=1 and x=6"), t("x=-1 i x=6", "x=-1 and x=6")), setOf(0),
            hint = quadraticHint),
        SelectFromList(1, t("Pierwiastki x² + x - 6 = 0 to:", "The roots of x² + x - 6 = 0 are:"),
            listOf(t("x=-3 i x=2", "x=-3 and x=2"), t("x=3 i x=-2", "x=3 and x=-2"), t("x=-1 i x=6", "x=-1 and x=6"), t("x=1 i x=-6", "x=1 and x=-6")), setOf(0)),
        SelectFromList(2, t("Pierwiastki x² - 4 = 0 to:", "The roots of x² - 4 = 0 are:"),
            listOf(t("x=-2 i x=2", "x=-2 and x=2"), t("x=2 i x=2", "x=2 and x=2"), t("x=-4 i x=4", "x=-4 and x=4"), t("x=1 i x=4", "x=1 and x=4")), setOf(0)),
        SelectFromList(3, t("Pierwiastki x² - 3x + 2 = 0 to:", "The roots of x² - 3x + 2 = 0 are:"),
            listOf(t("x=1 i x=2", "x=1 and x=2"), t("x=-1 i x=-2", "x=-1 and x=-2"), t("x=1 i x=3", "x=1 and x=3"), t("x=2 i x=3", "x=2 and x=3")), setOf(0)),
        SelectFromList(4, t("Pierwiastki x² + 2x - 8 = 0 to:", "The roots of x² + 2x - 8 = 0 are:"),
            listOf(t("x=-4 i x=2", "x=-4 and x=2"), t("x=4 i x=-2", "x=4 and x=-2"), t("x=-4 i x=-2", "x=-4 and x=-2"), t("x=4 i x=2", "x=4 and x=2")), setOf(0)),
        SelectFromList(5, t("Pierwiastki x² - 9 = 0 to:", "The roots of x² - 9 = 0 are:"),
            listOf(t("x=-3 i x=3", "x=-3 and x=3"), t("x=3 i x=3", "x=3 and x=3"), t("x=-9 i x=9", "x=-9 and x=9"), t("x=1 i x=9", "x=1 and x=9")), setOf(0)),
        SelectFromList(6, t("Pierwiastki x² + 4x + 4 = 0 to:", "The roots of x² + 4x + 4 = 0 are:"),
            listOf(t("x=-2 (podwójny)", "x=-2 (double)"), t("x=2 i x=-2", "x=2 and x=-2"), t("x=2 (podwójny)", "x=2 (double)"), t("x=-4 i x=-1", "x=-4 and x=-1")), setOf(0),
            hint = Hint(t("Δ=0 → jeden pierwiastek podwójny: x = -b/2a", "Δ=0 → one double root: x = -b/2a"))),
        SelectFromList(7, t("Ile pierwiastków ma x² + 1 = 0?", "How many roots does x² + 1 = 0 have?"),
            listOf(t("0 (brak rzeczywistych)", "0 (no real roots)"), "1", "2", t("nieskończenie wiele", "infinitely many")), setOf(0),
            hint = Hint(t("Δ = 0 - 4·1 = -4 < 0 → brak pierwiastków rzeczywistych.", "Δ = 0 - 4·1 = -4 < 0 → no real roots.")))
    )
}
