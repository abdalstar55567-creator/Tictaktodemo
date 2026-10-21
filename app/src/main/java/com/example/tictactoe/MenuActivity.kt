package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var soundButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<TextView>(R.id.menuCount).text =
            "${Games.all.size} ألعاب جاهزة — والمزيد قريباً!"

        val list = findViewById<LinearLayout>(R.id.gamesList)
        Games.all.forEachIndexed { i, game ->
            list.addView(
                Ui.button(this, game.emoji + "   " + game.title, i % 2 == 0, 12, 60) {
                    Feedback.tap(this)
                    startActivity(Intent(this, game.cls))
                }
            )
        }

        soundButton = findViewById(R.id.menuSound)
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
