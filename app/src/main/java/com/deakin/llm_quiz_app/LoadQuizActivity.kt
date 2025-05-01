package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoadQuizActivity : AppCompatActivity() {
    private val TAG = "QuestionActivity"
    private var loadingMessages = listOf(
        "Generating quiz...",
        "Loading...",
        "This seems to be taking a while...",
        "Almost there..."
    )
    var userId = -1

    private var loadingJob: Job? = null

    private fun startLoadingTextAnimation(loadingText: TextView) {
        loadingJob = CoroutineScope(Dispatchers.Main).launch {
            var index = 0
            while (isActive) {
                loadingText.text = loadingMessages[index % loadingMessages.size]
                index++
                delay(3000L) // 3 seconds
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_load_quiz)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val topic = intent.getStringExtra("topic") ?: ""
        userId = intent.getIntExtra("userId", -1)

        if (topic == "" || userId == -1) {
            val mainIntent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            startActivity(mainIntent)
        }

        val loadingText = findViewById<TextView>(R.id.loadingText)
        startLoadingTextAnimation(loadingText)

        fetchQuizFromFlask(topic)
    }

    private fun fetchQuizFromFlask(topic: String) {
        val topicUrl = topic.replace(" ", "+")
        val url = "http://10.0.2.2:6980/getQuiz?topic=$topicUrl" // Localhost for Android emulator

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                handleQuizResponse(response)
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    "HTTP ${it.statusCode}: ${String(it.data)}"
                } ?: error.message ?: "Unknown error"
                Log.e(TAG, "Error fetching quiz: $errorMsg", error)
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            600000,
            3, // 3 tries
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(jsonObjectRequest)
    }

    private fun handleQuizResponse(response: JSONObject) {
        // TODO: Display questions in UI
        Log.d(TAG, "Quiz JSON: $response")
        Toast.makeText(this, "Quiz loaded successfully!", Toast.LENGTH_SHORT).show()

        val questionIntent = Intent(this, QuizActivity::class.java)
        questionIntent.putExtra("quizData", response.toString())
        questionIntent.putExtra("userId", userId)

        startActivity(questionIntent)
        finish()
    }
}
