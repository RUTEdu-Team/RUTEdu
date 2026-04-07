package com.example.myapplication.multiplayer.model

enum class PvPQuestionType { SELECT, FIND_ANSWER, FIND_OPERATOR, TYPE_ANSWER, ELEMENT_CARD }

data class PvPQuestion(
    val questionType: PvPQuestionType,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String
)
