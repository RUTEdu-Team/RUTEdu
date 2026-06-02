package prz.rutedu.app.data

import androidx.compose.ui.graphics.Color
import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.MathViewport
import prz.rutedu.app.math.Pt
import prz.rutedu.app.math.TriangleBuilder
import prz.rutedu.app.math.mathEngineAvailable
import prz.rutedu.app.models.Equation
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.MathOperator.*
import prz.rutedu.app.models.Question
import prz.rutedu.app.models.Question.*
import kotlin.random.Random

/**
 * Procedurally generates math quiz questions from a random seed.
 */
object MathQuestionGenerator {

    private fun t(pl: String, en: String): String {
        return if (prz.rutedu.app.locale.getCurrentLanguage() == "pl") pl else en
    }


    /**
     * Generates or retrieves the ordered list of questions for a specific math lesson.
     *
     * Depending on [lessonId], this function delegates to a corresponding procedural generator
     * method that seeds a random number generator with [seed] to construct 5-10 math questions.
     * Optionally filters out already answered questions passed in [excludeIds].
     *
     * @param lessonId   The math lesson identifier (e.g. `"mat_1_1"`).
     * @param seed       Random seed to guarantee deterministic question generation.
     * @param excludeIds Set of question IDs to exclude from the generated list.
     * @return List of generated math [Question]s.
     */
    fun questionsFor(lessonId: String, seed: Long, excludeIds: Set<Int> = emptySet()): List<Question> {
        val all = when (lessonId) {
            "mat_1_1" -> mat_1_1(seed); "mat_1_2" -> mat_1_2(seed); "mat_1_3" -> mat_1_3(seed)
            "mat_1_4" -> mat_1_4(seed); "mat_1_5" -> mat_1_5(seed); "mat_2_1" -> mat_2_1(seed)
            "mat_2_2" -> mat_2_2(seed); "mat_2_3" -> mat_2_3(seed); "mat_3_1" -> mat_3_1(seed)
            "mat_3_2" -> mat_3_2(seed); "mat_3_3" -> mat_3_3(seed); "mat_4_1" -> mat_4_1(seed)
            "mat_4_2" -> mat_4_2(seed); "mat_4_3" -> mat_4_3(seed); "mat_4_4" -> mat_4_4(seed)
            "mat_5_1" -> mat_5_1(seed); "mat_5_2" -> mat_5_2(seed); "mat_5_3" -> mat_5_3(seed)
            "mat_5_4" -> mat_5_4(seed); "mat_5_5" -> mat_5_5(seed)
            "mat_6_1" -> mat_6_1(seed); "mat_6_2" -> mat_6_2(seed)
            "mat_6_3" -> mat_6_3(seed); "mat_6_4" -> mat_6_4(seed)
            "mat_7_1" -> mat_7_1(seed); "mat_7_2" -> mat_7_2(seed)
            "mat_7_3" -> mat_7_3(seed)
            "mat_8_1" -> mat_8_1(seed); "mat_8_2" -> mat_8_2(seed)
            "mat_8_3" -> mat_8_3(seed)
            "mat_9_1" -> mat_9_1(seed); "mat_9_2" -> mat_9_2(seed)
            "mat_9_3" -> mat_9_3(seed); "mat_9_4" -> mat_9_4(seed)
            "mat_9_5" -> mat_9_5(seed)
            "mat_10_1" -> mat_10_1(seed); "mat_10_2" -> mat_10_2(seed)
            "mat_10_3" -> mat_10_3(seed); "mat_10_4" -> mat_10_4(seed)
            "mat_11_1" -> mat_11_1(seed); "mat_11_2" -> mat_11_2(seed)
            "mat_11_3" -> mat_11_3(seed); "mat_11_4" -> mat_11_4(seed)
            "mat_12_1" -> mat_12_1(seed); "mat_12_2" -> mat_12_2(seed)
            "mat_12_3" -> mat_12_3(seed)
            else -> emptyList()
        }
        return if (excludeIds.isEmpty()) all else all.filter { it.id !in excludeIds }
    }

    /**
     * Returns the total number of questions available for [lessonId] before any filtering.
     */
    fun totalFor(lessonId: String): Int = questionsFor(lessonId, seed = 0L).size

