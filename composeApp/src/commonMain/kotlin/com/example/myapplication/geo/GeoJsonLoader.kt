package com.example.myapplication.geo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import rutedu.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

data class CountryFeature(
    val name: String,
    val iso2: String,
    // Each entry is one ring (outer contour) with lon/lat pairs
    val rings: List<List<LonLat>>
)

data class LonLat(val lon: Float, val lat: Float)

private var cachedCountries: List<CountryFeature>? = null

// Simplify a ring by removing points closer than epsilon degrees
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
    if (result.last() != last()) result.add(last()) // keep last point to close ring
    return result
}

private fun parseRing(ringArray: kotlinx.serialization.json.JsonArray): List<LonLat> {
    return ringArray.mapNotNull { ptElem ->
        val pt = ptElem.jsonArray
        if (pt.size >= 2) LonLat(pt[0].jsonPrimitive.float, pt[1].jsonPrimitive.float) else null
    }
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadCountries(): List<CountryFeature> {
    cachedCountries?.let { return it }

    return withContext(Dispatchers.Default) {
        loadCountriesInternal()
    }.also { cachedCountries = it }
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadCountriesInternal(): List<CountryFeature> {
    val bytes = Res.readBytes("files/countries.geojson")
    val jsonStr = bytes.decodeToString()
    val root = Json.parseToJsonElement(jsonStr).jsonObject
    val features = root["features"]!!.jsonArray

    val result = mutableListOf<CountryFeature>()
    for (featureElem in features) {
        val featureObj = featureElem.jsonObject
        val props = featureObj["properties"]!!.jsonObject
        val name = props["name"]?.jsonPrimitive?.content ?: continue
        val iso2 = props["ISO3166-1-Alpha-2"]?.jsonPrimitive?.content ?: ""
        val geomObj = featureObj["geometry"]?.jsonObject ?: continue
        val geomType = geomObj["type"]?.jsonPrimitive?.content ?: continue
        val coordsArr = geomObj["coordinates"]?.jsonArray ?: continue

        val rings = mutableListOf<List<LonLat>>()

        when (geomType) {
            "Polygon" -> {
                // coordsArr = [ outerRing, hole1, hole2, ... ]
                val outerRing = parseRing(coordsArr[0].jsonArray).simplify()
                if (outerRing.size >= 3) rings.add(outerRing)
            }
            "MultiPolygon" -> {
                // coordsArr = [ polygon1, polygon2, ... ]
                // polygon = [ outerRing, hole1, ... ]
                for (polyElem in coordsArr) {
                    val polyArr = polyElem.jsonArray
                    if (polyArr.isEmpty()) continue
                    val outerRing = parseRing(polyArr[0].jsonArray).simplify()
                    if (outerRing.size >= 3) rings.add(outerRing)
                }
            }
            else -> continue
        }

        if (rings.isEmpty()) continue

        result.add(CountryFeature(name, iso2, rings))
    }

    return result
}
