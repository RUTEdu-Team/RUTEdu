package prz.rutedu.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.hypot
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import prz.rutedu.app.geo.CountryFeature
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme
import org.jetbrains.compose.resources.stringResource
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.*
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

/** Resource path to `files/polish_national_parks.geojson`. */
private const val NATIONAL_PARKS_MAP_FILE = "files/polish_national_parks.geojson"
/** Tap target radius for national-park markers (canvas coords before zoom). */
private const val PARK_MARKER_RADIUS = 18f
/** Max distance to snap a tap to the nearest park marker. */
private const val PARK_MARKER_SNAP_RADIUS = 36f
/** How far the map may be dragged past the canvas edge (px). */
private const val MAP_PAN_OVERSCROLL_PX = 40f

/** @return `true` when [mapFile] is [NATIONAL_PARKS_MAP_FILE]. */
private fun isNationalParksMap(mapFile: String) = mapFile == NATIONAL_PARKS_MAP_FILE

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
    return centerLon in (r.lonMin - margin)..(r.lonMax + margin) &&
            centerLat in (r.latMin - margin)..(r.latMax + margin)
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
 * Letterboxed map rectangle in canvas pixels (before pan/zoom transform).
 *
 * Used by [clampPanOffset] to keep the projected map from being panned entirely off-screen.
 */
private data class MapScreenLayout(
    val originX: Float,
    val originY: Float,
    val mapW: Float,
    val mapH: Float,
    val canvasW: Float,
    val canvasH: Float,
) {
    val centerX get() = canvasW / 2f
    val centerY get() = canvasH / 2f
}

/**
 * Pairing of the original [CountryFeature] with its per-ring screen projections.
 *
 * When [isNationalParksMap] applies, selectable features include [markerCenter] for dot rendering.
 *
 * @property feature       Source feature from GeoJSON ([CountryFeature.name] matches [Question.MapQuiz.countryKey]).
 * @property rings         Per-ring paths in canvas space (polygons).
 * @property markerCenter  Centroid for park dot mode; `null` for standard polygon quizzes.
 */
private data class ScreenCountry(
    val feature: CountryFeature,
    val rings: List<ScreenRing>,
    val markerCenter: Offset? = null,
)

/**
 * Clamps [pan] so the zoomed map rectangle stays at least partially visible on the canvas.
 *
 * Screen position of the map edges (before [pan]): `edge + pan`. When the map is larger than
 * the canvas, [pan] may span a wide range so the user can reach every part of the map.
 *
 * @param pan    Current pan offset applied after `Modifier.transformable`.
 * @param zoom   Current uniform zoom scale around canvas center.
 * @param layout Letterboxed map bounds from [computeMapLayout].
 */
private fun clampPanOffset(pan: Offset, zoom: Float, layout: MapScreenLayout): Offset {
    val cx = layout.centerX
    val cy = layout.centerY
    val mapLeft = (layout.originX - cx) * zoom + cx
    val mapRight = (layout.originX + layout.mapW - cx) * zoom + cx
    val mapTop = (layout.originY - cy) * zoom + cy
    val mapBottom = (layout.originY + layout.mapH - cy) * zoom + cy
    val overscroll = MAP_PAN_OVERSCROLL_PX

    // mapRight + pan.x >= -overscroll  and  mapLeft + pan.x <= canvasW + overscroll
    val minPanX = -overscroll - mapRight
    val maxPanX = layout.canvasW + overscroll - mapLeft
    val minPanY = -overscroll - mapBottom
    val maxPanY = layout.canvasH + overscroll - mapTop

    return Offset(
        x = pan.x.coerceIn(minPanX, maxPanX),
        y = pan.y.coerceIn(minPanY, maxPanY),
    )
}

/**
 * Computes the letterboxed map rectangle for [r] inside a canvas of the given size.
 * Shared by [buildScreenCountries] and [clampPanOffset].
 */
