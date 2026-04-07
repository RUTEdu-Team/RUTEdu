package com.example.myapplication.multiplayer.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.multiplayer.engine.MultiplayerGameEngine
import com.example.myapplication.multiplayer.model.DiscoveredHost
import com.example.myapplication.multiplayer.model.GameMessage
import com.example.myapplication.multiplayer.model.GamePhase
import com.example.myapplication.multiplayer.model.MultiplayerMode
import com.example.myapplication.multiplayer.model.MultiplayerSessionState
import com.example.myapplication.multiplayer.model.PlayerFinalResult
import com.example.myapplication.multiplayer.model.PlayerRole
import com.example.myapplication.multiplayer.model.PvPQuestion
import com.example.myapplication.multiplayer.model.PvPQuestionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MultiplayerSessionManager {

    var state by mutableStateOf(MultiplayerSessionState())
        private set

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var server: GameServer? = null
    private var client: GameClient? = null
    private var engine: MultiplayerGameEngine? = null
    private var timerJob: Job? = null

    private val scoresMutex = Mutex()
    private val scores = mutableMapOf<String, Int>()
    private val answersMutex = Mutex()
    private val roundAnswers = mutableMapOf<String, Pair<String, Long>>()
    private var roundStartTime = 0L
    private var expectedPlayerCount = 0

    // ── Public API ──────────────────────────────────────────────────────────────

    fun initAsHost(
        nickname: String,
        mode: MultiplayerMode,
        subjectId: String,
        topicId: String,
        rounds: Int,
        timePerRoundSec: Int
    ) {
        cleanup()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        scope.launch {
            scoresMutex.withLock {
                scores.clear()
                scores[nickname] = 0
            }
        }

        val eng = MultiplayerGameEngine(subjectId, topicId, rounds, timePerRoundSec * 1000L)
        engine = eng
        val srv = GameServer()
        server = srv

        srv.onClientJoined = { clientName ->
            scope.launch {
                scoresMutex.withLock { scores[clientName] = 0 }
                refreshPlayers(srv, nickname)
                updateState { copy(errorMessage = null) }
                // Update mDNS/UDP advertisement with current player count
                val count = scoresMutex.withLock { scores.size }
                srv.updateAdvertisedPlayerCount(count)
            }
        }
        srv.onClientLeft = { clientName ->
            scope.launch {
                scoresMutex.withLock { scores.remove(clientName) }
                refreshPlayers(srv, nickname)
            }
        }
        srv.onMessageReceived = { clientName, message ->
            if (message is GameMessage.PlayerAnswer && state.phase == GamePhase.IN_ROUND) {
                scope.launch {
                    answersMutex.withLock {
                        if (!roundAnswers.containsKey(clientName)) {
                            roundAnswers[clientName] = message.answer to currentTimeMs()
                        }
                    }
                    checkRoundComplete(nickname, srv)
                }
            }
        }

        updateState {
            copy(
                phase = GamePhase.LOBBY_HOST,
                role = PlayerRole.HOST,
                myNickname = nickname,
                players = listOf(nickname),
                gameMode = mode,
                selectedSubjectId = subjectId,
                selectedTopicId = topicId,
                totalRounds = rounds,
                timePerRoundMs = timePerRoundSec * 1000L,
                scores = mapOf(nickname to 0),
                errorMessage = null
            )
        }

        scope.launch {
            try {
                srv.start(
                    deviceName = nickname,
                    mode = if (mode == MultiplayerMode.ONE_VS_ONE) "1v1" else "turniej",
                    maxPlayers = if (mode == MultiplayerMode.ONE_VS_ONE) 2 else 4
                ) { /* server ready */ }
            } catch (e: Exception) {
                updateState { copy(errorMessage = "Błąd serwera: ${e.message}") }
            }
        }
    }

    fun initAsClient(nickname: String) {
        cleanup()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val cli = GameClient()
        client = cli

        cli.onHostDiscovered = { host ->
            val current = state.discoveredHosts
            if (current.none { it.address == host.address }) {
                updateState { copy(discoveredHosts = current + host) }
            }
        }
        cli.onMessageReceived = { message -> handleClientMessage(message) }
        cli.onDisconnected = {
            if (state.phase != GamePhase.GAME_OVER && state.phase != GamePhase.IDLE) {
                updateState { copy(phase = GamePhase.IDLE, errorMessage = "Host rozłączył się") }
            }
        }

        updateState {
            copy(
                phase = GamePhase.DISCOVERING,
                role = PlayerRole.CLIENT,
                myNickname = nickname,
                discoveredHosts = emptyList(),
                errorMessage = null
            )
        }
        cli.startDiscovery()
    }

    fun joinHost(host: DiscoveredHost) {
        val cli = client ?: return
        val nickname = state.myNickname
        scope.launch {
            try {
                cli.stopDiscovery()
                cli.connect(host, nickname)
                updateState { copy(phase = GamePhase.LOBBY_CLIENT, players = listOf(nickname)) }
            } catch (e: Exception) {
                updateState {
                    copy(phase = GamePhase.DISCOVERING, errorMessage = "Nie można połączyć: ${e.message}")
                }
                cli.startDiscovery()
            }
        }
    }

    fun hostStartGame() {
        val eng = engine ?: return
        val srv = server ?: return
        if (state.players.size < 2) {
            updateState { copy(errorMessage = "Poczekaj na co najmniej 1 gracza") }
            return
        }
        updateState { copy(errorMessage = null) }
        scope.launch {
            srv.sendToAll(GameMessage.GameStart(eng.totalRounds, eng.timePerRoundMs))
            delay(500)
            startNextRound(srv)
        }
    }

    fun submitAnswer(answer: String) {
        if (state.answeredThisRound || state.phase != GamePhase.IN_ROUND) return
        updateState { copy(myAnswer = answer, answeredThisRound = true) }

        when (state.role) {
            PlayerRole.HOST -> {
                val srv = server ?: return
                scope.launch {
                    answersMutex.withLock {
                        roundAnswers[state.myNickname] = answer to currentTimeMs()
                    }
                    checkRoundComplete(state.myNickname, srv)
                }
            }
            PlayerRole.CLIENT -> {
                scope.launch {
                    client?.send(
                        GameMessage.PlayerAnswer(
                            nickname = state.myNickname,
                            answer = answer,
                            timestampMs = currentTimeMs()
                        )
                    )
                }
            }
            PlayerRole.NONE -> {}
        }
    }

    fun initAsClientDirect(nickname: String, hostIp: String) {
        cleanup()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val cli = GameClient()
        client = cli

        cli.onMessageReceived = { message -> handleClientMessage(message) }
        cli.onDisconnected = {
            if (state.phase != GamePhase.GAME_OVER && state.phase != GamePhase.IDLE) {
                updateState { copy(phase = GamePhase.IDLE, errorMessage = "Host rozłączył się") }
            }
        }

        updateState {
            copy(
                phase = GamePhase.LOBBY_CLIENT,
                role = PlayerRole.CLIENT,
                myNickname = nickname,
                discoveredHosts = emptyList(),
                players = listOf(nickname),
                errorMessage = null
            )
        }

        scope.launch {
            try {
                val host = DiscoveredHost(
                    name = "Host",
                    address = hostIp,
                    port = GAME_PORT,
                    mode = "1v1",
                    currentPlayers = 1,
                    maxPlayers = 2
                )
                cli.connect(host, nickname)
            } catch (e: Exception) {
                updateState { copy(phase = GamePhase.IDLE, errorMessage = "Nie można połączyć: ${e.message}") }
            }
        }
    }

    fun cleanup() {
        timerJob?.cancel()
        server?.stop()
        client?.stop()
        scope.cancel()
        server = null
        client = null
        engine = null
        state = MultiplayerSessionState()
    }

    // ── Internal host logic ─────────────────────────────────────────────────────

    private suspend fun refreshPlayers(srv: GameServer, hostNickname: String) {
        val allPlayers = listOf(hostNickname) + srv.connectedNicknames()
        val updatedScores = scoresMutex.withLock {
            allPlayers.associateWith { scores[it] ?: 0 }
        }
        updateState { copy(players = allPlayers, scores = updatedScores) }
        srv.sendToAll(GameMessage.PlayerJoined(allPlayers))
    }

    private suspend fun startNextRound(srv: GameServer) {
        val eng = engine ?: return
        val nextRound = state.currentRound + 1

        if (nextRound > eng.totalRounds) {
            endGame(srv)
            return
        }

        val question = eng.getQuestion(nextRound - 1) ?: run { endGame(srv); return }

        answersMutex.withLock { roundAnswers.clear() }
        roundStartTime = currentTimeMs()
        expectedPlayerCount = state.players.size

        val networkQuestion = GameMessage.PvPQuestion(
            round = nextRound,
            questionType = question.questionType.name,
            questionText = question.questionText,
            options = question.options
        )
        srv.sendToAll(networkQuestion)

        updateState {
            copy(
                phase = GamePhase.IN_ROUND,
                currentRound = nextRound,
                currentQuestion = question,
                timeLeftMs = eng.timePerRoundMs,
                myAnswer = "",
                answeredThisRound = false,
                roundResults = emptyMap()
            )
        }
        startTimer(eng.timePerRoundMs, srv)
    }

    private suspend fun checkRoundComplete(hostNickname: String, srv: GameServer) {
        val answered = answersMutex.withLock { roundAnswers.size }
        if (answered >= expectedPlayerCount) {
            timerJob?.cancel()
            finalizeRound(srv)
        }
    }

    private suspend fun finalizeRound(srv: GameServer) {
        val question = state.currentQuestion ?: return
        val answersCopy = answersMutex.withLock { roundAnswers.toMap() }
        val allPlayers = state.players

        val filledAnswers = allPlayers.associateWith { p ->
            answersCopy[p] ?: ("" to (roundStartTime + state.timePerRoundMs))
        }

        val scoresCopy = scoresMutex.withLock { scores.toMutableMap() }
        val results = engine?.evaluateAnswers(
            correctAnswer = question.correctAnswer,
            answers = filledAnswers,
            roundStartTime = roundStartTime,
            currentScores = scoresCopy
        ) ?: return

        scoresMutex.withLock { scores.putAll(scoresCopy) }

        srv.sendToAll(GameMessage.RoundResult(results, question.correctAnswer))
        updateState {
            copy(
                phase = GamePhase.ROUND_RESULT,
                roundResults = results,
                lastCorrectAnswer = question.correctAnswer,
                scores = scoresCopy.toMap()
            )
        }
        delay(3500)
        startNextRound(srv)
    }

    private suspend fun endGame(srv: GameServer) {
        val scoresCopy = scoresMutex.withLock { scores.toMap() }
        val ranking = engine?.computeFinalRanking(scoresCopy) ?: scoresCopy.entries
            .sortedByDescending { it.value }
            .mapIndexed { i, (name, score) -> PlayerFinalResult(name, score, i + 1) }

        srv.sendToAll(GameMessage.GameOver(ranking))
        updateState { copy(phase = GamePhase.GAME_OVER, finalRanking = ranking) }
    }

    private fun startTimer(durationMs: Long, srv: GameServer) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(100)
                remaining -= 100
                updateState { copy(timeLeftMs = maxOf(0, remaining)) }
            }
            if (state.phase == GamePhase.IN_ROUND) {
                finalizeRound(srv)
            }
        }
    }

    // ── Client message handler ──────────────────────────────────────────────────

    private fun handleClientMessage(message: GameMessage) {
        when (message) {
            is GameMessage.PlayerJoined -> {
                updateState { copy(players = message.players) }
            }
            is GameMessage.GameStart -> {
                updateState {
                    copy(totalRounds = message.totalRounds, timePerRoundMs = message.timePerRoundMs)
                }
            }
            is GameMessage.PvPQuestion -> {
                val pvpQ = PvPQuestion(
                    questionType = runCatching {
                        PvPQuestionType.valueOf(message.questionType)
                    }.getOrElse { PvPQuestionType.SELECT },
                    questionText = message.questionText,
                    options = message.options,
                    correctAnswer = ""
                )
                updateState {
                    copy(
                        phase = GamePhase.IN_ROUND,
                        currentRound = message.round,
                        currentQuestion = pvpQ,
                        timeLeftMs = timePerRoundMs,
                        myAnswer = "",
                        answeredThisRound = false,
                        roundResults = emptyMap()
                    )
                }
                startClientTimer(state.timePerRoundMs)
            }
            is GameMessage.RoundResult -> {
                timerJob?.cancel()
                val newScores = message.results.mapValues { it.value.totalScore }
                updateState {
                    copy(
                        phase = GamePhase.ROUND_RESULT,
                        roundResults = message.results,
                        lastCorrectAnswer = message.correctAnswer,
                        scores = newScores
                    )
                }
            }
            is GameMessage.GameOver -> {
                updateState { copy(phase = GamePhase.GAME_OVER, finalRanking = message.finalRanking) }
            }
            is GameMessage.HostLeft -> {
                updateState { copy(phase = GamePhase.IDLE, errorMessage = "Host zakończył grę") }
            }
            else -> {}
        }
    }

    private fun startClientTimer(durationMs: Long) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0 && state.phase == GamePhase.IN_ROUND) {
                delay(100)
                remaining -= 100
                updateState { copy(timeLeftMs = maxOf(0, remaining)) }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private fun updateState(transform: MultiplayerSessionState.() -> MultiplayerSessionState) {
        state = state.transform()
    }
}
