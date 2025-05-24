package com.deakin.llm_quiz_app.model

data class User(
    var username: String,
    var email: String,
    var password: String,
    var tier: Int = 0// 0: free, 1: intermediate, 2: advanced
)
