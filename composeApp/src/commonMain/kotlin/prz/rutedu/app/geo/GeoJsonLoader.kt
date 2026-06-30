package prz.rutedu.app.geo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import rutedu.composeapp.generated.resources.Res

/**
 * Parsed representation of one country from the GeoJSON asset.
 *
 * A country may consist of multiple disjoint polygons (e.g. an archipelago), hence
 * [rings] is a list of rings rather than a single polygon. Each ring is the outer
 * contour of one polygon - holes are discarded because the quiz only needs a fill
 * and hit-test surface.
 *
 * @property name  English country name as stored in the GeoJSON `"name"` property.
 *                 This is the value compared against [prz.rutedu.app.models.Question.MapQuiz.countryKey].
 * @property iso2  ISO 3166-1 alpha-2 country code (e.g. `"PL"` for Poland). Not used
 *                 in the current quiz logic but kept for potential future features.
 * @property rings Outer contour rings in geographic coordinates (longitude, latitude pairs).
 *                 Each ring has already been simplified by [simplify].
 * @property point   Geographic coordinate for Point geometries (e.g. province capitals).
 * @property selectable When `false`, the feature is drawn but excluded from tap hit-testing
 *                      (e.g. a country outline behind national parks). Set via GeoJSON
 *                      `"rutedu_selectable"` or inferred for country borders.
 * @property centerLon Pre-calculated centroid longitude for O(1) visibility filtering.
 * @property centerLat Pre-calculated centroid latitude for O(1) visibility filtering.
 */
data class CountryFeature(
    val name: String,
    val iso2: String,
    val rings: List<List<LonLat>> = emptyList(),
    val point: LonLat? = null,
    val selectable: Boolean = true,
    val centerLon: Float = 0f,
    val centerLat: Float = 0f,
)

/**
 * A geographic coordinate in decimal degrees.
 *
 * @property lon Longitude (east-west), range −180..180. Positive = East.
 * @property lat Latitude (north-south), range −90..90. Positive = North.
 */
data class LonLat(val lon: Float, val lat: Float)

/**
 * Module-level cache mapping GeoJSON asset paths to their parsed [CountryFeature] lists.
 * Subsequent calls to [loadGeoJson] with the same path return the cached list without
 * re-reading or re-parsing the asset.
 */
private val geoCache = mutableMapOf<String, List<CountryFeature>>()

/**
 * Removes points from a polygon ring that are closer than [epsilon] degrees to the
 * previous kept point. This reduces the number of draw calls at the cost of a small
 * loss in polygon fidelity - acceptable for a quiz map where pixel-perfect borders
 * are not required.
 *
 * The first and last points of the ring are always kept to preserve closure.
 *
 * @receiver The original ring as a list of [LonLat] points.
 * @param epsilon  Minimum distance (in degrees) between consecutive kept points.
 *                 Default 0.025° ~ 2.8 km at the equator.
 * @return A simplified ring with redundant intermediate points removed.
 */
private fun List<LonLat>.simplify(epsilon: Float = 0.025f): List<LonLat> {
    if (size <= 3) return this
    val result = mutableListOf(first())
    for (i in 1 until size) {
        val prev = result.last()
        val curr = this[i]
        val dx = curr.lon - prev.lon
        val dy = curr.lat - prev.lat
        if (dx * dx + dy * dy > epsilon * epsilon) result.add(curr)
    }
    // Always keep the last point to ensure the ring closes properly
    if (result.last() != last()) result.add(last())
    return result
}

/**
 * Parses one GeoJSON coordinate ring (an array of [lon, lat] pairs) into a list of [LonLat].
 * Points with fewer than two coordinates are silently skipped.
 */
private fun parseRing(ringArray: kotlinx.serialization.json.JsonArray): List<LonLat> {
    return ringArray.mapNotNull { ptElem ->
        val pt = ptElem.jsonArray
        if (pt.size >= 2) LonLat(pt[0].jsonPrimitive.float, pt[1].jsonPrimitive.float) else null
    }
}

/**
 * Derives [CountryFeature.selectable] from GeoJSON `properties`.
 *
 * Reads `"rutedu_selectable"` when present; otherwise treats country borders
 * (`boundary=administrative`, `admin_level=2`) as non-selectable.
 */
