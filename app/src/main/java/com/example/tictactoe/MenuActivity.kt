package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var soundButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        soundButton = findViewById(R.id.menuSound)

        findViewById<Button>(R.id.playXo).setOnClickListener {
            Feedback.tap(this)
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.playMemory).setOnClickListener {
            Feedback.tap(this)
            startActivity(Intent(this, MemoryActivity::class.java))
        }

        soundButton.setOnClickListener {
            Feedback.setEnabled(this, !Feedback.enabled(this))
            Feedback.tap(this)
            updateSoundButton()
        }

        updateSoundButton()
    }

    private fun updateSoundButton() {
        soundButton.text =
            if (Feedback.enabled(this)) "🔊  الصوت والاهتزاز: تشغيل" else "🔇  الصوت والاهتزاز: إيقاف"
    }
}
