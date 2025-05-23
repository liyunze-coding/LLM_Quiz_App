package com.deakin.llm_quiz_app.util

object Util {
    const val DATABASE_VERSION = 10
    const val DATABASE_NAME = "user_db"

    const val USER_TABLE_NAME = "user"
    const val USER_ID = "user_id"
    const val EMAIL = "email"
    const val USERNAME = "username"
    const val PASSWORD = "password"

    const val USER_INTEREST_TABLE_NAME = "user_interest"
    const val INTEREST_ID = "interest_id"
    const val INTEREST = "interest"

    const val QUESTION_TABLE_NAME = "questions"
    const val QUESTION_ID = "question_id"
    const val QUESTION = "question"
    const val OPTION_A = "option_0"
    const val OPTION_B = "option_1"
    const val OPTION_C = "option_2"
    const val OPTION_D = "option_3"
    const val SELECTED_OPTION = "selected"
    const val CORRECT_ANSWER = "correct_answer"
}
