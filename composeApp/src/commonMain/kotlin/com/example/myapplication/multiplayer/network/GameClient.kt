package com.example.myapplication.multiplayer.network

import com.example.myapplication.multiplayer.model.DiscoveredHost
import com.example.myapplication.multiplayer.model.GameMessage
import com.example.myapplication.multiplayer.model.gameJson
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.io.readByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class GameClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val selectorManager = SelectorManager(Dispatchers.Default)
    private var socket: Socket? = null
    private var writeChannel: ByteWriteChannel? = null
    private var discoveryJob: Job? = null

    var onHostDiscovered: ((DiscoveredHost) -> Unit)? = null
    var onMessageReceived: ((GameMessage) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    fun startDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = scope.launch {
            try {
                val udpSocket = aSocket(selectorManager).udp()
                    .bind(InetSocketAddress("0.0.0.0", DISCOVERY_PORT))
                while (isActive) {
                    try {
                        val datagram = udpSocket.receive()
                        val senderAddress = (datagram.address as? InetSocketAddress)?.hostname
                            ?: continue
                        val text = datagram.packet.readByteArray().decodeToString()
                        val host = Json.decodeFromString<DiscoveredHost>(text)
                        onHostDiscovered?.invoke(host.copy(address = senderAddress))
                    } catch (_: Exception) {}
                }
                udpSocket.close()
            } catch (_: Exception) {}
        }
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    suspend fun connect(host: DiscoveredHost, nickname: String) {
        val tcpSocket = aSocket(selectorManager).tcp()
            .connect(InetSocketAddress(host.address, host.port))
        socket = tcpSocket
        writeChannel = tcpSocket.openWriteChannel(autoFlush = true)

        send(GameMessage.Join(nickname))

        scope.launch {
            val readChannel = tcpSocket.openReadChannel()
            try {
                while (isActive) {
                    val line = readChannel.readUTF8Line() ?: break
                    val message = try {
                        gameJson.decodeFromString<GameMessage>(line)
                    } catch (_: Exception) { continue }
                    onMessageReceived?.invoke(message)
                }
            } catch (_: Exception) {}
            onDisconnected?.invoke()
        }
    }

    suspend fun send(message: GameMessage) {
        val json = gameJson.encodeToString<GameMessage>(message) + "\n"
        try { writeChannel?.writeStringUtf8(json) } catch (_: Exception) {}
    }

    fun stop() {
        stopDiscovery()
        scope.cancel()
        socket?.dispose()
        selectorManager.close()
    }
}
