package prz.rutedu.app.math

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * A general-purpose mathematical canvas composable that renders a coordinate system
 * and a list of [MathShape] objects on top of it.
 *
 * The canvas occupies whatever size its [modifier] allocates. All drawing uses the
 * mathematical coordinate convention (y-up), with a linear mapping to canvas pixels
 * defined by [viewport]. The y-axis is **inverted** during the mapping: a world point
 * with a larger y value is drawn higher on screen.
 *
 * ## Coordinate transform
 *
 * ```
 * canvasX = (worldX - xMin) / (xMax - xMin) * canvasWidth
 * canvasY = (1 - (worldY - yMin) / (yMax - yMin)) * canvasHeight
 * ```
 *
 * ## Draw order
 *
 * 1. Grid lines (if [MathViewport.showGrid])
 * 2. Axes with tick labels (if [MathViewport.showAxes])
 * 3. All [shapes], in list order (later shapes paint over earlier ones)
 *
 * ## Example
 * ```
 * MathCanvas(
 *     shapes = listOf(
 *         MathShape.FunctionPlot(f = { x -> x * x }),
 *         MathShape.PointMark(Pt(2.0, 4.0), label = "f(2) = 4")
 *     ),
 *     viewport = MathViewport(xMin = -4.0, xMax = 4.0, yMin = -1.0, yMax = 10.0)
 * )
 * ```
 *
 * @param shapes   Ordered list of [MathShape]s to draw.
 * @param modifier Standard Compose modifier - controls size, padding, etc.
 * @param viewport Defines which region of the coordinate plane is visible.
 */
@Composable
fun MathCanvas(
    shapes: List<MathShape>,
    modifier: Modifier = Modifier,
    viewport: MathViewport = MathViewport(),
    preserveAspectRatio: Boolean = true
) {
    val tm = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val midTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

    Canvas(modifier = modifier) {
        val worldWidth = viewport.xMax - viewport.xMin
        val worldHeight = viewport.yMax - viewport.yMin

        if (worldWidth <= 0 || worldHeight <= 0 || size.width <= 0f || size.height <= 0f) return@Canvas

        // Adjust viewport to preserve aspect ratio if requested
        val vp = if (preserveAspectRatio) {
            val canvasAspect = (size.width / size.height).toDouble()
            val viewAspect = worldWidth / worldHeight

            if (canvasAspect > viewAspect) {
                // Canvas is wider than viewport - expand world x range
                val newWidth = worldHeight * canvasAspect
                val dx = (newWidth - worldWidth) / 2.0
                viewport.copy(xMin = viewport.xMin - dx, xMax = viewport.xMax + dx)
            } else {
                // Canvas is taller than viewport - expand world y range
                val newHeight = worldWidth / canvasAspect
                val dy = (newHeight - worldHeight) / 2.0
                viewport.copy(yMin = viewport.yMin - dy, yMax = viewport.yMax + dy)
            }
        } else {
            viewport
        }

        if (vp.showGrid) drawGrid(vp, gridColor)
        if (vp.showAxes) drawAxes(vp, tm, axisColor, labelColor)
        shapes.forEach { shape ->
            when (shape) {
                is MathShape.FunctionPlot -> drawFunctionPlot(shape, vp)
                is MathShape.Circle       -> drawCircleShape(shape, vp)
                is MathShape.Triangle     -> drawTriangleShape(shape, vp, tm, textColor, midTextColor)
                is MathShape.Rectangle    -> drawRectangleShape(shape, vp)
                is MathShape.PointMark    -> drawPointMark(shape, vp, tm)
                is MathShape.Segment      -> drawSegmentShape(shape, vp)
                is MathShape.PieChart     -> drawPieChartShape(shape, vp, tm, textColor)
                is MathShape.TextLabel    -> {
                    val resolvedColor = if (shape.color == Color(0xFF1A1A1A)) textColor else shape.color
                    drawTextLabel(shape.copy(color = resolvedColor), vp, tm)
                }
            }
        }
    }
}

