package com.deakin.llm_quiz_app.model

data class Question(
    var userId: Int,
    var question: String,
    var optionA: String,
    var optionB: String,
    var optionC: String,
    var optionD: String,
    var selected: Int,
    var correctAnswer: Int
)
