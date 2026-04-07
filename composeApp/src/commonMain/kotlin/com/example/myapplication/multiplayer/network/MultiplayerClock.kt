package com.example.myapplication.multiplayer.network

import kotlin.time.TimeSource

private val timeMark = TimeSource.Monotonic.markNow()

fun currentTimeMs(): Long = timeMark.elapsedNow().inWholeMilliseconds
