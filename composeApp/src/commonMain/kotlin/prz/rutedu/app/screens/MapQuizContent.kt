package prz.rutedu.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.geo.CountryFeature
import prz.rutedu.app.geo.loadGeoJson
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.Question
import kotlin.math.PI
import kotlin.math.cos

/** Ocean / background fill. */
private val COLOR_OCEAN    = Color(0xFF9DC3D4)
/** Default country fill. */
private val COLOR_COUNTRY  = Color(0xFF4A9B5F)
/** Country border stroke. */
private val COLOR_BORDER   = Color(0xFF2D6B3A)
/** Tapped (selected, not yet checked) country fill. */
private val COLOR_SELECTED = Color(0xFFF4A430)
/** Wrong answer highlight color. */
private val COLOR_WRONG    = Color(0xFFE53935)

/**
 * Returns `true` if any of this country's rings has its centroid within [r]'s bounding box
 * plus a `margin` degree buffer.
 *
 * Used to filter the full world country list down to only those visible in the current
 * [MapRegion] before computing screen coordinates - avoids projecting thousands of polygons
 * that are completely off-screen.
 *
 * The 10-degree margin prevents edge-clipping artifacts for countries that straddle the region boundary.
 */
private fun CountryFeature.hasRingNear(r: MapRegion): Boolean {
    val margin = 10f
    return rings.any { ring ->
        if (ring.isEmpty()) return@any false
        val avgLon = ring.map { it.lon }.average().toFloat()
        val avgLat = ring.map { it.lat }.average().toFloat()
        avgLon in (r.lonMin - margin)..(r.lonMax + margin) &&
                avgLat in (r.latMin - margin)..(r.latMax + margin)
    }
}

/**
 * Returns `true` if screen point (`px`, `py`) lies inside [polygon] using the ray-casting algorithm.
 *
 * Casts a horizontal ray from (`px`, `py`) to the right and counts how many polygon edges it
 * crosses. An odd crossing count means the point is inside. Returns `false` for degenerate
 * polygons with fewer than 3 vertices.
 *
 * Note: works in **canvas/screen space**, not geographic space, because the projection is already
 * applied before this function is called.
 */
private fun pointInPolygon(px: Float, py: Float, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].x; val yi = polygon[i].y
        val xj = polygon[j].x; val yj = polygon[j].y
        if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi)
            inside = !inside
        j = i
    }
    return inside
}

/**
 * Pre-projected ring: [offsets] for hit-testing, [path] for `Canvas.drawPath`.
 * Both are in canvas pixel coordinates after `buildScreenCountries` runs.
 *
 * @property offsets Vertex positions in canvas coordinates used for hit-testing.
 * @property path    Drawable path in canvas coordinates, passed to `Canvas.drawPath`.
 */
private data class ScreenRing(val offsets: List<Offset>, val path: Path)

/**
 * Pairing of the original [CountryFeature] with its per-ring screen projections.
 * The feature is kept so the composable can compare [CountryFeature.name] against the answer key.
 *
 * @property feature The source [CountryFeature] from the GeoJSON data.
 * @property rings   Per-ring screen projections computed by `buildScreenCountries`.
 */
private data class ScreenCountry(val feature: CountryFeature, val rings: List<ScreenRing>)

/**
 * Projects each [CountryFeature]'s geographic rings into canvas pixel coordinates using an
 * equirectangular projection with a cosine-latitude correction.
 *
 * **Projection formula (per vertex):**
 * ```
 * lonCorrection = cos(midLat * π/180) // shrinks horizontal extent at high latitudes
 * canvasX = originX + (lon - lonMin) * lonCorrection / (lonRange * lonCorrection) * mapW
 * canvasY = originY + (latMax - lat) / latRange * mapH // Y flipped: north = top
 * ```
 *
 * **Letterboxing:** the map rectangle (`mapW` x `mapH`) is centered inside (`canvasW` x `canvasH`)
 * to preserve the natural aspect ratio. Empty bars appear on the short sides when the canvas
 * aspect ratio does not match the geographic region's corrected aspect ratio.
 *
 * This function is called from `remember(countries, canvasSize, region)` so it only recomputes
 * when the canvas is resized or the question changes - not on every recomposition.
 *
 * @param features All country features to project (pre-filtered to the region by [hasRingNear]).
 * @param canvasW  Canvas width in pixels.
 * @param canvasH  Canvas height in pixels.
 * @param r        The geographic bounding box that defines the visible map area.
 * @return List of [ScreenCountry] objects ready for drawing and hit-testing.
 */