/** Maps a world x-coordinate to a canvas x-coordinate (pixels from the left edge). */
private fun DrawScope.toCanvasX(wx: Double, vp: MathViewport): Float =
    ((wx - vp.xMin) / (vp.xMax - vp.xMin) * size.width).toFloat()

/**
 * Maps a world y-coordinate to a canvas y-coordinate (pixels from the top edge).
 * The inversion `1 - normalised` flips the axis so that larger world-y values appear higher.
 */
private fun DrawScope.toCanvasY(wy: Double, vp: MathViewport): Float =
    ((1.0 - (wy - vp.yMin) / (vp.yMax - vp.yMin)) * size.height).toFloat()

/** Converts a world [Pt] to a canvas [Offset] in one call. */
private fun DrawScope.toCanvas(pt: Pt, vp: MathViewport): Offset =
    Offset(toCanvasX(pt.x, vp), toCanvasY(pt.y, vp))

/**
 * Draws light grid lines at every [MathViewport.gridStep] interval.
 * The zero lines are skipped here because the axes (drawn separately) cover them.
 * A small epsilon (`1e-9`) is added to the loop bounds to avoid missing the last
 * grid line due to floating-point rounding.
 */
private fun DrawScope.drawGrid(vp: MathViewport, color: Color) {
    var wx = ceil(vp.xMin / vp.gridStep) * vp.gridStep
    while (wx <= vp.xMax + 1e-9) {
        if (abs(wx) > 1e-10) {
            val x = toCanvasX(wx, vp)
            drawLine(color, Offset(x, 0f), Offset(x, size.height))
        }
        wx += vp.gridStep
    }
    var wy = ceil(vp.yMin / vp.gridStep) * vp.gridStep
    while (wy <= vp.yMax + 1e-9) {
        if (abs(wy) > 1e-10) {
            val y = toCanvasY(wy, vp)
            drawLine(color, Offset(0f, y), Offset(size.width, y))
        }
        wy += vp.gridStep
    }
}

/**
 * Draws x and y axes with tick marks and numeric labels.
 * Each axis is only drawn when world-coordinate zero falls inside the viewport
 * (e.g. the x-axis is skipped if the entire visible range is above y = 0).
 */
private fun DrawScope.drawAxes(vp: MathViewport, tm: TextMeasurer, axisColor: Color, labelColor: Color) {
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    // x-axis - only visible when y = 0 is within the viewport
    if (vp.yMin <= 0.0 && vp.yMax >= 0.0) {
        val y0 = toCanvasY(0.0, vp)
        drawLine(axisColor, Offset(0f, y0), Offset(size.width, y0), strokeWidth = 1.5f)
        var wx = ceil(vp.xMin / vp.gridStep) * vp.gridStep
        while (wx <= vp.xMax + 1e-9) {
            if (abs(wx) > 1e-10) {
                val x = toCanvasX(wx, vp)
                drawLine(axisColor, Offset(x, y0 - 4f), Offset(x, y0 + 4f))
                if (vp.showXLabels) {
                    val m = tm.measure(formatNum(wx), labelStyle)
                    drawText(m, topLeft = Offset(x - m.size.width / 2f, y0 + 5f))
                }
            }
            wx += vp.gridStep
        }
    }

    // y-axis - only visible when x = 0 is within the viewport
    if (vp.xMin <= 0.0 && vp.xMax >= 0.0) {
        val x0 = toCanvasX(0.0, vp)
        drawLine(axisColor, Offset(x0, 0f), Offset(x0, size.height), strokeWidth = 1.5f)
        var wy = ceil(vp.yMin / vp.gridStep) * vp.gridStep
        while (wy <= vp.yMax + 1e-9) {
            if (abs(wy) > 1e-10) {
                val y = toCanvasY(wy, vp)
                drawLine(axisColor, Offset(x0 - 4f, y), Offset(x0 + 4f, y))
                if (vp.showYLabels) {
                    val m = tm.measure(formatNum(wy), labelStyle)
                    drawText(m, topLeft = Offset(x0 + 6f, y - m.size.height / 2f))
                }
            }
            wy += vp.gridStep
        }
    }
}

