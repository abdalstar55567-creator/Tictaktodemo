package com.example.tictactoe

import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random

class GuessNumberActivity : BaseGameActivity() {

    private var secret = 0
    private var tries = 0
    private var finished = false

    private lateinit var hintText: TextView
    private lateinit var triesText: TextView
    private lateinit var bestText: TextView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔢 خمّن الرقم")

        content.addView(
            label("فكّرت في رقم بين 1 و 100.\nهل تستطيع تخمينه بأقل عدد من المحاولات؟", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        hintText = label("ابدأ التخمين!", 26f)
        content.addView(hintText, lp(top = dp(24)))

        triesText = label("", 16f, R.color.x_color)
        content.addView(triesText, lp(top = dp(6)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(4)))

        input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "اكتب رقمك"
        input.setHintTextColor(color(R.color.text_dim))
        input.setTextColor(color(R.color.text_main))
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        input.gravity = Gravity.CENTER
        input.setBackgroundResource(R.drawable.bg_panel)
        input.setPadding(dp(12), dp(12), dp(12), dp(12))
        content.addView(input, lp(top = dp(24)))

        content.addView(button("خمّن") { guess() }, lp(height = dp(52), top = dp(12)))
        content.addView(button("لعبة جديدة", false) { newGame() }, lp(height = dp(46), top = dp(10)))

        newGame()
    }

    private fun newGame() {
        secret = Random.nextInt(1, 101)
        tries = 0
        finished = false
        hintText.text = "ابدأ التخمين!"
        triesText.text = "المحاولات: 0"
        input.setText("")
        val best = Scores.get(this, "guess_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best محاولة"
    }

    private fun guess() {
        if (finished) return
        val n = input.text.toString().toIntOrNull()
        if (n == null || n < 1 || n > 100) {
            hintText.text = "أدخل رقماً من 1 إلى 100"
            return
        }

        tries++
        triesText.text = "المحاولات: $tries"
        input.setText("")

        when {
            n < secret -> {
                hintText.text = "⬆️ الرقم أكبر من $n"
                Feedback.tap(this)
            }
            n > secret -> {
                hintText.text = "⬇️ الرقم أصغر من $n"
                Feedback.tap(this)
            }
            else -> {
                finished = true
                hintText.text = "🎉 صحيح! الرقم $secret"
                Feedback.win(this)
                val best = Scores.get(this, "guess_best")
                if (best == 0 || tries < best) {
                    Scores.put(this, "guess_best", tries)
                    triesText.text = "رقم قياسي جديد! 🏆 ($tries محاولة)"
                }
                val newBest = Scores.get(this, "guess_best")
                bestText.text = "أفضل نتيجة: $newBest محاولة"
            }
        }
    }
}
