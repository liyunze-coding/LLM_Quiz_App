package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.deakin.llm_quiz_app.data.DatabaseHelper

class ScoreActivity : AppCompatActivity() {
    var userId = -1
    private fun restartQuiz() {
        val quizIntent = Intent(this, HomeActivity::class.java)
        quizIntent.putExtra("userId", userId)
        startActivity(quizIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        // text info
        userId = intent.getIntExtra("userId", -1)
        val scoreDisplay = intent.getStringExtra("score")

        val db = DatabaseHelper(this, null)

        val username = db.getUser(userId).username

        findViewById<TextView>(R.id.score).text = scoreDisplay
        findViewById<TextView>(R.id.congratsText).text = "Congratulations ${username}!"

        // buttons
        findViewById<Button>(R.id.takeNewQuizButton).setOnClickListener { restartQuiz() }
        findViewById<Button>(R.id.finishButton).setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.putExtra("userId", userId)
            startActivity(homeIntent)
        }
    }
}