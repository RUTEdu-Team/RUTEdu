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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import org.jetbrains.compose.resources.stringResource
import prz.rutedu.app.geo.CountryFeature
import prz.rutedu.app.models.MapRegion
import prz.rutedu.app.models.Question
import prz.rutedu.app.theme.isAppInDarkTheme
import rutedu.composeapp.generated.resources.Res
import rutedu.composeapp.generated.resources.button_check
import rutedu.composeapp.generated.resources.button_hint

/** Ocean / background fill. */
private val POINT_MAP_COLOR_OCEAN = Color(0xFF9DC3D4)
/** Province polygon fill. */
private val POINT_MAP_COLOR_PROVINCE = Color(0xFFE0E0E0)
/** Province border stroke. */
private val POINT_MAP_COLOR_BORDER = Color(0xFFBDBDBD)
/** Default city marker color. */
private val POINT_MAP_COLOR_DOT = Color(0xFF424242)
/** Selected marker (before checking) color. */
private val POINT_MAP_COLOR_SELECTED = Color(0xFFF4A430)
/** Wrong selected marker color. */
private val POINT_MAP_COLOR_WRONG = Color(0xFFE53935)
/** How far the map may be dragged past the canvas edge (px). */
private const val POINT_MAP_PAN_OVERSCROLL_PX = 40f

/**
 * Pre-projected polygon ring in canvas coordinates.
 *
 * [offsets] are used for optional hit-testing/debugging and [path] for rendering.
 */
private data class PointMapRing(val offsets: List<Offset>, val path: Path)

/**
 * Letterboxed map rectangle in canvas pixels (before pan/zoom transform).
 *
 * Used by [pointMapClampPan] to keep the map at least partially visible.
 */
