package prz.rutedu.app.data

import androidx.compose.ui.graphics.Color
import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.MathViewport
import prz.rutedu.app.math.Pt
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.FractionAnswer
import prz.rutedu.app.models.Question.GraphSelectFromList
import prz.rutedu.app.models.Question.GraphTypeAnswer
import prz.rutedu.app.models.Question.SelectFromList
import prz.rutedu.app.models.Question.TypeAnswer
import kotlin.math.abs
import kotlin.random.Random
import kotlin.math.pow

/**
 * Procedural question set for "Matematyka rozszerzona".
 */
object ExtendedMathQuestionGenerator {

    /**
     * Generates a procedural set of questions for a given "Matematyka rozszerzona" lesson.
     *
     * @param lessonId The identifier of the lesson (e.g., "mat_roz_1_1").
     * @param seed The random seed used to shuffle and generate procedural values.
     * @param excludeIds Set of question IDs to exclude (already answered).
     * @return List of generated [Question] objects.
     */
    fun generateFor(
        lessonId: String,
        seed: Long,
        excludeIds: Set<Int> = emptySet()
    ): List<Question> {
        val all = when (lessonId) {
            "mat_roz_1_1" -> functionsWithParameter(seed)
            "mat_roz_1_2" -> graphTransformations(seed)
            "mat_roz_1_3" -> absoluteValueFunctions(seed)
            "mat_roz_2_1" -> polynomialFactorization(seed)
            "mat_roz_2_2" -> polynomialEquations(seed)
            "mat_roz_2_3" -> rationalFunctions(seed)
            "mat_roz_3_1" -> trigonometricIdentities(seed)
            "mat_roz_3_2" -> trigonometricEquations(seed)
            "mat_roz_3_3" -> trigonometricGraphs(seed)
            "mat_roz_4_1" -> lineAndCircle(seed)
            "mat_roz_4_2" -> pointLineDistance(seed)
            "mat_roz_4_3" -> vectors(seed)
            "mat_roz_5_1" -> sequences(seed)
            "mat_roz_5_2" -> sequenceLimits(seed)
            "mat_roz_5_3" -> geometricSeries(seed)
            "mat_roz_6_1" -> combinatorics(seed)
            "mat_roz_6_2" -> bernoulli(seed)
            "mat_roz_6_3" -> conditionalProbability(seed)
            "mat_roz_7_1" -> derivativeRate(seed)
            "mat_roz_7_2" -> extrema(seed)
            "mat_roz_7_3" -> tangent(seed)
            "mat_roz_8_1" -> logProperties(seed)
            "mat_roz_8_2" -> logEquations(seed)
            "mat_roz_8_3" -> logPowers(seed)
            "mat_roz_9_1" -> absEquations(seed)
            "mat_roz_9_2" -> absInequalities(seed)
            "mat_roz_9_3" -> absDistance(seed)
            "mat_roz_10_1" -> cubeDiagonals(seed)
            "mat_roz_10_2" -> prismVolume(seed)
            "mat_roz_10_3" -> pyramidVolume(seed)

            else -> emptyList()
        }
        return if (excludeIds.isEmpty()) all else all.filter { it.id !in excludeIds }
    }

    /**
     * Returns the total number of questions available for a given lesson.
     *
     * @param lessonId The identifier of the lesson.
     * @return Total count of questions.
     */
    fun totalFor(lessonId: String): Int = generateFor(lessonId, seed = 0L).size

    private fun select(
        id: Int,
        prompt: String,
        correct: String,
        wrong: List<String>,
        rng: Random,
        hint: Hint = Hint("")
    ): SelectFromList {
        val options = (listOf(correct) + wrong).distinct().shuffled(rng)
        return SelectFromList(id, prompt, options, setOf(options.indexOf(correct)), hint = hint)
    }