private fun buildScreenCountries(
    features: List<CountryFeature>,
    canvasW: Float,
    canvasH: Float,
    r: MapRegion
): List<ScreenCountry> {
    val midLatRad = ((r.latMin + r.latMax) / 2f) * (PI / 180.0).toFloat()
    val lonCorrection = cos(midLatRad) // squish longitude at high latitudes
    val lonRange = r.lonMax - r.lonMin
    val latRange = r.latMax - r.latMin
    val naturalAR = (lonRange * lonCorrection) / latRange // natural width/height ratio

    // Fit map inside canvas while keeping aspect ratio (letterbox).
    // Choose the axis that is the binding constraint so the map fills as much of the canvas as possible.
    val (mapW, mapH) = if (canvasW / canvasH > naturalAR) {
        Pair(canvasH * naturalAR, canvasH)
    } else {
        Pair(canvasW, canvasW / naturalAR)
    }
    val originX = (canvasW - mapW) / 2f
    val originY = (canvasH - mapH) / 2f

    return features.map { feature ->
        val screenRings = feature.rings.map { ring ->
            val offsets = ring.map { ll ->
                Offset(
                    x = originX + (ll.lon - r.lonMin) * lonCorrection / (lonRange * lonCorrection) * mapW,
                    y = originY + (r.latMax - ll.lat) / latRange * mapH
                )
            }
            val path = Path().apply {
                if (offsets.isEmpty()) return@apply
                moveTo(offsets[0].x, offsets[0].y)
                for (k in 1 until offsets.size) lineTo(offsets[k].x, offsets[k].y)
                close()
            }
            ScreenRing(offsets, path)
        }
        ScreenCountry(feature, screenRings)
    }
}

/**
 * Returns `true` if [tap] (canvas pixel coordinates) falls inside any ring of [country].
 *
 * Multi-ring countries (e.g. archipelagos) are handled correctly because `any` checks each
 * island ring separately - the tap only needs to land in one of them.
 */
private fun hitTest(tap: Offset, country: ScreenCountry): Boolean =
    country.rings.any { ring -> pointInPolygon(tap.x, tap.y, ring.offsets) }

/**
 * Interactive map quiz where the student taps a country to answer the question.
 *
 * ## Data flow
 * 1. GeoJSON country data is loaded once from [loadCountries] and cached globally.
 * 2. On each render (and whenever `canvasSize` changes), [buildScreenCountries] reprojects
 *    only the countries near [Question.MapQuiz.region] into canvas coordinates.
 * 3. When the user taps the canvas, the raw tap coordinate is **un-transformed** (reversing the
 *    current pan/zoom) before being passed to [hitTest]. This keeps hit-testing simple - it
 *    always works in the base (unzoomed) coordinate space.
 * 4. "Sprawdź" compares the selected country name to [Question.MapQuiz.countryKey] and calls
 *    [onCorrect] or [onWrong].
 *
 * ## Zoom / pan
 * Pinch-zoom and single-finger pan are implemented via `Modifier.transformable`. The transform
 * is applied as a `withTransform { translate; scale }` block inside the Canvas, so the hit-test
 * inversion must mirror this: `base = (tap - pan - pivot) / scale + pivot`.
 *
 * ## State reset
 * All per-question state (`selectedCountry`, `isWrong`, `panOffset`, `zoomScale`) is keyed on
 * `question.id` via `remember(question.id)` so navigating to the next question resets the map
 * to its default zoom/position without needing explicit reset logic.
 *
 * @param question      The map quiz question: bounding region, country key, hint, and prompt text.
 * @param accentColor   Subject accent color for buttons and UI accents.
 * @param bottomPadding System navigation bar height padding from `App`.
 * @param onCorrect     Called when the user taps "Sprawdź" and the selection matches [Question.MapQuiz.countryKey].
 * @param onWrong       Called when the user taps "Sprawdź" and the selection is incorrect.
 */
