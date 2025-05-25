package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.deakin.llm_quiz_app.data.DatabaseHelper

class ProfileActivity : AppCompatActivity() {
    var userId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = DatabaseHelper(this, null)
        userId = intent.getIntExtra("userId", -1)

        // POPULATING CONTENT
        val user = db.getUser(userId)
        val username = user.username
        val email = user.email
        val tier = user.tier
        var tierTitle = ""

        if (tier == 0) {
            tierTitle = "Beginner"
        } else if (tier == 1) {
            tierTitle = "Intermediate"
        } else if (tier == 2) {
            tierTitle = "Advanced"
        }

        findViewById<TextView>(R.id.profileUsername).text = username
        findViewById<TextView>(R.id.profileEmail).text = email
        findViewById<TextView>(R.id.profileTier).text = tierTitle

        val questionHistory = db.getUserQuestionsHistory(userId)
        val totalQuestions = questionHistory.size
        val incorrectAnswers = questionHistory.count { it.selected != it.correctAnswer }
        val correctAnswers = questionHistory.count { it.selected == it.correctAnswer }

        findViewById<TextView>(R.id.totalQuestions).text = totalQuestions.toString()
        findViewById<TextView>(R.id.incorrectAnswers).text = incorrectAnswers.toString()
        findViewById<TextView>(R.id.correctAnswers).text = correctAnswers.toString()

        // HISTORY
        val viewHistoryButton = findViewById<Button>(R.id.historyButton)
        viewHistoryButton.setOnClickListener {
            val viewHistoryIntent = Intent(this, HistoryActivity::class.java)
            viewHistoryIntent.putExtra("userId", userId)
            startActivity(viewHistoryIntent)
        }

        // UPGRADE
        val upgradeButton = findViewById<Button>(R.id.upgradeButton)
        upgradeButton.setOnClickListener {
            val upgradeIntent = Intent(this, UpgradeActivity::class.java)
            upgradeIntent.putExtra("userId", userId)
            startActivity(upgradeIntent)
        }

        // SHARE


        // BACK
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.putExtra("userId", userId)
            startActivity(homeIntent)
        }
    }
}