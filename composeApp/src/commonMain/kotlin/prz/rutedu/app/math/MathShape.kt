package prz.rutedu.app.math

import androidx.compose.ui.graphics.Color

/**
 * A 2-D point in **mathematical (world) coordinates**.
 *
 * The coordinate system follows mathematical convention: x increases to the right,
 * y increases upward. [MathCanvas] converts world coordinates to canvas (pixel)
 * coordinates internally - callers always work in world space.
 *
 * @property x Horizontal position (positive = right).
 * @property y Vertical position (positive = up).
 */
data class Pt(val x: Double, val y: Double)

/**
 * Defines the visible region of the coordinate plane drawn by [MathCanvas].
 *
 * The viewport describes which portion of the infinite mathematical plane is mapped
 * onto the composable's pixel area. The mapping is a simple linear (affine) scale with
 * y-axis inversion (canvas y grows downward; mathematical y grows upward).
 *
 * @property xMin     Left edge of the visible area in world units.
 * @property xMax     Right edge of the visible area in world units.
 * @property yMin     Bottom edge of the visible area in world units.
 * @property yMax     Top edge of the visible area in world units.
 * @property showGrid When `true`, evenly-spaced grid lines are drawn at [gridStep] intervals.
 * @property showAxes When `true`, x and y axes with numeric tick labels are drawn.
 * @property gridStep Distance between grid lines and tick marks, in world units.
 */
data class MathViewport(
    val xMin: Double = -5.0,
    val xMax: Double = 5.0,
    val yMin: Double = -4.0,
    val yMax: Double = 4.0,
    val showGrid: Boolean = true,
    val showAxes: Boolean = true,
    val gridStep: Double = 1.0
)

/**
 * Sealed hierarchy of all visual objects that [MathCanvas] knows how to draw.
 *
 * Each subclass carries enough information to render itself given a [MathViewport].
 * All coordinate values are in **world (mathematical) units** - [MathCanvas] handles
 * the conversion to canvas pixels.
 *
 * To draw shapes, build a `List<MathShape>` and pass it to [MathCanvas].
 *
 * ## Example
 * ```
 * MathCanvas(
 *     shapes = listOf(
 *         MathShape.FunctionPlot(f = { x -> x * x }),
 *         MathShape.PointMark(Pt(2.0, 4.0), label = "(2, 4)")
 *     ),
 *     viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -1.0, yMax = 10.0)
 * )
 * ```
 */
sealed class MathShape {

    /**
     * Plots the curve y = [f](x) by sampling it at [samples] evenly-spaced x positions
     * across the viewport's x range.
     *
     * Discontinuities (e.g. division by zero) are handled gracefully: any sample that
     * produces `NaN` or infinity lifts the pen, so the curve breaks instead of connecting
     * across the asymptote.
     *
     * @property f           The function to plot. Receives an x value in world units and
     *                       returns the corresponding y value.
     * @property color       Stroke colour of the plotted curve.
     * @property label       Optional text label placed near the curve (not yet rendered - reserved).
     * @property strokeWidth Width of the curve stroke in dp.
     * @property samples     Number of x samples used to approximate the curve. Higher values
     *                       produce smoother curves but cost more draw calls. 300 is a good
     *                       default for typical school-math functions.
     */
    data class FunctionPlot(
        val f: (Double) -> Double,
        val color: Color = Color(0xFF4A80F0),
        val label: String? = null,
        val strokeWidth: Float = 2.5f,
        val samples: Int = 300
    ) : MathShape()

    /**
     * A circle with a given centre and radius.
     *
     * @property cx          World x-coordinate of the centre.
     * @property cy          World y-coordinate of the centre.
     * @property r           Radius in world units.
     * @property color       Stroke colour (and fill colour at 12 % opacity when [filled] is `true`).
     * @property filled      When `true`, the interior is filled with a semi-transparent tint.
     * @property strokeWidth Width of the outline stroke in dp.
     */
    data class Circle(
        val cx: Double,
        val cy: Double,
        val r: Double,
        val color: Color = Color(0xFF4A80F0),
        val filled: Boolean = false,
        val strokeWidth: Float = 2f
    ) : MathShape()