private fun computeMapLayout(canvasW: Float, canvasH: Float, r: MapRegion): MapScreenLayout {
    val midLatRad = ((r.latMin + r.latMax) / 2f) * (PI / 180.0).toFloat()
    val lonCorrection = cos(midLatRad)
    val lonRange = r.lonMax - r.lonMin
    val latRange = r.latMax - r.latMin
    val naturalAR = (lonRange * lonCorrection) / latRange
    val (mapW, mapH) = if (canvasW / canvasH > naturalAR) {
        Pair(canvasH * naturalAR, canvasH)
    } else {
        Pair(canvasW, canvasW / naturalAR)
    }
    return MapScreenLayout(
        originX = (canvasW - mapW) / 2f,
        originY = (canvasH - mapH) / 2f,
        mapW = mapW,
        mapH = mapH,
        canvasW = canvasW,
        canvasH = canvasH,
    )
}

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
 * to preserve the natural aspect ratio.
 *
 * @param features       Features to project (pre-filtered by [hasRingNear]).
 * @param canvasW        Canvas width in pixels.
 * @param canvasH        Canvas height in pixels.
 * @param r              Geographic bounding box for the visible map area.
 * @param useParkMarkers When `true`, selectable features get [ScreenCountry.markerCenter] dots.
 * @return List of [ScreenCountry] objects ready for drawing and hit-testing.
 */
private fun buildScreenCountries(
    features: List<CountryFeature>,
    canvasW: Float,
    canvasH: Float,
    r: MapRegion,
    useParkMarkers: Boolean,
): List<ScreenCountry> {
    val layout = computeMapLayout(canvasW, canvasH, r)
    val originX = layout.originX
    val originY = layout.originY
    val mapW = layout.mapW
    val mapH = layout.mapH
    val midLatRad = ((r.latMin + r.latMax) / 2f) * (PI / 180.0).toFloat()
    val lonCorrection = cos(midLatRad)
    val lonRange = r.lonMax - r.lonMin
    val latRange = r.latMax - r.latMin

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
        val markerCenter = if (useParkMarkers && feature.selectable && screenRings.isNotEmpty()) {
            val allPts = screenRings.flatMap { it.offsets }
            if (allPts.isEmpty()) null
            else Offset(
                x = allPts.map { it.x }.average().toFloat(),
                y = allPts.map { it.y }.average().toFloat(),
            )
        } else null
        ScreenCountry(feature, screenRings, markerCenter)
    }
}

/**
 * Returns `true` if [tap] hits [country] — polygon ray-cast or circular marker (park mode).
 */
private fun hitTest(tap: Offset, country: ScreenCountry): Boolean {
    country.markerCenter?.let { center ->
        return hypot(tap.x - center.x, tap.y - center.y) <= PARK_MARKER_SNAP_RADIUS
    }
    return country.rings.any { ring -> pointInPolygon(tap.x, tap.y, ring.offsets) }
}

/**
 * When [isNationalParksMap] is active, returns the closest park marker within [PARK_MARKER_SNAP_RADIUS].
 */
private fun nearestParkMarker(tap: Offset, countries: List<ScreenCountry>): ScreenCountry? =
    countries
        .asSequence()
        .filter { it.feature.selectable && it.markerCenter != null }
        .mapNotNull { sc ->
            val c = sc.markerCenter!!
            val d = hypot(tap.x - c.x, tap.y - c.y)
            if (d <= PARK_MARKER_SNAP_RADIUS) sc to d else null
        }
        .minByOrNull { it.second }
        ?.first

