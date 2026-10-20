package com.example.tictactoe

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RockPaperScissorsActivity : AppCompatActivity() {
    private val choices = listOf("✊", "✋", "✌️")
    private val names = listOf("حجر", "ورقة", "مقص")
    private var wins = 0
    private var losses = 0
    private var draws = 0
    private lateinit var result: TextView
    private lateinit var score: TextView
    private lateinit var opponent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(android.graphics.Color.rgb(16, 10, 42))
        }
        fun label(text: String, size: Float, color: Int = android.graphics.Color.WHITE) =
            TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                gravity = Gravity.CENTER
                setPadding(8, 12, 8, 12)
            }

        root.addView(label("100+  •  ✊ حجر ورقة مقص", 27f, android.graphics.Color.rgb(255, 228, 77)))
        root.addView(label("اختر حركتك!", 20f))
        opponent = label("🤖 الكمبيوتر جاهز", 25f)
        root.addView(opponent)
        result = label("أول من يفوز بـ 5 جولات هو البطل!", 18f)
        root.addView(result)
        score = label("", 18f, android.graphics.Color.rgb(54, 231, 255))
        root.addView(score)

        val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        choices.forEachIndexed { index, emoji ->
            val button = Button(this).apply {
                text = "$emoji\n${names[index]}"
                textSize = 17f
                setOnClickListener { play(index) }
            }
            row.addView(button, LinearLayout.LayoutParams(0, 100, 1f))
        }
        root.addView(row)
        val again = Button(this).apply {
            text = "إعادة النتيجة"
            setOnClickListener {
                wins = 0; losses = 0; draws = 0
                result.text = "لنبدأ من جديد! 🎮"
                opponent.text = "🤖 الكمبيوتر جاهز"
                updateScore()
            }
        }
        root.addView(again)
        setContentView(root)
        updateScore()
    }

    private fun play(user: Int) {
        val computer = choices.indices.random()
        opponent.text = "أنت: ${choices[user]}     الكمبيوتر: ${choices[computer]}"
        when {
            user == computer -> { draws++; result.text = "تعادل! 🤝"; Feedback.draw(this) }
            (user - computer + 3) % 3 == 1 -> { wins++; result.text = "فزت! 🎉"; Feedback.win(this) }
            else -> { losses++; result.text = "حاول مرة أخرى! 💪"; Feedback.lose(this) }
        }
        if (wins >= 5) result.text = "🏆 بطل 100+! فزت بخمس جولات"
        updateScore()
    }

    private fun updateScore() {
        score.text = "انتصاراتك: $wins    الكمبيوتر: $losses    تعادل: $draws"
    }
}
