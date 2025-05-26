package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.deakin.llm_quiz_app.data.DatabaseHelper
import com.deakin.llm_quiz_app.model.UserRegistrationRequest
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val db = DatabaseHelper(this, null)
    private val LOGIN_URL = "http://10.0.2.2:5000/api/auth/login"

    private fun loginUser(usernameOrEmail: String, password: String) {
        val requestBodyJson = JSONObject().apply {
            put("username", usernameOrEmail)
            put("password", password)
        }

        val url = LOGIN_URL

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBodyJson,
            { response ->
                handleLoginResponse(response)
            },
            { error ->
                val errorMsg = error.networkResponse?.let {
                    "HTTP ${it.statusCode}: ${String(it.data)}"
                } ?: error.message ?: "Unknown error"
                Log.e("Error", "Error fetching quiz: $errorMsg", error)
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

    private fun handleLoginResponse(response: JSONObject) {
        val user = response.getJSONObject("user")
        val mongoID = user.getString("_id")

        Log.d("MainActivity", "$user")

        val userId = db.fetchUser(mongoID)

        Log.d("MainActivity", "$userId")

        if (userId != -1){
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.putExtra("userId", userId)
            startActivity(homeIntent)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)

        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            loginUser(username, password)

//            val userId = db.fetchUser(usernameEditText.text.toString().trim(), passwordEditText.text.toString())
//
//            if (userId > -1) {
//                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
//
//                val homeIntent = Intent(this, HomeActivity::class.java)
//                homeIntent.putExtra("userId", userId)
//                startActivity(homeIntent)
//            } else {
//                Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show()
//            }
        }

        signupButton.setOnClickListener {
            val signupIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupIntent)
        }
    }
}