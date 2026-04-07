package com.example.myapplication.multiplayer.network

import com.example.myapplication.multiplayer.model.DiscoveredHost

expect class PvPDiscovery() {
    fun advertise(deviceName: String, port: Int, mode: String, currentPlayers: Int, maxPlayers: Int)
    fun updatePlayerCount(currentPlayers: Int)
    fun stopAdvertising()
    fun browse(onFound: (DiscoveredHost) -> Unit, onLost: (String) -> Unit)
    fun stopBrowsing()
    fun close()
}
