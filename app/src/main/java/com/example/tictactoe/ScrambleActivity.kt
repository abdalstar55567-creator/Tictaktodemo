package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class ScrambleActivity : BaseGameActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val words = listOf(
        "سيارة", "حديقة", "مكتبة", "شجرة", "قطار", "سمكة", "برتقال", "موزة",
        "كتاب", "نجمة", "جبل", "دجاجة", "فراشة", "سلحفاة", "مدرسة", "مطبخ",
        "حصان", "تفاحة", "شمس", "وردة", "مفتاح", "ساعة", "جمل", "سفينة", "طاولة", "حاسوب"
    )
    private val total = 8

    private var queue = listOf<String>()
    private var index = 0
    private var score = 0
    private var current = ""
    private var locked = false

    private lateinit var progress: TextView
    private lateinit var scrambled: TextView
    private lateinit var feedback: TextView
    private lateinit var input: EditText
    private lateinit var checkButton: Button
    private lateinit var skipButton: Button
    private lateinit var restartButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🧩 رتّب الحروف")

        progress = label("", 16f, R.color.text_dim)
        content.addView(progress, lp(top = dp(6)))

        scrambled = label("", 40f, R.color.x_color)
        content.addView(scrambled, lp(top = dp(24)))

        feedback = label("", 18f, R.color.gold)
        content.addView(feedback, lp(top = dp(10), bottom = dp(10)))

        input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "اكتب الكلمة الصحيحة"
        input.setHintTextColor(color(R.color.text_dim))
        input.setTextColor(color(R.color.text_main))
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        input.gravity = Gravity.CENTER
        input.setBackgroundResource(R.drawable.bg_panel)
        input.setPadding(dp(12), dp(12), dp(12), dp(12))
        content.addView(input, lp(top = dp(8)))

        checkButton = button("تحقق") { check() }
        content.addView(checkButton, lp(height = dp(52), top = dp(12)))

        skipButton = button("تخطّي", false) { next() }
        content.addView(skipButton, lp(height = dp(46), top = dp(8)))

        restartButton = button("العب من جديد") { newGame() }
        restartButton.visibility = View.GONE
        content.addView(restartButton, lp(height = dp(52), top = dp(16)))

        newGame()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun newGame() {
        queue = words.shuffled().take(total)
        index = 0
        score = 0
        restartButton.visibility = View.GONE
        checkButton.visibility = View.VISIBLE
        skipButton.visibility = View.VISIBLE
        input.visibility = View.VISIBLE
        showWord()
    }

    private fun showWord() {
        locked = false
        current = queue[index]
        var letters = current.toList().shuffled()
        var tries = 0
        while (letters.joinToString("") == current && tries < 10) {
            letters = current.toList().shuffled()
            tries++
        }
        scrambled.text = letters.joinToString("  ")
        progress.text = "الكلمة ${index + 1} من $total   |   النقاط: $score"
        feedback.text = ""
        input.setText("")
    }

    private fun check() {
        if (locked) return
        val answer = input.text.toString().trim()
        if (answer.isEmpty()) return
        if (answer == current) {
            locked = true
            score++
            feedback.text = "✅ صحيح!"
            Feedback.match(this)
            handler.postDelayed({ next() }, 700)
        } else {
            feedback.text = "❌ حاول مرة أخرى"
            Feedback.lose(this)
        }
    }

    private fun next() {
        index++
        if (index >= total) finishGame() else showWord()
    }

    private fun finishGame() {
        checkButton.visibility = View.GONE
        skipButton.visibility = View.GONE
        input.visibility = View.GONE
        restartButton.visibility = View.VISIBLE
        progress.text = ""
        scrambled.text = "نتيجتك: $score / $total"

        val best = Scores.get(this, "scramble_best")
        if (score > best) {
            Scores.put(this, "scramble_best", score)
            feedback.text = "رقم قياسي جديد! 🏆"
        } else {
            feedback.text = "أفضل نتيجة: $best / $total"
        }
        Feedback.win(this)
    }
}
