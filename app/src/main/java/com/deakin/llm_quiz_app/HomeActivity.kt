package com.deakin.llm_quiz_app

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.deakin.llm_quiz_app.data.DatabaseHelper

class HomeActivity : AppCompatActivity() {
    fun String.title() = replaceFirstChar(Char::titlecase)
    var userId: Int = -1

    fun createQuestionDiv(interest: String): LinearLayout {
        val questionDiv = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            setBackgroundResource(R.drawable.question_div)
            isClickable = true
            isFocusable = true

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        // Create TextView and add them to the container
        val titleText = TextView(this).apply {
            text = "${interest.title()} Quiz"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add TextView to container
        questionDiv.addView(titleText)

        questionDiv.setOnClickListener {
            val loadQuizIntent = Intent(this, LoadQuizActivity::class.java)
            loadQuizIntent.putExtra("topic", interest)
            loadQuizIntent.putExtra("userId", userId)
            startActivity(loadQuizIntent)
        }

        return questionDiv
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = DatabaseHelper(this, null)
        userId = intent.getIntExtra("userId", -1)
        val interests = db.getUserInterests(userId)

        if (userId == -1) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_LONG).show()
            startActivity(homeIntent)
        }

        val usernameText = findViewById<TextView>(R.id.usernameText)
        usernameText.text = db.getUser(userId).username.title()

        val btnInterests = findViewById<Button>(R.id.btnInterests)
        btnInterests.setOnClickListener {
            val interestsIntent = Intent(this, InterestsActivity::class.java)
            interestsIntent.putExtra("userId", userId)
            startActivity(interestsIntent)
        }

        // Profile
        val profileButton = findViewById<Button>(R.id.ProfileButton)
        profileButton.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userId", userId)
            startActivity(profileIntent)
        }

        // Clear existing views
        val quizzesLayout = findViewById<LinearLayout>(R.id.quizzes)
         quizzesLayout.removeAllViews()

        // add interests
        for (interest in interests) {
            quizzesLayout.addView(createQuestionDiv(interest))
        }
    }
}