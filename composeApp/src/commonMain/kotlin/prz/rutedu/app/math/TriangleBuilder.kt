package prz.rutedu.app.math

import prz.rutedu.app.math.TriangleBuilder.fromAngles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Factory object for computing triangle vertices from geometric descriptions.
 *
 * All angles are in **degrees**. Vertices are returned centred at the mathematical
 * origin (centroid at (0, 0)) so [MathCanvas] can display them symmetrically
 * inside any viewport that includes the origin.
 *
 * **Vertex convention:**
 * - A is at the bottom-left.
 * - B is at the bottom-right.
 * - C is the apex (highest point for non-obtuse triangles).
 */
object TriangleBuilder {

    /**
     * Computes the three vertices of a triangle given two base angles and the base length.
     *
     * The algorithm:
     * 1. Derive the apex angle: `angleC = 180° - angleA - angleB`.
     * 2. Apply the Law of Sines to find side AC: `AC = base * sin(B) / sin(C)`.
     * 3. Place A at the origin and B along the positive x-axis.
     * 4. Compute C from A using the angle at A and length AC.
     * 5. Shift all three vertices so their centroid lands at the origin.
     *
     * @param angleA  Angle at vertex A, in degrees. Must be positive.
     * @param angleB  Angle at vertex B, in degrees. Must be positive.
     * @param baseLen Length of side AB (the base) in world units. Default is 4.0.
     * @return A [Triple] of (A, B, C) world-space vertices, centred at the origin.
     * @throws IllegalArgumentException if the angles sum to 180° or more
     *         (which would not form a valid triangle).
     */
    fun fromAngles(
        angleA: Double,
        angleB: Double,
        baseLen: Double = 4.0
    ): Triple<Pt, Pt, Pt> {
        val angleC = 180.0 - angleA - angleB
        require(angleC > 0.0) { "Angles must sum to less than 180° (got $angleA + $angleB)" }

        val aRad = angleA * PI / 180.0
        val bRad = angleB * PI / 180.0
        val cRad = angleC * PI / 180.0

        // Law of Sines: baseLen / sin(C) = AC / sin(B)
        val acLen = baseLen * sin(bRad) / sin(cRad)

        val ax = 0.0;  val ay = 0.0
        val bx = baseLen; val by = 0.0
        val cx = acLen * cos(aRad)
        val cy = acLen * sin(aRad)

        // Centre by centroid so the triangle sits symmetrically around the origin
        val gx = (ax + bx + cx) / 3.0
        val gy = (ay + by + cy) / 3.0

        return Triple(Pt(ax - gx, ay - gy), Pt(bx - gx, by - gy), Pt(cx - gx, cy - gy))
    }

    /**
     * Same as [fromAngles] but also computes a tight [MathViewport] that fits the
     * triangle with [margin] world-unit padding on each side.
     *
     * Grid lines and axes are disabled in the returned viewport since pure geometry
     * diagrams don't benefit from a coordinate grid.
     *
     * @param angleA  Angle at vertex A, in degrees.
     * @param angleB  Angle at vertex B, in degrees.
     * @param baseLen Length of side AB in world units.
     * @param margin  Extra space (world units) added to each side of the bounding box.
     * @return A [Pair] of (vertices Triple, viewport).
     */
    fun fromAnglesWithViewport(
        angleA: Double,
        angleB: Double,
        baseLen: Double = 4.0,
        margin: Double = 1.2
    ): Pair<Triple<Pt, Pt, Pt>, MathViewport> {
        val verts = fromAngles(angleA, angleB, baseLen)
        val (a, b, c) = verts
        val xs = listOf(a.x, b.x, c.x)
        val ys = listOf(a.y, b.y, c.y)
        return Pair(
            verts,
            MathViewport(
                xMin = xs.minOrNull()!! - margin,
                xMax = xs.maxOrNull()!! + margin,
                yMin = ys.minOrNull()!! - margin,
                yMax = ys.maxOrNull()!! + margin,
                showGrid = false,
                showAxes = false
            )
        )
    }

    /**
     * Returns vertices for an equilateral triangle with the given side length.
     *
     * All three angles are 60°. Vertices are centred at the origin.
     *
     * @param side Side length in world units.
     */
    fun equilateral(side: Double = 4.0): Triple<Pt, Pt, Pt> = fromAngles(60.0, 60.0, side)

    /**
     * Returns vertices for a right triangle with the right angle at vertex A.
     *
     * - A is at the right angle.
     * - B is at the end of the horizontal leg (AB = [legH]).
     * - C is at the end of the vertical leg (AC = [legV]).
     *
     * Vertices are centred at the origin.
     *
     * @param legH Length of the horizontal leg (AB) in world units.
     * @param legV Length of the vertical leg (AC) in world units.
     */
    fun rightTriangle(legH: Double = 3.0, legV: Double = 4.0): Triple<Pt, Pt, Pt> {
        val gx = legH / 3.0
        val gy = legV / 3.0
        return Triple(
            Pt(-gx, -gy),
            Pt(legH - gx, -gy),
            Pt(-gx, legV - gy)
        )
    }
}
