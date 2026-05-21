package prz.rutedu.app.data

import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.MathOperator
import prz.rutedu.app.models.MathOperator.ADD
import prz.rutedu.app.models.MathOperator.SUBTRACT
import prz.rutedu.app.models.MathOperator.MULTIPLY
import prz.rutedu.app.models.MathOperator.DIVIDE
import prz.rutedu.app.models.MathOperator.POWER
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.FindAnswer
import prz.rutedu.app.models.Question.FindOperator
import prz.rutedu.app.models.Question.MapQuiz
import prz.rutedu.app.models.Question.SelectFromList
import prz.rutedu.app.models.Question.TypeAnswer
import prz.rutedu.app.models.Question.EquationBalance
import prz.rutedu.app.models.Question.BalanceTerm
import prz.rutedu.app.models.Question.GraphTypeAnswer
import prz.rutedu.app.models.Question.GraphSelectFromList
import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.MathViewport
import prz.rutedu.app.math.TriangleBuilder

/**
 * Central registry of all static (hardcoded) quiz questions.
 *
 * Questions for non-chemistry lessons are stored as private `List<Question>` fields and
 * registered in the [banks] map at the bottom of this file. Chemistry lessons are routed
 * to [ChemistryQuestionGenerator] which creates questions dynamically from a random seed.
 *
 * ## How to add questions for a new lesson
 *
 * 1. Declare a new private `val` (e.g. `private val mat_6_1: List<Question> = listOf(...)`).
 * 2. Populate it with [Question] instances. Each question's `id` must be unique within
 *    the list (sequential integers from 0 work well).
 * 3. Register it in [banks]: `"mat_6_1" to mat_6_1`.
 * 4. Make sure the lesson exists in [SubjectRepository] with the same id.
 *
 * For chemistry lessons, implement a generator function in [ChemistryQuestionGenerator]
 * instead of adding a static list here.
 */
object QuestionBank {

    /**
     * Returns the ordered list of questions for the given lesson.
     *
     * Routing:
     * - Lesson IDs starting with `"chemia_"` are delegated to [ChemistryQuestionGenerator],
     *   which shuffles and filters the pool using [seed] and [excludeIds].
     * - All other IDs look up the static list in [banks]. The [seed] and [excludeIds]
     *   parameters are **ignored** for static banks.
     *
     * @param lessonId   The lesson identifier (e.g. `"mat_1_1"`, `"chemia_3_1"`).
     * @param seed       Random seed for chemistry lesson generation. Ignored for static banks.
     * @param excludeIds Set of question IDs to omit (previously answered chemistry questions).
     *                   Ignored for static banks.
     * @return Ordered list of questions to present, or an empty list if [lessonId] is not registered.
     */
    fun questionsFor(lessonId: String, seed: Long = 0L, excludeIds: Set<Int> = emptySet()): List<Question> =
        if (lessonId.startsWith("chemia_"))
            ChemistryQuestionGenerator.generateFor(lessonId, seed, excludeIds)
        else
            banks[lessonId] ?: emptyList()

    /**
     * Static questions for Lesson 1-1: "Dodawanie i odejmowanie" (Addition & Subtraction).
     *
     * Focuses on basic operations on real numbers, including using number lines,
     * carrying/borrowing, and identifying operators.
     */
    private val mat_1_1: List<Question> = listOf(
        FindAnswer(0, 3, 5, ADD,
            Hint("Dodawanie to liczenie do przodu.",
                steps = listOf("Masz 3", "Dodajesz 5 kroków", "Wynik: 8"))),
        FindOperator(1, 9, 4, 13, ADD,
            Hint("Wynik jest większy niż obie liczby — to dodawanie.",
                steps = listOf("9 + 4 = 13 ✓", "9 − 4 = 5 ✗"))),
        FindAnswer(2, 12, 4, SUBTRACT,
            Hint("Odejmowanie to liczenie wstecz.",
                steps = listOf("Masz 12", "Cofasz się o 4", "Wynik: 8"))),
        FindOperator(3, 15, 7, 8, SUBTRACT,
            Hint("Wynik jest mniejszy niż pierwsza liczba — to odejmowanie.",
                steps = listOf("15 − 7 = 8 ✓", "15 + 7 = 22 ✗"))),
        FindAnswer(4, 6, 8, ADD,
            Hint("Rozbij 8 na 4 + 4, żeby łatwiej liczyć.",
                steps = listOf("6 + 4 = 10", "10 + 4 = 14"))),
        FindOperator(5, 20, 5, 15, SUBTRACT,
            Hint("Wynik 15 jest mniejszy niż 20.",
                steps = listOf("20 − 5 = 15 ✓", "20 + 5 = 25 ✗"))),
        FindAnswer(6, 17, 9, SUBTRACT,
            Hint("Rozbij odejmowanie: 17 − 9 = 17 − 10 + 1.",
                steps = listOf("17 − 10 = 7", "7 + 1 = 8"))),
        FindOperator(7, 8, 6, 14, ADD,
            Hint("Wynik 14 jest większy niż obie liczby.",
                steps = listOf("8 + 6 = 14 ✓", "8 − 6 = 2 ✗"))),
        FindAnswer(8, 25, 13, ADD,
            Hint("Dodaj dziesiątki i jedności osobno.",
                steps = listOf("20 + 10 = 30", "5 + 3 = 8", "30 + 8 = 38"))),
        FindOperator(9, 30, 12, 18, SUBTRACT,
            Hint("Wynik 18 jest mniejszy niż 30.",
                steps = listOf("30 − 12 = 18 ✓", "30 + 12 = 42 ✗"))),
        FindAnswer(10, 100, 37, SUBTRACT,
            Hint("Odejmuj dziesiątki i jedności po kolei.",
                steps = listOf("100 − 30 = 70", "70 − 7 = 63"))),
        FindOperator(11, 44, 56, 100, ADD,
            Hint("44 + 56 daje okrągłą liczbę 100.",
                steps = listOf("4 + 6 = 10 (jedności)", "40 + 50 = 90 (dziesiątki)", "90 + 10 = 100 ✓")))
    )

