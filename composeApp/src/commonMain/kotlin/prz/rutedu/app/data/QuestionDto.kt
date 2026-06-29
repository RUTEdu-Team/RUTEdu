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

/**
 * Data transfer object wrapping a list of questions for a specific lesson.
 *
 * @property lessonId The lesson identifier (e.g. "mat_1_1").
 * @property questions The list of questions belonging to the lesson.
 */
@Serializable
data class LessonQuestionsDto(
    val lessonId: String,
    val questions: List<QuestionDto>
)

/**
 * Abstract base class for all serializable quiz question data transfer objects.
 *
 * @property id The unique identifier of the question.
 */
@Serializable
sealed class QuestionDto {
    abstract val id: Int

    /** Converts the DTO into its corresponding domain model. */
    abstract fun toModel(): Question
}

/**
 * Serializable DTO representing a localized hint layout for quiz questions.
 *
 * @property mainText The primary hint text.
 * @property boldPart Optional subtitle or highlighted text.
 * @property sectionTitle Optional header for the formulas or rules section.
 * @property items List of bullet points or rules.
 * @property steps List of step-by-step solution instructions.
 */
@Serializable
data class HintDto(
    val mainText: String,
    val boldPart: String? = null,
    val sectionTitle: String? = null,
    val items: List<String> = emptyList(),
    val steps: List<String> = emptyList()
) {
    /** Converts this DTO into a domain [Hint] model. */
    fun toModel() = Hint(mainText, boldPart, sectionTitle, items, steps)
}

/** Converts a [Hint] domain model into its corresponding [HintDto]. */
fun Hint.toDto() = HintDto(mainText, boldPart, sectionTitle, items, steps)

/**
 * Serializable DTO representing a pair of integers.
 *
 * @property first The first element of the pair.
 * @property second The second element of the pair.
 */
@Serializable
data class IntPairDto(val first: Int, val second: Int) {
    /** Converts this DTO into a Kotlin [Pair]. */
    fun toPair() = Pair(first, second)
}

/** Converts a [Pair] of integers into an [IntPairDto]. */
fun Pair<Int, Int>.toDto() = IntPairDto(first, second)

/**
 * DTO for a [Question.FindAnswer] question type.
 *
 * @property id The unique question identifier.
 * @property operand1 The first operand.
 * @property operand2 The second operand.
 * @property operator The mathematical operator to apply.
 * @property hint The localized help explanation.
 */
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

/**
 * DTO for a [Question.FindOperator] question type.
 *
 * @property id              The unique question identifier.
 * @property operand1        The first operand.
 * @property operand2        The second operand.
 * @property result          The expected result of the operation.
 * @property correctOperator The string representation of the correct operator.
 * @property hint            The localized help explanation.
 */
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

/**
 * DTO for a [Question.SelectFromList] question type.
 *
 * @property id             The unique question identifier.
 * @property prompt         The localized question text or description.
 * @property options        The list of multiple-choice options.
 * @property correctIndices The set of correct indices.
 * @property multiSelect    Whether multiple options can/must be selected.
 * @property hint           The localized help explanation.
 */
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

/**
 * DTO for a [Question.TypeAnswer] question type.
 *
 * @property id             The unique question identifier.
 * @property prompt         The localized question text or description.
 * @property correctAnswer  The correct integer answer.
 * @property unit           The unit label (e.g. "cm") appended to the answer.
 * @property triangleAngles Optional pair of angles for visual geometry helper cues.
 * @property inlineHint     Optional hint displayed inside the text field.
 * @property hint           The localized help explanation.
 */
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

/**
 * DTO for a [Question.BalanceTerm] used in chemistry equation balancing.
 *
 * @property formula            The chemical formula of the molecule.
 * @property fixedCoefficient   The pre-filled coefficient (if not blank).
 * @property correctCoefficient The correct coefficient expected from the player.
 */
@Serializable
data class BalanceTermDto(
    val formula: String,
    val fixedCoefficient: Int? = null,
    val correctCoefficient: Int? = null
) {
    /** Converts this DTO into a domain [Question.BalanceTerm] model. */
    fun toModel() = Question.BalanceTerm(formula, fixedCoefficient, correctCoefficient)
}

/** Converts a [Question.BalanceTerm] domain model into its corresponding [BalanceTermDto]. */
fun Question.BalanceTerm.toDto() = BalanceTermDto(formula, fixedCoefficient, correctCoefficient)

/**
 * DTO for a [Question.EquationBalance] question type.
 *
 * @property id             The unique question identifier.
 * @property instruction    The main instruction text.
 * @property subInstruction The sub-instruction text.
 * @property reactants      List of reactants on the left-hand side.
 * @property products       List of products on the right-hand side.
 * @property hint           The localized help explanation.
 */
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

/**
 * DTO for a [Question.ExpressionTypeAnswer] question type.
 *
 * @property id             The unique question identifier.
 * @property prompt         The localized question text or description.
 * @property correctExpr     The correct algebraic expression.
 * @property displayCorrect  The stylized string representation of the correct answer.
 * @property inlineHint      Optional hint displayed inside the text field.
 * @property hint            The localized help explanation.
 */
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

/**
 * DTO for a [Question.MapQuiz] question type.
 *
 * @property id           The unique question identifier.
 * @property countryKey   The key identifying the target country in the GeoJSON map.
 * @property questionText The localized question text or description.
 * @property region       The region/continent the quiz takes place in.
 * @property mapFile      Path to the GeoJSON map resource.
 * @property hint         The localized help explanation.
 */
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

