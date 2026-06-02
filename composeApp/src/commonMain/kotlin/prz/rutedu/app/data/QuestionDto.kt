package prz.rutedu.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.compose.ui.graphics.Color
import prz.rutedu.app.math.Pt
import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.MathViewport
import prz.rutedu.app.models.Hint
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.MathOperator
import prz.rutedu.app.models.Question

@Serializable
data class LessonQuestionsDto(
    val lessonId: String,
    val questions: List<QuestionDto>
)

@Serializable
sealed class QuestionDto {
    abstract val id: Int
    abstract fun toModel(): Question
}

@Serializable
data class HintDto(
    val mainText: String,
    val boldPart: String? = null,
    val sectionTitle: String? = null,
    val items: List<String> = emptyList(),
    val steps: List<String> = emptyList()
) {
    fun toModel() = Hint(mainText, boldPart, sectionTitle, items, steps)
}

fun Hint.toDto() = HintDto(mainText, boldPart, sectionTitle, items, steps)

@Serializable
data class IntPairDto(val first: Int, val second: Int) {
    fun toPair() = Pair(first, second)
}

fun Pair<Int, Int>.toDto() = IntPairDto(first, second)

@Serializable
@SerialName("FindAnswer")
data class FindAnswerDto(
    override val id: Int,
    val operand1: Int,
    val operand2: Int,
    val operator: String,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.FindAnswer(
        id, operand1, operand2, MathOperator.valueOf(operator), hint.toModel()
    )
}

@Serializable
@SerialName("FindOperator")
data class FindOperatorDto(
    override val id: Int,
    val operand1: Int,
    val operand2: Int,
    val result: Int,
    val correctOperator: String,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.FindOperator(
        id, operand1, operand2, result, MathOperator.valueOf(correctOperator), hint.toModel()
    )
}

@Serializable
@SerialName("SelectFromList")
data class SelectFromListDto(
    override val id: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndices: Set<Int>,
    val multiSelect: Boolean = false,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.SelectFromList(
        id, prompt, options, correctIndices, multiSelect, hint.toModel()
    )
}

@Serializable
@SerialName("TypeAnswer")
data class TypeAnswerDto(
    override val id: Int,
    val prompt: String,
    val correctAnswer: Int,
    val unit: String = "",
    val triangleAngles: IntPairDto? = null,
    val inlineHint: String? = null,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.TypeAnswer(
        id, prompt, correctAnswer, unit, triangleAngles?.toPair(), inlineHint, hint.toModel()
    )
}

@Serializable
data class BalanceTermDto(
    val formula: String,
    val fixedCoefficient: Int? = null,
    val correctCoefficient: Int? = null
) {
    fun toModel() = Question.BalanceTerm(formula, fixedCoefficient, correctCoefficient)
}

fun Question.BalanceTerm.toDto() = BalanceTermDto(formula, fixedCoefficient, correctCoefficient)

@Serializable
@SerialName("EquationBalance")
data class EquationBalanceDto(
    override val id: Int,
    val instruction: String = "Uzupełnij równanie reakcji",
    val subInstruction: String = "Dobierz odpowiednie współczynniki stechiometryczne",
    val reactants: List<BalanceTermDto>,
    val products: List<BalanceTermDto>,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.EquationBalance(
        id, instruction, subInstruction, reactants.map { it.toModel() }, products.map { it.toModel() }, hint.toModel()
    )
}

@Serializable
@SerialName("ExpressionTypeAnswer")
data class ExpressionTypeAnswerDto(
    override val id: Int,
    val prompt: String,
    val correctExpr: String,
    val displayCorrect: String,
    val inlineHint: String? = null,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.ExpressionTypeAnswer(
        id, prompt, correctExpr, displayCorrect, inlineHint, hint.toModel()
    )
}