/** Formats a Double tick label: shows it as a Long if it has no fractional part. */
private fun formatNum(v: Double): String {
    val l = v.toLong()
    return if (v == l.toDouble()) l.toString() else v.toString()
}

/**
 * Samples [shape].f at [MathShape.FunctionPlot.samples] evenly-spaced x values and
 * draws the resulting curve as a connected path. The "pen" is lifted whenever
 * a sample returns a non-finite value (NaN, +-Infinity), creating a visual gap at
 * discontinuities (e.g. asymptotes of rational functions).
 */
private fun DrawScope.drawFunctionPlot(shape: MathShape.FunctionPlot, vp: MathViewport) {
    val path = Path()
    val dx = (vp.xMax - vp.xMin) / shape.samples
    var penDown = false
    for (i in 0..shape.samples) {
        val wx = vp.xMin + i * dx
        val wy = try { shape.f(wx) } catch (e: Exception) { Double.NaN }
        if (!wy.isFinite()) { penDown = false; continue }
        val cp = toCanvas(Pt(wx, wy), vp)
        if (!penDown) { path.moveTo(cp.x, cp.y); penDown = true }
        else path.lineTo(cp.x, cp.y)
    }
    drawPath(path, shape.color, style = Stroke(width = shape.strokeWidth.dp.toPx()))
}

/** Draws a circle. Radius is converted from world units to canvas pixels proportionally. */
private fun DrawScope.drawCircleShape(shape: MathShape.Circle, vp: MathViewport) {
    val center = toCanvas(Pt(shape.cx, shape.cy), vp)
    // Use X-scale for radius. If preserveAspectRatio is true, scales are equal.
    // If not, drawCircle still expects a single radius, so we pick one.
    // Better yet: we could use drawOval to support non-uniform scaling.
    val rx = (shape.r * size.width / (vp.xMax - vp.xMin)).toFloat()
    val ry = (shape.r * size.height / (vp.yMax - vp.yMin)).toFloat()

    if (shape.filled) {
        drawOval(
            color = shape.color.copy(alpha = 0.12f),
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2, ry * 2)
        )
    }
    drawOval(
        color = shape.color,
        topLeft = Offset(center.x - rx, center.y - ry),
        size = Size(rx * 2, ry * 2),
        style = Stroke(width = shape.strokeWidth.dp.toPx())
    )
}

/**
 * Draws a triangle with optional angle arcs, vertex labels, and side labels.
 *
 * Labels are pushed outward from the centroid by a fixed pixel distance so they
 * remain outside the triangle regardless of its shape. The unknown-angle marker
 * `"?"` is rendered in red and at a larger size to draw the student's attention.
 */