/**
 * Interactive map quiz where the student taps a region on a zoomable map.
 *
 * ## Data flow
 * 1. GeoJSON is loaded via [prz.rutedu.app.geo.loadGeoJson] ([Question.MapQuiz.mapFile]) and cached.
 * 2. [buildScreenCountries] projects features near [Question.MapQuiz.region] into canvas space.
 * 3. Tap coordinates are inverted from pan/zoom, then matched via [hitTest] or [nearestParkMarker].
 * 4. "Sprawdź" compares the selection to [Question.MapQuiz.countryKey].
 *
 * ## National parks ([NATIONAL_PARKS_MAP_FILE])
 * - Non-selectable background features (e.g. Poland outline) use [CountryFeature.selectable].
 * - Selectable parks render as dots; [nearestParkMarker] snaps within [PARK_MARKER_SNAP_RADIUS].
 *
 * ## Zoom / pan
 * - Pan and pinch-zoom via `Modifier.transformable` (outermost in the modifier chain).
 * - [clampPanOffset] keeps the map at least partially on screen.
 * - Tap hit-test uses base coordinates: `base = (tap - pan - pivot) / scale + pivot`.
 *
 * ## State reset
 * Per-question state is keyed on `question.id` via `remember(question.id)`.
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

    val isDark = isAppInDarkTheme()
    val oceanColor = if (isDark) Color(0xFF1E262B) else COLOR_OCEAN
    val countryColor = if (isDark) Color(0xFF384A3E) else COLOR_COUNTRY
    val borderColor = if (isDark) Color(0xFF1E2022) else COLOR_BORDER
    val selectedColor = if (isDark) Color(0xFFE08D1E) else COLOR_SELECTED

    val region = question.region
    val useParkMarkers = isNationalParksMap(question.mapFile)

    val mapLayout = remember(canvasSize, region) {
        val cs = canvasSize ?: return@remember null
        computeMapLayout(cs.first.toFloat(), cs.second.toFloat(), region)
    }

    val screenCountries = remember(countries, canvasSize, region, useParkMarkers) {
        val cs = canvasSize ?: return@remember emptyList()
        buildScreenCountries(
            countries.filter { it.hasRingNear(region) },
            cs.first.toFloat(),
            cs.second.toFloat(),
            region,
            useParkMarkers,
        )
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val layout = mapLayout ?: return@rememberTransformableState
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 8f)
        panOffset = clampPanOffset(panOffset + panChange, zoomScale, layout)
    }

    LaunchedEffect(zoomScale, mapLayout) {
        val layout = mapLayout ?: return@LaunchedEffect
        panOffset = clampPanOffset(panOffset, zoomScale, layout)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = question.questionText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
                    modifier = Modifier.fillMaxSize().background(oceanColor),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { cs -> canvasSize = Pair(cs.width, cs.height) }
                        .pointerInput(screenCountries, question.id) {
                            detectTapGestures { tap ->
                                val cs = canvasSize ?: return@detectTapGestures
                                val w = cs.first.toFloat()
                                val h = cs.second.toFloat()
                                val cx = w / 2f
                                val cy = h / 2f
                                // Invert pan/zoom: base = (visual - pan - pivot) / scale + pivot
                                val base = Offset(
                                    x = (tap.x - panOffset.x - cx) / zoomScale + cx,
                                    y = (tap.y - panOffset.y - cy) / zoomScale + cy
                                )
                                val hit = if (useParkMarkers) {
                                    nearestParkMarker(base, screenCountries)
                                } else {
                                    screenCountries
                                        .asReversed()
                                        .firstOrNull { it.feature.selectable && hitTest(base, it) }
                                }
                                if (hit != null) {
                                    selectedCountry = hit.feature.name
                                    isWrong = false
                                }
                            }
                        }
                        .transformable(state = transformableState)
                ) {
                    drawRect(color = oceanColor)

                    withTransform(
                        transformBlock = {
                            translate(panOffset.x, panOffset.y)
                            scale(zoomScale, zoomScale, center)
                        }
                    ) {
                        fun drawPolygons(sc: ScreenCountry, fill: Color, strokeW: Float) {
                            sc.rings.forEach { ring ->
                                drawPath(ring.path, color = fill)
                                drawPath(ring.path, color = borderColor, style = Stroke(width = strokeW))
                            }
                        }

                        fun drawMarker(center: Offset, fill: Color, radius: Float, strokeW: Float) {
                            drawCircle(color = fill, radius = radius, center = center)
                            drawCircle(
                                color = borderColor,
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeW),
                            )
                        }

                        screenCountries.forEach { sc ->
                            if (sc.feature.name == selectedCountry) return@forEach
                            if (sc.markerCenter != null) {
                                drawMarker(sc.markerCenter, countryColor, PARK_MARKER_RADIUS, 1.2f)
                            } else {
                                drawPolygons(sc, countryColor, 1.2f)
                            }
                        }
                        screenCountries.firstOrNull { it.feature.name == selectedCountry }?.let { sc ->
                            val fill = if (isWrong) COLOR_WRONG else selectedColor
                            if (sc.markerCenter != null) {
                                drawMarker(sc.markerCenter, fill, PARK_MARKER_RADIUS + 3f, 1.8f)
                            } else {
                                drawPolygons(sc, fill, 1.8f)
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
                Text(stringResource(Res.string.button_hint), color = accentColor, fontWeight = FontWeight.SemiBold)
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
                Text(stringResource(Res.string.button_check), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
