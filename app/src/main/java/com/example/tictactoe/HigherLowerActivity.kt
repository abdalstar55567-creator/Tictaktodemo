package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.random.Random

class HigherLowerActivity : BaseGameActivity() {

    private var current = 0
    private var streak = 0
    private var over = false

    private lateinit var numberText: TextView
    private lateinit var statusText: TextView
    private lateinit var streakText: TextView
    private lateinit var bestText: TextView
    private lateinit var higherButton: Button
    private lateinit var lowerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("⬆⬇ أعلى أم أقل؟")

        content.addView(
            label("هل الرقم القادم (من 1 إلى 100) أعلى أم أقل من الرقم الحالي؟", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        streakText = label("", 20f, R.color.x_color)
        content.addView(streakText, lp(top = dp(16)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))

        numberText = label("", 90f)
        content.addView(numberText, lp(top = dp(20)))

        statusText = label("", 19f)
        content.addView(statusText, lp(top = dp(6), bottom = dp(18)))

        val r = row()
        higherButton = button("⬆ أعلى") { guess(true) }
        lowerButton = button("⬇ أقل") { guess(false) }
        higherButton.textSize = 20f
        lowerButton.textSize = 20f
        r.addView(higherButton, weightLp(dp(64)))
        r.addView(lowerButton, weightLp(dp(64)))
        content.addView(r, lp(top = dp(8)))

        content.addView(button("ابدأ من جديد", false) { newGame() }, lp(height = dp(46), top = dp(24)))
        newGame()
    }

    private fun newGame() {
        current = Random.nextInt(1, 101)
        streak = 0
        over = false
        numberText.text = current.toString()
        statusText.text = "خمّن!"
        higherButton.isEnabled = true
        lowerButton.isEnabled = true
        updateTexts()
    }

    private fun updateTexts() {
        streakText.text = "السلسلة: $streak"
        val best = Scores.get(this, "hl_best")
        bestText.text = if (best == 0) "" else "أفضل سلسلة: $best"
    }

    private fun guess(higher: Boolean) {
        if (over) return
        var next = Random.nextInt(1, 101)
        while (next == current) next = Random.nextInt(1, 101)

        val correct = if (higher) next > current else next < current
        numberText.text = next.toString()

        if (correct) {
            streak++
            current = next
            statusText.text = "✅ صحيح!"
            Feedback.match(this)
        } else {
            over = true
            statusText.text = "❌ خطأ! انتهت السلسلة عند $streak"
            higherButton.isEnabled = false
            lowerButton.isEnabled = false
            Feedback.lose(this)
            val best = Scores.get(this, "hl_best")
            if (streak > best) {
                Scores.put(this, "hl_best", streak)
                statusText.text = "انتهت! رقم قياسي جديد 🏆 $streak"
            }
        }
        updateTexts()
    }
}