private fun JsonObject.isFeatureSelectable(): Boolean {
    this["rutedu_selectable"]?.jsonPrimitive?.let { prim ->
        return when (prim.content.lowercase()) {
            "false", "0", "no" -> false
            else -> true
        }
    }
    val boundary = this["boundary"]?.jsonPrimitive?.content
    val adminLevel = this["admin_level"]?.jsonPrimitive?.content
    if (boundary == "administrative" && adminLevel == "2") return false
    return true
}

/**
 * Loads and parses a GeoJSON asset from the specified [path], returning a list of
 * [CountryFeature] objects ready for rendering and hit-testing.
 *
 * The function is **suspend** because reading a bundled resource is an IO operation on
 * some platforms. The result is cached in memory under [geoCache] after the first call
 * so repeated invocations for the same path are instant.
 *
 * Runs on [Dispatchers.Default] to avoid freezing the main UI thread during I/O and JSON parsing.
 *
 * @param path The resource path to the GeoJSON file.
 * @return A list of [CountryFeature] objects parsed from the specified file.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun loadGeoJson(path: String): List<CountryFeature> {
    geoCache[path]?.let { return it }

    return withContext(Dispatchers.Default) {
        val result = mutableListOf<CountryFeature>()
        try {
            val bytes = Res.readBytes(path)
            val jsonStr = bytes.decodeToString()
            val root = Json.parseToJsonElement(jsonStr).jsonObject
            val features = root["features"]?.jsonArray ?: return@withContext emptyList<CountryFeature>()

            for (featureElem in features) {
                val featureObj = featureElem.jsonObject
                val props = featureObj["properties"]?.jsonObject ?: continue

                val name = props["name:pl"]?.jsonPrimitive?.content
                    ?: props["name"]?.jsonPrimitive?.content
                    ?: props["JPT_NAZWA_"]?.jsonPrimitive?.content
                    ?: props["NAME"]?.jsonPrimitive?.content
                    ?: props["VARNAME_1"]?.jsonPrimitive?.content
                    ?: continue

                val iso2 = props["ISO3166-1-Alpha-2"]?.jsonPrimitive?.content ?: ""
                val geomObj = featureObj["geometry"]?.jsonObject ?: continue
                val geomType = geomObj["type"]?.jsonPrimitive?.content ?: continue
                val coordsArr = geomObj["coordinates"]?.jsonArray ?: continue

                var point: LonLat? = null
                val rings = mutableListOf<List<LonLat>>()

                when (geomType) {
                    "Point" -> {
                        if (coordsArr.size >= 2) {
                            point = LonLat(coordsArr[0].jsonPrimitive.float, coordsArr[1].jsonPrimitive.float)
                        }
                    }
                    "Polygon" -> {
                        if (coordsArr.isNotEmpty()) {
                            val outerRing = parseRing(coordsArr[0].jsonArray).simplify()
                            if (outerRing.size >= 3) rings.add(outerRing)
                        }
                    }
                    "MultiPolygon" -> {
                        for (polyElem in coordsArr) {
                            val polyArr = polyElem.jsonArray
                            if (polyArr.isNotEmpty()) {
                                val outerRing = parseRing(polyArr[0].jsonArray).simplify()
                                if (outerRing.size >= 3) rings.add(outerRing)
                            }
                        }
                    }
                }

                if (point != null || rings.isNotEmpty()) {
                    val allPts = rings.flatten()
                    val avgLon = if (allPts.isNotEmpty()) allPts.map { it.lon }.average().toFloat() else 0f
                    val avgLat = if (allPts.isNotEmpty()) allPts.map { it.lat }.average().toFloat() else 0f
                    result.add(
                        CountryFeature(
                            name = name,
                            iso2 = iso2,
                            rings = rings,
                            point = point,
                            selectable = props.isFeatureSelectable(),
                            centerLon = point?.lon ?: avgLon,
                            centerLat = point?.lat ?: avgLat
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Error loading GeoJSON from $path: ${e.message}")
            e.printStackTrace()
        }

        if (result.isNotEmpty()) {
            geoCache[path] = result
        }
        result
    }
}