    private fun mat_1_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val op = if (rng.nextBoolean()) ADD else SUBTRACT; val a = rng.nextInt(1, 100)
            val b = if (op == ADD) rng.nextInt(1, 100) else rng.nextInt(1, a + 1)
            questions += FindAnswer(1100 + i, a, b, op, Hint(if (op == ADD) t("Dodawanie to liczenie do przodu.", "Addition is counting forward.") else t("Odejmowanie to liczenie wstecz.", "Subtraction is counting backward.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val a = rng.nextInt(2, 11); val b = rng.nextInt(2, 11)
            questions += FindAnswer(1200 + i, a, b, MULTIPLY, Hint(t("Tabliczka mnożenia.", "Multiplication table.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val b = rng.nextInt(2, 6); val e = rng.nextInt(2, 4)
            questions += FindAnswer(1300 + i, b, e, POWER, Hint(t("Pomnóż $b przez siebie $e razy.", "Multiply $b by itself $e times.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val squares = listOf(4, 9, 16, 25, 36, 49, 64, 81, 100)
        squares.shuffled(rng).take(6).forEachIndexed { i, s ->
            questions += FindAnswer(1400 + i, 2, s, ROOT, Hint(t("Jaka liczba do kwadratu daje $s?", "What number squared gives $s?")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        listOf(2 to 8, 3 to 9, 10 to 100, 2 to 16, 5 to 25).forEachIndexed { i, (b, r) ->
            questions += FindAnswer(1500 + i, b, r, LOG, Hint(t("Do jakiej potęgi podnieść $b, aby otrzymać $r?", "To what power should $b be raised to get $r?")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val v = listOf("x", "y", "a").random(rng); val c = rng.nextInt(2, 10)
            val correct = "$c$v"; val opts = listOf(correct, "$c+$v", "$v-1", "2$v").distinct().shuffled(rng)
            questions += SelectFromList(2100 + i, t("Które z poniższych jest jednomianem?", "Which of the following is a monomial?"), opts, setOf(opts.indexOf(correct)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(1, 10); val prompt = "(x + $a)²"; val correct = "x² + ${2*a}x + ${a*a}"
            if (mathEngineAvailable) questions += ExpressionTypeAnswer(2200 + i, t("Rozwiń: $prompt", "Expand: $prompt"), "x^2 + ${2*a}*x + ${a*a}", correct)
            else questions += SelectFromList(2200 + i, t("Rozwiń: $prompt", "Expand: $prompt"), listOf(correct, "x² + ${a*a}", "x² + ${a}x").shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf(correct))) }
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val c = rng.nextInt(2, 6); val a = c * rng.nextInt(1, 4); val b = c * rng.nextInt(1, 4)
            val res = "$c(${a/c}x + ${b/c}y)"
            questions += SelectFromList(2300 + i, t("Wyłącz czynnik: ${a}x + ${b}y", "Factor out: ${a}x + ${b}y"), listOf(res, "$c(${a}x + ${b}y)", "2(${a/2}x + ${b/2}y)").distinct().shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf(res))) }
        }
        return questions.shuffled(rng)
    }

    private fun mat_3_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(-5, 6); val a = rng.nextInt(2, 6); val b = rng.nextInt(-10, 11); val c = a * x + b
            questions += LinearEquation(3100 + i, "$a*x + ($b) == $c", if (b >= 0) "${a}x + $b = $c" else "${a}x - ${-b} = $c")
        }
        return questions.shuffled(rng)
    }

    private fun mat_3_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(-3, 4); val y = rng.nextInt(-3, 4)
            questions += SystemOfEquations(3200 + i, listOf(Equation("x + y", "${x+y}"), Equation("x - y", "${x-y}")), listOf("x", "y"))
        }
        return questions.shuffled(rng)
    }

    private fun mat_3_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(1, 5); val s = rng.nextInt(-3, 4); val c = a * s
            val res = "x > $s"; val opts = listOf(res, "x < $s", "x > ${s+1}").shuffled(rng)
            questions += SelectFromList(3300 + i, "Rozwiąż: ${a}x > $c", opts, setOf(opts.indexOf(res)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(1, 5); val b = rng.nextInt(-5, 6); val x = rng.nextInt(0, 5); val y = a * x + b
            val opts = listOf("$y", "${y+1}", "${y-1}").shuffled(rng)
            questions += SelectFromList(4100 + i, "Dla f(x) = ${a}x + $b, oblicz f($x)", opts, setOf(opts.indexOf("$y")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val p = rng.nextInt(-3, 4); val q = rng.nextInt(-3, 4); val res = "($p, $q)"
            val opts = listOf(res, "(${-p}, $q)", "($q, $p)").distinct().shuffled(rng)
            questions += SelectFromList(4200 + i, "Podaj wierzchołek f(x) = (x - $p)² + $q", opts, setOf(opts.indexOf(res)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x0 = rng.nextInt(-3, 4); val f = { x: Double -> x - x0 }
            questions += GraphTypeAnswer(4300 + i, "Odczytaj miejsce zerowe z wykresu", listOf(MathShape.FunctionPlot(f)), MathViewport(), x0)
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val inc = rng.nextBoolean(); val f = { x: Double -> if(inc) x else -x }
            questions += GraphSelectFromList(4400 + i, t("Określ monotoniczność", "Determine monotonicity"), listOf(MathShape.FunctionPlot(f)), MathViewport(), listOf(t("Rosnąca", "Increasing"), t("Malejąca", "Decreasing")), setOf(if(inc) 0 else 1))
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(30, 80).toDouble(); val b = rng.nextInt(30, 80).toDouble(); val c = 180 - a - b
            val (v, vp) = TriangleBuilder.fromAnglesWithViewport(a, b)
            questions += GraphTypeAnswer(5100 + i, t("Oblicz miarę trzeciego kąta", "Calculate the measure of the third angle"), listOf(MathShape.Triangle(v.first, v.second, v.third, labelA="${a.toInt()}°", labelB="${b.toInt()}°", labelC="?")), vp, c.toInt(), "°")
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val w = rng.nextInt(2, 6); val h = rng.nextInt(2, 6)
            questions += GraphTypeAnswer(5200 + i, t("Oblicz pole prostokąta o bokach $w i $h", "Calculate the area of a rectangle with sides $w and $h"), listOf(MathShape.Rectangle(-w/2.0, -h/2.0, w.toDouble(), h.toDouble(), filled = true)), MathViewport(), w * h)
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val r = 4.0

        // Helper to get point on circle
        fun onCircle(ang: Double): Pt = Pt(r * kotlin.math.cos(ang), r * kotlin.math.sin(ang))
        // Helper to get midpoint for label
        fun mid(p1: Pt, p2: Pt): Pt = Pt((p1.x + p2.x)/2.0, (p1.y + p2.y)/2.0)

        data class NamedElement(val name: String, val hint: String, val getShapes: (String, Double) -> List<MathShape>, val color: Color)

        val allElements = listOf(
            NamedElement(t("promieniem", "the radius"), t("Promień łączy środek okręgu z punktem na okręgu.", "A radius connects the center of the circle to a point on the circle."), { l, a ->
                val p = onCircle(a)
                listOf(MathShape.Segment(Pt(0.0, 0.0), p, color = Color(0xFF4A80F0)), MathShape.TextLabel(Pt(p.x/2.0 + 0.3, p.y/2.0 + 0.3), l, color = Color(0xFF4A80F0)))
            }, Color(0xFF4A80F0)),
            NamedElement(t("średnicą", "the diameter"), t("Średnica przechodzi przez środek okręgu.", "A diameter passes through the center of the circle."), { l, a ->
                val p1 = onCircle(a); val p2 = onCircle(a + kotlin.math.PI)
                listOf(MathShape.Segment(p1, p2, color = Color(0xFF3DBD7D)), MathShape.TextLabel(Pt(p1.x*0.4 + 0.3, p1.y*0.4 + 0.3), l, color = Color(0xFF3DBD7D)))
            }, Color(0xFF3DBD7D)),
            NamedElement(t("cięciwą", "the chord"), t("Cięciwa łączy dwa punkty na okręgu, nie musi przechodzić przez środek.", "A chord connects two points on the circle and does not have to pass through the center."), { l, a ->
                val p1 = onCircle(a); val p2 = onCircle(a + 1.2)
                val m = mid(p1, p2)
                listOf(MathShape.Segment(p1, p2, color = Color(0xFFF47B20)), MathShape.TextLabel(Pt(m.x + 0.4, m.y + 0.4), l, color = Color(0xFFF47B20)))
            }, Color(0xFFF47B20))
        )

        // 1. Three distinct questions for core elements
        allElements.forEachIndexed { qIdx, target ->
            val labels = listOf("A", "B", "C")
            val shuffled = allElements.shuffled(Random(seed + qIdx))
            val shapes = mutableListOf<MathShape>(MathShape.Circle(0.0, 0.0, r))

            shuffled.forEachIndexed { i, el ->
                // Ensure segments are separated by using fixed offsets + random base
                val angle = (seed % 100) / 10.0 + (i * 2.0)
                shapes.addAll(el.getShapes(labels[i], angle))
            }

            questions += GraphSelectFromList(5300 + qIdx, t("Który odcinek jest ${target.name} okręgu?", "Which segment is ${target.name} of the circle?"), shapes, 
                MathViewport(xMin = -6.0, xMax = 6.0, yMin = -6.0, yMax = 6.0, showGrid = false, showAxes = false),
                labels, setOf(shuffled.indexOf(target)), hint = Hint(target.hint))
        }

        // 2. One question: segment outside the circle
        repeat(1) { i ->
            val rot = rng.nextDouble(0.0, 6.0)
            val shapes = mutableListOf<MathShape>(MathShape.Circle(0.0, 0.0, r))
            val labels = listOf("A", "B", "C")

            // A: Radius (Inside)
            val pr = onCircle(rot)
            shapes += MathShape.Segment(Pt(0.0,0.0), pr, color = Color(0xFF4A80F0))
            shapes += MathShape.TextLabel(Pt(pr.x/2.0 + 0.3, pr.y/2.0 + 0.3), "A", Color(0xFF4A80F0))

            // B: Diameter (Inside)
            val pd1 = onCircle(rot + 2.0); val pd2 = onCircle(rot + 2.0 + kotlin.math.PI)
            shapes += MathShape.Segment(pd1, pd2, color = Color(0xFF3DBD7D))
            shapes += MathShape.TextLabel(Pt(pd1.x * 0.5 + 0.3, pd1.y * 0.5 + 0.3), "B", Color(0xFF3DBD7D))

            // C: Outside segment (Zoomed out)
            val po1 = Pt(r * 1.3 * kotlin.math.cos(rot + 4.5), r * 1.3 * kotlin.math.sin(rot + 4.5))
            val po2 = Pt(r * 1.9 * kotlin.math.cos(rot + 4.8), r * 1.9 * kotlin.math.sin(rot + 4.8))
            shapes += MathShape.Segment(po1, po2, color = Color(0xFFF47B20))
            shapes += MathShape.TextLabel(mid(po1, po2).let { Pt(it.x + 0.5, it.y + 0.5) }, "C", Color(0xFFF47B20))

            questions += GraphSelectFromList(5340 + i, t("Który odcinek znajduje się poza obszarem koła?", "Which segment lies outside the area of the circle?"), shapes,
                MathViewport(xMin = -8.0, xMax = 8.0, yMin = -8.0, yMax = 8.0, showGrid = false, showAxes = false),
                labels, setOf(2), hint = Hint(t("Odcinek poza kołem nie przecina okręgu i znajduje się w całości na zewnątrz.", "The segment outside the circle does not intersect the circle and is entirely on the outside.")))
        }

        // 3. Two calculation questions
        repeat(2) { i ->
            val rad = rng.nextInt(3, 10); val dia = 2 * rad
            questions += SelectFromList(5350 + i, t("Jeśli promień r = $rad, to ile wynosi średnica d?", "If the radius is r = $rad, what is the diameter d?"), listOf("$dia", "${rad+2}", "${rad-1}").shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf("$dia"))) }
        }

        return questions.shuffled(rng)
    }

    private fun mat_5_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(2, 8); val h = rng.nextInt(2, 6); val area = (a * h) / 2
            questions += GraphTypeAnswer(5400 + i, t("Oblicz pole trójkąta (a=$a, h=$h)", "Calculate the area of a triangle (a=$a, h=$h)"), listOf(MathShape.Triangle(Pt(-a/2.0, 0.0), Pt(a/2.0, 0.0), Pt(0.0, h.toDouble()))), MathViewport(), area)
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val (a, b, c) = listOf(Triple(3, 4, 5), Triple(6, 8, 10), Triple(5, 12, 13)).random(rng)
            questions += GraphTypeAnswer(5500 + i, t("Przyprostokątne $a i $b, oblicz przeciwprostokątną", "Legs $a and $b, calculate the hypotenuse"), listOf(MathShape.Triangle(Pt(0.0,0.0), Pt(a.toDouble(),0.0), Pt(0.0,b.toDouble()), labelAB="$a", labelCA="$b", labelBC="?")), MathViewport(), c)
        }
        return questions.shuffled(rng)
    }

    private fun mat_6_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Fractions to Decimals
        val pairs = listOf(1 to 2 to 0.5, 1 to 4 to 0.25, 3 to 4 to 0.75, 1 to 5 to 0.2, 2 to 5 to 0.4, 1 to 8 to 0.125, 1 to 10 to 0.1)
        pairs.forEachIndexed { i, pair ->
            val (f, d) = pair
            val (n, den) = f
            questions += DecimalAnswer(6100 + i, t("Zamień ułamek zwykły $n/$den na dziesiętny", "Convert the common fraction $n/$den to a decimal"), d, precision = 3, hint = Hint(t("Podziel licznik przez mianownik.", "Divide the numerator by the denominator.")))
        }
        // Decimals to Fractions
        val revPairs = listOf(0.5 to (1 to 2), 0.2 to (1 to 5), 0.75 to (3 to 4), 0.6 to (3 to 5))
        revPairs.forEachIndexed { i, pair ->
            val (d, f) = pair
            val (n, den) = f
            questions += FractionAnswer(6150 + i, t("Zamień ułamek dziesiętny $d na zwykły nieskracalny", "Convert the decimal $d to an irreducible common fraction"), n, den, hint = Hint(t("Zapisz jako ułamek o mianowniku 10, 100... a następnie skróć.", "Write as a fraction with a denominator of 10, 100... and then reduce it.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_6_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(8) { i ->
            val den = rng.nextInt(2, 10)
            val n1 = rng.nextInt(1, den); val n2 = rng.nextInt(1, den)
            val op = if (rng.nextBoolean()) "+" else "-"
            val resN = if (op == "+") n1 + n2 else n1 - n2
            if (resN > 0) {
                questions += FractionAnswer(6200 + i, t("Oblicz: $n1/$den $op $n2/$den", "Calculate: $n1/$den $op $n2/$den"), resN, den, hint = Hint(t("Dodaj lub odejmij liczniki, mianownik pozostaje bez zmian.", "Add or subtract the numerators, the denominator remains unchanged.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_6_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val cases = listOf(
            Triple("1/2", "1/3", ">"), Triple("1/4", "1/2", "<"), Triple("2/5", "3/5", "<"),
            Triple("4/8", "1/2", "="), Triple("0.5", "1/2", "="), Triple("0.7", "0.65", ">")
        )
        cases.forEachIndexed { i, (l, r, s) ->
            questions += ComparisonQuiz(6300 + i, t("Porównaj liczby", "Compare the numbers"), l, r, s, hint = Hint(t("Sprowadź do wspólnego mianownika lub zamień na ułamki dziesiętne.", "Find a common denominator or convert to decimals.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_6_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val a = rng.nextInt(1, 100) / 10.0
            val b = rng.nextInt(1, 100) / 10.0
            val op = listOf("+", "-", "*").random(rng)
            val res = when(op) {
                "+" -> a + b
                "-" -> a - b
                else -> a * b
            }
            if (res >= 0) {
                questions += DecimalAnswer(6400 + i, t("Oblicz: $a $op $b", "Calculate: $a $op $b"), res, precision = 2, hint = Hint(t("Działaj tak jak na liczbach całkowitych, pamiętając o przecinku.", "Perform the operation as on integers, keeping the decimal point in mind.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_7_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val percents = listOf(10, 20, 25, 50, 75, 5, 15)
        repeat(10) { i ->
            val p = percents.random(rng)
            val base = rng.nextInt(1, 21) * 10
            val res = (p * base) / 100.0
            if (res == res.toInt().toDouble()) {
                questions += TypeAnswer(7100 + i, t("Oblicz $p% z liczby $base", "Calculate $p% of $base"), res.toInt(), hint = Hint(t("Pomnóż liczbę przez procent i podziel przez 100, lub zamień procent na ułamek.", "Multiply the number by the percentage and divide by 100, or convert the percentage to a fraction.")))
            } else {
                questions += DecimalAnswer(7100 + i, t("Oblicz $p% z liczby $base", "Calculate $p% of $base"), res, hint = Hint(t("Pomnóż liczbę przez ułamek dziesiętny odpowiadający procentowi.", "Multiply the number by the decimal fraction corresponding to the percentage.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_7_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(8) { i ->
            val price = rng.nextInt(2, 21) * 10
            val p = listOf(10, 20, 25, 50).random(rng)
            val isDown = rng.nextBoolean()
            val diff = (price * p) / 100
            val res = if (isDown) price - diff else price + diff
            val type = if (isDown) t("obniżono", "decreased") else t("podniesiono", "increased")
            questions += TypeAnswer(7200 + i, t("Cenę $price zł $type o $p%. Jaka jest nowa cena?", "The price of $price zł was $type by $p%. What is the new price?"), res, unit = t("zł", "PLN"), hint = Hint(t("Najpierw oblicz kwotę zmiany, a potem dodaj ją do ceny lub odejmij od niej.", "First calculate the amount of change, then add it to or subtract it from the price.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_7_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Case 1: Percent of group
        repeat(3) { i ->
            val total = 20 + rng.nextInt(0, 4) * 5 // 20, 25, 30, 35
            val girls = rng.nextInt(total / 2, total - 2)
            val percent = (girls * 100) / total
            if ((girls * 100) % total == 0) {
                questions += TypeAnswer(7300 + i, t("W klasie jest $total uczniów, w tym $girls dziewcząt. Jaki procent klasy stanowią dziewczęta?", "There are $total students in a class, including $girls girls. What percentage of the class are girls?"), percent, unit = "%", hint = Hint(t("Podziel liczbę dziewcząt przez wszystkich uczniów i pomnóż przez 100%.", "Divide the number of girls by the total number of students and multiply by 100%.")))
            }
        }
        // Case 2: Simple interest / other
        questions += SelectFromList(7310, t("Jeśli 25% liczby wynosi 10, to cała liczba wynosi:", "If 25% of a number is 10, then the whole number is:"), listOf("40", "30", "20", "50"), setOf(0), hint = Hint(t("Skoro 1/4 liczby to 10, to cała liczba jest 4 razy większa.", "Since 1/4 of the number is 10, the whole number is 4 times larger.")))
        questions += SelectFromList(7311, t("Półtora to jaki procent całości?", "One and a half is what percentage of the whole?"), listOf("150%", "15%", "50%", "105%"), setOf(0), hint = Hint(t("Jedna całoś to 100%, więc 1.5 to 1.5 * 100%.", "One whole is 100%, so 1.5 is 1.5 * 100%.")))

        return questions.shuffled(rng)
    }

    private fun mat_8_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Simple direct proportion: a/b = x/c => x = (a*c)/b
        repeat(10) { i ->
            val b = rng.nextInt(2, 11)
            val x = rng.nextInt(1, 13)
            val a = b * x // ensure a/b is an integer x
            val c = rng.nextInt(2, 11)
            val res = x * c
            questions += TypeAnswer(8100 + i, t("Rozwiąż proporcję: $a / $b = x / $c", "Solve the proportion: $a / $b = x / $c"), res, hint = Hint(t("Mnóż na krzyż: $a * $c = $b * x, a następnie podziel przez $b.", "Cross-multiply: $a * $c = $b * x, then divide by $b.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_8_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val scales = listOf(100, 1000, 10000, 50000, 100000)
        repeat(10) { i ->
            val s = scales.random(rng)
            val mapDist = rng.nextInt(1, 21)
            val realDistCm = mapDist.toLong() * s

            val (res, unit) = when {
                realDistCm >= 100000 -> (realDistCm / 100000.0) to "km"
                realDistCm >= 100 -> (realDistCm / 100.0) to "m"
                else -> realDistCm.toDouble() to "cm"
            }

            if (res == res.toLong().toDouble()) {
                questions += TypeAnswer(8200 + i, t("Skala 1:$s. Odległość na mapie to $mapDist cm. Ile to w terenie?", "Scale 1:$s. The distance on the map is $mapDist cm. How much is it in the field?"), res.toInt(), unit = unit, hint = Hint(t("Pomnóż odległość na mapie przez mianownik skali i zamień jednostki.", "Multiply the distance on the map by the denominator of the scale and convert units.")))
            } else {
                questions += DecimalAnswer(8200 + i, t("Skala 1:$s. Odległość na mapie to $mapDist cm. Ile to w terenie?", "Scale 1:$s. The distance on the map is $mapDist cm. How much is it in the field?"), res, precision = 2, hint = Hint(t("1 cm na mapie to $s cm w terenie. Pomnóż i przelicz na $unit.", "1 cm on the map is $s cm in the field. Multiply and convert to $unit.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_8_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(8) { i ->
            val realDistKm = rng.nextInt(1, 51) * 10
            val scale = listOf(500000, 1000000, 2000000).random(rng)
            // realDistKm * 100,000 = scale * mapDistCm
            // mapDistCm = (realDistKm * 100,000) / scale
            val res = (realDistKm.toLong() * 100000) / scale
            if ((realDistKm.toLong() * 100000) % scale == 0L) {
                questions += TypeAnswer(8300 + i, t("Odległość w terenie wynosi $realDistKm km. Jaka będzie to odległość na mapie w skali 1:$scale?", "The distance in the field is $realDistKm km. What will be the distance on a map at a scale of 1:$scale?"), res.toInt(), unit = "cm", hint = Hint(t("Zamień kilometry na centymetry, a potem podziel przez mianownik skali.", "Convert kilometers to centimeters, then divide by the scale denominator.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val types = listOf(
            Triple(t("czworokątny", "quadrangular"), 4, "kwadrat"),
            Triple(t("trójkątny", "triangular"), 3, "trójkąt"),
            Triple(t("sześciokątny", "hexagonal"), 6, "sześciokąt")
        )
        types.forEachIndexed { i, (name, n, _) ->
            questions += TypeAnswer(9100 + i*3, t("Ile wierzchołków ma graniastosłup $name?", "How many vertices does a $name prism have?"), 2 * n, hint = Hint(t("Graniastosłup ma dwie identyczne podstawy, więc liczba wierzchołków to 2 * n.", "A prism has two identical bases, so the number of vertices is 2 * n.")))
            questions += TypeAnswer(9101 + i*3, t("Ile krawędzi ma graniastosłup $name?", "How many edges does a $name prism have?"), 3 * n, hint = Hint(t("Graniastosłup ma n krawędzi w każdej podstawie i n krawędzi bocznych.", "A prism has n edges in each base and n lateral edges.")))
            questions += TypeAnswer(9102 + i*3, t("Ile ścian ma graniastosłup $name?", "How many faces does a $name prism have?"), n + 2, hint = Hint(t("Graniastosłup ma n ścian bocznych i 2 podstawy.", "A prism has n lateral faces and 2 bases.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val types = listOf(
            Pair(t("czworokątny", "quadrangular"), 4),
            Pair(t("trójkątny", "triangular"), 3),
            Pair(t("pięciokątny", "pentagonal"), 5)
        )
        types.forEachIndexed { i, (name, n) ->
            questions += TypeAnswer(9200 + i*3, t("Ile wierzchołków ma ostrosłup $name?", "How many vertices does a $name pyramid have?"), n + 1, hint = Hint(t("Ostrosłup ma n wierzchołków w podstawie i jeden wierzchołek na górze.", "A pyramid has n vertices in the base and one vertex at the top.")))
            questions += TypeAnswer(9201 + i*3, t("Ile krawędzi ma ostrosłup $name?", "How many edges does a $name pyramid have?"), 2 * n, hint = Hint(t("Ostrosłup ma n krawędzi w podstawie i n krawędzi bocznych.", "A pyramid has n edges in the base and n lateral edges.")))
            questions += TypeAnswer(9202 + i*3, t("Ile ścian ma ostrosłup $name?", "How many faces does a $name pyramid have?"), n + 1, hint = Hint(t("Ostrosłup ma n ścian bocznych i 1 podstawę.", "A pyramid has n lateral faces and 1 base.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        questions += SelectFromList(9300, t("Która z tych brył powstaje przez obrót prostokąta wokół boku?", "Which of these solids is formed by rotating a rectangle around a side?"), listOf(t("Walec", "Cylinder"), t("Stożek", "Cone"), t("Kula", "Sphere"), t("Graniastosłup", "Prism")), setOf(0))
        questions += SelectFromList(9301, t("Która z tych brył powstaje przez obrót trójkąta prostokątnego wokół przyprostokątnej?", "Which of these solids is formed by rotating a right triangle around a leg?"), listOf(t("Stożek", "Cone"), t("Walec", "Cylinder"), t("Kula", "Sphere"), t("Ostrosłup", "Pyramid")), setOf(0))
        questions += SelectFromList(9302, t("Przekrój osiowy kuli jest:", "The axial section of a sphere is:"), listOf(t("Kołem", "A circle"), t("Kwadratem", "A square"), t("Trójkątem", "A triangle"), t("Prostokątem", "A rectangle")), setOf(0))
        return questions.shuffled(rng)
    }

    private fun mat_9_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Cube surface area: 6 * a^2
        repeat(5) { i ->
            val a = rng.nextInt(2, 7)
            questions += TypeAnswer(9400 + i, t("Oblicz pole powierzchni sześcianu o krawędzi $a", "Calculate the surface area of a cube with edge $a"), 6 * a * a, unit = "j²", hint = Hint(t("Sześcian ma 6 identycznych kwadratowych ścian. Pole jednej to a².", "A cube has 6 identical square faces. The area of one is a².")))
        }
        // Rectangular prism: 2(ab + bc + ac)
        val a = 2; val b = 3; val c = 4
        questions += TypeAnswer(9410, t("Oblicz pole powierzchni prostopadłościanu o wymiarach $a x $b x $c", "Calculate the surface area of a rectangular prism with dimensions $a x $b x $c"), 2 * (a*b + b*c + a*c), unit = "j²", hint = Hint(t("Pole powierzchni to suma pól wszystkich 6 ścian: 2 * (a*b + b*c + a*c).", "The surface area is the sum of the areas of all 6 faces: 2 * (a*b + b*c + a*c).")))
        return questions.shuffled(rng)
    }

    private fun mat_9_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Cube volume: a^3
        repeat(5) { i ->
            val a = rng.nextInt(2, 6)
            questions += TypeAnswer(9500 + i, t("Oblicz objętość sześcianu o krawędzi $a", "Calculate the volume of a cube with edge $a"), a * a * a, unit = "j³", hint = Hint(t("Objętość sześcianu to krawędź podniesiona do potęgi trzeciej: V = a³.", "The volume of a cube is the edge raised to the third power: V = a³.")))
        }
        // Rectangular prism: a * b * c
        repeat(5) { i ->
            val a = rng.nextInt(2, 6); val b = rng.nextInt(2, 6); val h = rng.nextInt(2, 6)
            questions += TypeAnswer(9510 + i, t("Oblicz objętość prostopadłościanu o wymiarach $a, $b i wysokości $h", "Calculate the volume of a rectangular prism with dimensions $a, $b and height $h"), a * b * h, unit = "j³", hint = Hint(t("Objętość prostopadłościanu to iloczyn jego trzech wymiarów: V = a * b * h.", "The volume of a rectangular prism is the product of its three dimensions: V = a * b * h.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_10_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(-4, 5); val y = rng.nextInt(-3, 4)
            val askX = rng.nextBoolean()
            val shapes = listOf(MathShape.PointMark(Pt(x.toDouble(), y.toDouble()), color = Color(0xFFE53935)))
            val correct = if (askX) x else y
            val axis = if (askX) t("X (odcięta)", "X (abscissa)") else t("Y (rzędna)", "Y (ordinate)")
            questions += GraphTypeAnswer(10100 + i, t("Podaj współrzędną $axis zaznaczonego punktu", "Provide the $axis coordinate of the marked point"), shapes, MathViewport(), correct, hint = Hint(t("Oś X to oś pozioma, oś Y to oś pionowa. Pierwsza liczba w (x, y) to X, druga to Y.", "The X axis is the horizontal axis, the Y axis is the vertical axis. The first number in (x, y) is X, the second is Y.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_10_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(-4, 5); val y = rng.nextInt(-3, 4)
            val shapes = listOf(MathShape.PointMark(Pt(x.toDouble(), y.toDouble()), color = Color(0xFF4A80F0)))
            val correct = "($x, $y)"
            val options = listOf(correct, "($y, $x)", "(${-x}, $y)", "($x, ${-y})").distinct().shuffled(rng)
            questions += GraphSelectFromList(10200 + i, t("Jakie są współrzędne tego punktu?", "What are the coordinates of this point?"), shapes, MathViewport(), options, setOf(options.indexOf(correct)), hint = Hint(t("Najpierw odczytaj wartość z osi poziomej (X), a potem z pionowej (Y).", "First read the value from the horizontal axis (X), then from the vertical axis (Y).")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_10_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(1, 4).toDouble(); val y = rng.nextInt(1, 4).toDouble()
            val shapes = listOf(MathShape.Rectangle(-x, -y, 2*x, 2*y, color = Color(0xFF3DBD7D)))
            val area = (2 * x * 2 * y).toInt()
            questions += GraphTypeAnswer(10300 + i, t("Oblicz pole narysowanego prostokąta", "Calculate the area of the drawn rectangle"), shapes, MathViewport(), area, hint = Hint(t("Oblicz długości boków licząc jednostki na osiach, a potem pomnóż je przez siebie.", "Calculate the side lengths by counting the units on the axes, then multiply them by each other.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_10_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x1 = rng.nextInt(-3, 1); val y1 = rng.nextInt(-2, 3)
            val dx = rng.nextInt(3, 5); val dy = 0
            val x2 = x1 + dx; val y2 = y1 + dy
            val shapes = listOf(
                MathShape.PointMark(Pt(x1.toDouble(), y1.toDouble())),
                MathShape.PointMark(Pt(x2.toDouble(), y2.toDouble())),
                MathShape.Segment(Pt(x1.toDouble(), y1.toDouble()), Pt(x2.toDouble(), y2.toDouble()), dashed = true)
            )
            questions += GraphTypeAnswer(10400 + i, t("Jaka jest odległość między tymi punktami?", "What is the distance between these points?"), shapes, MathViewport(), dx, hint = Hint(t("Punkty leżą na tej samej wysokości, więc wystarczy odjąć ich współrzędne X.", "The points lie at the same height, so it is enough to subtract their X coordinates.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_11_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val count = rng.nextInt(3, 6)
            val nums = List(count) { rng.nextInt(1, 11) }
            val sum = nums.sum()
            val mean = sum.toDouble() / count
            if (sum % count == 0) {
                questions += TypeAnswer(11100 + i, t("Oblicz średnią arytmetyczną liczb: ${nums.joinToString(", ")}", "Calculate the arithmetic mean of the numbers: ${nums.joinToString(", ")}"), mean.toInt(), hint = Hint(t("Średnia to suma wszystkich liczb podzielona przez ich ilość.", "The mean is the sum of all numbers divided by their quantity.")))
            } else {
                questions += DecimalAnswer(11100 + i, t("Oblicz średnią arytmetyczną liczb: ${nums.joinToString(", ")}", "Calculate the arithmetic mean of the numbers: ${nums.joinToString(", ")}"), mean, precision = 2, hint = Hint(t("Dodaj wszystkie liczby i podziel sumę ($sum) przez $count.", "Add all the numbers and divide the sum ($sum) by $count.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_11_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val count = listOf(3, 5).random(rng) // stick to odd counts for simple median
            val nums = List(count) { rng.nextInt(1, 15) }.sorted()
            val isMedian = rng.nextBoolean()
            if (isMedian) {
                val res = nums[count / 2]
                questions += TypeAnswer(11200 + i, t("Podaj medianę zbioru: ${nums.shuffled(rng).joinToString(", ")}", "Provide the median of the set: ${nums.shuffled(rng).joinToString(", ")}"), res, hint = Hint(t("Mediana to środkowa liczba w uporządkowanym zbiorze.", "The median is the middle number in an ordered set.")))
            } else {
                // Dominanta (Mode) - ensure one clear winner
                val mode = rng.nextInt(1, 10)
                val otherNums = List(count - 2) { rng.nextInt(11, 20) }
                val set = (listOf(mode, mode) + otherNums).shuffled(rng)
                questions += TypeAnswer(11200 + i, t("Podaj dominantę (liczbę najczęstszą) zbioru: ${set.joinToString(", ")}", "Provide the mode (most frequent number) of the set: ${set.joinToString(", ")}"), mode, hint = Hint(t("Dominanta to liczba, która występuje w zbiorze najwięcej razy.", "The mode is the number that appears the most times in the set.")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_11_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Case 1: Bar Chart
        repeat(3) { i ->
            val values = List(4) { rng.nextInt(1, 6) }
            val labels = listOf("A", "B", "C", "D")
            val shapes = mutableListOf<MathShape>()

            values.forEachIndexed { idx, v ->
                // Space out: 1.0 (bar) + 0.8 (gap). Start at x=1.0.
                val x = 1.0 + idx.toDouble() * 1.8
                shapes += MathShape.Rectangle(x, 0.0, 1.0, v.toDouble(), color = Color(0xFF4A80F0), filled = true)
                shapes += MathShape.TextLabel(Pt(x + 0.5, -0.4), labels[idx], color = Color(0xFF1A1A1A))
            }
            val targetIdx = rng.nextInt(0, 4)
            questions += GraphTypeAnswer(11300 + i, t("Odczytaj wartość dla słupka ${labels[targetIdx]}", "Read the value for bar ${labels[targetIdx]}"), shapes,
                MathViewport(xMin = -1.0, xMax = 8.5, yMin = -1.5, yMax = 6.5, showGrid = true, showAxes = true, showXLabels = false, showYLabels = true),
                values[targetIdx], hint = Hint(t("Spójrz na wysokość słupka i odczytaj wartość z osi pionowej (liczby po lewej).", "Look at the height of the bar and read the value from the vertical axis (numbers on the left).")))
        }
        // Case 2: Pie Chart
        repeat(2) { i ->
            val data = listOf(
                MathShape.PieChart.Slice(40.0, Color(0xFF4A80F0), "A"),
                MathShape.PieChart.Slice(30.0, Color(0xFF3DBD7D), "B"),
                MathShape.PieChart.Slice(20.0, Color(0xFFF47B20), "C"),
                MathShape.PieChart.Slice(10.0, Color(0xFF7C4DFF), "D")
            ).shuffled(rng)
            val shapes = listOf(MathShape.PieChart(0.0, 0.0, 3.8, data))
            questions += GraphSelectFromList(11350 + i, t("Który sektor wykresu kołowego jest największy?", "Which sector of the pie chart is the largest?"), shapes,
                MathViewport(xMin = -5.0, xMax = 5.0, yMin = -5.0, yMax = 5.0, showAxes = false, showGrid = false),
                data.map { it.label!! }, setOf(data.indexOf(data.maxBy { it.value })), hint = Hint(t("Największy sektor zajmuje najwięcej miejsca na kole.", "The largest sector takes up the most space on the circle.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_11_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val total = listOf(4, 5, 6, 10).random(rng)
            val target = rng.nextInt(1, total)
            val scenarios = listOf(
                t("W pudełku jest $total kul, w tym $target czerwonych. Wyciągamy jedną. Prawdopodobieństwo wylosowania czerwonej to:", "There are $total balls in a box, including $target red ones. We draw one. The probability of drawing a red one is:"),
                t("Rzucamy kostką o $total ścianach. Prawdopodobieństwo wyrzucenia '1' to:", "We roll a $total-sided die. The probability of rolling a '1' is:"),
                t("Na loterii jest $total losów, a wygrywających jest $target. Prawdopodobieństwo wygranej to:", "There are $total tickets in a lottery, and $target are winning. The probability of winning is:")
            )
            val scenario = if (i % 2 == 0) scenarios[0] else scenarios[2]
            questions += FractionAnswer(11400 + i, scenario, target, total, hint = Hint(t("Prawdopodobieństwo to liczba zdarzeń sprzyjających podzielona przez liczbę wszystkich możliwych zdarzeń.", "Probability is the number of favorable events divided by the number of all possible events.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_12_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Absolute value
        repeat(5) { i ->
            val n = rng.nextInt(-20, 21)
            val res = kotlin.math.abs(n)
            questions += TypeAnswer(12100 + i, t("Oblicz wartość bezwzględną: |$n|", "Calculate the absolute value: |$n|"), res, hint = Hint(t("Wartość bezwzględna to odległość liczby od zera na osi liczbowej (zawsze nieujemna).", "The absolute value is the distance of a number from zero on the number line (always non-negative).")))
        }
        // Opposite numbers
        repeat(5) { i ->
            val n = rng.nextInt(-20, 21)
            if (n == 0) return@repeat
            val res = -n
            questions += TypeAnswer(12110 + i, t("Podaj liczbę przeciwną do $n", "Provide the opposite number of $n"), res, hint = Hint(t("Liczba przeciwna leży po drugiej stronie zera na osi liczbowej. Suma liczby i jej przeciwnej to 0.", "The opposite number lies on the other side of zero on the number line. The sum of a number and its opposite is 0.")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_12_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val a = rng.nextInt(-15, 16)
            val b = rng.nextInt(-15, 16)
            val op = if (rng.nextBoolean()) ADD else SUBTRACT
            val res = if (op == ADD) a + b else a - b
            val display = if (op == ADD) {
                if (b < 0) "$a + ($b)" else "$a + $b"
            } else {
                if (b < 0) "$a - ($b)" else "$a - $b"
            }
            questions += TypeAnswer(12200 + i, t("Oblicz: $display", "Calculate: $display"), res, hint = Hint(t("Pamiętaj: plus i minus daje minus ($a + (-$b) = $a - $b), a dwa minusy dają plus ($a - (-$b) = $a + $b).", "Remember: plus and minus gives minus ($a + (-$b) = $a - $b), and two minuses give plus ($a - (-$b) = $a + $b).")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_12_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val a = rng.nextInt(-10, 11)
            val b = rng.nextInt(-10, 11)
            if (a == 0 || b == 0) return@repeat
            val op = if (rng.nextBoolean()) MULTIPLY else DIVIDE

            val qA: Int; val qB: Int; val res: Int
            if (op == MULTIPLY) {
                qA = a; qB = b; res = a * b
            } else {
                res = a; qB = b; qA = res * qB
            }

            val display = if (op == MULTIPLY) {
                val sA = if (qA < 0) "($qA)" else "$qA"
                val sB = if (qB < 0) "($qB)" else "$qB"
                "$sA * $sB"
            } else {
                val sA = if (qA < 0) "($qA)" else "$qA"
                val sB = if (qB < 0) "($qB)" else "$qB"
                "$sA / $sB"
            }.replace("*", "×").replace("/", "÷")

            questions += TypeAnswer(12300 + i, t("Oblicz: $display", "Calculate: $display"), res, hint = Hint(t("Zasada znaków: dwa takie same znaki dają PLUS, dwa różne znaki dają MINUS.", "Sign rule: two of the same signs give PLUS, two different signs give MINUS.")))
        }
        return questions.shuffled(rng)
    }
}