@Serializable
@SerialName("MapQuiz")
data class MapQuizDto(
    override val id: Int,
    val countryKey: String,
    val questionText: String,
    val region: String,
    val mapFile: String = "files/countries.geojson",
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.MapQuiz(
        id, countryKey, questionText, MapRegion.valueOf(region), mapFile, hint.toModel()
    )
}

@Serializable
@SerialName("GraphTypeAnswer")
data class GraphTypeAnswerDto(
    override val id: Int,
    val prompt: String,
    val shapes: List<MathShapeDto>,
    val viewport: MathViewportDto = MathViewportDto(),
    val correctAnswer: Int,
    val unit: String = "",
    val inlineHint: String? = null,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.GraphTypeAnswer(
        id, prompt, shapes.map { it.toModel() }, viewport.toModel(), correctAnswer, unit, inlineHint, hint.toModel()
    )
}

@Serializable
@SerialName("GraphSelectFromList")
data class GraphSelectFromListDto(
    override val id: Int,
    val prompt: String,
    val shapes: List<MathShapeDto>,
    val viewport: MathViewportDto = MathViewportDto(),
    val options: List<String>,
    val correctIndices: Set<Int>,
    val hint: HintDto
) : QuestionDto() {
    override fun toModel() = Question.GraphSelectFromList(
        id, prompt, shapes.map { it.toModel() }, viewport.toModel(), options, correctIndices, hint.toModel()
    )
}

@Serializable
sealed class MathShapeDto {
    abstract fun toModel(): MathShape
}

@Serializable
data class PtDto(val x: Double, val y: Double) {
    fun toModel() = Pt(x, y)
}

fun Pt.toDto() = PtDto(x, y)

@Serializable
data class MathViewportDto(
    val xMin: Double = -5.0,
    val xMax: Double = 5.0,
    val yMin: Double = -4.0,
    val yMax: Double = 4.0,
    val showGrid: Boolean = true,
    val showAxes: Boolean = true,
    val showXLabels: Boolean = true,
    val showYLabels: Boolean = true,
    val gridStep: Double = 1.0
) {
    fun toModel() = MathViewport(xMin, xMax, yMin, yMax, showGrid, showAxes, showXLabels, showYLabels, gridStep)
}

fun MathViewport.toDto() = MathViewportDto(xMin, xMax, yMin, yMax, showGrid, showAxes, showXLabels, showYLabels, gridStep)

@Serializable
@SerialName("Triangle")
data class TriangleDto(
    val a: PtDto,
    val b: PtDto,
    val c: PtDto,
    val color: String? = null,
    val showAngleArcs: Boolean = true,
    val labelA: String? = null,
    val labelB: String? = null,
    val labelC: String? = null,
    val labelAB: String? = null,
    val labelBC: String? = null,
    val labelCA: String? = null
) : MathShapeDto() {
    override fun toModel() = MathShape.Triangle(
        a.toModel(), b.toModel(), c.toModel(),
        parseColor(color), showAngleArcs,
        labelA, labelB, labelC, labelAB, labelBC, labelCA
    )
}

@Serializable
@SerialName("FunctionPlot")
data class FunctionPlotDto(
    val formula: String,
    val color: String? = null,
    val label: String? = null,
    val strokeWidth: Float = 2.5f,
    val samples: Int = 300
) : MathShapeDto() {
    override fun toModel() = MathShape.FunctionPlot(
        getFunctionForFormula(formula),
        parseColor(color), label, strokeWidth, samples
    )
}

@Serializable
@SerialName("Circle")
data class CircleDto(
    val cx: Double,
    val cy: Double,
    val r: Double,
    val color: String? = null,
    val filled: Boolean = false,
    val strokeWidth: Float = 2f
) : MathShapeDto() {
    override fun toModel() = MathShape.Circle(
        cx, cy, r, parseColor(color), filled, strokeWidth
    )
}

