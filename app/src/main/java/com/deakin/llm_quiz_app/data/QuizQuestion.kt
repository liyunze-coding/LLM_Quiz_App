package com.deakin.llm_quiz_app.data

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
) {
    val correctAnswerIndex: Int
        get() = when (correctAnswer.uppercase()) {
            "A" -> 0
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> -1
        }
}
