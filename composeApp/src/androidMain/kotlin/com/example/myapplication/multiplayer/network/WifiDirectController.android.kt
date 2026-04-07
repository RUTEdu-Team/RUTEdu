package com.example.myapplication.multiplayer.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import com.example.myapplication.AppContext

actual val isWifiDirectSupported: Boolean = true

actual class WifiDirectController actual constructor() {

    private val context: Context = AppContext.context
    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, context.mainLooper, null)

    private var receiver: WifiDirectBroadcastReceiver? = null
    private var onPeersFoundCallback: ((List<WifiPeerDevice>) -> Unit)? = null

    actual fun createGroup(onReady: () -> Unit, onError: (String) -> Unit) {
        // Remove any existing group first to avoid BUSY errors
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = doCreateGroup(onReady, onError)
            override fun onFailure(reason: Int) = doCreateGroup(onReady, onError)
        })
    }

    private fun doCreateGroup(onReady: () -> Unit, onError: (String) -> Unit) {
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // As Group Owner, our IP is always 192.168.49.1
                onReady()
            }
            override fun onFailure(reason: Int) {
                onError("Nie można utworzyć grupy WiFi Direct (kod: $reason)")
            }
        })
    }

    actual fun discoverPeers(
        onFound: (List<WifiPeerDevice>) -> Unit,
        onError: (String) -> Unit
    ) {
        onPeersFoundCallback = onFound
        registerReceiver(
            onPeersChanged = { deviceList ->
                val peers = deviceList.deviceList.map { WifiPeerDevice(it.deviceName, it.deviceAddress) }
                onFound(peers)
            },
            onConnected = null
        )
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {
                onError("Błąd skanowania (kod: $reason)")
            }
        })
    }

    actual fun connectToPeer(
        peer: WifiPeerDevice,
        onConnected: (groupOwnerIp: String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Re-register receiver to also handle connection events
        receiver?.let { try { context.unregisterReceiver(it) } catch (_: Exception) {} }
        registerReceiver(
            onPeersChanged = { deviceList ->
                val peers = deviceList.deviceList.map { WifiPeerDevice(it.deviceName, it.deviceAddress) }
                onPeersFoundCallback?.invoke(peers)
            },
            onConnected = { goIp -> onConnected(goIp) }
        )

        val config = WifiP2pConfig().apply {
            deviceAddress = peer.address
            wps.setup = WpsInfo.PBC
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {
                onError("Nie można połączyć z urządzeniem (kod: $reason)")
            }
        })
    }

    actual fun stop() {
        receiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        receiver = null
        try { manager.removeGroup(channel, null) } catch (_: Exception) {}
        try { manager.stopPeerDiscovery(channel, null) } catch (_: Exception) {}
    }

    private fun registerReceiver(
        onPeersChanged: (WifiP2pDeviceList) -> Unit,
        onConnected: ((String) -> Unit)?
    ) {
        val recv = WifiDirectBroadcastReceiver(manager, channel, onPeersChanged, onConnected)
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            if (onConnected != null) addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }
        context.registerReceiver(recv, filter)
        receiver = recv
    }
}

private class WifiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val onPeersChanged: (WifiP2pDeviceList) -> Unit,
    private val onConnected: ((String) -> Unit)?
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peers -> onPeersChanged(peers) }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                manager.requestConnectionInfo(channel) { info ->
                    if (info?.groupFormed == true) {
                        val goIp = info.groupOwnerAddress?.hostAddress ?: WIFI_DIRECT_HOST_IP
                        onConnected?.invoke(goIp)
                    }
                }
            }
        }
    }
}
