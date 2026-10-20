package com.example.tictactoe

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GuessNumberActivity : AppCompatActivity() {
    private var secret = (1..100).random()
    private var attempts = 0
    private var best = 0
    private lateinit var message: TextView
    private lateinit var attemptsText: TextView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        best = getSharedPreferences("hundredplus", MODE_PRIVATE).getInt("guess_best", 0)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 24, 28, 24)
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

        root.addView(label("100+  •  🔢 خمن الرقم", 28f, android.graphics.Color.rgb(255, 228, 77)))
        root.addView(label("اخترت رقمًا من 1 إلى 100. هل تستطيع معرفته؟", 18f))
        message = label("أدخل تخمينك أدناه 👇", 19f, android.graphics.Color.rgb(54, 231, 255))
        root.addView(message)
        input = EditText(this).apply {
            hint = "رقم من 1 إلى 100"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            textSize = 22f
            gravity = Gravity.CENTER
            setSingleLine(true)
        }
        root.addView(input, LinearLayout.LayoutParams(-1, 60))
        val guess = Button(this).apply {
            text = "تحقق من الرقم"
            setOnClickListener { checkGuess() }
        }
        root.addView(guess)
        attemptsText = label("", 17f)
        root.addView(attemptsText)
        root.addView(Button(this).apply {
            text = "رقم جديد"
            setOnClickListener { newGame() }
        })
        setContentView(root)
        updateAttempts()
    }

    private fun checkGuess() {
        val value = input.text.toString().toIntOrNull()
        if (value == null || value !in 1..100) {
            message.text = "أدخل رقمًا صحيحًا بين 1 و100."
            return
        }
        attempts++
        when {
            value < secret -> { message.text = "⬆️ الرقم أكبر من $value"; Feedback.tap(this) }
            value > secret -> { message.text = "⬇️ الرقم أصغر من $value"; Feedback.tap(this) }
            else -> {
                message.text = "🎉 أحسنت! الرقم هو $secret"
                if (best == 0 || attempts < best) {
                    best = attempts
                    getSharedPreferences("hundredplus", MODE_PRIVATE).edit().putInt("guess_best", best).apply()
                    message.append("\n🏆 أفضل نتيجة جديدة!")
                }
                Feedback.win(this)
            }
        }
        updateAttempts()
    }

    private fun newGame() {
        secret = (1..100).random()
        attempts = 0
        input.text.clear()
        message.text = "رقم جديد جاهز! حظًا موفقًا 🎮"
        updateAttempts()
    }

    private fun updateAttempts() {
        attemptsText.text = "المحاولات: $attempts     أفضل نتيجة: " + if (best == 0) "—" else best.toString()
    }
}