@Composable
internal fun MapQuizContent(
    question: Question.MapQuiz,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {}
) {
    var countries    by remember { mutableStateOf<List<CountryFeature>>(emptyList()) }
    var loading      by remember { mutableStateOf(true) }
    // All per-question state keyed by question.id so they reset on every new question
    var selectedCountry by remember(question.id) { mutableStateOf<String?>(null) }
    var isWrong      by remember(question.id) { mutableStateOf(false) }
    var showHint     by remember(question.id) { mutableStateOf(false) }
    var panOffset    by remember(question.id) { mutableStateOf(Offset.Zero) }
    var zoomScale    by remember(question.id) { mutableStateOf(1f) }
    var canvasSize   by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val region = question.region
    val screenCountries = remember(countries, canvasSize, region) {
        val cs = canvasSize ?: return@remember emptyList()
        buildScreenCountries(
            countries.filter { it.hasRingNear(region) },
            cs.first.toFloat(),
            cs.second.toFloat(),
            region
        )
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 8f)
        panOffset += panChange
    }

    LaunchedEffect(question.mapFile) {
        loading = true
        countries = prz.rutedu.app.geo.loadGeoJson(question.mapFile)
        loading = false
    }

    if (showHint) {
        HintBottomSheet(
            hint = question.hint,
            accentColor = accentColor,
            onDismiss = { showHint = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = question.questionText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(COLOR_OCEAN),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { cs -> canvasSize = Pair(cs.width, cs.height) }
                        // transformable handles single-finger pan + two-finger pinch zoom
                        .transformable(state = transformableState)
                        // pointerInput for tap - MUST include question.id so it restarts
                        // on each new question and captures the fresh selectedCountry state
                        .pointerInput(screenCountries, question.id) {
                            detectTapGestures { tap ->
                                val cs = canvasSize ?: return@detectTapGestures
                                val w = cs.first.toFloat()
                                val h = cs.second.toFloat()
                                val cx = w / 2f
                                val cy = h / 2f
                                // The Canvas draws with: visual = (base - pivot) * scale + pivot + pan
                                // To find which base coordinate the user tapped, invert that:
                                //   base = (visual - pan - pivot)/scale + pivot
                                // Without this inversion, tapping a zoomed-in country would test
                                // the wrong coordinates and miss the polygon.
                                val base = Offset(
                                    x = (tap.x - panOffset.x - cx) / zoomScale + cx,
                                    y = (tap.y - panOffset.y - cy) / zoomScale + cy
                                )
                                val hit = screenCountries.firstOrNull { hitTest(base, it) }
                                if (hit != null) {
                                    selectedCountry = hit.feature.name
                                    isWrong = false
                                }
                            }
                        }
                ) {
                    drawRect(color = COLOR_OCEAN)

                    withTransform(
                        transformBlock = {
                            translate(panOffset.x, panOffset.y)
                            scale(zoomScale, zoomScale, center)
                        }
                    ) {
                        // Unselected countries first
                        screenCountries.forEach { sc ->
                            if (sc.feature.name != selectedCountry) {
                                sc.rings.forEach { ring ->
                                    drawPath(ring.path, color = COLOR_COUNTRY)
                                    drawPath(ring.path, color = COLOR_BORDER, style = Stroke(width = 1.2f))
                                }
                            }
                        }
                        // Selected country drawn on top
                        screenCountries.firstOrNull { it.feature.name == selectedCountry }?.let { sc ->
                            val fill = if (isWrong) COLOR_WRONG else COLOR_SELECTED
                            sc.rings.forEach { ring ->
                                drawPath(ring.path, color = fill)
                                drawPath(ring.path, color = COLOR_BORDER, style = Stroke(width = 1.8f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showHint = true },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor)
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Podpowiedź", color = accentColor, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    if (selectedCountry == question.countryKey) onCorrect()
                    else onWrong()
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                enabled = selectedCountry != null
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Sprawdź", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
