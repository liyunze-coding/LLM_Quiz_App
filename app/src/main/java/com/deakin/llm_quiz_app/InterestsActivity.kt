package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.deakin.llm_quiz_app.data.DatabaseHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class InterestsActivity : AppCompatActivity() {
    private val maxSelection = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_interests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = intent.getIntExtra("userId", -1)
        val db = DatabaseHelper(this, null)

        if (userId == -1) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            startActivity(homeIntent)
        }

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        val btnHome = findViewById<Button>(R.id.btnHome)
        var selectedTopics = db.getUserInterests(userId)

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip

            if (selectedTopics.contains(chip.text.toString())) {
                chip.isChecked = true
            }

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                val label = chip.text.toString().trim()

                if (isChecked) {
                    if (selectedTopics.size >= maxSelection) {
                        chip.isChecked = false
                        Toast.makeText(this, "Maximum 10 topics allowed", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedTopics.add(label)
                    }
                } else {
                    selectedTopics.remove(label)
                }
            }
        }

        btnHome.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)

            // add interests to db
            val result = db.insertUserInterests(userId, selectedTopics)

            if (result) {
                Toast.makeText(this, "Saved your interests!", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Failed to save interests", Toast.LENGTH_SHORT).show()
            }

            homeIntent.putExtra("userId", userId)
            startActivity(homeIntent)
        }
    }
}