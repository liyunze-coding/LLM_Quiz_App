package com.deakin.llm_quiz_app.model

data class Option(
    val text: String,
    var color: String? = null,
    val hasTopMargin: Boolean = true
)