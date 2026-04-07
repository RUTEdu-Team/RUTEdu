package com.example.myapplication.multiplayer

import platform.UIKit.UIDevice

actual fun getDeviceName(): String = UIDevice.currentDevice.name