    /**
     * A triangle defined by three vertices in world coordinates.
     *
     * Vertex labels (e.g. angle values like `"60°"` or `"?"`) are placed outward from
     * the centroid at a fixed offset so they never overlap the triangle's sides.
     * The unknown-angle marker `"?"` is rendered larger and in red to stand out.
     *
     * Side labels are placed outward from the centroid at the midpoint of each side.
     *
     * @property a            First vertex (world coordinates).
     * @property b            Second vertex.
     * @property c            Third vertex (typically the apex).
     * @property color        Colour of the triangle sides and angle arcs.
     * @property showAngleArcs When `true`, small arcs are drawn at each interior angle.
     * @property labelA       Label at vertex [a] (e.g. `"60°"`, `"?"`).
     * @property labelB       Label at vertex [b].
     * @property labelC       Label at vertex [c].
     * @property labelAB      Label at the midpoint of side A–B.
     * @property labelBC      Label at the midpoint of side B–C.
     * @property labelCA      Label at the midpoint of side C–A.
     */
    data class Triangle(
        val a: Pt,
        val b: Pt,
        val c: Pt,
        val color: Color = Color(0xFF4A80F0),
        val showAngleArcs: Boolean = true,
        val labelA: String? = null,
        val labelB: String? = null,
        val labelC: String? = null,
        val labelAB: String? = null,
        val labelBC: String? = null,
        val labelCA: String? = null
    ) : MathShape()

    /**
     * An axis-aligned rectangle with its bottom-left corner at ([x], [y]).
     *
     * Note: world y increases upward but canvas y increases downward, so [MathCanvas]
     * maps the bottom-left world point to the top-left canvas point when drawing.
     *
     * @property x           World x-coordinate of the left edge.
     * @property y           World y-coordinate of the bottom edge.
     * @property w           Width in world units.
     * @property h           Height in world units.
     * @property color       Stroke colour (and 12 % tint fill when [filled] is `true`).
     * @property filled      When `true`, interior is filled with a semi-transparent tint.
     * @property strokeWidth Width of the outline stroke in dp.
     */
    data class Rectangle(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        val color: Color = Color(0xFF4A80F0),
        val filled: Boolean = false,
        val strokeWidth: Float = 2f
    ) : MathShape()

    /**
     * A filled dot at [pt] with an optional text label to its right.
     *
     * Commonly used to highlight a specific coordinate on a function plot
     * (e.g. the vertex of a parabola or a specific f(x) evaluation point).
     *
     * @property pt       World-space position of the dot.
     * @property label    Text rendered to the right of the dot (e.g. `"f(2) = 4"`).
     * @property color    Dot and label colour.
     * @property radiusDp Dot radius in dp.
     */
    data class PointMark(
        val pt: Pt,
        val label: String? = null,
        val color: Color = Color(0xFF4A80F0),
        val radiusDp: Float = 4f
    ) : MathShape()

    /**
     * A straight line segment between two world-space points.
     *
     * Can be rendered solid or dashed. Typically used to draw helper lines such as
     * a vertical drop from a curve to the x-axis.
     *
     * @property from        Start point in world coordinates.
     * @property to          End point in world coordinates.
     * @property color       Stroke colour.
     * @property dashed      When `true`, the segment is drawn with an 8px-on / 4px-off dash pattern.
     * @property strokeWidth Width of the stroke in dp.
     */
    data class Segment(
        val from: Pt,
        val to: Pt,
        val color: Color = Color(0xFF4A80F0),
        val dashed: Boolean = false,
        val strokeWidth: Float = 2f
    ) : MathShape()

    /**
     * Free-floating text centred on a world-space point.
     *
     * Useful for labelling regions of a graph or annotating geometric diagrams directly
     * on the canvas rather than in surrounding UI.
     *
     * @property pt     Centre position of the text in world coordinates.
     * @property text   String to render.
     * @property color  Text colour.
     * @property sizeSp Font size in sp.
     */
    data class TextLabel(
        val pt: Pt,
        val text: String,
        val color: Color = Color(0xFF1A1A1A),
        val sizeSp: Float = 13f
    ) : MathShape()
}
