package prz.rutedu.app.models

import prz.rutedu.app.math.MathShape
import prz.rutedu.app.math.MathViewport

/**
 * The five arithmetic operators that can appear in quiz questions.
 *
 * @property symbol Unicode character shown in the equation display (e.g. `"x"` instead of `*`).
 */
enum class MathOperator(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷"),
    POWER("^")
}

/**
 * Structured hint shown in the [prz.rutedu.app.screens.HintBottomSheet].
 *
 * A hint can have up to four visual sections, all optional:
 * 1. **Main text block** - a short rule or explanation with one optional bold excerpt.
 * 2. **Section title + items** - a labelled bullet list (e.g. a formula sheet).
 * 3. **Steps** - a numbered "Krok po kroku" walkthrough.
 *
 * At minimum, set [mainText] to a non-empty string so the hint box is visible.
 *
 * @property mainText     Primary explanation text rendered inside the left-border card.
 * @property boldPart     Substring of [mainText] to render in bold. Must match exactly
 *                        (case-sensitive). `null` = no bold highlight.
 * @property sectionTitle Optional small-caps label rendered above [items]
 *                        (e.g. `"WZORY SKRÓCONEGO MNOŻENIA"`).
 * @property items        Bullet rows rendered below [sectionTitle].
 * @property steps        Lines for the "Krok po kroku" step list. Each string is one step.
 */
data class Hint(
    val mainText: String,
    val boldPart: String? = null,
    val sectionTitle: String? = null,
    val items: List<String> = emptyList(),
    val steps: List<String> = emptyList()
)

/**
 * Geographic bounding box that controls which part of the world map is displayed for
 * a [Question.MapQuiz]. Each enum value defines the visible longitude/latitude window.
 *
 * @property lonMin Western boundary in decimal degrees (negative = west of Greenwich).
 * @property lonMax Eastern boundary in decimal degrees.
 * @property latMin Southern boundary in decimal degrees (negative = south of equator).
 * @property latMax Northern boundary in decimal degrees.
 */
enum class MapRegion(
    val lonMin: Float, val lonMax: Float,
    val latMin: Float, val latMax: Float
) {
    /** Shows the entire European continent from Iceland to the Urals. */
    EUROPE(-27f, 60f, 27f, 75f),
    /** Zoomed-in view of Poland - suitable for Polish rivers, provinces, cities and national parks questions. */
    POLAND(14f, 24f, 48f, 55f),
    /** Zoomed-in view of Central Europe - suitable for Poland-neighbour questions. */
    CENTRAL_EUROPE(-8f, 35f, 44f, 60f),
    /** Shows the Asian continent from Turkey to Japan. */
    ASIA(25f, 155f, 5f, 75f)
}

/**
 * Sealed hierarchy of all question types that the app can present during a lesson.
 *
 * Every subclass carries an [id] that is unique **within its lesson**. The id is used by
 * [prz.rutedu.app.data.ChemistrySessionStore] to track which questions have
 * already been answered so they are excluded from future sessions.
 *
 * `LessonGameScreen` dispatches each [Question] to the appropriate `*Content` composable
 * via a `when` expression. To add a new question type, add a subclass here, implement a
 * matching `*Content` composable in the `screens` package, and add a branch to the
 * `when` block in `LessonGameScreen.kt`.
 *
 * @property id Unique integer identifier within the lesson. For static banks the ids are
 *              sequential from 0. For generated chemistry questions, a four-digit prefix
 *              scheme is used (e.g. `1100 + index`).
 */
sealed class Question(open val id: Int) {