@Serializable
@SerialName("Rectangle")
data class RectangleDto(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val color: String? = null,
    val filled: Boolean = false,
    val strokeWidth: Float = 2f
) : MathShapeDto() {
    override fun toModel() = MathShape.Rectangle(
        x, y, w, h, parseColor(color), filled, strokeWidth
    )
}

@Serializable
@SerialName("PointMark")
data class PointMarkDto(
    val pt: PtDto,
    val label: String? = null,
    val color: String? = null,
    val radiusDp: Float = 4f
) : MathShapeDto() {
    override fun toModel() = MathShape.PointMark(
        pt.toModel(), label, parseColor(color), radiusDp
    )
}

@Serializable
@SerialName("Segment")
data class SegmentDto(
    val from: PtDto,
    val to: PtDto,
    val color: String? = null,
    val dashed: Boolean = false,
    val strokeWidth: Float = 2f
) : MathShapeDto() {
    override fun toModel() = MathShape.Segment(
        from.toModel(), to.toModel(), parseColor(color), dashed, strokeWidth
    )
}

@Serializable
@SerialName("TextLabel")
data class TextLabelDto(
    val pt: PtDto,
    val text: String,
    val color: String? = null,
    val sizeSp: Float = 13f
) : MathShapeDto() {
    override fun toModel() = MathShape.TextLabel(
        pt.toModel(), text, parseColor(color), sizeSp
    )
}

// Helper color parsers
fun parseColor(colorStr: String?): Color {
    if (colorStr == null) return Color(0xFF4A80F0)
    val cleanStr = colorStr.removePrefix("#")
    if (cleanStr.length == 6) {
        val r = cleanStr.substring(0, 2).toInt(16) / 255f
        val g = cleanStr.substring(2, 4).toInt(16) / 255f
        val b = cleanStr.substring(4, 6).toInt(16) / 255f
        return Color(r, g, b, 1f)
    } else if (cleanStr.length == 8) {
        val a = cleanStr.substring(0, 2).toInt(16) / 255f
        val r = cleanStr.substring(2, 4).toInt(16) / 255f
        val g = cleanStr.substring(4, 6).toInt(16) / 255f
        val b = cleanStr.substring(6, 8).toInt(16) / 255f
        return Color(r, g, b, a)
    }
    return Color(0xFF4A80F0)
}

fun Color.toStr(): String {
    val a = (this.alpha * 255f + 0.5f).toInt().coerceIn(0, 255)
    val r = (this.red * 255f + 0.5f).toInt().coerceIn(0, 255)
    val g = (this.green * 255f + 0.5f).toInt().coerceIn(0, 255)
    val b = (this.blue * 255f + 0.5f).toInt().coerceIn(0, 255)
    return "#" +
        a.toString(16).padStart(2, '0') +
        r.toString(16).padStart(2, '0') +
        g.toString(16).padStart(2, '0') +
        b.toString(16).padStart(2, '0')
}

// Function map for quadratic curves
fun getFunctionForFormula(formula: String): (Double) -> Double = when (formula) {
    "x^2" -> { x -> x * x }
    "x^2 - 2" -> { x -> x * x - 2 }
    "-x^2" -> { x -> -x * x }
    "x^2 + 1" -> { x -> x * x + 1 }
    "(x - 2)^2" -> { x -> (x - 2) * (x - 2) }
    "2*x^2" -> { x -> 2 * x * x }
    "x^2 - 3" -> { x -> x * x - 3 }
    else -> throw IllegalArgumentException("Unknown formula: $formula")
}

