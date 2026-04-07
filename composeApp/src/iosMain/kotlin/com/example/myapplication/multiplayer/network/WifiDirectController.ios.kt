package com.example.myapplication.multiplayer.network

actual val isWifiDirectSupported: Boolean = false

actual class WifiDirectController actual constructor() {

    actual fun createGroup(onReady: () -> Unit, onError: (String) -> Unit) {
        onError("WiFi Direct nie jest obsługiwane na iOS")
    }

    actual fun discoverPeers(onFound: (List<WifiPeerDevice>) -> Unit, onError: (String) -> Unit) {
        onError("WiFi Direct nie jest obsługiwane na iOS")
    }

    actual fun connectToPeer(
        peer: WifiPeerDevice,
        onConnected: (groupOwnerIp: String) -> Unit,
        onError: (String) -> Unit
    ) {
        onError("WiFi Direct nie jest obsługiwane na iOS")
    }

    actual fun stop() {}
}
