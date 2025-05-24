package com.deakin.llm_quiz_app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.deakin.llm_quiz_app.data.DatabaseHelper
import androidx.core.graphics.toColorInt
import com.deakin.llm_quiz_app.model.Option
import kotlinx.coroutines.selects.select

class HistoryActivity : AppCompatActivity() {
    // Helper to create default layout params
    private fun defaultLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Extension function to convert dp to px
    private fun Int.dp(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun createQuestionLayout(context: Context, questionIndex: Int, question: String, options: List<String>, selectedAnswer: Int, correctAnswer: Int) {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 20.dp(context)
                }
                setPadding(20,20,20,20)
                setBackgroundColor(Color.BLUE)
            }

            // Question number and title
            val questionTitle = TextView(context).apply {
                text = "Question"
                setTypeface(null, Typeface.BOLD)
                textSize = 20f
                layoutParams = defaultLayoutParams()
                setTextColor(Color.WHITE)
            }

            // Question description
            val questionDescription = TextView(context).apply {
                text = question
                layoutParams = defaultLayoutParams()
                setTextColor(Color.WHITE)
            }

            // Options
            var optionList: List<Option> = listOf(
                Option("⬤ ${options[0]}", "#FFFFFF", true),
                Option("⬤ ${options[1]}", "#FFFFFF", true),
                Option("⬤ ${options[2]}", "#FFFFFF", true),
                Option("⬤ ${options[3]}", "#FFFFFF", true)
            )

            optionList[selectedAnswer].color = "#ff0000"
            optionList[correctAnswer].color = "#00ff00"

            layout.addView(questionTitle)
            layout.addView(questionDescription)

            for ((text, color, addTopMargin) in optionList) {
                val optionText = TextView(context).apply {
                    this.text = text
                    textSize = 15f
                    if (color != null) setTextColor(color.toColorInt())
                    layoutParams = defaultLayoutParams().apply {
                        if (addTopMargin) topMargin = 10.dp(context)
                    }
                }
                layout.addView(optionText)
            }

            findViewById<LinearLayout>(R.id.history).addView(layout)
        }

        val userId = intent.getIntExtra("userId", -1)
        val db = DatabaseHelper(this, null)
        var questionHistory = db.getUserQuestionsHistory(userId).reversed()

        findViewById<LinearLayout>(R.id.history).removeAllViews()
        for ((index,pastQuestion) in questionHistory.withIndex()) {
            var options = listOf(pastQuestion.optionA, pastQuestion.optionB, pastQuestion.optionC, pastQuestion.optionD)
            createQuestionLayout(this, index+1, pastQuestion.question, options, pastQuestion.selected, pastQuestion.correctAnswer)
        }
    }
}