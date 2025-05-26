package com.deakin.llm_quiz_app

import android.content.Intent
import android.nfc.Tag
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
import com.deakin.llm_quiz_app.model.User
import com.deakin.llm_quiz_app.model.UserRegistrationRequest
import org.json.JSONException
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {
    private val TAG = "SignUpActivity"
    private val REGISTER_URL = "http://10.0.2.2:5000/api/auth/register"


    private fun insertUser(user: UserRegistrationRequest) {
        val requestBodyJson = JSONObject().apply {
            put("username", user.username)
            put("email", user.email)
            put("password", user.password)
        }

        val url = REGISTER_URL

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, requestBodyJson,
            { response ->
                handleInsertUserResponse(response)
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

    private fun handleInsertUserResponse(response: JSONObject) {
        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()

        Log.d(TAG, "Quiz JSON: $response")

        val db = DatabaseHelper(this, null)

        val accountDetails = response.getJSONObject("account")
        val mongoID = accountDetails.getString("_id")
        val email = accountDetails.getString("email")
        val username = accountDetails.getString("username")

        val newUser = User(
            accountID = mongoID,
            username = username,
            email = email,
            tier = 0
        )

        Log.e(TAG, "${newUser}")

        val insertUserResult = db.insertUser(newUser)

        Log.d(TAG, "$insertUserResult")

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val createAccountButton = findViewById<Button>(R.id.CreateAccountButton)

        createAccountButton.setOnClickListener {
            val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
            val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
            val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
            val editTextPasswordConfirm = findViewById<EditText>(R.id.editTextPasswordConfirm)

            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()
            val passwordConfirm = editTextPasswordConfirm.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.contains(" ")) {
                Toast.makeText(this, "Username cannot contain whitespace", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userObj = UserRegistrationRequest(
                username,
                email,
                password
            )

            insertUser(userObj)
        }
    }
}