    /**
     * Static questions for Lesson 1-2: "Mnożenie i dzielenie" (Multiplication & Division).
     *
     * Covers multiplication tables, division as inverse of multiplication, sign rules,
     * and the order of operations.
     */
    private val mat_1_2: List<Question> = listOf(
        FindAnswer(0, 7, 8, MULTIPLY,
            Hint("7 × 8 to wynik, który warto zapamiętać.",
                steps = listOf("7 × 4 = 28", "28 × 2 = 56"))),
        FindOperator(1, 5, 4, 20, MULTIPLY,
            Hint("Wynik 20 = 5 razy 4.",
                steps = listOf("5 × 4 = 20 ✓", "5 + 4 = 9 ✗", "20 ÷ 4 = 5 ✓"))),
        FindAnswer(2, 36, 6, DIVIDE,
            Hint("Dzielenie to odwrotność mnożenia.",
                steps = listOf("6 × 6 = 36", "Więc 36 ÷ 6 = 6"))),
        FindOperator(3, 24, 4, 6, DIVIDE,
            Hint("Sprawdź: 4 × 6 = 24.",
                steps = listOf("24 ÷ 4 = 6 ✓", "24 × 4 = 96 ✗"))),
        FindAnswer(4, 9, 9, MULTIPLY,
            Hint("9 × 9 to jeden z ważniejszych wyników do zapamiętania.",
                steps = listOf("9 × 10 = 90", "90 − 9 = 81"))),
        FindOperator(5, 6, 7, 42, MULTIPLY,
            Hint("6 × 7 = 42 — warto znać na pamięć.",
                steps = listOf("6 × 7 = 42 ✓", "6 + 7 = 13 ✗"))),
        FindAnswer(6, 56, 8, DIVIDE,
            Hint("Przypomnij sobie tabliczkę mnożenia dla 8.",
                steps = listOf("8 × 7 = 56", "Więc 56 ÷ 8 = 7"))),
        FindOperator(7, 48, 6, 8, DIVIDE,
            Hint("Sprawdź: 6 × 8 = 48.",
                steps = listOf("48 ÷ 6 = 8 ✓", "6 × 8 = 48 ✓"))),
        FindAnswer(8, 12, 11, MULTIPLY,
            Hint("Mnożenie przez 11: dodaj cyfrę do siebie.",
                steps = listOf("12 × 10 = 120", "12 × 1 = 12", "120 + 12 = 132"))),
        FindOperator(9, 3, 8, 24, MULTIPLY,
            Hint("3 × 8 = 24.",
                steps = listOf("3 × 8 = 24 ✓", "3 + 8 = 11 ✗"))),
        FindAnswer(10, 81, 9, DIVIDE,
            Hint("Dzielenie to odwrotność mnożenia.",
                steps = listOf("9 × 9 = 81", "Więc 81 ÷ 9 = 9"))),
        FindOperator(11, 72, 8, 9, DIVIDE,
            Hint("Sprawdź: 8 × 9 = 72.",
                steps = listOf("72 ÷ 8 = 9 ✓", "8 × 9 = 72 ✓")))
    )

    /**
     * Static questions for Lesson 1-3: "Potęgowanie" (Exponentiation).
     *
     * Covers powers with integer exponents, their definitions, properties, and base calculations.
     */
    private val mat_1_3: List<Question> = listOf(
        FindAnswer(0, 2, 2, POWER,
            Hint("2 do potęgi 2 to 2 × 2.",
                sectionTitle = "DEFINICJA POTĘGI",
                items = listOf("a^n = a × a × ... × a (n razy)", "2^2 = 2 × 2"),
                steps = listOf("2 × 2 = 4"))),
        FindAnswer(1, 3, 2, POWER,
            Hint("3 do potęgi 2 to 3 × 3.",
                steps = listOf("3 × 3 = 9"))),
        FindAnswer(2, 2, 3, POWER,
            Hint("2 do potęgi 3 to 2 × 2 × 2.",
                steps = listOf("2 × 2 = 4", "4 × 2 = 8"))),
        FindOperator(3, 2, 3, 8, POWER,
            Hint("2 ^ 3 oznacza 2 mnożone przez siebie 3 razy.",
                steps = listOf("2 × 2 = 4", "4 × 2 = 8 ✓"))),
        FindAnswer(4, 4, 2, POWER,
            Hint("4 do potęgi 2 to 4 × 4.",
                steps = listOf("4 × 4 = 16"))),
        FindAnswer(5, 5, 2, POWER,
            Hint("5 do potęgi 2 to 5 × 5.",
                steps = listOf("5 × 5 = 25"))),
        FindAnswer(6, 2, 4, POWER,
            Hint("2 do potęgi 4 to 2 × 2 × 2 × 2.",
                steps = listOf("2 × 2 = 4", "4 × 2 = 8", "8 × 2 = 16"))),
        FindOperator(7, 3, 3, 27, POWER,
            Hint("3 ^ 3 oznacza 3 mnożone przez siebie 3 razy.",
                steps = listOf("3 × 3 = 9", "9 × 3 = 27 ✓"))),
        FindAnswer(8, 10, 2, POWER,
            Hint("10 do potęgi 2 to 10 × 10.",
                steps = listOf("10 × 10 = 100"))),
        FindAnswer(9, 3, 4, POWER,
            Hint("3 do potęgi 4 to 3 × 3 × 3 × 3.",
                steps = listOf("3 × 3 = 9", "9 × 3 = 27", "27 × 3 = 81")))
    )

    /**
     * Static questions for Lesson 2-1: "Jednomiany i wielomiany" (Monomials & Polynomials).
     *
     * Focuses on defining polynomials, identifying degrees, combining like terms,
     * and basic algebraic simplification.
     */
    private val mat_2_1: List<Question> = listOf(
        SelectFromList(0,
            "Który z poniższych wyrażeń jest jednomianem?",
            listOf("3x + 2", "5x²y", "x + y + z", "a² + b"),
            setOf(1),
            hint = Hint("Jednomian to wyrażenie algebraiczne będące iloczynem liczb i zmiennych.",
                boldPart = "iloczynem liczb i zmiennych",
                steps = listOf("3x+2 — suma, nie iloczyn ✗", "5x²y — iloczyn: 5·x²·y ✓", "x+y+z — suma ✗", "a²+b — suma ✗"))),
        SelectFromList(1,
            "Ile wyrazów ma wielomian  3x² − 2x + 1?",
            listOf("1", "2", "3", "4"),
            setOf(2),
            hint = Hint("Wyrazy wielomianu oddzielone są znakami + lub −.",
                steps = listOf("3x² — wyraz 1", "−2x — wyraz 2", "+1 — wyraz 3", "Razem: 3 wyrazy"))),
        SelectFromList(2,
            "Które wyrażenia są jednomianami? (zaznacz wszystkie)",
            listOf("7a³b", "2x − 3", "−4xy²", "m + n"),
            setOf(0, 2),
            multiSelect = true,
            hint = Hint("Jednomian nie może zawierać dodawania ani odejmowania.",
                steps = listOf("7a³b ✓ — sam iloczyn", "2x−3 ✗ — suma", "−4xy² ✓ — sam iloczyn", "m+n ✗ — suma"))),
        SelectFromList(3,
            "Które z poniższych to wielomian drugiego stopnia?",
            listOf("5x³ + 1", "3x² − x + 4", "7x", "2"),
            setOf(1),
            hint = Hint("Stopień wielomianu = najwyższy wykładnik zmiennej.",
                steps = listOf("5x³ → stopień 3 ✗", "3x² → stopień 2 ✓", "7x → stopień 1 ✗", "2 → stopień 0 ✗"))),
        SelectFromList(4,
            "Dodaj wyrazy podobne: 4x + 3x = ?",
            listOf("7x²", "7x", "12x", "7"),
            setOf(1),
            hint = Hint("Wyrazy podobne to te z taką samą częścią literową — dodajemy współczynniki.",
                steps = listOf("4x + 3x = (4+3)x = 7x"))),
        SelectFromList(5,
            "Które wyrażenia są wyrazami podobnymi do  2x²? (zaznacz wszystkie)",
            listOf("3x²", "2x", "−5x²", "x² + 1"),
            setOf(0, 2),
            multiSelect = true,
            hint = Hint("Wyrazy podobne mają identyczną część literową.",
                steps = listOf("3x² ✓ — część literowa x²", "2x ✗ — część literowa x", "−5x² ✓ — część literowa x²", "x²+1 ✗ — to wielomian"))),
        SelectFromList(6,
            "Uprość: 5a − 2a + a = ?",
            listOf("4a", "3a", "8a", "2a"),
            setOf(0),
            hint = Hint("Odejmuj i dodawaj współczynniki przy a.",
                steps = listOf("5a − 2a = 3a", "3a + a = 4a"))),
        SelectFromList(7,
            "Który wyraz jest wyrazem wolnym w  x² + 3x − 7?",
            listOf("x²", "3x", "−7", "3"),
            setOf(2),
            hint = Hint("Wyraz wolny nie zawiera zmiennej.",
                steps = listOf("−7 nie ma x → to wyraz wolny ✓"))),
        SelectFromList(8,
            "Które z poniższych jest wielomianem? (zaznacz wszystkie)",
            listOf("x² + 2x + 1", "3/x", "4x³ − x", "√x + 1"),
            setOf(0, 2),
            multiSelect = true,
            hint = Hint("Wielomian nie może mieć zmiennej w mianowniku ani pod pierwiastkiem.",
                steps = listOf("x²+2x+1 ✓", "3/x ✗ — zmienna w mianowniku", "4x³−x ✓", "√x ✗ — pod pierwiastkiem"))),
        SelectFromList(9,
            "Pomnóż jednomian: 3x · 4x = ?",
            listOf("12x", "12x²", "7x²", "7x"),
            setOf(1),
            hint = Hint("Mnożymy liczby przez liczby, potęgi przez potęgi.",
                steps = listOf("3·4 = 12", "x·x = x²", "Wynik: 12x²")))
    )