private data class PointMapLayout(
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
 * Screen-space representation of one [CountryFeature]:
 * - [rings] for background province polygons
 * - [point] for selectable capital-city markers
 */
private data class PointMapFeature(
    val feature: CountryFeature,
    val rings: List<PointMapRing> = emptyList(),
    val point: Offset? = null,
)

/**
 * Returns `true` when this feature has geometry near [r], with a small margin.
 *
 * Supports both polygon features and point features.
 */
private fun CountryFeature.hasGeometryNearPointMap(r: MapRegion): Boolean {
    val margin = 5f
    point?.let {
        return it.lon in (r.lonMin - margin)..(r.lonMax + margin) &&
            it.lat in (r.latMin - margin)..(r.latMax + margin)
    }
    return rings.any { ring ->
        if (ring.isEmpty()) return@any false
        val avgLon = ring.map { it.lon }.average().toFloat()
        val avgLat = ring.map { it.lat }.average().toFloat()
        avgLon in (r.lonMin - margin)..(r.lonMax + margin) &&
            avgLat in (r.latMin - margin)..(r.latMax + margin)
    }
}

/**
 * Computes the letterboxed map rectangle for [r] inside the current canvas.
 */
private fun pointMapLayout(canvasW: Float, canvasH: Float, r: MapRegion): PointMapLayout {
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
    return PointMapLayout(
        originX = (canvasW - mapW) / 2f,
        originY = (canvasH - mapH) / 2f,
        mapW = mapW,
        mapH = mapH,
        canvasW = canvasW,
        canvasH = canvasH,
    )
}

/**
 * Clamps [pan] so the zoomed map cannot be dragged completely off-screen.
 */
private fun pointMapClampPan(pan: Offset, zoom: Float, layout: PointMapLayout): Offset {
    val cx = layout.centerX
    val cy = layout.centerY
    val mapLeft = (layout.originX - cx) * zoom + cx
    val mapRight = (layout.originX + layout.mapW - cx) * zoom + cx
    val mapTop = (layout.originY - cy) * zoom + cy
    val mapBottom = (layout.originY + layout.mapH - cy) * zoom + cy

    val minPanX = -POINT_MAP_PAN_OVERSCROLL_PX - mapRight
    val maxPanX = layout.canvasW + POINT_MAP_PAN_OVERSCROLL_PX - mapLeft
    val minPanY = -POINT_MAP_PAN_OVERSCROLL_PX - mapBottom
    val maxPanY = layout.canvasH + POINT_MAP_PAN_OVERSCROLL_PX - mapTop

    return Offset(
        x = pan.x.coerceIn(minPanX, maxPanX),
        y = pan.y.coerceIn(minPanY, maxPanY),
    )
}

/**
 * Projects polygons and points from geographic coordinates into canvas coordinates.
 */
private fun pointMapBuildFeatures(
    features: List<CountryFeature>,
    canvasW: Float,
    canvasH: Float,
    region: MapRegion,
): List<PointMapFeature> {
    if (canvasW <= 0f || canvasH <= 0f) return emptyList()

    val layout = pointMapLayout(canvasW, canvasH, region)
    val midLatRad = ((region.latMin + region.latMax) / 2f) * (PI / 180.0).toFloat()
    val lonCorrection = cos(midLatRad)
    val lonRange = region.lonMax - region.lonMin
    val latRange = region.latMax - region.latMin

    fun project(lon: Float, lat: Float): Offset = Offset(
        x = layout.originX + (lon - region.lonMin) * lonCorrection / (lonRange * lonCorrection) * layout.mapW,
        y = layout.originY + (region.latMax - lat) / latRange * layout.mapH,
    )

    return features.map { feature ->
        val screenRings = feature.rings.map { ring ->
            val offsets = ring.map { ll -> project(ll.lon, ll.lat) }
            val path = Path().apply {
                if (offsets.isEmpty()) return@apply
                moveTo(offsets[0].x, offsets[0].y)
                for (k in 1 until offsets.size) lineTo(offsets[k].x, offsets[k].y)
                close()
            }
            PointMapRing(offsets, path)
        }
        val screenPoint = feature.point?.let { project(it.lon, it.lat) }
        PointMapFeature(feature = feature, rings = screenRings, point = screenPoint)
    }
}

/**
 * Interactive point-map quiz used for province-capital questions.
 *
 * ## Data flow
 * 1. GeoJSON is loaded via [prz.rutedu.app.geo.loadGeoJson] ([Question.PointMapQuiz.mapFile]).
 * 2. [pointMapBuildFeatures] projects nearby polygons and city points into canvas space.
 * 3. Tap coordinates are inverted from pan/zoom and snapped to the closest marker.
 * 4. "Sprawdź" compares the selected city name against [Question.PointMapQuiz.targetNames].
 *
 * ## Behavior
 * - Wrong answer does **not** lock the question: the student can immediately select
 *   another point and check again (same UX as other geography modules).
 *
 * @param question      Point-map question with target city names and map region.
 * @param accentColor   Subject accent color for buttons and UI accents.
 * @param bottomPadding System navigation bar height padding from `App`.
 * @param onCorrect     Called when selected point matches one of [question.targetNames].
 * @param onWrong       Called when selected point is incorrect.
 */
@Composable
internal fun PointMapQuizContent(
    question: Question.PointMapQuiz,
    accentColor: Color,
    bottomPadding: Dp,
    onCorrect: () -> Unit,
    onWrong: () -> Unit = {},
) {
    var countries by remember { mutableStateOf<List<CountryFeature>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedName by remember(question.id) { mutableStateOf<String?>(null) }
    var isWrong by remember(question.id) { mutableStateOf(false) }
    var showHint by remember(question.id) { mutableStateOf(false) }
    var panOffset by remember(question.id) { mutableStateOf(Offset.Zero) }
    var zoomScale by remember(question.id) { mutableStateOf(1f) }
    var canvasSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val density = LocalDensity.current
    val hitRadiusPx = with(density) { 20.dp.toPx() }

    val isDark = isAppInDarkTheme()
    val oceanColor = if (isDark) Color(0xFF1E262B) else POINT_MAP_COLOR_OCEAN
    val provinceColor = if (isDark) Color(0xFF2C2C2C) else POINT_MAP_COLOR_PROVINCE
    val borderColor = if (isDark) Color(0xFF424242) else POINT_MAP_COLOR_BORDER
    val dotColor = if (isDark) Color(0xFFBDBDBD) else POINT_MAP_COLOR_DOT

    val region = question.region

    val mapLayout = remember(canvasSize, region) {
        val cs = canvasSize ?: return@remember null
        pointMapLayout(cs.first.toFloat(), cs.second.toFloat(), region)
    }

    val screenFeatures = remember(countries, canvasSize, region) {
        val cs = canvasSize ?: return@remember emptyList()
        pointMapBuildFeatures(
            countries.filter { it.hasGeometryNearPointMap(region) },
            cs.first.toFloat(),
            cs.second.toFloat(),
            region,
        )
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val layout = mapLayout ?: return@rememberTransformableState
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 8f)
        panOffset = pointMapClampPan(panOffset + panChange, zoomScale, layout)
    }

    LaunchedEffect(zoomScale, mapLayout) {
        val layout = mapLayout ?: return@LaunchedEffect
        panOffset = pointMapClampPan(panOffset, zoomScale, layout)
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
            onDismiss = { showHint = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Text(
                text = question.questionText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(oceanColor),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { cs -> canvasSize = Pair(cs.width, cs.height) }
                        .pointerInput(screenFeatures, question.id) {
                            detectTapGestures { tap ->
                                val cs = canvasSize ?: return@detectTapGestures
                                val w = cs.first.toFloat()
                                val h = cs.second.toFloat()
                                val cx = w / 2f
                                val cy = h / 2f
                                val base = Offset(
                                    x = (tap.x - panOffset.x - cx) / zoomScale + cx,
                                    y = (tap.y - panOffset.y - cy) / zoomScale + cy,
                                )

                                val hit = screenFeatures
                                    .asSequence()
                                    .filter { it.feature.selectable && it.point != null }
                                    .mapNotNull { sf ->
                                        val p = sf.point!!
                                        val dist = hypot(p.x - base.x, p.y - base.y)
                                        if (dist < hitRadiusPx / zoomScale.coerceAtLeast(1f)) sf to dist else null
                                    }
                                    .minByOrNull { it.second }
                                    ?.first

                                if (hit != null) {
                                    selectedName = hit.feature.name
                                    isWrong = false
                                }
                            }
                        }
                        .transformable(state = transformableState),
                ) {
                    drawRect(color = oceanColor)

                    withTransform(
                        transformBlock = {
                            translate(panOffset.x, panOffset.y)
                            scale(zoomScale, zoomScale, center)
                        },
                    ) {
                        screenFeatures.filter { it.feature.rings.isNotEmpty() }.forEach { sf ->
                            sf.rings.forEach { ring ->
                                drawPath(ring.path, color = provinceColor)
                                drawPath(ring.path, color = borderColor, style = Stroke(width = 0.8f))
                            }
                        }

                        screenFeatures.filter { it.feature.selectable && it.point != null }.forEach { sf ->
                            val center = sf.point!!
                            val isSelected = sf.feature.name == selectedName
                            val color = when {
                                isSelected && isWrong -> POINT_MAP_COLOR_WRONG
                                isSelected -> POINT_MAP_COLOR_SELECTED
                                else -> dotColor
                            }
                            val radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()
                            drawCircle(color = color, radius = radius, center = center)
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 1.dp.toPx()),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { showHint = true },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor),
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.button_hint), color = accentColor, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    if (question.targetNames.contains(selectedName)) {
                        onCorrect()
                    } else {
                        isWrong = true
                        onWrong()
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                enabled = selectedName != null,
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.button_check), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}