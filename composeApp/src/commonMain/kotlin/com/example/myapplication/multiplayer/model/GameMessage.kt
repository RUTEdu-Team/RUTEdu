package com.example.myapplication.multiplayer.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val gameJson = Json {
    classDiscriminator = "msgType"
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
sealed class GameMessage {

    @Serializable @SerialName("join")
    data class Join(val nickname: String) : GameMessage()

    @Serializable @SerialName("player_joined")
    data class PlayerJoined(val players: List<String>) : GameMessage()

    @Serializable @SerialName("game_start")
    data class GameStart(val totalRounds: Int, val timePerRoundMs: Long) : GameMessage()

    @Serializable @SerialName("question")
    data class PvPQuestion(
        val round: Int,
        val questionType: String,
        val questionText: String,
        val options: List<String> = emptyList()
    ) : GameMessage()

    @Serializable @SerialName("answer")
    data class PlayerAnswer(
        val nickname: String,
        val answer: String,
        val timestampMs: Long
    ) : GameMessage()

    @Serializable @SerialName("round_result")
    data class RoundResult(
        val results: Map<String, PlayerRoundResult>,
        val correctAnswer: String
    ) : GameMessage()

    @Serializable @SerialName("game_over")
    data class GameOver(val finalRanking: List<PlayerFinalResult>) : GameMessage()

    @Serializable @SerialName("host_left")
    data class HostLeft(val reason: String = "") : GameMessage()
}

@Serializable
data class PlayerRoundResult(
    val correct: Boolean,
    val pointsEarned: Int,
    val totalScore: Int,
    val answer: String,
    val timeMs: Long
)

@Serializable
data class PlayerFinalResult(
    val nickname: String,
    val totalScore: Int,
    val rank: Int
)

@Serializable
data class DiscoveredHost(
    val name: String,
    val address: String,
    val port: Int,
    val mode: String,
    val currentPlayers: Int,
    val maxPlayers: Int
)
