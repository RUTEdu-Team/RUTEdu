package com.example.myapplication.multiplayer.engine

import com.example.myapplication.data.QuestionBank
import com.example.myapplication.data.SubjectRepository
import com.example.myapplication.models.MathOperator
import com.example.myapplication.models.Question
import com.example.myapplication.multiplayer.model.PvPQuestion
import com.example.myapplication.multiplayer.model.PvPQuestionType

object PvPQuestionFilter {

    fun getQuestionsForTopic(subjectId: String, topicId: String): List<PvPQuestion> {
        val subject = SubjectRepository.getById(subjectId) ?: return emptyList()
        val topic = subject.topics.find { it.id == topicId } ?: return emptyList()
        return topic.lessons
            .flatMap { lesson -> QuestionBank.questionsFor(lesson.id) }
            .mapNotNull { question -> toPvPQuestion(question) }
            .shuffled()
    }

    private fun toPvPQuestion(question: Question): PvPQuestion? = when (question) {
        is Question.FindAnswer -> PvPQuestion(
            questionType = PvPQuestionType.FIND_ANSWER,
            questionText = "${question.operand1} ${question.operator.symbol} ${question.operand2} = ?",
            options = emptyList(),
            correctAnswer = question.correctAnswer.toString()
        )
        is Question.FindOperator -> PvPQuestion(
            questionType = PvPQuestionType.FIND_OPERATOR,
            questionText = "${question.operand1} ? ${question.operand2} = ${question.result}",
            options = MathOperator.entries.map { it.symbol },
            correctAnswer = question.correctOperator.symbol
        )
        is Question.SelectFromList -> {
            if (!question.multiSelect && question.correctIndices.size == 1) {
                PvPQuestion(
                    questionType = PvPQuestionType.SELECT,
                    questionText = question.prompt,
                    options = question.options,
                    correctAnswer = question.correctIndices.first().toString()
                )
            } else null
        }
        is Question.TypeAnswer -> PvPQuestion(
            questionType = PvPQuestionType.TYPE_ANSWER,
            questionText = question.prompt,
            options = emptyList(),
            correctAnswer = question.correctAnswer.toString()
        )
        is Question.ElementCardQuiz -> PvPQuestion(
            questionType = PvPQuestionType.ELEMENT_CARD,
            questionText = question.prompt,
            options = question.options,
            correctAnswer = question.correctIndex.toString()
        )
        else -> null
    }
}
