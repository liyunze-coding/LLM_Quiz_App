package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.deakin.llm_quiz_app.data.DatabaseHelper
import com.deakin.llm_quiz_app.model.QuizQuestion
import com.deakin.llm_quiz_app.model.Question
import org.json.JSONObject

class QuizActivity : AppCompatActivity() {
    var choice = -1
    var score = 0
    var currentQuestionIndex = 0
    var questionList = mutableListOf<QuizQuestion>()
    var buttons: List<Button> = listOf()
    var userId: Int = -1
    val db = DatabaseHelper(this, null)

    private fun selectChoice(selectedIndex: Int) {
        choice = selectedIndex

        buttons.forEach {
            it.setBackgroundColor(getColor(R.color.optionWhite))
            it.setTextColor(getColor(R.color.black))
        }

        buttons[choice].setBackgroundColor(getColor(R.color.selectGray))
        findViewById<Button>(R.id.submitButton).isEnabled = true
    }

    private fun showResult() {
        val resultsIntent = Intent(this, ScoreActivity::class.java)
        resultsIntent.putExtra("score", "$score/${questionList.size}")
        resultsIntent.putExtra("userId", userId)
        startActivity(resultsIntent)
    }

    private fun nextQuestion() {
        findViewById<Button>(R.id.submitButton).text = "Submit"
        findViewById<Button>(R.id.submitButton).setOnClickListener { checkAnswer(choice) }
        if (currentQuestionIndex < questionList.size - 1) {
            resetButtons()
            currentQuestionIndex++
            loadQuestion()
        } else {

            showResult()
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        val currentQuestion = questionList[currentQuestionIndex]
        val correctIndex = currentQuestion.correctAnswerIndex

        // Turn correct answer green
        buttons[correctIndex].setBackgroundColor(getColor(R.color.correctGreen))
        buttons[correctIndex].setTextColor(getColor(R.color.white))

        // Disable all buttons to prevent multiple selections
        buttons.forEach { it.isEnabled = false }

        if (selectedIndex == correctIndex) {
            score++
        } else {
            // Turn selected answer red
            buttons[choice].setBackgroundColor(getColor(R.color.wrongRed))
            buttons[choice].setTextColor(getColor(R.color.white))
        }

        val question = Question(
            userId = userId,
            question = currentQuestion.question,
            optionA = currentQuestion.options[0],
            optionB = currentQuestion.options[1],
            optionC = currentQuestion.options[2],
            optionD = currentQuestion.options[3],
            selected = selectedIndex,
            correctAnswer = correctIndex
        )
        db.insertQuestion(question)

        // hide welcome text
        findViewById<TextView>(R.id.tvGreeting).visibility = View.INVISIBLE

        findViewById<Button>(R.id.submitButton).text = "Next"
        findViewById<Button>(R.id.submitButton).setOnClickListener { nextQuestion() }
    }

    private fun resetButtons() {
        buttons.forEach {
            it.isEnabled = true
            it.setBackgroundColor(getColor(R.color.optionWhite))
            it.setTextColor(getColor(R.color.black))
        }

        findViewById<Button>(R.id.submitButton).isEnabled = false
    }

    private fun loadQuestion() {
        val question = questionList[currentQuestionIndex]
        findViewById<TextView>(R.id.questionTitle).text = question.question
        findViewById<Button>(R.id.choice1).text = question.options[0]
        findViewById<Button>(R.id.choice2).text = question.options[1]
        findViewById<Button>(R.id.choice3).text = question.options[2]
        findViewById<Button>(R.id.choice4).text = question.options[3]
        findViewById<TextView>(R.id.progressDisplay).text = "${currentQuestionIndex + 1}/${questionList.size}"
        findViewById<ProgressBar>(R.id.progressBar).progress = currentQuestionIndex + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get questions
        val quizDataString = intent.getStringExtra("quizData") ?: ""
        userId = intent.getIntExtra("userId", -1)

        if (quizDataString == "" || userId == -1) {
            val mainIntent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            startActivity(mainIntent)
        }

        buttons = listOf(
            findViewById<Button>(R.id.choice1),
            findViewById<Button>(R.id.choice2),
            findViewById<Button>(R.id.choice3),
            findViewById<Button>(R.id.choice4)
        )

        // create list of quiz questions
        // parse to json object
        val jsonObject = JSONObject(quizDataString)

        // get value of "quiz"
        val quizArray = jsonObject.getJSONArray("quiz")

        for (i in 0 until quizArray.length()) {
            val item = quizArray.getJSONObject(i)
            val question = item.getString("question")
            val options = item.getJSONArray("options")
            val correctAnswer = item.getString("correct_answer")

            val optionList = mutableListOf<String>()
            for (j in 0 until options.length()) {
                optionList.add(options.getString(j))
            }

            val questionItem = QuizQuestion(question, optionList, correctAnswer)
            questionList.add(questionItem)
        }

        loadQuestion()
        resetButtons()
        findViewById<ProgressBar>(R.id.progressBar).max = quizArray.length()

        findViewById<Button>(R.id.choice1).setOnClickListener { selectChoice(0) }
        findViewById<Button>(R.id.choice2).setOnClickListener { selectChoice(1) }
        findViewById<Button>(R.id.choice3).setOnClickListener { selectChoice(2) }
        findViewById<Button>(R.id.choice4).setOnClickListener { selectChoice(3) }
        findViewById<Button>(R.id.submitButton).setOnClickListener { checkAnswer(choice) }
    }
}