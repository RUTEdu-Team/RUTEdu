package com.example.myapplication.multiplayer.network

import com.example.myapplication.multiplayer.model.DiscoveredHost
import com.example.myapplication.multiplayer.model.GameMessage
import com.example.myapplication.multiplayer.model.gameJson
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.io.Buffer
import kotlinx.io.write
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal const val GAME_PORT = 8888
internal const val DISCOVERY_PORT = 8889

class GameServer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val selectorManager = SelectorManager(Dispatchers.Default)
    private var serverSocket: ServerSocket? = null
    private var broadcastJob: Job? = null

    private data class ClientConn(
        val nickname: String,
        val socket: Socket,
        val writeChannel: ByteWriteChannel
    )

    private val clientsMutex = Mutex()
    private val clients = mutableListOf<ClientConn>()

    var onClientJoined: ((String) -> Unit)? = null
    var onClientLeft: ((String) -> Unit)? = null
    var onMessageReceived: ((String, GameMessage) -> Unit)? = null

    suspend fun start(
        deviceName: String,
        mode: String,
        maxPlayers: Int,
        onReady: () -> Unit
    ) {
        serverSocket = aSocket(selectorManager).tcp().bind(InetSocketAddress("0.0.0.0", GAME_PORT))
        onReady()
        startBroadcasting(deviceName, mode, maxPlayers)
        acceptClients()
    }

    private fun startBroadcasting(deviceName: String, mode: String, maxPlayers: Int) {
        broadcastJob = scope.launch {
            try {
                val udpSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 0)) {
                    broadcast = true
                }
                while (isActive) {
                    try {
                        val currentCount = clientsMutex.withLock { clients.size } + 1
                        val info = Json.encodeToString(
                            DiscoveredHost(
                                name = deviceName,
                                address = "",
                                port = GAME_PORT,
                                mode = mode,
                                currentPlayers = currentCount,
                                maxPlayers = maxPlayers
                            )
                        )
                        val buffer = Buffer()
                        buffer.write(info.encodeToByteArray())
                        udpSocket.send(
                            Datagram(
                                packet = buffer,
                                address = InetSocketAddress("255.255.255.255", DISCOVERY_PORT)
                            )
                        )
                    } catch (_: Exception) {}
                    delay(2000)
                }
                udpSocket.close()
            } catch (_: Exception) {}
        }
    }

    private fun acceptClients() {
        scope.launch {
            val server = serverSocket ?: return@launch
            while (isActive) {
                try {
                    val socket = server.accept()
                    scope.launch { handleClient(socket) }
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        val readChannel = socket.openReadChannel()
        val writeChannel = socket.openWriteChannel(autoFlush = true)
        var nickname = "Gracz"

        try {
            while (true) {
                val line = readChannel.readUTF8Line() ?: break
                val message = try {
                    gameJson.decodeFromString<GameMessage>(line)
                } catch (_: Exception) { continue }

                if (message is GameMessage.Join) {
                    nickname = message.nickname
                    val conn = ClientConn(nickname, socket, writeChannel)
                    clientsMutex.withLock { clients.add(conn) }
                    onClientJoined?.invoke(nickname)
                }
                onMessageReceived?.invoke(nickname, message)
            }
        } catch (_: Exception) {}

        clientsMutex.withLock { clients.removeAll { it.nickname == nickname } }
        onClientLeft?.invoke(nickname)
        socket.dispose()
    }

    suspend fun sendToAll(message: GameMessage) {
        val json = gameJson.encodeToString<GameMessage>(message) + "\n"
        val snapshot = clientsMutex.withLock { clients.toList() }
        for (conn in snapshot) {
            try { conn.writeChannel.writeStringUtf8(json) } catch (_: Exception) {}
        }
    }

    suspend fun connectedNicknames(): List<String> =
        clientsMutex.withLock { clients.map { it.nickname } }

    fun stop() {
        broadcastJob?.cancel()
        scope.launch {
            clientsMutex.withLock {
                clients.forEach { it.socket.dispose() }
                clients.clear()
            }
        }
        scope.cancel()
        serverSocket?.dispose()
        selectorManager.close()
    }
}