    /**
     * Static questions for Lesson 2-2: "Wzory skróconego mnożenia" (Short Multiplication Formulas).
     *
     * Focuses on expansion and calculation using formulas like (a+b)², (a-b)², and (a-b)(a+b).
     */
    private val mat_2_2: List<Question> = listOf(
        SelectFromList(0,
            "Co jest rozwinięciem  (a + b)²?",
            listOf("a² + b²", "a² + 2ab + b²", "a² − 2ab + b²", "2a + 2b"),
            setOf(1),
            hint = Hint("(a+b)² = a·a + 2·a·b + b·b",
                sectionTitle = "WZORY SKRÓCONEGO MNOŻENIA",
                items = listOf("(a+b)² = a² + 2ab + b²", "(a−b)² = a² − 2ab + b²", "(a+b)(a−b) = a² − b²"),
                steps = listOf("(a+b)² = (a+b)(a+b)", "= a² + ab + ab + b²", "= a² + 2ab + b²"))),
        SelectFromList(1,
            "Co jest rozwinięciem  (a − b)²?",
            listOf("a² − b²", "a² + 2ab + b²", "a² − 2ab + b²", "a − b"),
            setOf(2),
            hint = Hint("(a−b)² = a·a − 2·a·b + b·b",
                steps = listOf("(a−b)² = (a−b)(a−b)", "= a² − ab − ab + b²", "= a² − 2ab + b²"))),
        SelectFromList(2,
            "Co jest rozwinięciem  (a + b)(a − b)?",
            listOf("a² + b²", "a² − b²", "a² − 2ab + b²", "2ab"),
            setOf(1),
            hint = Hint("To wzór na różnicę kwadratów.",
                steps = listOf("(a+b)(a−b) = a² − ab + ab − b²", "= a² − b²"))),
        SelectFromList(3,
            "Rozwiń  (x + 3)²",
            listOf("x² + 9", "x² + 6x + 9", "x² − 6x + 9", "x² + 3x + 9"),
            setOf(1),
            hint = Hint("Podstaw a=x, b=3 do wzoru (a+b)².",
                steps = listOf("(x+3)² = x² + 2·x·3 + 3²", "= x² + 6x + 9"))),
        SelectFromList(4,
            "Rozwiń  (x − 5)²",
            listOf("x² − 25", "x² − 10x + 25", "x² + 10x + 25", "x² − 10x − 25"),
            setOf(1),
            hint = Hint("Podstaw a=x, b=5 do wzoru (a−b)².",
                steps = listOf("(x−5)² = x² − 2·x·5 + 5²", "= x² − 10x + 25"))),
        SelectFromList(5,
            "Oblicz  (2x + 3)²",
            listOf("4x² + 9", "4x² + 12x + 9", "4x² − 12x + 9", "2x² + 12x + 9"),
            setOf(1),
            hint = Hint("Podstaw a=2x, b=3.",
                steps = listOf("(2x)² = 4x²", "2·(2x)·3 = 12x", "3² = 9", "Wynik: 4x² + 12x + 9"))),
        SelectFromList(6,
            "Oblicz  (x + 4)(x − 4)",
            listOf("x² + 16", "x² − 16", "x² − 8x + 16", "x² + 8x − 16"),
            setOf(1),
            hint = Hint("To wzór (a+b)(a−b) = a² − b².",
                steps = listOf("a=x, b=4", "x² − 4² = x² − 16"))),
        SelectFromList(7,
            "Które wzory skróconego mnożenia są poprawne? (zaznacz wszystkie)",
            listOf("(a+b)² = a² + 2ab + b²", "(a+b)² = a² + b²", "(a−b)² = a² − 2ab + b²", "(a+b)(a−b) = a² − b²"),
            setOf(0, 2, 3),
            multiSelect = true,
            hint = Hint("Zapamiętaj trzy podstawowe wzory skróconego mnożenia.",
                items = listOf("(a+b)² = a² + 2ab + b²  ✓", "(a−b)² = a² − 2ab + b²  ✓", "(a+b)(a−b) = a² − b²  ✓"))),
        SelectFromList(8,
            "Rozwiń  (3 − x)²",
            listOf("9 − x²", "9 − 6x + x²", "9 + 6x + x²", "9 − 3x + x²"),
            setOf(1),
            hint = Hint("Podstaw a=3, b=x do wzoru (a−b)².",
                steps = listOf("3² = 9", "2·3·x = 6x", "x² = x²", "Wynik: 9 − 6x + x²"))),
        SelectFromList(9,
            "Która z wartości jest równa  (10 + 1)²?",
            listOf("101", "100 + 1", "100 + 20 + 1", "10² + 1²"),
            setOf(2),
            hint = Hint("Użyj wzoru (a+b)² z a=10, b=1.",
                steps = listOf("10² = 100", "2·10·1 = 20", "1² = 1", "Razem: 121")))
    )