private fun DrawScope.drawTriangleShape(
    shape: MathShape.Triangle,
    vp: MathViewport,
    tm: TextMeasurer,
    textColor: Color,
    midTextColor: Color
) {
    val pa = toCanvas(shape.a, vp)
    val pb = toCanvas(shape.b, vp)
    val pc = toCanvas(shape.c, vp)

    // Triangle outline
    drawPath(Path().apply {
        moveTo(pa.x, pa.y); lineTo(pb.x, pb.y); lineTo(pc.x, pc.y); close()
    }, shape.color, style = Stroke(width = 2.5.dp.toPx()))

    // Interior angle arcs at each vertex
    if (shape.showAngleArcs) {
        drawAngleArc(pa, pb, pc, shape.color)
        drawAngleArc(pb, pa, pc, shape.color)
        drawAngleArc(pc, pa, pb, shape.color)
    }

    // Vertex labels - pushed outward from the centroid
    // "?" gets distinct red colour and larger size to stand out against triangle lines
    val gx = (pa.x + pb.x + pc.x) / 3f
    val gy = (pa.y + pb.y + pc.y) / 3f
    val vertexLabelOffsetPx = 40.dp.toPx() // must clear the 18dp angle arc radius

    listOf(pa to shape.labelA, pb to shape.labelB, pc to shape.labelC)
        .forEach { (v, lbl) ->
            if (lbl == null) return@forEach
            val isUnknown = lbl == "?"
            val style = if (isUnknown) {
                TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            } else {
                TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            val dx = v.x - gx; val dy = v.y - gy
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val m = tm.measure(lbl, style)
            drawText(m, topLeft = Offset(
                v.x + dx / len * vertexLabelOffsetPx - m.size.width / 2f,
                v.y + dy / len * vertexLabelOffsetPx - m.size.height / 2f
            ))
        }

    // Side labels - pushed outward from the centroid at each side's midpoint
    val midStyle = TextStyle(fontSize = 12.sp, color = midTextColor)
    val midLabelOffsetPx = 14.dp.toPx()
    listOf(
        Offset((pb.x + pc.x) / 2f, (pb.y + pc.y) / 2f) to shape.labelBC,
        Offset((pc.x + pa.x) / 2f, (pc.y + pa.y) / 2f) to shape.labelCA,
        Offset((pa.x + pb.x) / 2f, (pa.y + pb.y) / 2f) to shape.labelAB
    ).forEach { (mid, lbl) ->
        if (lbl == null) return@forEach
        val dx = mid.x - gx; val dy = mid.y - gy
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val m = tm.measure(lbl, midStyle)
        drawText(m, topLeft = Offset(
            mid.x + dx / len * midLabelOffsetPx - m.size.width / 2f,
            mid.y + dy / len * midLabelOffsetPx - m.size.height / 2f
        ))
    }
}

/**
 * Draws a small arc at [vertex] that sweeps from the direction of [arm1] to [arm2].
 *
 * The sweep angle is normalised so we always draw the **shorter** (interior) arc,
 * which correctly represents the angle for any triangle where all angles are < 180°.
 * If `sweep > 180°` after the initial `% 360`, subtracting 360° flips it to the
 * shorter equivalent negative sweep.
 *
 * @param vertex The corner where the arc is drawn.
 * @param arm1   A point on one of the two sides meeting at [vertex].
 * @param arm2   A point on the other side.
 * @param color  Colour of the arc stroke (rendered at 65 % opacity).
 */
private fun DrawScope.drawAngleArc(vertex: Offset, arm1: Offset, arm2: Offset, color: Color) {
    val arcR = 18.dp.toPx()
    val d1 = (atan2((arm1.y - vertex.y).toDouble(), (arm1.x - vertex.x).toDouble()) * 180.0 / PI).toFloat()
    val d2 = (atan2((arm2.y - vertex.y).toDouble(), (arm2.x - vertex.x).toDouble()) * 180.0 / PI).toFloat()
    var sweep = (d2 - d1 + 360f) % 360f
    if (sweep > 180f) sweep -= 360f  // always take the interior (shorter) arc
    drawPath(
        Path().apply { addArc(Rect(center = vertex, radius = arcR), d1, sweep) },
        color.copy(alpha = 0.65f),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

/**
 * Draws an axis-aligned rectangle.
 *
 * [MathShape.Rectangle] specifies the **bottom-left** corner in world coordinates
 * (mathematical convention), but canvas y is inverted, so the bottom-left world point
 * maps to the **top-left** canvas point. The top-left canvas position is therefore
 * computed from `(x, y + h)` in world space.
 */
private fun DrawScope.drawRectangleShape(shape: MathShape.Rectangle, vp: MathViewport) {
    val tl = toCanvas(Pt(shape.x, shape.y + shape.h), vp)
    val br = toCanvas(Pt(shape.x + shape.w, shape.y), vp)
    val s = Size(br.x - tl.x, br.y - tl.y)
    if (shape.filled) drawRect(shape.color.copy(alpha = 0.12f), tl, s)
    drawRect(shape.color, tl, s, style = Stroke(width = shape.strokeWidth.dp.toPx()))
}

/** Draws a filled dot and an optional text label 8px to its right. */
private fun DrawScope.drawPointMark(shape: MathShape.PointMark, vp: MathViewport, tm: TextMeasurer) {
    val cp = toCanvas(shape.pt, vp)
    drawCircle(shape.color, shape.radiusDp.dp.toPx(), cp)
    if (shape.label != null) {
        val m = tm.measure(shape.label, TextStyle(fontSize = 11.sp, color = shape.color))
        drawText(m, topLeft = Offset(cp.x + 8f, cp.y - m.size.height / 2f))
    }
}

/** Draws a straight line segment, optionally with an 8px-on / 4px-off dash pattern. */
private fun DrawScope.drawSegmentShape(shape: MathShape.Segment, vp: MathViewport) {
    val from = toCanvas(shape.from, vp)
    val to   = toCanvas(shape.to,   vp)
    val effect = if (shape.dashed) PathEffect.dashPathEffect(floatArrayOf(8f, 4f)) else null
    drawPath(
        Path().apply { moveTo(from.x, from.y); lineTo(to.x, to.y) },
        shape.color,
        style = Stroke(width = shape.strokeWidth.dp.toPx(), pathEffect = effect)
    )
}

/** Draws a pie chart with slices and optional labels. */
private fun DrawScope.drawPieChartShape(
    shape: MathShape.PieChart,
    vp: MathViewport,
    tm: TextMeasurer,
    textColor: Color
) {
    val center = toCanvas(Pt(shape.cx, shape.cy), vp)
    val rx = (shape.r * size.width / (vp.xMax - vp.xMin)).toFloat()
    val ry = (shape.r * size.height / (vp.yMax - vp.yMin)).toFloat()
    val total = shape.slices.sumOf { it.value }
    if (total <= 0) return

    var currentAngle = -90f // Start from top
    val rect = Rect(center.x - rx, center.y - ry, center.x + rx, center.y + ry)

    shape.slices.forEach { slice ->
        val sweep = (slice.value / total * 360f).toFloat()
        val path = Path().apply {
            moveTo(center.x, center.y)
            arcTo(rect, currentAngle, sweep, false)
            close()
        }
        drawPath(path, slice.color)
        drawPath(path, textColor.copy(alpha = 0.3f), style = Stroke(width = shape.strokeWidth.dp.toPx()))

        if (slice.label != null && sweep > 15f) {
            val midAngle = (currentAngle + sweep / 2f) * PI / 180f
            val labelRX = rx * 0.65f
            val labelRY = ry * 0.65f
            val labelPos = Offset(
                center.x + labelRX * kotlin.math.cos(midAngle).toFloat(),
                center.y + labelRY * kotlin.math.sin(midAngle).toFloat()
            )
            val m = tm.measure(slice.label, TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
            drawText(m, topLeft = Offset(labelPos.x - m.size.width / 2f, labelPos.y - m.size.height / 2f))
        }
        currentAngle += sweep
    }
}

/** Draws text centred on the world-space point [shape].pt. */
private fun DrawScope.drawTextLabel(shape: MathShape.TextLabel, vp: MathViewport, tm: TextMeasurer) {
    val cp = toCanvas(shape.pt, vp)
    val m = tm.measure(shape.text, TextStyle(fontSize = shape.sizeSp.sp, color = shape.color))
    drawText(m, topLeft = Offset(cp.x - m.size.width / 2f, cp.y - m.size.height / 2f))
}
