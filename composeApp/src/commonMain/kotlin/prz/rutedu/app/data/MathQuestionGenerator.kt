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

    private fun s(key: String): String = GeneratorStrings.math(key)

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
            questions += FindAnswer(1100 + i, a, b, op, Hint(if (op == ADD) s("hint.addition") else s("hint.subtraction")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val a = rng.nextInt(2, 11); val b = rng.nextInt(2, 11)
            questions += FindAnswer(1200 + i, a, b, MULTIPLY, Hint(s("hint.multiplication_table")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val b = rng.nextInt(2, 6); val e = rng.nextInt(2, 4)
            questions += FindAnswer(1300 + i, b, e, POWER, Hint(s("hint.power").format(b, e)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val squares = listOf(4, 9, 16, 25, 36, 49, 64, 81, 100)
        squares.shuffled(rng).take(6).forEachIndexed { i, sq ->
            questions += FindAnswer(1400 + i, 2, sq, ROOT, Hint(s("hint.square_root").format(sq)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_1_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        listOf(2 to 8, 3 to 9, 10 to 100, 2 to 16, 5 to 25).forEachIndexed { i, (b, r) ->
            questions += FindAnswer(1500 + i, b, r, LOG, Hint(s("hint.logarithm").format(b, r)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val v = listOf("x", "y", "a").random(rng); val c = rng.nextInt(2, 10)
            val correct = "$c$v"; val opts = listOf(correct, "$c+$v", "$v-1", "2$v").distinct().shuffled(rng)
            questions += SelectFromList(2100 + i, s("prompt.monomial"), opts, setOf(opts.indexOf(correct)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(1, 10); val prompt = "(x + $a)²"; val correct = "x² + ${2*a}x + ${a*a}"
            if (mathEngineAvailable) questions += ExpressionTypeAnswer(2200 + i, s("prompt.expand").format(prompt), "x^2 + ${2*a}*x + ${a*a}", correct)
            else questions += SelectFromList(2200 + i, s("prompt.expand").format(prompt), listOf(correct, "x² + ${a*a}", "x² + ${a}x").shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf(correct))) }
        }
        return questions.shuffled(rng)
    }

    private fun mat_2_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val c = rng.nextInt(2, 6); val a = c * rng.nextInt(1, 4); val b = c * rng.nextInt(1, 4)
            val res = "$c(${a/c}x + ${b/c}y)"
            questions += SelectFromList(2300 + i, s("prompt.factor_out").format(a, b), listOf(res, "$c(${a}x + ${b}y)", "2(${a/2}x + ${b/2}y)").distinct().shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf(res))) }
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
            val a = rng.nextInt(1, 5); val sol = rng.nextInt(-3, 4); val c = a * sol
            val res = "x > $sol"; val opts = listOf(res, "x < $sol", "x > ${sol+1}").shuffled(rng)
            questions += SelectFromList(3300 + i, s("prompt.solve").format("${a}x > $c"), opts, setOf(opts.indexOf(res)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(1, 5); val b = rng.nextInt(-5, 6); val x = rng.nextInt(0, 5); val y = a * x + b
            val opts = listOf("$y", "${y+1}", "${y-1}").shuffled(rng)
            questions += SelectFromList(4100 + i, s("prompt.calculate_function").format(a, b, x), opts, setOf(opts.indexOf("$y")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val p = rng.nextInt(-3, 4); val q = rng.nextInt(-3, 4); val res = "($p, $q)"
            val opts = listOf(res, "(${-p}, $q)", "($q, $p)").distinct().shuffled(rng)
            questions += SelectFromList(4200 + i, s("prompt.function_vertex").format(p, q), opts, setOf(opts.indexOf(res)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x0 = rng.nextInt(-3, 4); val f = { x: Double -> x - x0 }
            questions += GraphTypeAnswer(4300 + i, s("prompt.read_zero"), listOf(MathShape.FunctionPlot(f)), MathViewport(), x0)
        }
        return questions.shuffled(rng)
    }

    private fun mat_4_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val inc = rng.nextBoolean(); val f = { x: Double -> if(inc) x else -x }
            questions += GraphSelectFromList(4400 + i, s("prompt.monotonicity"), listOf(MathShape.FunctionPlot(f)), MathViewport(), listOf(s("word.increasing"), s("word.decreasing")), setOf(if(inc) 0 else 1))
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(30, 80).toDouble(); val b = rng.nextInt(30, 80).toDouble(); val c = 180 - a - b
            val (v, vp) = TriangleBuilder.fromAnglesWithViewport(a, b)
            questions += GraphTypeAnswer(5100 + i, s("prompt.third_angle"), listOf(MathShape.Triangle(v.first, v.second, v.third, labelA="${a.toInt()}°", labelB="${b.toInt()}°", labelC="?")), vp, c.toInt(), "°")
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val w = rng.nextInt(2, 6); val h = rng.nextInt(2, 6)
            questions += GraphTypeAnswer(5200 + i, s("prompt.rectangle_area").format(w, h), listOf(MathShape.Rectangle(-w/2.0, -h/2.0, w.toDouble(), h.toDouble(), filled = true)), MathViewport(), w * h)
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
            NamedElement(s("word.radius"), s("hint.radius"), { l, a ->
                val p = onCircle(a)
                listOf(MathShape.Segment(Pt(0.0, 0.0), p, color = Color(0xFF4A80F0)), MathShape.TextLabel(Pt(p.x/2.0 + 0.3, p.y/2.0 + 0.3), l, color = Color(0xFF4A80F0)))
            }, Color(0xFF4A80F0)),
            NamedElement(s("word.diameter"), s("hint.diameter"), { l, a ->
                val p1 = onCircle(a); val p2 = onCircle(a + kotlin.math.PI)
                listOf(MathShape.Segment(p1, p2, color = Color(0xFF3DBD7D)), MathShape.TextLabel(Pt(p1.x*0.4 + 0.3, p1.y*0.4 + 0.3), l, color = Color(0xFF3DBD7D)))
            }, Color(0xFF3DBD7D)),
            NamedElement(s("word.chord"), s("hint.chord"), { l, a ->
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

            questions += GraphSelectFromList(5300 + qIdx, s("prompt.circle_segment").format(target.name), shapes, 
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

            questions += GraphSelectFromList(5340 + i, s("prompt.segment_outside_circle"), shapes,
                MathViewport(xMin = -8.0, xMax = 8.0, yMin = -8.0, yMax = 8.0, showGrid = false, showAxes = false),
                labels, setOf(2), hint = Hint(s("hint.segment_outside_circle")))
        }

        // 3. Two calculation questions
        repeat(2) { i ->
            val rad = rng.nextInt(3, 10); val dia = 2 * rad
            questions += SelectFromList(5350 + i, s("prompt.diameter_from_radius").format(rad), listOf("$dia", "${rad+2}", "${rad-1}").shuffled(rng), setOf(0)).let { it.copy(correctIndices = setOf(it.options.indexOf("$dia"))) }
        }

        return questions.shuffled(rng)
    }

    private fun mat_5_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val a = rng.nextInt(2, 8); val h = rng.nextInt(2, 6); val area = (a * h) / 2
            questions += GraphTypeAnswer(5400 + i, s("prompt.triangle_area").format(a, h), listOf(MathShape.Triangle(Pt(-a/2.0, 0.0), Pt(a/2.0, 0.0), Pt(0.0, h.toDouble()))), MathViewport(), area)
        }
        return questions.shuffled(rng)
    }

    private fun mat_5_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val (a, b, c) = listOf(Triple(3, 4, 5), Triple(6, 8, 10), Triple(5, 12, 13)).random(rng)
            questions += GraphTypeAnswer(5500 + i, s("prompt.hypotenuse").format(a, b), listOf(MathShape.Triangle(Pt(0.0,0.0), Pt(a.toDouble(),0.0), Pt(0.0,b.toDouble()), labelAB="$a", labelCA="$b", labelBC="?")), MathViewport(), c)
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
            questions += DecimalAnswer(6100 + i, s("prompt.fraction_to_decimal").format(n, den), d, precision = 3, hint = Hint(s("hint.fraction_to_decimal")))
        }
        // Decimals to Fractions
        val revPairs = listOf(0.5 to (1 to 2), 0.2 to (1 to 5), 0.75 to (3 to 4), 0.6 to (3 to 5))
        revPairs.forEachIndexed { i, pair ->
            val (d, f) = pair
            val (n, den) = f
            questions += FractionAnswer(6150 + i, s("prompt.decimal_to_fraction").format(d), n, den, hint = Hint(s("hint.decimal_to_fraction")))
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
                questions += FractionAnswer(6200 + i, s("prompt.calculate").format("$n1/$den $op $n2/$den"), resN, den, hint = Hint(s("hint.fraction_same_denom")))
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
        cases.forEachIndexed { i, (l, r, sign) ->
            questions += ComparisonQuiz(6300 + i, s("prompt.compare_numbers"), l, r, sign, hint = Hint(s("hint.compare_fractions")))
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
                questions += DecimalAnswer(6400 + i, s("prompt.calculate").format("$a $op $b"), res, precision = 2, hint = Hint(s("hint.decimal_operations")))
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
                questions += TypeAnswer(7100 + i, s("prompt.percent_of").format(p, base), res.toInt(), hint = Hint(s("hint.percent_of")))
            } else {
                questions += DecimalAnswer(7100 + i, s("prompt.percent_of").format(p, base), res, hint = Hint(s("hint.percent_decimal")))
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
            val type = if (isDown) s("word.decreased") else s("word.increased")
            questions += TypeAnswer(7200 + i, s("prompt.price_change").format(price, s("word.currency"), type, p), res, unit = s("word.currency"), hint = Hint(s("hint.price_change")))
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
                questions += TypeAnswer(7300 + i, s("prompt.percent_of_total").format(total, girls), percent, unit = "%", hint = Hint(s("hint.percent_of_total")))
            }
        }
        // Case 2: Simple interest / other
        questions += SelectFromList(7310, s("prompt.find_whole_from_percent"), listOf("40", "30", "20", "50"), setOf(0), hint = Hint(s("hint.find_whole_from_quarter")))
        questions += SelectFromList(7311, s("prompt.one_half_percent"), listOf("150%", "15%", "50%", "105%"), setOf(0), hint = Hint(s("hint.one_half_percent")))

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
            questions += TypeAnswer(8100 + i, s("prompt.solve_proportion").format(a, b, c), res, hint = Hint(s("hint.solve_proportion").format(a, c, b, b)))
        }
        return questions.shuffled(rng)
    }

    private fun mat_8_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val scales = listOf(100, 1000, 10000, 50000, 100000)
        repeat(10) { i ->
            val scale = scales.random(rng)
            val mapDist = rng.nextInt(1, 21)
            val realDistCm = mapDist.toLong() * scale

            val (res, unit) = when {
                realDistCm >= 100000 -> (realDistCm / 100000.0) to "km"
                realDistCm >= 100 -> (realDistCm / 100.0) to "m"
                else -> realDistCm.toDouble() to "cm"
            }

            if (res == res.toLong().toDouble()) {
                questions += TypeAnswer(8200 + i, s("prompt.map_scale").format(scale, mapDist), res.toInt(), unit = unit, hint = Hint(s("hint.map_scale")))
            } else {
                questions += DecimalAnswer(8200 + i, s("prompt.map_scale").format(scale, mapDist), res, precision = 2, hint = Hint(s("hint.map_scale_detail").format(scale, unit)))
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
                questions += TypeAnswer(8300 + i, s("prompt.map_scale_reverse").format(realDistKm, scale), res.toInt(), unit = "cm", hint = Hint(s("hint.map_scale_reverse")))
            }
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val types = listOf(
            Triple(s("word.quadrangular"), 4, "kwadrat"),
            Triple(s("word.triangular"), 3, "trójkąt"),
            Triple(s("word.hexagonal"), 6, "sześciokąt")
        )
        types.forEachIndexed { i, (name, n, _) ->
            questions += TypeAnswer(9100 + i*3, s("prompt.prism_vertices").format(name), 2 * n, hint = Hint(s("hint.prism_vertices")))
            questions += TypeAnswer(9101 + i*3, s("prompt.prism_edges").format(name), 3 * n, hint = Hint(s("hint.prism_edges")))
            questions += TypeAnswer(9102 + i*3, s("prompt.prism_faces").format(name), n + 2, hint = Hint(s("hint.prism_faces")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_2(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        val types = listOf(
            Pair(s("word.quadrangular"), 4),
            Pair(s("word.triangular"), 3),
            Pair(s("word.pentagonal"), 5)
        )
        types.forEachIndexed { i, (name, n) ->
            questions += TypeAnswer(9200 + i*3, s("prompt.pyramid_vertices").format(name), n + 1, hint = Hint(s("hint.pyramid_vertices")))
            questions += TypeAnswer(9201 + i*3, s("prompt.pyramid_edges").format(name), 2 * n, hint = Hint(s("hint.pyramid_edges")))
            questions += TypeAnswer(9202 + i*3, s("prompt.pyramid_faces").format(name), n + 1, hint = Hint(s("hint.pyramid_faces")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_9_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        questions += SelectFromList(9300, s("prompt.solid_from_rectangle"), listOf(s("word.cylinder"), s("word.cone"), s("word.sphere"), s("word.prism")), setOf(0))
        questions += SelectFromList(9301, s("prompt.solid_from_triangle"), listOf(s("word.cone"), s("word.cylinder"), s("word.sphere"), s("word.pyramid")), setOf(0))
        questions += SelectFromList(9302, s("prompt.sphere_section"), listOf(s("word.circle_noun"), s("word.square_noun"), s("word.triangle_noun"), s("word.rectangle_noun")), setOf(0))
        return questions.shuffled(rng)
    }

    private fun mat_9_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Cube surface area: 6 * a^2
        repeat(5) { i ->
            val a = rng.nextInt(2, 7)
            questions += TypeAnswer(9400 + i, s("prompt.cube_surface").format(a), 6 * a * a, unit = "j²", hint = Hint(s("hint.cube_surface")))
        }
        // Rectangular prism: 2(ab + bc + ac)
        val a = 2; val b = 3; val c = 4
        questions += TypeAnswer(9410, s("prompt.cuboid_surface").format(a, b, c), 2 * (a*b + b*c + a*c), unit = "j²", hint = Hint(s("hint.cuboid_surface")))
        return questions.shuffled(rng)
    }

    private fun mat_9_5(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Cube volume: a^3
        repeat(5) { i ->
            val a = rng.nextInt(2, 6)
            questions += TypeAnswer(9500 + i, s("prompt.cube_volume").format(a), a * a * a, unit = "j³", hint = Hint(s("hint.cube_volume")))
        }
        // Rectangular prism: a * b * c
        repeat(5) { i ->
            val a = rng.nextInt(2, 6); val b = rng.nextInt(2, 6); val h = rng.nextInt(2, 6)
            questions += TypeAnswer(9510 + i, s("prompt.cuboid_volume").format(a, b, h), a * b * h, unit = "j³", hint = Hint(s("hint.cuboid_volume")))
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
            val axis = if (askX) s("word.x_axis") else s("word.y_axis")
            questions += GraphTypeAnswer(10100 + i, s("prompt.coordinate_of").format(axis), shapes, MathViewport(), correct, hint = Hint(s("hint.coordinate_axes")))
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
            questions += GraphSelectFromList(10200 + i, s("prompt.coordinates"), shapes, MathViewport(), options, setOf(options.indexOf(correct)), hint = Hint(s("hint.read_coordinates")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_10_3(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(5) { i ->
            val x = rng.nextInt(1, 4).toDouble(); val y = rng.nextInt(1, 4).toDouble()
            val shapes = listOf(MathShape.Rectangle(-x, -y, 2*x, 2*y, color = Color(0xFF3DBD7D)))
            val area = (2 * x * 2 * y).toInt()
            questions += GraphTypeAnswer(10300 + i, s("prompt.drawn_rectangle_area"), shapes, MathViewport(), area, hint = Hint(s("hint.area_from_axes")))
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
            questions += GraphTypeAnswer(10400 + i, s("prompt.distance_between_points"), shapes, MathViewport(), dx, hint = Hint(s("hint.horizontal_distance")))
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
            val numStr = nums.joinToString(", ")
            if (sum % count == 0) {
                questions += TypeAnswer(11100 + i, s("prompt.mean").format(numStr), mean.toInt(), hint = Hint(s("hint.mean")))
            } else {
                questions += DecimalAnswer(11100 + i, s("prompt.mean").format(numStr), mean, precision = 2, hint = Hint(s("hint.mean_detail").format(sum, count)))
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
                questions += TypeAnswer(11200 + i, s("prompt.median").format(nums.shuffled(rng).joinToString(", ")), res, hint = Hint(s("hint.median")))
            } else {
                // Dominanta (Mode) - ensure one clear winner
                val mode = rng.nextInt(1, 10)
                val otherNums = List(count - 2) { rng.nextInt(11, 20) }
                val set = (listOf(mode, mode) + otherNums).shuffled(rng)
                questions += TypeAnswer(11200 + i, s("prompt.mode").format(set.joinToString(", ")), mode, hint = Hint(s("hint.mode")))
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
            questions += GraphTypeAnswer(11300 + i, s("prompt.bar_value").format(labels[targetIdx]), shapes,
                MathViewport(xMin = -1.0, xMax = 8.5, yMin = -1.5, yMax = 6.5, showGrid = true, showAxes = true, showXLabels = false, showYLabels = true),
                values[targetIdx], hint = Hint(s("hint.bar_chart")))
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
            questions += GraphSelectFromList(11350 + i, s("prompt.largest_pie_sector"), shapes,
                MathViewport(xMin = -5.0, xMax = 5.0, yMin = -5.0, yMax = 5.0, showAxes = false, showGrid = false),
                data.map { it.label!! }, setOf(data.indexOf(data.maxBy { it.value })), hint = Hint(s("hint.largest_pie_sector")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_11_4(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        repeat(10) { i ->
            val total = listOf(4, 5, 6, 10).random(rng)
            val target = rng.nextInt(1, total)
            val scenarios = listOf(
                s("prompt.probability_balls").format(total, target),
                s("prompt.probability_die").format(total),
                s("prompt.probability_lottery").format(total, target)
            )
            val scenario = if (i % 2 == 0) scenarios[0] else scenarios[2]
            questions += FractionAnswer(11400 + i, scenario, target, total, hint = Hint(s("hint.probability")))
        }
        return questions.shuffled(rng)
    }

    private fun mat_12_1(seed: Long): List<Question> {
        val rng = Random(seed); val questions = mutableListOf<Question>()
        // Absolute value
        repeat(5) { i ->
            val n = rng.nextInt(-20, 21)
            val res = kotlin.math.abs(n)
            questions += TypeAnswer(12100 + i, s("prompt.abs_value").format(n), res, hint = Hint(s("hint.abs_value")))
        }
        // Opposite numbers
        repeat(5) { i ->
            val n = rng.nextInt(-20, 21)
            if (n == 0) return@repeat
            val res = -n
            questions += TypeAnswer(12110 + i, s("prompt.opposite_number").format(n), res, hint = Hint(s("hint.opposite_number")))
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
            questions += TypeAnswer(12200 + i, s("prompt.calculate").format(display), res, hint = Hint(s("hint.sign_rules_add_sub").format(a, b, a, b, a, b, a, b)))
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
                val strA = if (qA < 0) "($qA)" else "$qA"
                val strB = if (qB < 0) "($qB)" else "$qB"
                "$strA * $strB"
            } else {
                val strA = if (qA < 0) "($qA)" else "$qA"
                val strB = if (qB < 0) "($qB)" else "$qB"
                "$strA / $strB"
            }.replace("*", "×").replace("/", "÷")

            questions += TypeAnswer(12300 + i, s("prompt.calculate").format(display), res, hint = Hint(s("hint.sign_rule")))
        }
        return questions.shuffled(rng)
    }
}
