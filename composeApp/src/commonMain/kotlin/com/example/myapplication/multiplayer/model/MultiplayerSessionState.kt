package com.example.myapplication.multiplayer.model

enum class GamePhase {
    IDLE,
    DISCOVERING,
    LOBBY_HOST,
    LOBBY_CLIENT,
    COUNTDOWN,
    IN_ROUND,
    ROUND_RESULT,
    GAME_OVER
}

enum class PlayerRole { NONE, HOST, CLIENT }

enum class MultiplayerMode { ONE_VS_ONE, TOURNAMENT }

data class MultiplayerSessionState(
    val phase: GamePhase = GamePhase.IDLE,
    val role: PlayerRole = PlayerRole.NONE,
    val myNickname: String = "",
    val players: List<String> = emptyList(),
    val currentRound: Int = 0,
    val totalRounds: Int = 10,
    val timePerRoundMs: Long = 15_000L,
    val currentQuestion: PvPQuestion? = null,
    val timeLeftMs: Long = 0L,
    val myAnswer: String = "",
    val answeredThisRound: Boolean = false,
    val roundResults: Map<String, PlayerRoundResult> = emptyMap(),
    val lastCorrectAnswer: String = "",
    val scores: Map<String, Int> = emptyMap(),
    val finalRanking: List<PlayerFinalResult> = emptyList(),
    val discoveredHosts: List<DiscoveredHost> = emptyList(),
    val gameMode: MultiplayerMode = MultiplayerMode.ONE_VS_ONE,
    val selectedSubjectId: String = "",
    val selectedTopicId: String = "",
    val errorMessage: String? = null
)
