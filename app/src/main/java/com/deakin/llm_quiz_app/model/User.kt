package com.deakin.llm_quiz_app.model

data class User(
    var accountID: String,
    var username: String,
    var email: String,
    var tier: Int = 0// 0: free, 1: intermediate, 2: advanced
)

data class UserRegistrationRequest(
    val username: String,
    val email: String,
    val password: String // Send plaintext to backend, backend hashes it
)