    /**
     * Static questions for Lesson 5-1: "Kąty w trójkącie" (Angles in a Triangle).
     *
     * Generates triangle geometries dynamically using [TriangleBuilder] to quiz students
     * on finding the missing interior angle (summing to 180°).
     */
    private val mat_5_1: List<Question> = run {
        fun tri(aA: Double, aB: Double, id: Int, answer: Int, hint: Hint): GraphTypeAnswer {
            val (verts, vp) = TriangleBuilder.fromAnglesWithViewport(aA, aB)
            val (a, b, c) = verts
            return GraphTypeAnswer(
                id = id,
                prompt = "Oblicz miarę brakującego kąta trójkąta oznaczonego znakiem '?'.",
                shapes = listOf(
                    MathShape.Triangle(a, b, c,
                        labelA = "${aA.toInt()}°",
                        labelB = "${aB.toInt()}°",
                        labelC = "?")
                ),
                viewport = vp,
                correctAnswer = answer,
                unit = "°",
                inlineHint = "Suma kątów wewnętrznych trójkąta wynosi zawsze 180°.",
                hint = hint
            )
        }
        listOf(
            tri(60.0, 70.0, 0, 50,
                Hint("Suma kątów = 180°.", boldPart = "180°", steps = listOf("180° − 60° − 70° = 50°"))),
            tri(45.0, 90.0, 1, 45,
                Hint("Trójkąt prostokątny ma jeden kąt 90°.", boldPart = "90°",
                    steps = listOf("180° − 45° − 90° = 45°"))),
            tri(30.0, 60.0, 2, 90,
                Hint("Jeśli wynik to 90°, trójkąt jest prostokątny.", boldPart = "90°",
                    steps = listOf("180° − 30° − 60° = 90°"))),
            tri(55.0, 65.0, 3, 60,
                Hint("Suma kątów = 180°.", boldPart = "180°", steps = listOf("180° − 55° − 65° = 60°"))),
            tri(80.0, 40.0, 4, 60,
                Hint("Suma kątów = 180°.", boldPart = "180°", steps = listOf("180° − 80° − 40° = 60°"))),
            tri(50.0, 55.0, 5, 75,
                Hint("Suma kątów = 180°.", boldPart = "180°", steps = listOf("180° − 50° − 55° = 75°"))),
            tri(60.0, 60.0, 6, 60,
                Hint("Trójkąt równoboczny — wszystkie kąty równe 60°.", boldPart = "60°",
                    steps = listOf("180° ÷ 3 = 60°"))),
            tri(110.0, 35.0, 7, 35,
                Hint("Kąt rozwarty (>90°) też należy do trójkąta.", boldPart = "180°",
                    steps = listOf("180° − 110° − 35° = 35°"))),
            tri(130.0, 30.0, 8, 20,
                Hint("Suma kątów = 180°.", boldPart = "180°", steps = listOf("180° − 130° − 30° = 20°"))),
            tri(70.0, 70.0, 9, 40,
                Hint("Trójkąt równoramienny — dwa kąty równe.", boldPart = "180°",
                    steps = listOf("180° − 70° − 70° = 40°")))
        )
    }