    private fun functionsWithParameter(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val a = rng.nextInt(-4, 5).let { if (it == 0) 2 else it }
            val b = rng.nextInt(-6, 7)
            val x = rng.nextInt(-3, 4)
            val value = a * x + b
            questions += TypeAnswer(
                10100 + i,
                "Dla f(x) = ${a}x + ($b) oblicz f($x).",
                value,
                hint = Hint(
                    "Podstaw $x w miejsce x.",
                    steps = listOf("f($x) = $a * $x + ($b)", "f($x) = $value")
                )
            )
        }

        return questions.shuffled(rng)
    }

    private fun graphTransformations(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(6) { i ->
            val p = rng.nextInt(-3, 4)
            val q = rng.nextInt(-3, 4)
            val shapes = listOf(
                MathShape.FunctionPlot(
                    f = { x -> (x - p) * (x - p) + q },
                    color = Color(0xFFE53935)
                ),
                MathShape.PointMark(
                    Pt(p.toDouble(), q.toDouble()),
                    label = "W",
                    color = Color(0xFFE53935)
                )
            )
            questions += GraphSelectFromList(
                10200 + i,
                "Wskaż wierzchołek paraboli y = (x - $p)^2 + ($q).",
                shapes,
                MathViewport(xMin = -6.0, xMax = 6.0, yMin = -4.0, yMax = 10.0),
                listOf("($p, $q)", "(${-p}, $q)", "($p, ${-q})").distinct().shuffled(rng),
                emptySet(),
                hint = Hint("Postać y = (x - p)^2 + q ma wierzchołek w punkcie (p, q).")
            ).let { qn -> qn.copy(correctIndices = setOf(qn.options.indexOf("($p, $q)"))) }
        }

        return questions.shuffled(rng)
    }

    private fun absoluteValueFunctions(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val a = rng.nextInt(-5, 6)
            val b = rng.nextInt(-3, 4)
            val x = rng.nextInt(-6, 7)
            val value = abs(x - a) + b
            questions += TypeAnswer(
                10300 + i,
                "Dla f(x) = |x - ($a)| + ($b) oblicz f($x).",
                value,
                hint = Hint(
                    "Najpierw oblicz wartość w module, potem dodaj przesunięcie.",
                    steps = listOf("|$x - ($a)| = ${abs(x - a)}", "f($x) = $value")
                )
            )
        }

        return questions.shuffled(rng)
    }

    private fun polynomialFactorization(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val p = rng.nextInt(1, 7)
            val q = rng.nextInt(1, 7)
            val b = p + q
            val c = p * q
            val correct = "(x + $p)(x + $q)"
            questions += select(
                20100 + i,
                "Rozłóż na czynniki: x^2 + ${b}x + $c",
                correct,
                listOf("(x - $p)(x - $q)", "(x + $b)(x + $c)", "(x + $p)(x - $q)"),
                rng,
                Hint("Szukamy dwóch liczb o sumie $b i iloczynie $c.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun polynomialEquations(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val r1 = rng.nextInt(-5, 0)
            val r2 = (1..5).filter { it != -r1 }.random(rng)
            val b = -(r1 + r2)
            val c = r1 * r2
            val middle = if (b >= 0) "+ ${b}x" else "- ${-b}x"
            val end = if (c >= 0) "+ $c" else "- ${-c}"
            val correct = "x = $r1 lub x = $r2"
            questions += select(
                20200 + i,
                "Rozwiąż równanie: x^2 $middle $end = 0",
                correct,
                listOf(
                    "x = ${-r1} lub x = ${-r2}",
                    "x = ${r1 + r2}",
                    "brak rozwiązań rzeczywistych"
                ),
                rng,
                Hint("Równanie ma postać (x - r1)(x - r2) = 0.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun rationalFunctions(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(6) { i ->
            val a = rng.nextInt(-4, 5).let { if (it == 0) 1 else it }
            val shapes = listOf(
                MathShape.FunctionPlot(
                    f = { x -> 1.0 / (x - a) },
                    color = Color(0xFFE53935)
                )
            )
            questions += GraphSelectFromList(
                20300 + i,
                "Dla f(x) = 1/(x - ($a)) wskaż asymptotę pionową.",
                shapes,
                MathViewport(xMin = -7.0, xMax = 7.0, yMin = -6.0, yMax = 6.0),
                listOf("x = $a", "y = $a", "x = ${-a}", "y = 0").distinct().shuffled(rng),
                emptySet(),
                hint = Hint("Mianownik zeruje się dla x = $a, więc tam jest asymptota pionowa.")
            ).let { qn -> qn.copy(correctIndices = setOf(qn.options.indexOf("x = $a"))) }
        }

        return questions.shuffled(rng)
    }

    private fun trigonometricIdentities(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            select(
                30100,
                "Która tożsamość jest prawdziwa?",
                "sin^2(x) + cos^2(x) = 1",
                listOf("sin(x) + cos(x) = 1", "tg(x) = cos(x)/sin(x)", "sin^2(x) - cos^2(x) = 1"),
                rng
            ),
            select(
                30101,
                "Czemu równa się tg(x)?",
                "sin(x)/cos(x)",
                listOf("cos(x)/sin(x)", "sin(x)*cos(x)", "1/sin(x)"),
                rng
            ),
            select(
                30102,
                "Jeśli sin(x)=3/5 i x jest ostry, to cos(x) wynosi:",
                "4/5",
                listOf("3/4", "5/4", "2/5"),
                rng
            ),
            select(
                30103,
                "W trójkącie prostokątnym sin kąta ostrego to:",
                "przyprostokątna naprzeciw / przeciwprostokątna",
                listOf(
                    "przyprostokątna przy kącie / przeciwprostokątna",
                    "przeciwprostokątna / przyprostokątna",
                    "suma przyprostokątnych"
                ),
                rng
            )
        ).shuffled(rng)
    }

    private fun trigonometricEquations(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            select(
                30200,
                "Rozwiązania równania sin(x)=0 w przedziale <0, 2π> to:",
                "0, π, 2π",
                listOf("π/2", "0, π/2", "π/2, 3π/2"),
                rng
            ),
            select(
                30201,
                "Rozwiązania równania cos(x)=0 w przedziale <0, 2π> to:",
                "π/2, 3π/2",
                listOf("0, π", "π", "0, 2π"),
                rng
            ),
            select(
                30202,
                "Rozwiązania równania sin(x)=1 w przedziale <0, 2π> to:",
                "π/2",
                listOf("0", "π", "3π/2"),
                rng
            ),
            select(
                30203,
                "Rozwiązania równania cos(x)=1 w przedziale <0, 2π> to:",
                "0, 2π",
                listOf("π/2", "π", "π/2, 3π/2"),
                rng
            )
        ).shuffled(rng)
    }

    private fun trigonometricGraphs(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            GraphSelectFromList(
                30300,
                "Który wykres jest narysowany?",
                listOf(MathShape.FunctionPlot({ x -> kotlin.math.sin(x) }, Color(0xFFE53935))),
                MathViewport(xMin = -6.5, xMax = 6.5, yMin = -1.5, yMax = 1.5, gridStep = 1.0),
                listOf("sin(x)", "cos(x)", "tg(x)"),
                setOf(0),
                hint = Hint("Sinus przechodzi przez punkt (0, 0).")
            ),
            GraphSelectFromList(
                30301,
                "Który wykres jest narysowany?",
                listOf(MathShape.FunctionPlot({ x -> kotlin.math.cos(x) }, Color(0xFFE53935))),
                MathViewport(xMin = -6.5, xMax = 6.5, yMin = -1.5, yMax = 1.5, gridStep = 1.0),
                listOf("cos(x)", "sin(x)", "tg(x)"),
                setOf(0),
                hint = Hint("Cosinus dla x=0 ma wartość 1.")
            )
        ).shuffled(rng)
    }

    private fun lineAndCircle(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            if (rng.nextBoolean()) {
                val a = rng.nextInt(-4, 5).let { if (it == 0) 1 else it }
                val b = rng.nextInt(-5, 6)
                questions += TypeAnswer(
                    40100 + i,
                    "Jaki jest współczynnik kierunkowy prostej y = ${a}x + ($b)?",
                    a,
                    hint = Hint("W równaniu y = ax + b współczynnik kierunkowy to a.")
                )
            } else {
                val r = rng.nextInt(2, 8)
                questions += TypeAnswer(
                    40100 + i,
                    "Okrąg ma równanie (x-a)^2 + (y-b)^2 = ${r * r}. Podaj promień.",
                    r,
                    hint = Hint("Prawa strona równania okręgu to r^2.")
                )
            }
        }

        return questions.shuffled(rng)
    }

    private fun pointLineDistance(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val point = rng.nextInt(-6, 7)
            val line = rng.nextInt(-4, 5)
            val vertical = rng.nextBoolean()
            val distance = abs(point - line)
            val prompt = if (vertical) {
                "Oblicz odległość punktu P($point, 2) od prostej x = $line."
            } else {
                "Oblicz odległość punktu P(2, $point) od prostej y = $line."
            }
            questions += TypeAnswer(
                40200 + i,
                prompt,
                distance,
                hint = Hint("Dla prostej pionowej lub poziomej wystarczy różnica odpowiednich współrzędnych.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun vectors(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val ax = rng.nextInt(-4, 5)
            val ay = rng.nextInt(-4, 5)
            val bx = rng.nextInt(-4, 5)
            val by = rng.nextInt(-4, 5)
            val askX = rng.nextBoolean()
            val answer = if (askX) ax + bx else ay + by
            val coord = if (askX) "pierwszą" else "drugą"
            questions += TypeAnswer(
                40300 + i,
                "Dane są wektory u=($ax,$ay), v=($bx,$by). Podaj $coord współrzędną u+v.",
                answer,
                hint = Hint("Wektory dodajemy współrzędna po współrzędnej.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun sequences(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val a1 = rng.nextInt(1, 8)
            val r = rng.nextInt(2, 6)
            val n = rng.nextInt(4, 9)
            val value = a1 + (n - 1) * r
            questions += TypeAnswer(
                50100 + i,
                "Ciąg arytmetyczny: a1=$a1, r=$r. Oblicz a$n.",
                value,
                hint = Hint("Wzór: an = a1 + (n-1)r.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun sequenceLimits(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val c = rng.nextInt(1, 5)
            val limit = rng.nextInt(1, 6)
            val a = c * limit

            val b = rng.nextInt(-5, 6)
            val d = rng.nextInt(-5, 6)

            val signB = if (b >= 0) "+ $b" else "- ${-b}"
            val signD = if (d >= 0) "+ $d" else "- ${-d}"

            questions += TypeAnswer(
                50200 + i,
                "Oblicz granicę ciągu dla n dążącego do nieskończoności: a_n = (${a}n $signB) / (${c}n $signD)",
                limit,
                hint = Hint("Granica ilorazu wielomianów tego samego stopnia to po prostu iloraz liczb stojących przy 'n', czyli $a podzielone przez $c.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun geometricSeries(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            TypeAnswer(
                50300,
                "Suma nieskończonego szeregu geometrycznego: a1=1, q=1/2.",
                2,
                hint = Hint("S = a1/(1-q) = 1/(1-1/2) = 2.")
            ),
            FractionAnswer(
                50301,
                "Suma nieskończonego szeregu geometrycznego: a1=3, q=1/3.",
                9,
                2,
                hint = Hint("S = 3/(1-1/3) = 3/(2/3) = 9/2.")
            ),
            TypeAnswer(50302, "Suma szeregu: a1=2, q=0.5.", 4, hint = Hint("S = 2/(1-0.5) = 4.")),
            select(
                50303,
                "Kiedy istnieje suma nieskończonego szeregu geometrycznego?",
                "|q| < 1",
                listOf("q > 1", "q = 1", "|q| > 1"),
                rng
            )
        ).shuffled(rng)
    }

    private fun combinatorics(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(4) { i ->
            val n = rng.nextInt(3, 6)
            val silnia = (1..n).reduce { acc, j -> acc * j }
            questions += TypeAnswer(
                60100 + i,
                "Na ile sposobów można ustawić w szeregu $n różnych osób?",
                silnia,
                hint = Hint("Musisz policzyć silnię, czyli pomnożyć liczby do $n włącznie (1 * 2 * ... * $n).")
            )
        }

        repeat(4) { i ->
            val k = rng.nextInt(2, 6)
            val s = rng.nextInt(2, 5)
            questions += TypeAnswer(
                60110 + i,
                "W szafie masz $k różnych koszul i $s różnych par spodni. Na ile sposobów możesz skompletować ubiór?",
                k * s,
                hint = Hint("Użyj reguły mnożenia. Wystarczy pomnożyć $k przez $s.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun bernoulli(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            FractionAnswer(
                60200,
                "Rzucamy monetą 2 razy. Jakie jest prawdopodobieństwo dokładnie jednego orła?",
                1,
                2,
                hint = Hint("Są dwa sprzyjające wyniki: OR, RO z czterech możliwych.")
            ),
            FractionAnswer(
                60201,
                "Rzucamy monetą 3 razy. Prawdopodobieństwo dokładnie trzech orłów wynosi:",
                1,
                8,
                hint = Hint("Jedna sekwencja OOO z ośmiu możliwych.")
            ),
            FractionAnswer(
                60202,
                "Rzucamy kostką 2 razy. Prawdopodobieństwo dwóch szóstek wynosi:",
                1,
                36,
                hint = Hint("Każdy rzut ma szansę 1/6, więc 1/6 * 1/6.")
            ),
            FractionAnswer(
                60203,
                "Rzucamy monetą 3 razy. Prawdopodobieństwo dokładnie jednego orła wynosi:",
                3,
                8,
                hint = Hint("Wybieramy miejsce dla jednego orła: C(3,1)=3.")
            )
        ).shuffled(rng)
    }

    private fun conditionalProbability(seed: Long): List<Question> {
        val rng = Random(seed)

        return listOf(
            FractionAnswer(
                60300,
                "W klasie jest 12 dziewczyn, 8 chłopaków. 6 dziewczyn lubi fizykę. Losujemy dziewczynę. Prawdopodobieństwo, że lubi fizykę:",
                1,
                2,
                hint = Hint("Warunek zawęża zbiór do 12 dziewczyn. 6/12 = 1/2.")
            ),
            FractionAnswer(
                60301,
                "W pudełku są 3 kule czerwone i 2 niebieskie. Losujemy jedną kulę. Prawdopodobieństwo wylosowania czerwonej wynosi:",
                3,
                5,
                hint = Hint("Przed losowaniem 3 z 5 kul są czerwone.")
            ),
            FractionAnswer(
                60302,
                "W grupie 10 osób 4 uczą się rozszerzonej matematyki. Losujemy osobę z tej grupy. Prawdopodobieństwo:",
                2,
                5,
                hint = Hint("4/10 skracamy do 2/5.")
            ),
            FractionAnswer(
                60303,
                "Z talii liczb 1-10 wylosowano liczbę parzystą. Jakie jest prawdopodobieństwo, że jest większa od 5?",
                3,
                5,
                hint = Hint(
                    "Parzyste: 2,4,6,8,10. Większe od 5: 6,8,10, czyli 3/5.",
                    steps = listOf(
                        "Tu warunkiem są liczby parzyste.",
                        "Sprzyjające: 6, 8, 10.",
                        "Wynik: 3/5."
                    )
                )
            )
        ).shuffled(rng)
    }

    private fun derivativeRate(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val power = rng.nextInt(2, 4)
            val a = rng.nextInt(1, 4).let { if (rng.nextBoolean()) it else -it }
            val x = rng.nextInt(-2, 3)

            val value = if (power == 2) {
                (2 * a) * x
            } else {
                (3 * a) * (x * x)
            }

            questions += TypeAnswer(
                70100 + i,
                "Dla f(x) = ${a}x^$power oblicz wartość pochodnej f'($x).",
                value,
                hint = Hint(
                    "Najpierw wyznacz wzór pochodnej z x^$power, a potem podstaw za x liczbę $x.",
                    steps = listOf(
                        "Wzór f'(x) wynosi: ${power * a}x^${power - 1}",
                        "Teraz podstawiamy $x: ${power * a} * ($x)^${power - 1} = $value"
                    )
                )
            )
        }

        return questions.shuffled(rng)
    }

    private fun extrema(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val p = rng.nextInt(-4, 5)
            val q = rng.nextInt(-6, 7)
            questions += TypeAnswer(
                70200 + i,
                "Funkcja f(x) = (x - ($p))^2 + ($q). Podaj argument minimum.",
                p,
                hint = Hint("Wierzchołek paraboli ma współrzędne ($p, $q), więc minimum jest dla x=$p.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun tangent(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()

        repeat(8) { i ->
            val x = rng.nextInt(-4, 5).let { if (it == 0) 2 else it }
            val slope = 2 * x
            val shapes = listOf(
                MathShape.FunctionPlot({ t -> t * t }, Color(0xFFE53935)),
                MathShape.PointMark(
                    Pt(x.toDouble(), (x * x).toDouble()),
                    label = "P",
                    color = Color(0xFFE53935)
                )
            )
            questions += GraphTypeAnswer(
                70300 + i,
                "Dla f(x)=x^2 podaj współczynnik kierunkowy stycznej w punkcie x=$x.",
                shapes,
                MathViewport(xMin = -6.0, xMax = 6.0, yMin = -2.0, yMax = 18.0),
                slope,
                hint = Hint("Współczynnik stycznej to wartość pochodnej. f'(x)=2x.")
            )
        }

        return questions.shuffled(rng)
    }

    private fun logProperties(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val base = rng.nextInt(2, 4)
            val p1 = rng.nextInt(1, 4)
            val p2 = rng.nextInt(1, 3)
            val arg1 = base.toDouble().pow(p1.toDouble()).toInt()
            val arg2 = base.toDouble().pow(p2.toDouble()).toInt()

            questions += TypeAnswer(
                80100 + i,
                "Oblicz wartość wyrażenia: log_$base($arg1) + log_$base($arg2)",
                p1 + p2,
                hint = Hint("Wzór na dodawanie logarytmów: log_a(x) + log_a(y) = log_a(x * y). Skoro podstawa to $base, oblicz log_$base(${arg1 * arg2}).")
            )
        }
        return questions.shuffled(rng)
    }

    private fun logEquations(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val base = rng.nextInt(2, 4)
            val wynik = rng.nextInt(1, 4)
            val wartosc = base.toDouble().pow(wynik.toDouble()).toInt()
            val a = rng.nextInt(1, 5)

            questions += TypeAnswer(
                80200 + i,
                "Rozwiąż równanie: log_$base(x - $a) = $wynik. Podaj samo x.",
                wartosc + a,
                hint = Hint("Z definicji logarytmu: podstawa do potęgi wyniku daje środek. Więc x - $a = $base^$wynik.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun logPowers(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val base = rng.nextInt(2, 4)
            val potega = rng.nextInt(2, 6)
            questions += TypeAnswer(
                80300 + i,
                "Oblicz: log_$base($base^$potega)",
                potega,
                hint = Hint("Wyrzuć potęgę ($potega) przed logarytm. Zostanie $potega * log_$base($base). A logarytm o tej samej podstawie i argumencie to zawsze 1.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun absEquations(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val a = rng.nextInt(-3, 4)
            val b = rng.nextInt(1, 6)
            val znakA = if (a >= 0) "- $a" else "+ ${-a}"
            val prawidlowa = "x = ${a + b} lub x = ${a - b}"
            val bledne = listOf(
                "x = ${a + b}",
                "x = ${-a - b} lub x = ${-a + b}",
                "brak rozwiązań"
            )

            questions += select(
                90100 + i,
                "Rozwiąż równanie: |x $znakA| = $b",
                prawidlowa,
                bledne,
                rng,
                Hint("Z modułu wyciągasz dwa przypadki: środek = $b ORAZ środek = -$b.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun absInequalities(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val a = rng.nextInt(-2, 3)
            val b = rng.nextInt(2, 5)
            val znakA = if (a > 0) "- $a" else "+ ${-a}"
            val min = a - b
            val max = a + b
            val prawidlowa = "x należy do ($min, $max)"

            questions += select(
                90200 + i,
                "Rozwiąż nierówność: |x $znakA| < $b",
                prawidlowa,
                listOf("x należy do (-$b, $b)", "x < $min lub x > $max", "x należy do <$min, $max>"),
                rng,
                Hint("Odległość liczby x od punktu $a jest mniejsza niż $b. Szukasz wszystkiego pomiędzy $min a $max.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun absDistance(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val x1 = rng.nextInt(-5, 0)
            val x2 = rng.nextInt(1, 6)
            val dist = abs(x1 - x2)

            questions += TypeAnswer(
                90300 + i,
                "Jaka jest odległość między punktami $x1 oraz $x2 na osi liczbowej?",
                dist,
                hint = Hint("Oblicz różnicę tych liczb i weź wartość bezwzględną: |$x1 - $x2| = $dist.")
            )
        }
        return questions.shuffled(rng)
    }


    private fun cubeDiagonals(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val a = rng.nextInt(2, 6)
            val zapytajO_Szerokosc = rng.nextBoolean()
            val pytanie = if (zapytajO_Szerokosc) "przekątnej ściany bocznej" else "przekątnej sześcianu"
            val odpowiedz = if (zapytajO_Szerokosc) "$a√2" else "$a√3"
            val zla1 = if (zapytajO_Szerokosc) "$a√3" else "$a√2"

            questions += select(
                100100 + i,
                "Krawędź sześcianu wynosi $a. Jaka jest długość $pytanie?",
                odpowiedz,
                listOf(zla1, "${a*2}√2", "${a*a}"),
                rng,
                Hint("Przekątna ściany (kwadratu) to a√2, a przekątna całego sześcianu to a√3.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun prismVolume(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val a = rng.nextInt(2, 5)
            val h = rng.nextInt(3, 7)
            val objetosc = (a * a) * h

            questions += TypeAnswer(
                100200 + i,
                "Oblicz objętość graniastosłupa o podstawie kwadratu. Bok podstawy a=$a, wysokość bryły H=$h.",
                objetosc,
                hint = Hint("Wzór na objętość to Pole podstawy razy Wysokość (V = Pp * H). Podstawa to kwadrat, więc Pp = $a * $a.")
            )
        }
        return questions.shuffled(rng)
    }

    private fun pyramidVolume(seed: Long): List<Question> {
        val rng = Random(seed)
        val questions = mutableListOf<Question>()
        repeat(4) { i ->
            val a = rng.nextInt(2, 5)
            val h = rng.nextInt(1, 4) * 3
            val objetosc = (a * a * h) / 3

            questions += TypeAnswer(
                100300 + i,
                "Oblicz objętość ostrosłupa prawidłowego czworokątnego (podstawa to kwadrat). Krawędź podstawy a=$a, wysokość bryły H=$h.",
                objetosc,
                hint = Hint("Wzór dla ostrosłupa to 1/3 * Pp * H. Podstawa to kwadrat, czyli Pp = $a*$a=${a*a}. Wylicz: 1/3 * ${a*a} * $h.")
            )
        }
        return questions.shuffled(rng)
    }
}
