package com.example.myapplication.multiplayer

import android.os.Build

actual fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val model = Build.MODEL
    return if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
}