    /**
     * Static questions for Lesson 4-1: "Funkcja kwadratowa" (Quadratic Function).
     *
     * Presents quadratic plots on a coordinate system and quizzes students on evaluating
     * the function or finding properties of the parabola.
     */
    private val mat_4_1: List<Question> = listOf(
        GraphSelectFromList(0,
            prompt = "Dana jest funkcja f(x) = x².\nIle wynosi f(3)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> x * x }),
                MathShape.PointMark(prz.rutedu.app.math.Pt(3.0, 9.0), label = "f(3) = ?")
            ),
            viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -1.0, yMax = 10.0),
            options = listOf("6", "7", "8", "9"),
            correctIndices = setOf(3),
            hint = Hint("f(3) = 3² = 3 × 3.", boldPart = "3²", steps = listOf("f(3) = 3² = 9"))),

        GraphSelectFromList(1,
            prompt = "Dana jest funkcja f(x) = x² − 2.\nIle wynosi f(0)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> x * x - 2 }),
                MathShape.PointMark(prz.rutedu.app.math.Pt(0.0, -2.0), label = "f(0) = ?")
            ),
            viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -3.0, yMax = 8.0),
            options = listOf("−3", "−2", "−1", "0"),
            correctIndices = setOf(1),
            hint = Hint("f(0) = 0² − 2.", boldPart = "0²", steps = listOf("f(0) = 0 − 2 = −2"))),

        GraphSelectFromList(2,
            prompt = "Dana jest funkcja f(x) = −x².\nDla jakiej wartości x funkcja osiąga wartość maksymalną?",
            shapes = listOf(MathShape.FunctionPlot(f = { x -> -x * x })),
            viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -10.0, yMax = 2.0),
            options = listOf("x = −1", "x = 0", "x = 1", "brak maksimum"),
            correctIndices = setOf(1),
            hint = Hint("Parabola skierowana w dół — wierzchołek to maksimum.", boldPart = "wierzchołek",
                steps = listOf("Wierzchołek f(x) = −x² jest w (0, 0)", "Maksimum dla x = 0, f(0) = 0"))),

        GraphSelectFromList(3,
            prompt = "Dana jest funkcja f(x) = x² + 1.\nJaka jest minimalna wartość tej funkcji?",
            shapes = listOf(MathShape.FunctionPlot(f = { x -> x * x + 1 })),
            viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -1.0, yMax = 9.0),
            options = listOf("0", "1", "2", "−1"),
            correctIndices = setOf(1),
            hint = Hint("Wierzchołek paraboli y = x² + 1 jest podniesiony o 1.", boldPart = "+1",
                steps = listOf("Minimum w wierzchołku (0, 1)", "f(0) = 0² + 1 = 1"))),

        GraphSelectFromList(4,
            prompt = "Dana jest funkcja f(x) = (x − 2)².\nIle wynosi f(2)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> (x - 2) * (x - 2) }),
                MathShape.PointMark(prz.rutedu.app.math.Pt(2.0, 0.0), label = "f(2) = ?")
            ),
            viewport = MathViewport(xMin = -1.0, xMax = 5.0, yMin = -1.0, yMax = 8.0),
            options = listOf("0", "1", "2", "4"),
            correctIndices = setOf(0),
            hint = Hint("f(2) = (2 − 2)² = 0² = 0.", boldPart = "(x−2)²",
                steps = listOf("f(2) = (2 − 2)²", "= 0² = 0"))),

        GraphTypeAnswer(5,
            prompt = "Dana jest funkcja f(x) = x².\nIle wynosi f(4)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> x * x }),
                MathShape.Segment(
                    from = prz.rutedu.app.math.Pt(4.0, 0.0),
                    to   = prz.rutedu.app.math.Pt(4.0, 16.0),
                    dashed = true, color = androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                ),
                MathShape.PointMark(prz.rutedu.app.math.Pt(4.0, 16.0), label = "f(4)=?")
            ),
            viewport = MathViewport(xMin = -1.0, xMax = 5.0, yMin = -1.0, yMax = 18.0),
            correctAnswer = 16,
            inlineHint = "f(x) = x²  →  f(4) = 4²",
            hint = Hint("f(4) = 4² = 4 × 4.", boldPart = "4²", steps = listOf("f(4) = 4 × 4 = 16"))),

        GraphTypeAnswer(6,
            prompt = "Dana jest funkcja f(x) = x² − 3.\nIle wynosi f(2)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> x * x - 3 }),
                MathShape.PointMark(prz.rutedu.app.math.Pt(2.0, 1.0), label = "f(2)=?")
            ),
            viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -4.0, yMax = 6.0),
            correctAnswer = 1,
            inlineHint = "f(2) = 2² − 3",
            hint = Hint("f(2) = 2² − 3.", boldPart = "2²", steps = listOf("2² = 4", "4 − 3 = 1"))),

        GraphTypeAnswer(7,
            prompt = "Dana jest funkcja f(x) = 2x².\nIle wynosi f(2)?",
            shapes = listOf(
                MathShape.FunctionPlot(f = { x -> 2 * x * x }),
                MathShape.PointMark(prz.rutedu.app.math.Pt(2.0, 8.0), label = "f(2)=?")
            ),
            viewport = MathViewport(xMin = -3.0, xMax = 3.0, yMin = -1.0, yMax = 10.0),
            correctAnswer = 8,
            inlineHint = "f(2) = 2 · 2²",
            hint = Hint("f(2) = 2 · 2² = 2 · 4.", boldPart = "2·2²", steps = listOf("2² = 4", "2 × 4 = 8")))
    )

    /**
     * Static questions for Lesson 3-1: "Równania liniowe" (Linear Equations).
     *
     * Generates simple linear equations to solve for variable x using elementary arithmetic.
     */
    private val genericMath: List<Question> = listOf(
        FindAnswer(0, 5, 3, ADD,
            Hint("Dodaj obie liczby.", steps = listOf("5 + 3 = 8"))),
        FindAnswer(1, 9, 2, SUBTRACT,
            Hint("Odejmij.", steps = listOf("9 − 2 = 7"))),
        FindOperator(2, 4, 3, 12, MULTIPLY,
            Hint("4 × 3 = 12.", steps = listOf("4 × 3 = 12 ✓"))),
        FindAnswer(3, 8, 4, DIVIDE,
            Hint("8 ÷ 4 = 2.", steps = listOf("4 × 2 = 8", "Więc 8 ÷ 4 = 2"))),
        FindAnswer(4, 6, 7, ADD,
            Hint("6 + 7 = 13.", steps = listOf("6 + 7 = 13"))),
        FindOperator(5, 10, 5, 5, DIVIDE,
            Hint("10 ÷ 5 = 2.", steps = listOf("10 ÷ 5 = 2 ✓", "5 × 2 = 10 ✓"))),
        FindAnswer(6, 15, 8, SUBTRACT,
            Hint("15 − 8 = 7.", steps = listOf("15 − 8 = 7"))),
        FindAnswer(7, 3, 9, MULTIPLY,
            Hint("3 × 9 = 27.", steps = listOf("3 × 9 = 27"))),
        FindOperator(8, 11, 4, 15, ADD,
            Hint("11 + 4 = 15.", steps = listOf("11 + 4 = 15 ✓"))),
        FindAnswer(9, 20, 4, DIVIDE,
            Hint("20 ÷ 4 = 5.", steps = listOf("4 × 5 = 20", "Więc 20 ÷ 4 = 5")))
    )

    /**
     * Static questions for Lesson 1-1: "Lądy i oceany świata" (Lands & Oceans).
     *
     * Presents interactive world map quizzes where students locate various European countries.
     */
    private val geo_1_1: List<Question> = listOf(
        MapQuiz(0, "Poland", "Gdzie leży Polska?", MapRegion.EUROPE,
            hint = Hint("Polska leży w środkowej Europie, nad Morzem Bałtyckim.",
                steps = listOf("Centrum-wschodnia Europa", "Na południe od Morza Bałtyckiego"))),
        MapQuiz(1, "Germany", "Gdzie leżą Niemcy?", MapRegion.EUROPE,
            hint = Hint("Niemcy leżą w środkowej Europie Zachodniej.",
                steps = listOf("Na zachód od Polski", "Największy kraj Europy Zachodniej"))),
        MapQuiz(2, "France", "Gdzie leży Francja?", MapRegion.EUROPE,
            hint = Hint("Francja leży w zachodniej Europie.",
                steps = listOf("Na zachód od Niemiec", "Od La Manche po Morze Śródziemne"))),
        MapQuiz(3, "Italy", "Gdzie leżą Włochy?", MapRegion.EUROPE,
            hint = Hint("Włochy to półwysep w kształcie buta.",
                steps = listOf("Południe Europy", "Półwysep Apeniński wchodzi do Morza Śródziemnego"))),
        MapQuiz(4, "Spain", "Gdzie leży Hiszpania?", MapRegion.EUROPE,
            hint = Hint("Hiszpania leży na Półwyspie Iberyjskim.",
                steps = listOf("Skrajny południe-zachód Europy", "Sąsiaduje z Francją i Portugalią"))),
        MapQuiz(5, "Ukraine", "Gdzie leży Ukraina?", MapRegion.EUROPE,
            hint = Hint("Ukraina to największy kraj w całości leżący w Europie.",
                steps = listOf("Wschodnia Europa", "Na południe od Białorusi, na wschód od Polski"))),
        MapQuiz(6, "Sweden", "Gdzie leży Szwecja?", MapRegion.EUROPE,
            hint = Hint("Szwecja leży na Półwyspie Skandynawskim.",
                steps = listOf("Północna Europa — Skandynawia", "Wschodnia część Półwyspu Skandynawskiego"))),
        MapQuiz(7, "Norway", "Gdzie leży Norwegia?", MapRegion.EUROPE,
            hint = Hint("Norwegia leży wzdłuż zachodniego wybrzeża Skandynawii.",
                steps = listOf("Zachodnia część Półwyspu Skandynawskiego", "Długa linia brzegowa nad Atlantykiem"))),
        MapQuiz(8, "Romania", "Gdzie leży Rumunia?", MapRegion.EUROPE,
            hint = Hint("Rumunia leży na Bałkanach.",
                steps = listOf("Południowo-wschodnia Europa", "Sąsiaduje z Ukrainą, Bułgarią i Serbią"))),
        MapQuiz(9, "Greece", "Gdzie leży Grecja?", MapRegion.EUROPE,
            hint = Hint("Grecja leży na południu Bałkanów.",
                steps = listOf("Południe Europy", "Dużo wysp na Morzu Egejskim")))
    )

    /**
     * Static questions for Lesson 4-1: "Sąsiedzi Polski" (Poland's Neighbors).
     *
     * Quizzes students on pinpointing the 7 neighboring countries of Poland on a regional map.
     */
    private val geo_4_1: List<Question> = listOf(
        MapQuiz(0, "Germany", "Zaznacz Niemcy — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Niemcy graniczą z Polską na zachodzie.",
                steps = listOf("Szukaj na zachód od Polski", "Nad Morzem Bałtyckim i Północnym"))),
        MapQuiz(1, "Czechia", "Zaznacz Czechy — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Czechy graniczą z Polską na południe-zachodzie.",
                steps = listOf("Szukaj na południe od Polski", "Bez dostępu do morza — w środku Europy"))),
        MapQuiz(2, "Slovakia", "Zaznacz Słowację — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Słowacja graniczy z Polską na południu.",
                steps = listOf("Na południe od Polski, na wschód od Czech", "Mały kraj w środkowej Europie"))),
        MapQuiz(3, "Ukraine", "Zaznacz Ukrainę — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Ukraina graniczy z Polską na wschodzie.",
                steps = listOf("Na wschód od Polski", "Duży kraj — największy w całości w Europie"))),
        MapQuiz(4, "Belarus", "Zaznacz Białoruś — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Białoruś graniczy z Polską na północnym-wschodzie.",
                steps = listOf("Północny-wschód od Polski", "Na południe od Litwy"))),
        MapQuiz(5, "Lithuania", "Zaznacz Litwę — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Litwa graniczy z Polską na północy.",
                steps = listOf("Na północ od Polski", "Jedno z trzech państw bałtyckich"))),
        MapQuiz(6, "Russia", "Zaznacz Rosję (obwód kaliningradzki) — sąsiada Polski", MapRegion.CENTRAL_EUROPE,
            hint = Hint("Rosja graniczy z Polską przez obwód kaliningradzki.",
                steps = listOf("Szukaj enklawy nad Morzem Bałtyckim", "Rosyjski obwód odcięty od Rosji, między Polską a Litwą")))
    )

    /**
     * Static questions for Lesson 4-2: "Kraje Azji" (Asian Countries).
     *
     * Presents map locations for major Asian countries such as China, India, Japan, etc.
     */
    private val geo_4_2: List<Question> = listOf(
        MapQuiz(0, "China", "Gdzie leżą Chiny?", MapRegion.ASIA,
            hint = Hint("Chiny to największy kraj Azji pod względem populacji.",
                steps = listOf("Wschód Azji", "Sąsiad Mongolii, Rosji, Indii i Wietnamu"))),
        MapQuiz(1, "Japan", "Gdzie leży Japonia?", MapRegion.ASIA,
            hint = Hint("Japonia to archipelag wysp na wschodnim wybrzeżu Azji.",
                steps = listOf("Daleki Wschód — wyspy na Oceanie Spokojnym", "Na wschód od Korei i Chin"))),
        MapQuiz(2, "India", "Gdzie leżą Indie?", MapRegion.ASIA,
            hint = Hint("Indie leżą na Półwyspie Indyjskim.",
                steps = listOf("Azja Południowa", "Wielki półwysep wchodzący do Oceanu Indyjskiego"))),
        MapQuiz(3, "South Korea", "Gdzie leży Korea Południowa?", MapRegion.ASIA,
            hint = Hint("Korea Południowa leży na Półwyspie Koreańskim.",
                steps = listOf("Azja Wschodnia", "Południowa część Półwyspu Koreańskiego"))),
        MapQuiz(4, "Mongolia", "Gdzie leży Mongolia?", MapRegion.ASIA,
            hint = Hint("Mongolia leży między Chinami a Rosją.",
                steps = listOf("Azja Środkowa/Wschodnia", "Duży kraj otoczony Chinami od południa i Rosją od północy"))),
        MapQuiz(5, "Kazakhstan", "Gdzie leży Kazachstan?", MapRegion.ASIA,
            hint = Hint("Kazachstan to największy kraj Azji Środkowej.",
                steps = listOf("Azja Środkowa", "Na południe od Rosji, na północ od Morza Kaspijskiego"))),
        MapQuiz(6, "Iran", "Gdzie leży Iran?", MapRegion.ASIA,
            hint = Hint("Iran leży na Bliskim Wschodzie.",
                steps = listOf("Azja Zachodnia — Bliski Wschód", "Między Irakiem a Afganistanem, nad Zatoką Perską"))),
        MapQuiz(7, "Saudi Arabia", "Gdzie leży Arabia Saudyjska?", MapRegion.ASIA,
            hint = Hint("Arabia Saudyjska zajmuje większość Półwyspu Arabskiego.",
                steps = listOf("Półwysep Arabski", "Duży kraj w centrum Bliskiego Wschodu"))),
        MapQuiz(8, "Vietnam", "Gdzie leży Wietnam?", MapRegion.ASIA,
            hint = Hint("Wietnam leży w Azji Południowo-Wschodniej.",
                steps = listOf("Półwysep Indochiński", "Długi, wąski kraj wzdłuż Morza Południowochińskiego"))),
        MapQuiz(9, "Thailand", "Gdzie leży Tajlandia?", MapRegion.ASIA,
            hint = Hint("Tajlandia leży w Azji Południowo-Wschodniej.",
                steps = listOf("Azja Południowo-Wschodnia", "Na południe od Laosu, na zachód od Wietnamu")))
    )

    /**
     * Static questions for Lesson 4-3: "Stolice Europy" (European Capitals).
     *
     * Asks students to identify European countries on the map based on their capital cities.
     */
    private val geo_4_3: List<Question> = listOf(
        MapQuiz(0, "France", "Wskaż kraj, którego stolicą jest Paryż", MapRegion.EUROPE,
            hint = Hint("Paryż to stolica Francji.",
                steps = listOf("Francja leży w zachodniej Europie", "Graniczy z Niemcami, Belgią i Hiszpanią"))),
        MapQuiz(1, "Germany", "Wskaż kraj, którego stolicą jest Berlin", MapRegion.EUROPE,
            hint = Hint("Berlin to stolica Niemiec.",
                steps = listOf("Niemcy leżą w środkowej Europie", "Sąsiad Polski na zachodzie"))),
        MapQuiz(2, "Italy", "Wskaż kraj, którego stolicą jest Rzym", MapRegion.EUROPE,
            hint = Hint("Rzym to stolica Włoch.",
                steps = listOf("Włochy to półwysep w kształcie buta", "Morze Śródziemne — południe Europy"))),
        MapQuiz(3, "Spain", "Wskaż kraj, którego stolicą jest Madryt", MapRegion.EUROPE,
            hint = Hint("Madryt to stolica Hiszpanii.",
                steps = listOf("Półwysep Iberyjski — południe-zachód Europy", "Sąsiad Francji i Portugalii"))),
        MapQuiz(4, "United Kingdom", "Wskaż kraj, którego stolicą jest Londyn", MapRegion.EUROPE,
            hint = Hint("Londyn to stolica Zjednoczonego Królestwa.",
                steps = listOf("Wyspy Brytyjskie — północny-zachód Europy", "Oddzielony od Francji Kanałem La Manche"))),
        MapQuiz(5, "Czechia", "Wskaż kraj, którego stolicą jest Praga", MapRegion.EUROPE,
            hint = Hint("Praga to stolica Czech.",
                steps = listOf("Środkowa Europa bez dostępu do morza", "Sąsiad Polski na południu"))),
        MapQuiz(6, "Hungary", "Wskaż kraj, którego stolicą jest Budapeszt", MapRegion.EUROPE,
            hint = Hint("Budapeszt to stolica Węgier.",
                steps = listOf("Środkowa Europa", "Na południe od Czech i Słowacji"))),
        MapQuiz(7, "Austria", "Wskaż kraj, którego stolicą jest Wiedeń", MapRegion.EUROPE,
            hint = Hint("Wiedeń to stolica Austrii.",
                steps = listOf("Środkowa Europa — Alpy", "Sąsiad Niemiec, Czech, Węgier i Włoch"))),
        MapQuiz(8, "Sweden", "Wskaż kraj, którego stolicą jest Sztokholm", MapRegion.EUROPE,
            hint = Hint("Sztokholm to stolica Szwecji.",
                steps = listOf("Półwysep Skandynawski — północna Europa", "Wschodnia część Skandynawii"))),
        MapQuiz(9, "Greece", "Wskaż kraj, którego stolicą są Ateny", MapRegion.EUROPE,
            hint = Hint("Ateny to stolica Grecji.",
                steps = listOf("Południe Bałkanów", "Wiele wysp na Morzu Egejskim")))
    )

    /**
     * Static questions for Lesson 4-4: "Województwa Polski" (Polish Provinces).
     *
     * Asks students to identify Polish provinces on the map based on their names.
     */
    private val geo_4_4: List<Question> = listOf(
        MapQuiz(4401, "małopolskie", "Wskaż województwo małopolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Małopolskie leży na południu Polski, ze stolicą w Krakowie.")),
        MapQuiz(4402, "mazowieckie", "Wskaż województwo mazowieckie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Mazowieckie to największe województwo, leży w centrum-wschodzie, ze stolicą w Warszawie.")),
        MapQuiz(4403, "pomorskie", "Wskaż województwo pomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Pomorskie leży nad Morzem Bałtyckim, ze stolicą w Gdańsku.")),
        MapQuiz(4404, "śląskie", "Wskaż województwo śląskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Śląskie leży na południu, jest najbardziej zaludnione, ze stolicą w Katowicach.")),
        MapQuiz(4405, "wielkopolskie", "Wskaż województwo wielkopolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Wielkopolskie leży w zachodniej części kraju, ze stolicą w Poznaniu.")),
        MapQuiz(4406, "dolnośląskie", "Wskaż województwo dolnośląskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Dolnośląskie leży na południowym zachodzie, ze stolicą we Wrocławiu.")),
        MapQuiz(4407, "podkarpackie", "Wskaż województwo podkarpackie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Podkarpackie leży na południowym wschodzie, ze stolicą w Rzeszowie.")),
        MapQuiz(4408, "zachodniopomorskie", "Wskaż województwo zachodniopomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Zachodniopomorskie leży na północnym zachodzie, nad Bałtykiem, ze stolicą w Szczecinie.")),
        MapQuiz(4409, "lubelskie", "Wskaż województwo lubelskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Lubelskie leży na wschodzie Polski, ze stolicą w Lublinie.")),
        MapQuiz(4410, "warmińsko-mazurskie", "Wskaż województwo warmińsko-mazurskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Warmińsko-mazurskie to kraina tysiąca jezior na północy, ze stolicą w Olsztynie.")),
        MapQuiz(4411, "podlaskie", "Wskaż województwo podlaskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Podlaskie leży na północnym wschodzie, ze stolicą w Białymstoku.")),
        MapQuiz(4412, "łódzkie", "Wskaż województwo łódzkie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Łódzkie leży w samym centrum Polski, ze stolicą w Łodzi.")),
        MapQuiz(4413, "kujawsko-pomorskie", "Wskaż województwo kujawsko-pomorskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Kujawsko-pomorskie leży w północnej części centrum, ze stolicami w Bydgoszczy i Toruniu.")),
        MapQuiz(4414, "opolskie", "Wskaż województwo opolskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Opolskie to najmniejsze województwo, leży na południowym zachodzie, ze stolicą w Opolu.")),
        MapQuiz(4415, "lubuskie", "Wskaż województwo lubuskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Lubuskie leży na zachodzie, przy granicy z Niemcami, ze stolicami w Gorzowie Wlkp. i Zielonej Górze.")),
        MapQuiz(4416, "świętokrzyskie", "Wskaż województwo świętokrzyskie", MapRegion.POLAND, "files/polish_provinces.geojson",
            Hint("Świętokrzyskie leży w południowo-środkowej Polsce, ze stolicą w Kielcach."))
    )
    
    /**
     * Static questions for Lesson 3-1: "Wzory kwasów" (Acid Formulas).
     *
     * Covers identifying names and chemical formulas for binary and oxyacids.
     */
    private val chemia_3_1: List<Question> = listOf(
        SelectFromList(
            id = 3101,
            prompt = "Wzór kwasu chlorowodorowego (solnego)",
            options = listOf("HCl", "H₂S", "HF", "HBr"),
            correctIndices = setOf(0),
            hint = Hint(
                mainText = "Kwas chlorowodorowy to kwas beztlenowy. Składa się z wodoru i chloru.",
                boldPart = "HCl",
                sectionTitle = "Kwasy beztlenowe",
                items = listOf("HCl – chlorowodorowy", "H₂S – siarkowodorowy", "HF – fluorowodorowy", "HBr – bromowodorowy")
            )
        ),
        SelectFromList(
            id = 3102,
            prompt = "Wzór kwasu siarkowodorowego",
            options = listOf("H₂SO₄", "H₂S", "H₂SO₃", "HCl"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas siarkowodorowy to H₂S – beztlenowy kwas znany z zapachu zgniłych jaj.",
                boldPart = "H₂S"
            )
        ),
        SelectFromList(
            id = 3103,
            prompt = "Wzór kwasu fluorowodorowego",
            options = listOf("HBr", "HCl", "HF", "H₂S"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "Kwas fluorowodorowy (HF) używany jest do trawienia szkła.",
                boldPart = "HF"
            )
        ),
        SelectFromList(
            id = 3104,
            prompt = "Wzór kwasu bromowodorowego",
            options = listOf("HF", "HCl", "H₂S", "HBr"),
            correctIndices = setOf(3),
            hint = Hint(
                mainText = "Kwas bromowodorowy to HBr – beztlenowy kwas podobny do HCl.",
                boldPart = "HBr"
            )
        ),
        SelectFromList(
            id = 3105,
            prompt = "Wzór kwasu siarkowego(VI)",
            options = listOf("H₂SO₃", "HNO₃", "H₂SO₄", "H₃PO₄"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "Kwas siarkowy(VI) – H₂SO₄ – najważniejszy kwas przemysłowy, silnie żrący.",
                boldPart = "H₂SO₄",
                sectionTitle = "Kwasy tlenowe siarki",
                items = listOf("H₂SO₄ – siarkowy(VI), siarka na +6", "H₂SO₃ – siarkowy(IV), siarka na +4")
            )
        ),
        SelectFromList(
            id = 3106,
            prompt = "Wzór kwasu azotowego(V)",
            options = listOf("H₂SO₄", "HNO₃", "H₃PO₄", "H₂CO₃"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas azotowy(V) to HNO₃. Używany do produkcji nawozów i materiałów wybuchowych.",
                boldPart = "HNO₃"
            )
        ),
        SelectFromList(
            id = 3107,
            prompt = "Wzór kwasu węglowego",
            options = listOf("H₂SO₄", "H₃PO₄", "HNO₃", "H₂CO₃"),
            correctIndices = setOf(3),
            hint = Hint(
                mainText = "Kwas węglowy H₂CO₃ to słaby kwas, powstaje gdy CO₂ rozpuszcza się w wodzie.",
                boldPart = "H₂CO₃"
            )
        ),
        SelectFromList(
            id = 3108,
            prompt = "Wzór kwasu fosforowego(V) (ortofosforowego)",
            options = listOf("H₃PO₄", "H₂SO₄", "H₂CO₃", "HNO₃"),
            correctIndices = setOf(0),
            hint = Hint(
                mainText = "Kwas fosforowy(V) to H₃PO₄. Składnik nawozów i napojów cola.",
                boldPart = "H₃PO₄"
            )
        ),
        SelectFromList(
            id = 3109,
            prompt = "Wzór kwasu siarkowego(IV)",
            options = listOf("H₂SO₄", "H₂SO₃", "HNO₃", "H₃PO₄"),
            correctIndices = setOf(1),
            hint = Hint(
                mainText = "Kwas siarkowy(IV) to H₂SO₃. Powstaje przy spalaniu siarki – przyczyna kwaśnych deszczy.",
                boldPart = "H₂SO₃",
                sectionTitle = "Kwasy tlenowe siarki",
                items = listOf("H₂SO₄ – siarkowy(VI), siarka na +6", "H₂SO₃ – siarkowy(IV), siarka na +4")
            )
        ),
        SelectFromList(
            id = 3110,
            prompt = "Kwas o wzorze H₂SO₄ to…",
            options = listOf("kwas azotowy(V)", "kwas węglowy", "kwas siarkowy(VI)", "kwas fosforowy(V)"),
            correctIndices = setOf(2),
            hint = Hint(
                mainText = "H₂SO₄ to kwas siarkowy(VI). Liczba (VI) oznacza stopień utlenienia siarki.",
                boldPart = "kwas siarkowy(VI)"
            )
        )
    )

    /**
     * Static questions for Lesson 3-2: "Równania reakcji" (Chemical Equations Balancing).
     *
     * Focuses on balancing chemical equations for synthesis, acid production, and neutralizations.
     */
    private val chemia_3_2: List<Question> = listOf(
        EquationBalance(
            id = 3201,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Dobierz odpowiednie współczynniki stechiometryczne",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("Cl₂", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("HCl", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "H₂ + Cl₂ → 2HCl. Po lewej: 2H i 2Cl. Po prawej: w 2 cząsteczkach HCl też 2H i 2Cl.",
                boldPart = "2HCl",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "Policz atomy H po lewej: 1×H₂ = 2 atomy H",
                    "Policz atomy Cl po lewej: 1×Cl₂ = 2 atomy Cl",
                    "Po prawej HCl ma 1H i 1Cl → potrzeba 2×HCl"
                )
            )
        ),
        EquationBalance(
            id = 3202,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Dobierz odpowiednie współczynniki stechiometryczne",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 2),
                BalanceTerm("O₂", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "2H₂ + O₂ → 2H₂O. Klasyczna reakcja syntezy wody.",
                boldPart = "2H₂O",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "Po prawej 2 cząsteczki H₂O = 4H i 2O",
                    "4H po lewej to 2×H₂",
                    "2O po lewej to 1×O₂"
                )
            )
        ),
        EquationBalance(
            id = 3203,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas siarkowy(VI) powstaje z SO₃ i wody",
            reactants = listOf(
                BalanceTerm("SO₃", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂SO₄", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "SO₃ + H₂O → H₂SO₄. Wszystkie współczynniki wynoszą 1.",
                boldPart = "H₂SO₄"
            )
        ),
        EquationBalance(
            id = 3204,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas siarkowy(IV) powstaje z SO₂ i wody",
            reactants = listOf(
                BalanceTerm("SO₂", fixedCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂SO₃", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "SO₂ + H₂O → H₂SO₃. Kwas siarkowy(IV) odpowiada za kwaśne deszcze.",
                boldPart = "H₂SO₃"
            )
        ),
        EquationBalance(
            id = 3205,
            instruction = "Uzupełnij równanie otrzymywania kwasu",
            subInstruction = "Kwas węglowy powstaje z CO₂ i wody",
            reactants = listOf(
                BalanceTerm("CO₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂CO₃", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "CO₂ + H₂O → H₂CO₃. Tak powstaje kwas węglowy w napojach gazowanych.",
                boldPart = "H₂CO₃"
            )
        ),
        EquationBalance(
            id = 3206,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Kwas azotowy(V) z N₂O₅ i wody",
            reactants = listOf(
                BalanceTerm("N₂O₅", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("HNO₃", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "N₂O₅ + H₂O → 2HNO₃. Jedna cząsteczka N₂O₅ zawiera 2 atomy N → 2 cząsteczki HNO₃.",
                boldPart = "2HNO₃",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "N₂O₅ ma 2 atomy azotu",
                    "Każda cząsteczka HNO₃ ma 1 atom azotu",
                    "Potrzeba 2×HNO₃ aby zbilansować azot"
                )
            )
        ),
        EquationBalance(
            id = 3207,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Kwas fosforowy(V) z P₂O₅ i wody",
            reactants = listOf(
                BalanceTerm("P₂O₅", fixedCoefficient = 1),
                BalanceTerm("H₂O", fixedCoefficient = null, correctCoefficient = 3)
            ),
            products = listOf(
                BalanceTerm("H₃PO₄", fixedCoefficient = null, correctCoefficient = 2)
            ),
            hint = Hint(
                mainText = "P₂O₅ + 3H₂O → 2H₃PO₄. Dwa atomy P → 2 cząsteczki H₃PO₄, a to wymaga 3 cząsteczek wody.",
                boldPart = "2H₃PO₄",
                sectionTitle = "Krok po kroku",
                steps = listOf(
                    "P₂O₅ ma 2 atomy P → potrzeba 2×H₃PO₄",
                    "2×H₃PO₄ ma 6 atomów H → potrzeba 3×H₂O",
                    "Sprawdź O: P₂O₅(5) + 3H₂O(3) = 8 = 2×H₃PO₄(8) ✓"
                )
            )
        ),
        EquationBalance(
            id = 3208,
            instruction = "Zbilansuj równanie reakcji",
            subInstruction = "Reakcja syntezy kwasu siarkowodorowego",
            reactants = listOf(
                BalanceTerm("H₂", fixedCoefficient = null, correctCoefficient = 1),
                BalanceTerm("S", fixedCoefficient = 1)
            ),
            products = listOf(
                BalanceTerm("H₂S", fixedCoefficient = null, correctCoefficient = 1)
            ),
            hint = Hint(
                mainText = "H₂ + S → H₂S. Wszystkie współczynniki wynoszą 1.",
                boldPart = "H₂S"
            )
        )
    )

    // Maps lesson IDs to their static question lists. Add new entries here after
    // declaring the corresponding private val above.
    private val banks: Map<String, List<Question>> = mapOf(
        "mat_1_1" to mat_1_1,
        "mat_1_2" to mat_1_2,
        "mat_1_3" to mat_1_3,
        "mat_2_1" to mat_2_1,
        "mat_2_2" to mat_2_2,
        "mat_3_1" to genericMath,
        "mat_4_1" to mat_4_1,
        "mat_5_1" to mat_5_1,
        "geo_1_1" to geo_1_1,
        "geo_4_1" to geo_4_1,
        "geo_4_2" to geo_4_2,
        "geo_4_3" to geo_4_3,
        "geo_4_4" to geo_4_4,
    )
}