    /**
     * The student must type the numeric result of a simple arithmetic expression.
     *
     * The displayed equation is `operand1 operator operand2 = ?`.
     * [correctAnswer] is computed automatically from the operands and operator,
     * so it does not need to be set manually.
     *
     * Rendered by `FindAnswerContent`.
     *
     * @property operand1 Left-hand operand.
     * @property operand2 Right-hand operand. For [MathOperator.DIVIDE], must evenly divide [operand1].
     * @property operator The arithmetic operation to display and compute.
     * @property hint     Hint shown when the student taps "Podpowiedź".
     */
    data class FindAnswer(
        override val id: Int,
        val operand1: Int,
        val operand2: Int,
        val operator: MathOperator,
        val hint: Hint = Hint("")
    ) : Question(id) {
        /** Computed answer - derived from the operands, never stored separately. */
        val correctAnswer: Int = when (operator) {
            MathOperator.ADD      -> operand1 + operand2
            MathOperator.SUBTRACT -> operand1 - operand2
            MathOperator.MULTIPLY -> operand1 * operand2
            MathOperator.DIVIDE   -> operand1 / operand2
            MathOperator.POWER    -> {
                var r = 1
                repeat(operand2) { r *= operand1 }
                r
            }
        }
    }

    /**
     * The student must identify the correct arithmetic operator for a given equation.
     *
     * The equation `operand1 __ operand2 = result` is displayed with the operator blank.
     * The student drags one of several operator chips into the blank. Only [correctOperator]
     * makes the equation true.
     *
     * Rendered by `FindOperatorContent`.
     *
     * @property operand1         Left-hand operand.
     * @property operand2         Right-hand operand.
     * @property result           The pre-computed result shown on the right side of the equation.
     * @property correctOperator  The operator that makes `operand1 __ operand2 = result` true.
     * @property hint             Hint shown when the student taps "Podpowiedź".
     */
    data class FindOperator(
        override val id: Int,
        val operand1: Int,
        val operand2: Int,
        val result: Int,
        val correctOperator: MathOperator,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * The student selects one or more correct answers from a list of options.
     *
     * When [multiSelect] is `false` (the default), only one option can be chosen
     * (radio-button behaviour) and [correctIndices] must contain exactly one index.
     * When [multiSelect] is `true`, checkboxes are shown and all indices in
     * [correctIndices] must be selected.
     *
     * Rendered by `SelectFromListContent`.
     *
     * @property prompt         The question text displayed above the options.
     * @property options        The answer choices. Order matters - [correctIndices] reference
     *                          positions in this list.
     * @property correctIndices Set of zero-based indices into [options] that are correct.
     * @property multiSelect    `true` = checkbox mode (one or more correct answers);
     *                          `false` = radio mode (exactly one correct answer).
     * @property hint           Hint shown when the student taps "Podpowiedź".
     */
    data class SelectFromList(
        override val id: Int,
        val prompt: String,
        val options: List<String>,
        val correctIndices: Set<Int>,
        val multiSelect: Boolean = false,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * The student types a numeric answer to a text-based prompt.
     *
     * Optionally, a triangle diagram is displayed above the input field when
     * [triangleAngles] is set, and a short inline tip can appear between the diagram
     * and the input via [inlineHint].
     *
     * Rendered by `TypeAnswerContent`.
     *
     * @property prompt          The question text.
     * @property correctAnswer   Expected integer answer.
     * @property unit            Unit suffix appended after the input (e.g. `"°"`, `"cm"`).
     *                           Empty string means no unit.
     * @property triangleAngles  When non-null, renders a triangle with these two known
     *                           angles. The third (unknown) angle is the question's answer.
     * @property inlineHint      Short text shown directly under the triangle or prompt as
     *                           a persistent hint (not the hint bottom sheet).
     * @property hint            Full hint shown in the bottom sheet.
     */
    data class TypeAnswer(
        override val id: Int,
        val prompt: String,
        val correctAnswer: Int,
        val unit: String = "",
        val triangleAngles: Pair<Int, Int>? = null,
        val inlineHint: String? = null,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * The student taps a country on an interactive, zoomable world map.
     *
     * The map is loaded from a GeoJSON asset and rendered on a `Canvas`. The student
     * can pinch-to-zoom and pan the map before selecting a country. The check is done
     * by name matching against [countryKey] (English, exactly as stored in the GeoJSON).
     *
     * Rendered by `MapQuizContent`.
     *
     * @property countryKey    The country's English name as it appears in the GeoJSON `"name"`
     *                         property (e.g. `"Poland"`, `"South Korea"`). This is the
     *                         authoritative identifier used for hit-testing.
     * @property questionText  Full grammatically correct question in Polish
     *                         (e.g. `"Gdzie leży Polska?"`).
     * @property region        Which portion of the world map to display. Defaults to [MapRegion.EUROPE].
     * @property mapFile       File path to the geojson file for the desired game.
     *                         (e.g. `"files/countries.geojson"`)
     * @property hint          Full hint shown in the bottom sheet.
     */
    data class MapQuiz(
        override val id: Int,
        val countryKey: String,
        val questionText: String,
        val region: MapRegion = MapRegion.EUROPE,
        val mapFile: String = "files/countries.geojson",
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * A zoomable full periodic table from which several elements have been removed.
     * The student selects one element from a tray at the bottom and taps its correct
     * empty slot in the table.
     *
     * Rendered by `PeriodicTableContent`.
     *
     * @property questionText         Instruction text displayed above the table.
     * @property missingAtomicNumbers Atomic numbers of the 4–5 elements removed from
     *                                the table. These become the tray items.
     * @property hint                 Full hint shown in the bottom sheet.
     */
    data class PeriodicTableQuiz(
        override val id: Int,
        val questionText: String,
        val missingAtomicNumbers: List<Int>,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * The student sees a Bohr-model shell configuration (e.g. `"2,8,1"`) and must tap
     * the matching element cell on a zoomable periodic table.
     *
     * Rendered by `PeriodicTableContent` (via the `PeriodicTableByShell` branch).
     *
     * @property shellConfig         Shell notation string (comma-separated electron counts
     *                               per shell, K first). Example: `"2,8,1"` for sodium.
     * @property targetAtomicNumber  Atomic number of the element that matches [shellConfig].
     * @property hint                Full hint shown in the bottom sheet.
     */
    data class PeriodicTableByShell(
        override val id: Int,
        val shellConfig: String,
        val targetAtomicNumber: Int,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * The student sees an element's Polish name and must tap the matching cell on a
     * zoomable periodic table.
     *
     * Rendered by `PeriodicTableContent` (via the `PeriodicTableByName` branch).
     *
     * @property elementNamePL       Polish name of the element (e.g. `"Sód"` for sodium).
     *                               Must match [Element.namePL] exactly.
     * @property targetAtomicNumber  Atomic number of the element with that name.
     * @property hint                Full hint shown in the bottom sheet.
     */
    data class PeriodicTableByName(
        override val id: Int,
        val elementNamePL: String,
        val targetAtomicNumber: Int,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * Represents one term in a chemical equation (reactant or product).
     *
     * Either [fixedCoefficient] **or** [correctCoefficient] should be set, but not both.
     * - Set [fixedCoefficient] to display a read-only coefficient.
     * - Set [correctCoefficient] (and leave [fixedCoefficient] = `null`) to render an
     *   editable input box where the student types the missing value.
     *
     * @property formula              Chemical formula with Unicode subscript characters
     *                                (e.g. `"H₂O"`, `"CO₂"`).
     * @property fixedCoefficient     Coefficient shown as a static label; `null` = student fills it in.
     * @property correctCoefficient   Expected answer when [fixedCoefficient] is `null`; ignored otherwise.
     */
    data class BalanceTerm(
        val formula: String,
        val fixedCoefficient: Int? = null,
        val correctCoefficient: Int? = null
    )

    /**
     * The student fills in missing stoichiometric coefficients to balance a chemical equation.
     *
     * The equation is displayed as `reactants -> products`. Each [BalanceTerm] is either
     * a read-only label or an editable field, controlled by [BalanceTerm.fixedCoefficient].
     * The student types values into the blank fields and taps "Sprawdź".
     *
     * Rendered by `EquationBalanceContent`.
     *
     * @property instruction     Main instruction line displayed above the equation
     *                           (default: `"Uzupełnij równanie reakcji"`).
     * @property subInstruction  Secondary instruction below the main line.
     * @property reactants       Ordered list of terms on the left side of the arrow.
     * @property products        Ordered list of terms on the right side of the arrow.
     * @property hint            Full hint shown in the bottom sheet.
     */
    data class EquationBalance(
        override val id: Int,
        val instruction: String = "Uzupełnij równanie reakcji",
        val subInstruction: String = "Dobierz odpowiednie współczynniki stechiometryczne",
        val reactants: List<BalanceTerm>,
        val products: List<BalanceTerm>,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * An element's information card (symbol, name, atomic mass, electron configuration,
     * group name) is shown and the student selects the correct answer to [prompt] from
     * four options arranged in a 2x2 grid.
     *
     * Rendered by `ElementCardContent`.
     *
     * @property prompt        The question being asked based on the card's data
     *                         (e.g. `"Jaki jest symbol tego pierwiastka?"`).
     * @property subtitle      Secondary instruction shown below [prompt].
     * @property atomicNumber  Atomic number of the element whose card is displayed.
     *                         Must be a key in [elementByNumber].
     * @property options       Exactly four answer strings.
     * @property correctIndex  Zero-based index of the correct option in [options].
     * @property hint          Full hint shown in the bottom sheet.
     */
    data class ElementCardQuiz(
        override val id: Int,
        val prompt: String,
        val subtitle: String = "Wybierz poprawną odpowiedź na podstawie karty pierwiastka.",
        val atomicNumber: Int,
        val options: List<String>,
        val correctIndex: Int,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * A mathematical function or geometric figure is rendered on a [prz.rutedu.app.math.MathCanvas]
     * and the student types a numeric answer to [prompt].
     *
     * Use this type for questions like "calculate the missing angle" or "evaluate f(4)".
     *
     * Rendered by `GraphTypeAnswerContent` (formerly `MathCanvasContent`).
     *
     * @property prompt       The question text.
     * @property shapes       List of [MathShape]s to draw on the canvas (function curves,
     *                        triangles, highlighted points, etc.).
     * @property viewport     The coordinate window for the canvas. Defaults to +-5 on both axes.
     * @property correctAnswer Expected integer answer.
     * @property unit         Unit suffix appended after the answer input (e.g. `"°"`).
     * @property inlineHint   Short persistent tip shown beneath the canvas.
     * @property hint         Full hint shown in the bottom sheet.
     */
    data class GraphTypeAnswer(
        override val id: Int,
        val prompt: String,
        val shapes: List<MathShape>,
        val viewport: MathViewport = MathViewport(),
        val correctAnswer: Int,
        val unit: String = "",
        val inlineHint: String? = null,
        val hint: Hint = Hint("")
    ) : Question(id)

    /**
     * A mathematical function or geometric figure is rendered on a [prz.rutedu.app.math.MathCanvas]
     * and the student selects the correct answer(s) from a list of options.
     *
     * Use this type for questions like "which function is plotted?" or "what is the maximum value?".
     *
     * Rendered by `GraphSelectFromListContent`.
     *
     * @property prompt         The question text.
     * @property shapes         List of [MathShape]s to draw on the canvas.
     * @property viewport       Coordinate window for the canvas.
     * @property options        The answer choices.
     * @property correctIndices Set of zero-based indices into [options] that are correct.
     * @property hint           Full hint shown in the bottom sheet.
     */
    data class GraphSelectFromList(
        override val id: Int,
        val prompt: String,
        val shapes: List<MathShape>,
        val viewport: MathViewport = MathViewport(),
        val options: List<String>,
        val correctIndices: Set<Int>,
        val hint: Hint = Hint("")
    ) : Question(id)
}
