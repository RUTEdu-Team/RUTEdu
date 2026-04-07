package com.example.myapplication.multiplayer.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import com.example.myapplication.AppContext
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.write
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val NSD_TYPE = "_pvpgame._tcp"

actual class PvPDiscovery actual constructor() {

    private val context: Context = AppContext.context
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val multicastLock: WifiManager.MulticastLock =
        (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .createMulticastLock("pvp_discovery")
            .also { it.setReferenceCounted(false) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val selectorManager = SelectorManager(Dispatchers.Default)

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var udpBroadcastJob: Job? = null
    private var udpListenJob: Job? = null

    private var currentDeviceName = ""
    private var currentPort = GAME_PORT
    private var currentMode = ""
    private var currentPlayers = 1
    private var currentMaxPlayers = 2

    private val foundHostsMutex = Mutex()
    private val foundHosts = mutableMapOf<String, DiscoveredHost>()

    actual fun advertise(deviceName: String, port: Int, mode: String, currentPlayers: Int, maxPlayers: Int) {
        currentDeviceName = deviceName
        currentPort = port
        currentMode = mode
        this.currentPlayers = currentPlayers
        currentMaxPlayers = maxPlayers
        registerNsd(deviceName, port, mode, currentPlayers, maxPlayers)
        startUdpBroadcast(deviceName, port, mode, currentPlayers, maxPlayers)
    }

    actual fun updatePlayerCount(currentPlayers: Int) {
        this.currentPlayers = currentPlayers
        stopAdvertising()
        advertise(currentDeviceName, currentPort, currentMode, currentPlayers, currentMaxPlayers)
    }

    actual fun stopAdvertising() {
        udpBroadcastJob?.cancel()
        udpBroadcastJob = null
        registrationListener?.let {
            try { nsdManager.unregisterService(it) } catch (_: Exception) {}
        }
        registrationListener = null
    }

    actual fun browse(onFound: (DiscoveredHost) -> Unit, onLost: (String) -> Unit) {
        multicastLock.acquire()
        startNsdDiscovery(onFound, onLost)
        startUdpListen(onFound)
    }

    actual fun stopBrowsing() {
        udpListenJob?.cancel()
        udpListenJob = null
        discoveryListener?.let {
            try { nsdManager.stopServiceDiscovery(it) } catch (_: Exception) {}
        }
        discoveryListener = null
        if (multicastLock.isHeld) multicastLock.release()
    }

    actual fun close() {
        stopAdvertising()
        stopBrowsing()
        scope.cancel()
        selectorManager.close()
    }

    private fun registerNsd(deviceName: String, port: Int, mode: String, cp: Int, mp: Int) {
        val info = NsdServiceInfo().apply {
            serviceName = deviceName
            serviceType = NSD_TYPE
            setPort(port)
            setAttribute("mode", mode)
            setAttribute("cp", cp.toString())
            setAttribute("mp", mp.toString())
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(i: NsdServiceInfo, e: Int) {}
            override fun onUnregistrationFailed(i: NsdServiceInfo, e: Int) {}
            override fun onServiceRegistered(i: NsdServiceInfo) {}
            override fun onServiceUnregistered(i: NsdServiceInfo) {}
        }
        registrationListener = listener
        try { nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
        catch (_: Exception) {}
    }

    private fun startUdpBroadcast(deviceName: String, port: Int, mode: String, cp: Int, mp: Int) {
        udpBroadcastJob?.cancel()
        udpBroadcastJob = scope.launch {
            try {
                val udpSocket = aSocket(selectorManager).udp()
                    .bind(InetSocketAddress("0.0.0.0", 0)) { broadcast = true }
                val json = Json.encodeToString(DiscoveredHost(deviceName, "", port, mode, cp, mp))
                while (isActive) {
                    try {
                        val buf = Buffer().also { it.write(json.encodeToByteArray()) }
                        udpSocket.send(Datagram(buf, InetSocketAddress("255.255.255.255", DISCOVERY_PORT)))
                    } catch (_: Exception) {}
                    delay(2000)
                }
                udpSocket.close()
            } catch (_: Exception) {}
        }
    }

    private fun startNsdDiscovery(onFound: (DiscoveredHost) -> Unit, onLost: (String) -> Unit) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) {}
            override fun onDiscoveryStopped(type: String) {}
            override fun onStartDiscoveryFailed(type: String, e: Int) {}
            override fun onStopDiscoveryFailed(type: String, e: Int) {}

            override fun onServiceFound(info: NsdServiceInfo) {
                nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(i: NsdServiceInfo, e: Int) {}
                    override fun onServiceResolved(i: NsdServiceInfo) {
                        val address = i.host?.hostAddress ?: return
                        val host = DiscoveredHost(
                            name = i.serviceName,
                            address = address,
                            port = i.port,
                            mode = i.attributes["mode"]?.decodeToString() ?: "1v1",
                            currentPlayers = i.attributes["cp"]?.decodeToString()?.toIntOrNull() ?: 1,
                            maxPlayers = i.attributes["mp"]?.decodeToString()?.toIntOrNull() ?: 2
                        )
                        scope.launch {
                            val isNew = foundHostsMutex.withLock {
                                if (!foundHosts.containsKey(address)) { foundHosts[address] = host; true }
                                else false
                            }
                            if (isNew) onFound(host)
                        }
                    }
                })
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                onLost(info.serviceName)
                scope.launch {
                    foundHostsMutex.withLock {
                        foundHosts.entries.removeIf { it.value.name == info.serviceName }
                    }
                }
            }
        }
        discoveryListener = listener
        try { nsdManager.discoverServices(NSD_TYPE, NsdManager.PROTOCOL_DNS_SD, listener) }
        catch (_: Exception) {}
    }

    private fun startUdpListen(onFound: (DiscoveredHost) -> Unit) {
        udpListenJob?.cancel()
        udpListenJob = scope.launch {
            try {
                val udpSocket = aSocket(selectorManager).udp()
                    .bind(InetSocketAddress("0.0.0.0", DISCOVERY_PORT))
                while (isActive) {
                    try {
                        val datagram = udpSocket.receive()
                        val senderIp = (datagram.address as? InetSocketAddress)?.hostname ?: continue
                        val text = datagram.packet.readByteArray().decodeToString()
                        val host = Json.decodeFromString<DiscoveredHost>(text).copy(address = senderIp)
                        val isNew = foundHostsMutex.withLock {
                            if (!foundHosts.containsKey(senderIp)) { foundHosts[senderIp] = host; true }
                            else false
                        }
                        if (isNew) onFound(host)
                    } catch (_: Exception) {}
                }
                udpSocket.close()
            } catch (_: Exception) {}
        }
    }
}
