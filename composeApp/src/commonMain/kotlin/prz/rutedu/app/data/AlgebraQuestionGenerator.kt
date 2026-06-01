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

    fun totalFor(lessonId: String): Int = generateFor(lessonId, seed = 0L).size

    // -- algebra_1_1: Upraszczanie wyrażeń ---------------------------------------------------

    private fun algebra_1_1_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = "Rozwiń i uprość: x*(x + 2)",
            correctExpr = "x^2+2*x", displayCorrect = "x² + 2x",
            hint = Hint("Mnożymy każdy składnik nawiasu przez x.",
                steps = listOf("x*(x+2)", "= x*x + x*2", "= x² + 2x"))),
        ExpressionTypeAnswer(1,
            prompt = "Rozwiń i uprość: (x + 1)*(x + 1)",
            correctExpr = "x^2+2*x+1", displayCorrect = "x² + 2x + 1",
            hint = Hint("To wzór skróconego mnożenia: (a+b)² = a²+2ab+b².",
                sectionTitle = "WZORY SKRÓCONEGO MNOŻENIA",
                items = listOf("(a+b)² = a² + 2ab + b²"),
                steps = listOf("(x+1)² = x² + 2·x·1 + 1²", "= x² + 2x + 1"))),
        ExpressionTypeAnswer(2,
            prompt = "Rozwiń i uprość: (x + 3)*(x - 3)",
            correctExpr = "x^2-9", displayCorrect = "x² - 9",
            hint = Hint("To wzór skróconego mnożenia: (a+b)(a-b) = a²-b².",
                steps = listOf("(x+3)(x-3) = x² - 3²", "= x² - 9"))),
        ExpressionTypeAnswer(3,
            prompt = "Rozwiń i uprość: 2*x*(x + 4)",
            correctExpr = "2*x^2+8*x", displayCorrect = "2x² + 8x",
            hint = Hint("Mnożymy 2x przez każdy składnik.",
                steps = listOf("2x*(x+4)", "= 2x*x + 2x*4", "= 2x² + 8x"))),
        ExpressionTypeAnswer(4,
            prompt = "Rozwiń i uprość: (x + 2)*(x + 3)",
            correctExpr = "x^2+5*x+6", displayCorrect = "x² + 5x + 6",
            hint = Hint("Mnożymy krzyżowo każdą parę składników.",
                steps = listOf("(x+2)(x+3)", "= x²+3x+2x+6", "= x² + 5x + 6"))),
        ExpressionTypeAnswer(5,
            prompt = "Rozwiń i uprość: (x - 4)*(x + 4)",
            correctExpr = "x^2-16", displayCorrect = "x² - 16",
            hint = Hint("Wzór (a+b)(a-b) = a²-b².",
                steps = listOf("(x-4)(x+4) = x² - 4²", "= x² - 16"))),
        ExpressionTypeAnswer(6,
            prompt = "Rozwiń i uprość: (x - 2)²",
            correctExpr = "x^2-4*x+4", displayCorrect = "x² - 4x + 4",
            hint = Hint("Wzór (a-b)² = a²-2ab+b².",
                steps = listOf("(x-2)² = x² - 2·x·2 + 2²", "= x² - 4x + 4"))),
        ExpressionTypeAnswer(7,
            prompt = "Rozwiń i uprość: 3*x*(2*x - 1)",
            correctExpr = "6*x^2-3*x", displayCorrect = "6x² - 3x",
            hint = Hint("Mnożymy 3x przez każdy składnik.",
                steps = listOf("3x*(2x-1)", "= 3x*2x - 3x*1", "= 6x² - 3x"))),
        ExpressionTypeAnswer(8,
            prompt = "Rozwiń i uprość: (2*x + 1)²",
            correctExpr = "4*x^2+4*x+1", displayCorrect = "4x² + 4x + 1",
            hint = Hint("Wzór (a+b)² = a²+2ab+b², gdzie a=2x, b=1.",
                steps = listOf("(2x)² = 4x²", "2*(2x)*1 = 4x", "1² = 1", "Wynik: 4x²+4x+1"))),
        ExpressionTypeAnswer(9,
            prompt = "Rozwiń i uprość: (x + 5)*(x - 2)",
            correctExpr = "x^2+3*x-10", displayCorrect = "x² + 3x - 10",
            hint = Hint("Mnożymy krzyżowo każdą parę.",
                steps = listOf("x*x + x*(-2) + 5*x + 5*(-2)", "= x² - 2x + 5x - 10", "= x² + 3x - 10")))
    )

    private fun algebra_1_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, "Ile wynosi x*(x+2)?",
            listOf("x²+2x", "x²+2", "2x+2", "x²-2x"), setOf(0),
            hint = Hint("Mnożymy x przez każdy składnik: x*x + x*2 = x²+2x.")),
        SelectFromList(1, "Ile wynosi (x+1)²?",
            listOf("x²+2x+1", "x²+1", "x²-2x+1", "2x+1"), setOf(0),
            hint = Hint("(a+b)² = a²+2ab+b².")),
        SelectFromList(2, "Ile wynosi (x+3)(x-3)?",
            listOf("x²-9", "x²+9", "x²-3", "x²+6x-9"), setOf(0),
            hint = Hint("(a+b)(a-b) = a²-b².")),
        SelectFromList(3, "Ile wynosi 2x*(x+4)?",
            listOf("2x²+8x", "2x+8", "x²+8x", "2x²+4"), setOf(0)),
        SelectFromList(4, "Ile wynosi (x+2)(x+3)?",
            listOf("x²+5x+6", "x²+6x+5", "x²+5x+5", "x²+6"), setOf(0)),
        SelectFromList(5, "Ile wynosi (x-4)(x+4)?",
            listOf("x²-16", "x²+16", "x²-8x-16", "x²-8"), setOf(0),
            hint = Hint("(a-b)(a+b) = a²-b².")),
        SelectFromList(6, "Ile wynosi (x-2)²?",
            listOf("x²-4x+4", "x²+4x+4", "x²-4", "x²-2x+4"), setOf(0)),
        SelectFromList(7, "Ile wynosi 3x*(2x-1)?",
            listOf("6x²-3x", "6x-3x", "6x²-1", "5x²-3x"), setOf(0)),
        SelectFromList(8, "Ile wynosi (2x+1)²?",
            listOf("4x²+4x+1", "2x²+4x+1", "4x²+1", "4x²+2x+1"), setOf(0)),
        SelectFromList(9, "Ile wynosi (x+5)(x-2)?",
            listOf("x²+3x-10", "x²-3x-10", "x²+3x+10", "x²-10"), setOf(0))
    )

    // -- algebra_1_2: Pochodne ---------------------------------------------------------------

    private val derivativeHint = Hint(
        mainText = "Reguła: d/dx(xⁿ) = n·xⁿ⁻¹",
        boldPart = "d/dx(xⁿ) = n·xⁿ⁻¹",
        sectionTitle = "REGUŁY RÓŻNICZKOWANIA",
        items = listOf(
            "d/dx(xⁿ) = n·xⁿ⁻¹",
            "d/dx(c·f(x)) = c·f'(x)",
            "d/dx(f(x)+g(x)) = f'(x)+g'(x)",
            "d/dx(stała) = 0"
        )
    )

    private fun algebra_1_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = "Oblicz pochodną f(x) = x²",
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint("d/dx(x²) = 2·x²⁻¹ = 2x",
                steps = listOf("f(x) = x²", "n=2, więc f'(x) = 2·x^(2-1)", "= 2x"))),
        ExpressionTypeAnswer(1,
            prompt = "Oblicz pochodną f(x) = x³",
            correctExpr = "3*x^2", displayCorrect = "3x²",
            hint = Hint("d/dx(x³) = 3x²",
                steps = listOf("n=3, więc f'(x) = 3·x^(3-1)", "= 3x²"))),
        ExpressionTypeAnswer(2,
            prompt = "Oblicz pochodną f(x) = 3*x²",
            correctExpr = "6*x", displayCorrect = "6x",
            hint = Hint("Stały mnożnik 3 zostaje, różniczkujemy x².",
                steps = listOf("f'(x) = 3·d/dx(x²)", "= 3·2x", "= 6x"))),
        ExpressionTypeAnswer(3,
            prompt = "Oblicz pochodną f(x) = 2*x³",
            correctExpr = "6*x^2", displayCorrect = "6x²",
            hint = Hint("Stały mnożnik 2 zostaje, różniczkujemy x³.",
                steps = listOf("f'(x) = 2·3x²", "= 6x²"))),
        ExpressionTypeAnswer(4,
            prompt = "Oblicz pochodną f(x) = x² + 2*x",
            correctExpr = "2*x+2", displayCorrect = "2x + 2",
            hint = Hint("Różniczkujemy każdy składnik osobno.",
                steps = listOf("d/dx(x²) = 2x", "d/dx(2x) = 2", "f'(x) = 2x + 2"))),
        ExpressionTypeAnswer(5,
            prompt = "Oblicz pochodną f(x) = 3*x² + 4*x",
            correctExpr = "6*x+4", displayCorrect = "6x + 4",
            hint = derivativeHint.copy(steps = listOf("d/dx(3x²) = 6x", "d/dx(4x) = 4", "f'(x) = 6x + 4"))),
        ExpressionTypeAnswer(6,
            prompt = "Oblicz pochodną f(x) = x² - 5",
            correctExpr = "2*x", displayCorrect = "2x",
            hint = Hint("Pochodna stałej (-5) wynosi 0.",
                steps = listOf("d/dx(x²) = 2x", "d/dx(-5) = 0", "f'(x) = 2x"))),
        ExpressionTypeAnswer(7,
            prompt = "Oblicz pochodną f(x) = 4*x² + 2*x + 1",
            correctExpr = "8*x+2", displayCorrect = "8x + 2",
            hint = derivativeHint.copy(steps = listOf("d/dx(4x²)=8x", "d/dx(2x)=2", "d/dx(1)=0", "f'(x)=8x+2"))),
        ExpressionTypeAnswer(8,
            prompt = "Oblicz pochodną f(x) = x³ + x²",
            correctExpr = "3*x^2+2*x", displayCorrect = "3x² + 2x",
            hint = derivativeHint.copy(steps = listOf("d/dx(x³)=3x²", "d/dx(x²)=2x", "f'(x)=3x²+2x"))),
        ExpressionTypeAnswer(9,
            prompt = "Oblicz pochodną f(x) = 2*x³ + 3*x",
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
        mainText = "Reguła: ∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
        boldPart = "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
        sectionTitle = "REGUŁY CAŁKOWANIA",
        items = listOf(
            "∫xⁿ dx = xⁿ⁺¹/(n+1) + C",
            "∫c·f(x) dx = c·∫f(x) dx",
            "∫(f+g) dx = ∫f dx + ∫g dx"
        )
    )

    private fun algebra_1_3_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0,
            prompt = "Oblicz całkę nieoznaczoną: ∫2x dx",
            correctExpr = "x^2", displayCorrect = "x²",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = Hint("∫2x dx = 2·x²/2 = x²",
                steps = listOf("∫2x dx = 2·∫x dx", "= 2·x²/2", "= x²"))),
        ExpressionTypeAnswer(1,
            prompt = "Oblicz całkę nieoznaczoną: ∫3x² dx",
            correctExpr = "x^3", displayCorrect = "x³",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = Hint("∫3x² dx = 3·x³/3 = x³",
                steps = listOf("∫3x² dx = 3·x³/3", "= x³"))),
        ExpressionTypeAnswer(2,
            prompt = "Oblicz całkę nieoznaczoną: ∫x dx",
            correctExpr = "x^2/2", displayCorrect = "x²/2",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫x dx = x^(1+1)/(1+1)", "= x²/2"))),
        ExpressionTypeAnswer(3,
            prompt = "Oblicz całkę nieoznaczoną: ∫4x³ dx",
            correctExpr = "x^4", displayCorrect = "x⁴",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = Hint("∫4x³ dx = 4·x⁴/4 = x⁴",
                steps = listOf("4·x^(3+1)/(3+1)", "= 4·x⁴/4", "= x⁴"))),
        ExpressionTypeAnswer(4,
            prompt = "Oblicz całkę nieoznaczoną: ∫(2x + 3) dx",
            correctExpr = "x^2+3*x", displayCorrect = "x² + 3x",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫2x dx = x²", "∫3 dx = 3x", "Wynik: x²+3x"))),
        ExpressionTypeAnswer(5,
            prompt = "Oblicz całkę nieoznaczoną: ∫(6x² + 2x) dx",
            correctExpr = "2*x^3+x^2", displayCorrect = "2x³ + x²",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫6x² dx = 6·x³/3 = 2x³", "∫2x dx = x²", "Wynik: 2x³+x²"))),
        ExpressionTypeAnswer(6,
            prompt = "Oblicz całkę nieoznaczoną: ∫5 dx",
            correctExpr = "5*x", displayCorrect = "5x",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = Hint("∫stała dx = stała·x", steps = listOf("∫5 dx = 5x"))),
        ExpressionTypeAnswer(7,
            prompt = "Oblicz całkę nieoznaczoną: ∫(x² + 1) dx",
            correctExpr = "x^3/3+x", displayCorrect = "x³/3 + x",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫x² dx = x³/3", "∫1 dx = x", "Wynik: x³/3+x"))),
        ExpressionTypeAnswer(8,
            prompt = "Oblicz całkę nieoznaczoną: ∫(3x² - 2x) dx",
            correctExpr = "x^3-x^2", displayCorrect = "x³ - x²",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫3x² dx = x³", "∫2x dx = x²", "Wynik: x³-x²"))),
        ExpressionTypeAnswer(9,
            prompt = "Oblicz całkę nieoznaczoną: ∫(4x + 6) dx",
            correctExpr = "2*x^2+6*x", displayCorrect = "2x² + 6x",
            inlineHint = "Podaj wynik bez stałej całkowania C",
            hint = integralHint.copy(steps = listOf("∫4x dx = 2x²", "∫6 dx = 6x", "Wynik: 2x²+6x")))
    )

    private fun algebra_1_3_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, "Jaka jest całka ∫2x dx?",
            listOf("x²", "x²/2", "2x²", "x"), setOf(0)),
        SelectFromList(1, "Jaka jest całka ∫3x² dx?",
            listOf("x³", "x²", "3x³", "x³/3"), setOf(0)),
        SelectFromList(2, "Jaka jest całka ∫x dx?",
            listOf("x²/2", "x²", "2x", "x/2"), setOf(0)),
        SelectFromList(3, "Jaka jest całka ∫4x³ dx?",
            listOf("x⁴", "4x⁴", "x³", "x⁴/4"), setOf(0)),
        SelectFromList(4, "Jaka jest całka ∫(2x+3) dx?",
            listOf("x²+3x", "x²+3", "2x²+3x", "x+3x"), setOf(0)),
        SelectFromList(5, "Jaka jest całka ∫5 dx?",
            listOf("5x", "5", "x/5", "5x²"), setOf(0)),
        SelectFromList(6, "Jaka jest całka ∫(x²+1) dx?",
            listOf("x³/3+x", "x³+x", "x²/2+x", "x³/3+1"), setOf(0)),
        SelectFromList(7, "Jaka jest całka ∫(3x²-2x) dx?",
            listOf("x³-x²", "3x³-x²", "x³-2x²", "x²-x"), setOf(0)),
        SelectFromList(8, "Jaka jest całka ∫(4x+6) dx?",
            listOf("2x²+6x", "4x²+6x", "2x+6", "4x²+6"), setOf(0)),
        SelectFromList(9, "Jaka jest całka ∫6x² dx?",
            listOf("2x³", "6x³", "x³", "6x²/2"), setOf(0))
    )

    // -- algebra_2_1: Równania liniowe -------------------------------------------------------

    private val linearHint = Hint(
        mainText = "Przenosimy x na lewą stronę, liczby na prawą.",
        sectionTitle = "METODA ROZWIĄZYWANIA",
        items = listOf(
            "1. Przenieś wyrazy z x na lewą stronę",
            "2. Przenieś stałe na prawą stronę",
            "3. Podziel obie strony przez współczynnik przy x"
        )
    )

    private fun algebra_2_1_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, "Rozwiąż równanie: 2x + 4 = 0",
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = "Wpisz samą liczbę (np. -2)",
            hint = linearHint.copy(steps = listOf("2x = -4", "x = -4/2", "x = -2"))),
        ExpressionTypeAnswer(1, "Rozwiąż równanie: 3x - 6 = 0",
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("3x = 6", "x = 6/3", "x = 2"))),
        ExpressionTypeAnswer(2, "Rozwiąż równanie: x + 5 = 10",
            correctExpr = "5", displayCorrect = "x = 5",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("x = 10-5", "x = 5"))),
        ExpressionTypeAnswer(3, "Rozwiąż równanie: 4x = 12",
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("x = 12/4", "x = 3"))),
        ExpressionTypeAnswer(4, "Rozwiąż równanie: 2x - 8 = 0",
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("2x = 8", "x = 4"))),
        ExpressionTypeAnswer(5, "Rozwiąż równanie: 5x + 10 = 0",
            correctExpr = "-2", displayCorrect = "x = -2",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("5x = -10", "x = -10/5", "x = -2"))),
        ExpressionTypeAnswer(6, "Rozwiąż równanie: 3x + 9 = 0",
            correctExpr = "-3", displayCorrect = "x = -3",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("3x = -9", "x = -3"))),
        ExpressionTypeAnswer(7, "Rozwiąż równanie: x - 7 = 3",
            correctExpr = "10", displayCorrect = "x = 10",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("x = 3+7", "x = 10"))),
        ExpressionTypeAnswer(8, "Rozwiąż równanie: 6x = 18",
            correctExpr = "3", displayCorrect = "x = 3",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("x = 18/6", "x = 3"))),
        ExpressionTypeAnswer(9, "Rozwiąż równanie: 2x + 6 = 14",
            correctExpr = "4", displayCorrect = "x = 4",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("2x = 14-6 = 8", "x = 4"))),
        ExpressionTypeAnswer(10, "Rozwiąż równanie: 7x - 14 = 0",
            correctExpr = "2", displayCorrect = "x = 2",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("7x = 14", "x = 2"))),
        ExpressionTypeAnswer(11, "Rozwiąż równanie: x + 3 = -1",
            correctExpr = "-4", displayCorrect = "x = -4",
            inlineHint = "Wpisz samą liczbę",
            hint = linearHint.copy(steps = listOf("x = -1-3", "x = -4")))
    )

    private fun algebra_2_1_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, "Rozwiąż: 2x + 4 = 0",
            listOf("x = -2", "x = 2", "x = -4", "x = 4"), setOf(0)),
        SelectFromList(1, "Rozwiąż: 3x - 6 = 0",
            listOf("x = 2", "x = -2", "x = 6", "x = 3"), setOf(0)),
        SelectFromList(2, "Rozwiąż: x + 5 = 10",
            listOf("x = 5", "x = 15", "x = -5", "x = 10"), setOf(0)),
        SelectFromList(3, "Rozwiąż: 4x = 12",
            listOf("x = 3", "x = 4", "x = 8", "x = 12"), setOf(0)),
        SelectFromList(4, "Rozwiąż: 5x + 10 = 0",
            listOf("x = -2", "x = 2", "x = -10", "x = 10"), setOf(0)),
        SelectFromList(5, "Rozwiąż: 6x = 18",
            listOf("x = 3", "x = 6", "x = 12", "x = 2"), setOf(0)),
        SelectFromList(6, "Rozwiąż: x - 7 = 3",
            listOf("x = 10", "x = -4", "x = 4", "x = 7"), setOf(0)),
        SelectFromList(7, "Rozwiąż: 2x + 6 = 14",
            listOf("x = 4", "x = 10", "x = 3", "x = 7"), setOf(0)),
        SelectFromList(8, "Rozwiąż: 3x + 9 = 0",
            listOf("x = -3", "x = 3", "x = -9", "x = 9"), setOf(0)),
        SelectFromList(9, "Rozwiąż: 7x - 14 = 0",
            listOf("x = 2", "x = -2", "x = 7", "x = 14"), setOf(0))
    )

    // -- algebra_2_2: Równania kwadratowe ---------------------------------------------------

    private val quadraticHint = Hint(
        mainText = "x² + bx + c = 0. Delta = b²-4c. Pierwiastki: (-b±√Δ)/2",
        boldPart = "Delta = b²-4c",
        sectionTitle = "WZORY",
        items = listOf(
            "Δ = b² - 4ac",
            "x₁ = (-b - √Δ) / 2a",
            "x₂ = (-b + √Δ) / 2a",
            "Jeśli Δ=0, jedno rozwiązanie: x = -b/2a"
        )
    )

    private fun algebra_2_2_android(): List<ExpressionTypeAnswer> = listOf(
        ExpressionTypeAnswer(0, "Rozwiąż: x² - 5x + 6 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "2", displayCorrect = "x₁ = 2",
            inlineHint = "Wpisz mniejszy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("Δ=25-24=1", "x₁=(5-1)/2=2", "x₂=(5+1)/2=3"))),
        ExpressionTypeAnswer(1, "Rozwiąż: x² - 5x + 6 = 0  (podaj większy pierwiastek)",
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = "Wpisz większy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("Δ=1", "x₁=2, x₂=3"))),
        ExpressionTypeAnswer(2, "Rozwiąż: x² + x - 6 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = "Wpisz mniejszy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("Δ=1+24=25", "x₁=(-1-5)/2=-3", "x₂=(-1+5)/2=2"))),
        ExpressionTypeAnswer(3, "Rozwiąż: x² + x - 6 = 0  (podaj większy pierwiastek)",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = "Wpisz większy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("x₁=-3, x₂=2"))),
        ExpressionTypeAnswer(4, "Rozwiąż: x² - 4 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "-2", displayCorrect = "x₁ = -2",
            inlineHint = "Wpisz mniejszy pierwiastek",
            hint = Hint("x²=4, więc x=±2", steps = listOf("x²=4", "x₁=-2, x₂=2"))),
        ExpressionTypeAnswer(5, "Rozwiąż: x² - 4 = 0  (podaj większy pierwiastek)",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = "Wpisz większy pierwiastek"),
        ExpressionTypeAnswer(6, "Rozwiąż: x² - 3x + 2 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "1", displayCorrect = "x₁ = 1",
            inlineHint = "Wpisz mniejszy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("Δ=9-8=1", "x₁=(3-1)/2=1", "x₂=2"))),
        ExpressionTypeAnswer(7, "Rozwiąż: x² - 3x + 2 = 0  (podaj większy pierwiastek)",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = "Wpisz większy pierwiastek"),
        ExpressionTypeAnswer(8, "Rozwiąż: x² + 2x - 8 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "-4", displayCorrect = "x₁ = -4",
            inlineHint = "Wpisz mniejszy pierwiastek",
            hint = quadraticHint.copy(steps = listOf("Δ=4+32=36", "x₁=(-2-6)/2=-4", "x₂=2"))),
        ExpressionTypeAnswer(9, "Rozwiąż: x² + 2x - 8 = 0  (podaj większy pierwiastek)",
            correctExpr = "2", displayCorrect = "x₂ = 2",
            inlineHint = "Wpisz większy pierwiastek"),
        ExpressionTypeAnswer(10, "Rozwiąż: x² - 9 = 0  (podaj mniejszy pierwiastek)",
            correctExpr = "-3", displayCorrect = "x₁ = -3",
            inlineHint = "Wpisz mniejszy pierwiastek"),
        ExpressionTypeAnswer(11, "Rozwiąż: x² - 9 = 0  (podaj większy pierwiastek)",
            correctExpr = "3", displayCorrect = "x₂ = 3",
            inlineHint = "Wpisz większy pierwiastek")
    )

    private fun algebra_2_2_ios(): List<SelectFromList> = listOf(
        SelectFromList(0, "Pierwiastki x² - 5x + 6 = 0 to:",
            listOf("x=2 i x=3", "x=-2 i x=-3", "x=1 i x=6", "x=-1 i x=6"), setOf(0),
            hint = quadraticHint),
        SelectFromList(1, "Pierwiastki x² + x - 6 = 0 to:",
            listOf("x=-3 i x=2", "x=3 i x=-2", "x=-1 i x=6", "x=1 i x=-6"), setOf(0)),
        SelectFromList(2, "Pierwiastki x² - 4 = 0 to:",
            listOf("x=-2 i x=2", "x=2 i x=2", "x=-4 i x=4", "x=1 i x=4"), setOf(0)),
        SelectFromList(3, "Pierwiastki x² - 3x + 2 = 0 to:",
            listOf("x=1 i x=2", "x=-1 i x=-2", "x=1 i x=3", "x=2 i x=3"), setOf(0)),
        SelectFromList(4, "Pierwiastki x² + 2x - 8 = 0 to:",
            listOf("x=-4 i x=2", "x=4 i x=-2", "x=-4 i x=-2", "x=4 i x=2"), setOf(0)),
        SelectFromList(5, "Pierwiastki x² - 9 = 0 to:",
            listOf("x=-3 i x=3", "x=3 i x=3", "x=-9 i x=9", "x=1 i x=9"), setOf(0)),
        SelectFromList(6, "Pierwiastki x² + 4x + 4 = 0 to:",
            listOf("x=-2 (podwójny)", "x=2 i x=-2", "x=2 (podwójny)", "x=-4 i x=-1"), setOf(0),
            hint = Hint("Δ=0 → jeden pierwiastek podwójny: x = -b/2a")),
        SelectFromList(7, "Ile pierwiastków ma x² + 1 = 0?",
            listOf("0 (brak rzeczywistych)", "1", "2", "nieskończenie wiele"), setOf(0),
            hint = Hint("Δ = 0 - 4·1 = -4 < 0 → brak pierwiastków rzeczywistych."))
    )
}
