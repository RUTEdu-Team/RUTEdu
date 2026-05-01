package com.example.myapplication.multiplayer.network

import com.example.myapplication.multiplayer.model.DiscoveredHost
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.write
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// iOS: UDP broadcast/listen only (NSD discovery is Android-specific).
actual class PvPDiscovery actual constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val selectorManager = SelectorManager(Dispatchers.Default)
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null

    private data class AdvertiseParams(
        val deviceName: String, val port: Int, val mode: String,
        val currentPlayers: Int, val maxPlayers: Int, val hasPassword: Boolean
    )
    private var lastParams: AdvertiseParams? = null

    actual fun advertise(deviceName: String, port: Int, mode: String, currentPlayers: Int, maxPlayers: Int, hasPassword: Boolean) {
        lastParams = AdvertiseParams(deviceName, port, mode, currentPlayers, maxPlayers, hasPassword)
        startBroadcast(deviceName, port, mode, currentPlayers, maxPlayers, hasPassword)
    }

    actual fun updatePlayerCount(currentPlayers: Int) {
        val p = lastParams?.copy(currentPlayers = currentPlayers) ?: return
        lastParams = p
        stopAdvertising()
        startBroadcast(p.deviceName, p.port, p.mode, p.currentPlayers, p.maxPlayers, p.hasPassword)
    }

    actual fun stopAdvertising() {
        broadcastJob?.cancel()
        broadcastJob = null
    }

    actual fun browse(onFound: (DiscoveredHost) -> Unit, onLost: (String) -> Unit) {
        listenJob?.cancel()
        listenJob = scope.launch {
            val udpSocket = try {
                aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", DISCOVERY_PORT))
            } catch (_: Exception) { return@launch }
            try {
                while (isActive) {
                    try {
                        val datagram = udpSocket.receive()
                        val senderIp = (datagram.address as? InetSocketAddress)?.hostname ?: continue
                        val text = datagram.packet.readByteArray().decodeToString()
                        val host = Json.decodeFromString<DiscoveredHost>(text).copy(address = senderIp)
                        onFound(host)
                    } catch (_: Exception) {}
                }
            } finally {
                try { udpSocket.close() } catch (_: Exception) {}
            }
        }
    }

    actual fun stopBrowsing() {
        listenJob?.cancel()
        listenJob = null
    }

    actual fun close() {
        stopAdvertising()
        stopBrowsing()
        scope.cancel()
        selectorManager.close()
    }

    private fun startBroadcast(deviceName: String, port: Int, mode: String, cp: Int, mp: Int, hasPassword: Boolean) {
        broadcastJob?.cancel()
        broadcastJob = scope.launch {
            val udpSocket = try {
                aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 0)) { broadcast = true }
            } catch (_: Exception) { return@launch }
            try {
                val json = Json.encodeToString(DiscoveredHost(deviceName, "", port, mode, cp, mp, hasPassword))
                while (isActive) {
                    try {
                        val buf = Buffer().also { it.write(json.encodeToByteArray()) }
                        udpSocket.send(Datagram(buf, InetSocketAddress("255.255.255.255", DISCOVERY_PORT)))
                    } catch (_: Exception) {}
                    delay(2000)
                }
            } finally {
                try { udpSocket.close() } catch (_: Exception) {}
            }
        }
    }
}