/**
 * DTO for a [Question.GraphTypeAnswer] question type.
 *
 * @property id            The unique question identifier.
 * @property prompt        The localized question text or description.
 * @property shapes        The list of shapes rendered on the coordinate plane.
 * @property viewport      The bounds and grid configuration of the canvas viewport.
 * @property correctAnswer The correct integer answer.
 * @property unit          The unit label appended to the answer.
 * @property inlineHint    Optional hint displayed inside the text field.
 * @property hint          The localized help explanation.
 */
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

/**
 * DTO for a [Question.GraphSelectFromList] question type.
 *
 * @property id             The unique question identifier.
 * @property prompt         The localized question text or description.
 * @property shapes         The list of shapes rendered on the coordinate plane.
 * @property viewport      The bounds and grid configuration of the canvas viewport.
 * @property options        The list of multiple-choice options.
 * @property correctIndices The set of correct indices.
 * @property hint           The localized help explanation.
 */
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

/**
 * Abstract base class for all serializable mathematical shape DTOs.
 */
@Serializable
sealed class MathShapeDto {
    /** Converts this DTO into a domain [MathShape] model. */
    abstract fun toModel(): MathShape
}

/**
 * DTO representing a point in 2D space.
 *
 * @property x The x-coordinate.
 * @property y The y-coordinate.
 */
@Serializable
data class PtDto(val x: Double, val y: Double) {
    /** Converts this DTO into a domain [Pt] model. */
    fun toModel() = Pt(x, y)
}

/** Converts a [Pt] domain model into its corresponding [PtDto]. */
fun Pt.toDto() = PtDto(x, y)

/**
 * DTO representing a [MathViewport] visible area configuration.
 *
 * @property xMin     Left edge of the visible area.
 * @property xMax     Right edge of the visible area.
 * @property yMin     Bottom edge of the visible area.
 * @property yMax     Top edge of the visible area.
 * @property showGrid Whether to render grid lines.
 * @property showAxes Whether to render coordinate axes and ticks.
 * @property showXLabels Whether to show labels on the x-axis.
 * @property showYLabels Whether to show labels on the y-axis.
 * @property gridStep Distance between grid lines and tick marks.
 */
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
    /** Converts this DTO into a domain [MathViewport] model. */
    fun toModel() = MathViewport(xMin, xMax, yMin, yMax, showGrid, showAxes, showXLabels, showYLabels, gridStep)
}

/** Converts a [MathViewport] domain model into its corresponding [MathViewportDto]. */
fun MathViewport.toDto() = MathViewportDto(xMin, xMax, yMin, yMax, showGrid, showAxes, showXLabels, showYLabels, gridStep)

/**
 * DTO representing a [MathShape.Triangle].
 *
 * @property a              The first vertex.
 * @property b              The second vertex.
 * @property c              The third vertex.
 * @property color          Hex string color of the outline/fill.
 * @property showAngleArcs  Whether to draw interior angle arcs.
 * @property labelA         Label for vertex A.
 * @property labelB         Label for vertex B.
 * @property labelC         Label for vertex C.
 * @property labelAB        Label for side AB.
 * @property labelBC        Label for side BC.
 * @property labelCA        Label for side CA.
 */
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

/**
 * DTO representing a [MathShape.FunctionPlot].
 *
 * @property formula     The mathematical formula expression string (e.g. "x^2").
 * @property color       Hex string color of the curve.
 * @property label       Optional label text for the curve.
 * @property strokeWidth Stroke thickness in dp.
 * @property samples     Number of evaluation samples.
 */
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

/**
 * DTO representing a [MathShape.Circle].
 *
 * @property cx          World x-coordinate of the center.
 * @property cy          World y-coordinate of the center.
 * @property r           Radius in world units.
 * @property color       Hex string color of the outline/fill.
 * @property filled      Whether the circle is solid-filled.
 * @property strokeWidth Outline stroke width in dp.
 */
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

/**
 * DTO representing a [MathShape.Rectangle].
 *
 * @property x           Left edge world coordinate.
 * @property y           Bottom edge world coordinate.
 * @property w           Width in world units.
 * @property h           Height in world units.
 * @property color       Hex string color of the outline/fill.
 * @property filled      Whether the rectangle is solid-filled.
 * @property strokeWidth Outline stroke width in dp.
 */
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

/**
 * DTO representing a [MathShape.PointMark].
 *
 * @property pt       The point location.
 * @property label    Optional text label near the point.
 * @property color    Hex string color of the point.
 * @property radiusDp Dot radius in dp.
 */
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

/**
 * DTO representing a [MathShape.Segment].
 *
 * @property from        Start point.
 * @property to          End point.
 * @property color       Hex string color of the line.
 * @property dashed      Whether to render as a dashed line.
 * @property strokeWidth Width of the line in dp.
 */
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

/**
 * DTO representing a [MathShape.TextLabel].
 *
 * @property pt     Center coordinate of the text.
 * @property text   String content to display.
 * @property color  Hex string color of the text.
 * @property sizeSp Font size in sp.
 */
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

/**
 * Parses a hex color string (e.g. "#FF4A80F0" or "#4A80F0") into a Compose [Color].
 * Falls back to blue if null or malformed.
 */
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

/**
 * Formats a Compose [Color] into a hex string representation (e.g. "#ffaabbcc").
 */
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

/**
 * Returns a mathematical evaluation function (Double to Double) for a given formula string expression.
 * Used during parsing to recreate plot curve lambdas dynamically.
 */
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

/**
 * Converts a [MathShape] domain model into its corresponding serializable [MathShapeDto] representation.
 */
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

/**
 * Converts a domain [Question] model into its corresponding serializable [QuestionDto] representation.
 */
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
