package com.example.myapplication.multiplayer.network

data class WifiPeerDevice(val name: String, val address: String)

// IP always assigned to Group Owner in WiFi Direct
const val WIFI_DIRECT_HOST_IP = "192.168.49.1"

expect val isWifiDirectSupported: Boolean

expect class WifiDirectController() {
    fun createGroup(onReady: () -> Unit, onError: (String) -> Unit)
    fun discoverPeers(onFound: (List<WifiPeerDevice>) -> Unit, onError: (String) -> Unit)
    fun connectToPeer(peer: WifiPeerDevice, onConnected: (groupOwnerIp: String) -> Unit, onError: (String) -> Unit)
    fun stop()
}
