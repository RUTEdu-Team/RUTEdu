package com.example.myapplication.data

import app.cash.sqldelight.db.SqlDriver
import kotlin.random.Random

object ChemistrySessionStore {

    private fun seedKey(lessonId: String) = "chseed_$lessonId"
    private fun answeredKey(lessonId: String) = "chansw_$lessonId"

    fun getOrCreateSeed(driver: SqlDriver, lessonId: String): Long {
        val existing = loadString(driver, seedKey(lessonId))
        if (existing != null) return existing.toLongOrNull() ?: createSeed(driver, lessonId)
        return createSeed(driver, lessonId)
    }

    private fun createSeed(driver: SqlDriver, lessonId: String): Long {
        val seed = Random.nextLong()
        saveString(driver, seedKey(lessonId), seed.toString())
        return seed
    }

    fun getAnsweredIds(driver: SqlDriver, lessonId: String): Set<Int> {
        val raw = loadString(driver, answeredKey(lessonId)) ?: return emptySet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun markAnswered(driver: SqlDriver, lessonId: String, questionId: Int) {
        val current = getAnsweredIds(driver, lessonId)
        val updated = (current + questionId).joinToString(",")
        saveString(driver, answeredKey(lessonId), updated)
    }

    private fun loadString(driver: SqlDriver, key: String): String? =
        driver.executeQuery(
            identifier = null,
            sql = "SELECT setting_value FROM appSettings WHERE setting_key = ?",
            mapper = { cursor ->
                app.cash.sqldelight.db.QueryResult.Value(
                    if (cursor.next().value) cursor.getString(0) else null
                )
            },
            parameters = 1,
            binders = { bindString(0, key) }
        ).value

    private fun saveString(driver: SqlDriver, key: String, value: String) {
        driver.execute(
            identifier = null,
            sql = "INSERT OR REPLACE INTO appSettings (setting_key, setting_value) VALUES (?, ?)",
            parameters = 2,
            binders = {
                bindString(0, key)
                bindString(1, value)
            }
        )
    }
}
