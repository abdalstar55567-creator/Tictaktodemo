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
import kotlin.random.Random

class NumberMemoryActivity : BaseGameActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var level = 3
    private var secret = ""
    private var waitingInput = false

    private lateinit var levelText: TextView
    private lateinit var bestText: TextView
    private lateinit var numberText: TextView
    private lateinit var statusText: TextView
    private lateinit var input: EditText
    private lateinit var confirmButton: Button
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🧠 ذاكرة الأرقام")

        content.addView(
            label("احفظ الرقم الذي يظهر لثوانٍ ثم اكتبه من ذاكرتك!", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        levelText = label("", 22f, R.color.x_color)
        content.addView(levelText, lp(top = dp(14)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))

        numberText = label("", 44f)
        numberText.textDirection = View.TEXT_DIRECTION_LTR
        content.addView(numberText, lp(top = dp(24)))

        statusText = label("", 18f)
        content.addView(statusText, lp(top = dp(8), bottom = dp(12)))

        input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "اكتب الرقم"
        input.setHintTextColor(color(R.color.text_dim))
        input.setTextColor(color(R.color.text_main))
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        input.gravity = Gravity.CENTER
        input.setBackgroundResource(R.drawable.bg_panel)
        input.setPadding(dp(12), dp(12), dp(12), dp(12))
        input.visibility = View.GONE
        content.addView(input, lp(top = dp(8)))

        confirmButton = button("تأكيد") { confirm() }
        confirmButton.visibility = View.GONE
        content.addView(confirmButton, lp(height = dp(52), top = dp(12)))

        startButton = button("ابدأ") { startGame() }
        content.addView(startButton, lp(height = dp(52), top = dp(16)))

        updateTexts()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun updateTexts() {
        levelText.text = "المستوى: ${level - 2}  (${level} أرقام)"
        val best = Scores.get(this, "nummem_best")
        bestText.text = if (best == 0) "" else "أفضل مستوى: $best"
    }

    private fun startGame() {
        handler.removeCallbacksAndMessages(null)
        level = 3
        startButton.visibility = View.GONE
        showNumber()
    }

    private fun showNumber() {
        updateTexts()
        waitingInput = false
        input.visibility = View.GONE
        confirmButton.visibility = View.GONE
        input.setText("")

        val sb = StringBuilder()
        for (i in 0 until level) sb.append(Random.nextInt(10))
        secret = sb.toString()

        numberText.text = secret
        statusText.text = "احفظ الرقم..."

        handler.postDelayed({
            numberText.text = "؟؟؟"
            statusText.text = "اكتب الرقم الذي رأيته"
            input.visibility = View.VISIBLE
            confirmButton.visibility = View.VISIBLE
            waitingInput = true
        }, 1200L + level * 450L)
    }

    private fun confirm() {
        if (!waitingInput) return
        waitingInput = false
        val answer = input.text.toString().trim()
        input.visibility = View.GONE
        confirmButton.visibility = View.GONE

        if (answer == secret) {
            Feedback.match(this)
            statusText.text = "✅ صحيح!"
            numberText.text = secret
            level++
            handler.postDelayed({ showNumber() }, 900)
        } else {
            Feedback.lose(this)
            val reached = level - 3
            numberText.text = secret
            statusText.text = "❌ الرقم كان $secret — وصلت للمستوى $reached"
            val best = Scores.get(this, "nummem_best")
            if (reached > best) {
                Scores.put(this, "nummem_best", reached)
                statusText.text = "🏆 رقم قياسي! وصلت للمستوى $reached"
            }
            startButton.text = "العب من جديد"
            startButton.visibility = View.VISIBLE
            level = 3
            updateTexts()
        }
    }
}
