package com.example.myapplication.multiplayer.engine

import com.example.myapplication.multiplayer.model.PlayerFinalResult
import com.example.myapplication.multiplayer.model.PlayerRoundResult
import com.example.myapplication.multiplayer.model.PvPQuestion

class MultiplayerGameEngine(
    subjectId: String,
    topicId: String,
    val totalRounds: Int,
    val timePerRoundMs: Long
) {
    private val questions: List<PvPQuestion> =
        PvPQuestionFilter.getQuestionsForTopic(subjectId, topicId)
            .take(totalRounds)

    fun getQuestion(roundIndex: Int): PvPQuestion? = questions.getOrNull(roundIndex)

    fun hasEnoughQuestions(): Boolean = questions.isNotEmpty()

    /**
     * Evaluates answers for a round.
     * @param answers  nickname → (answer, timestampMs) — timestamps are host-side arrival times
     * @param currentScores  mutable map updated in place
     */
    fun evaluateAnswers(
        correctAnswer: String,
        answers: Map<String, Pair<String, Long>>,
        roundStartTime: Long,
        currentScores: MutableMap<String, Int>
    ): Map<String, PlayerRoundResult> {
        val correctEntries = answers.filter { (_, v) -> v.first.trim() == correctAnswer.trim() }
        val fastestTs = correctEntries.values.minOfOrNull { it.second } ?: Long.MAX_VALUE

        return answers.mapValues { (nickname, v) ->
            val (answer, timestamp) = v
            val isCorrect = answer.trim() == correctAnswer.trim()
            val isFastest = isCorrect && timestamp <= fastestTs && correctEntries.size > 1
            val points = when {
                !isCorrect -> 0
                isFastest -> 3
                else -> 2
            }
            val newTotal = (currentScores[nickname] ?: 0) + points
            currentScores[nickname] = newTotal
            PlayerRoundResult(
                correct = isCorrect,
                pointsEarned = points,
                totalScore = newTotal,
                answer = answer,
                timeMs = timestamp - roundStartTime
            )
        }
    }

    fun computeFinalRanking(scores: Map<String, Int>): List<PlayerFinalResult> =
        scores.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, (name, score) ->
                PlayerFinalResult(nickname = name, totalScore = score, rank = index + 1)
            }
}