// Domain-to-DTO converters (used during generation/seeding)
fun MathShape.toDto(): MathShapeDto = when (this) {
    is MathShape.Triangle -> TriangleDto(
        a.toDto(), b.toDto(), c.toDto(),
        color.toStr(), showAngleArcs,
        labelA, labelB, labelC, labelAB, labelBC, labelCA
    )
    is MathShape.Circle -> CircleDto(cx, cy, r, color.toStr(), filled, strokeWidth)
    is MathShape.Rectangle -> RectangleDto(x, y, w, h, color.toStr(), filled, strokeWidth)
    is MathShape.TextLabel -> TextLabelDto(pt.toDto(), text, color.toStr(), sizeSp)
    is MathShape.PointMark -> PointMarkDto(pt.toDto(), label, color.toStr(), radiusDp)
    is MathShape.Segment -> SegmentDto(from.toDto(), to.toDto(), color.toStr(), dashed, strokeWidth)
    is MathShape.FunctionPlot -> throw UnsupportedOperationException("FunctionPlot requires formula string from question context")
    is MathShape.PieChart -> throw UnsupportedOperationException("PieChart is not serialisable to a static JSON DTO")
}

fun Question.toDto(): QuestionDto = when (this) {
    is Question.FindAnswer -> FindAnswerDto(id, operand1, operand2, operator.name, hint.toDto())
    is Question.FindOperator -> FindOperatorDto(id, operand1, operand2, result, correctOperator.name, hint.toDto())
    is Question.SelectFromList -> SelectFromListDto(id, prompt, options, correctIndices, multiSelect, hint.toDto())
    is Question.TypeAnswer -> TypeAnswerDto(id, prompt, correctAnswer, unit, triangleAngles?.toDto(), inlineHint, hint.toDto())
    is Question.MapQuiz -> MapQuizDto(id, countryKey, questionText, region.name, mapFile, hint.toDto())
    is Question.GraphTypeAnswer -> GraphTypeAnswerDto(
        id, prompt,
        shapes.map { shape ->
            when (shape) {
                is MathShape.FunctionPlot -> {
                    val formula = when (id) {
                        5 -> "x^2"
                        6 -> "x^2 - 3"
                        7 -> "2*x^2"
                        else -> "x^2"
                    }
                    FunctionPlotDto(formula, shape.color.toStr(), shape.label, shape.strokeWidth, shape.samples)
                }
                is MathShape.Segment -> SegmentDto(shape.from.toDto(), shape.to.toDto(), shape.color.toStr(), shape.dashed, shape.strokeWidth)
                is MathShape.PointMark -> PointMarkDto(shape.pt.toDto(), shape.label, shape.color.toStr(), shape.radiusDp)
                else -> shape.toDto()
            }
        },
        viewport.toDto(), correctAnswer, unit, inlineHint, hint.toDto()
    )
    is Question.GraphSelectFromList -> GraphSelectFromListDto(
        id, prompt,
        shapes.map { shape ->
            when (shape) {
                is MathShape.FunctionPlot -> {
                    val formula = when (id) {
                        0 -> "x^2"
                        1 -> "x^2 - 2"
                        2 -> "-x^2"
                        3 -> "x^2 + 1"
                        4 -> "(x - 2)^2"
                        else -> "x^2"
                    }
                    FunctionPlotDto(formula, shape.color.toStr(), shape.label, shape.strokeWidth, shape.samples)
                }
                is MathShape.Segment -> SegmentDto(shape.from.toDto(), shape.to.toDto(), shape.color.toStr(), shape.dashed, shape.strokeWidth)
                is MathShape.PointMark -> PointMarkDto(shape.pt.toDto(), shape.label, shape.color.toStr(), shape.radiusDp)
                else -> shape.toDto()
            }
        },
        viewport.toDto(), options, correctIndices, hint.toDto()
    )
    is Question.EquationBalance -> EquationBalanceDto(
        id, instruction, subInstruction, reactants.map { it.toDto() }, products.map { it.toDto() }, hint.toDto()
    )
    is Question.ExpressionTypeAnswer -> ExpressionTypeAnswerDto(
        id, prompt, correctExpr, displayCorrect, inlineHint, hint.toDto()
    )
    else -> throw UnsupportedOperationException("Type not statically serialized: ${this::class.simpleName}")
}
