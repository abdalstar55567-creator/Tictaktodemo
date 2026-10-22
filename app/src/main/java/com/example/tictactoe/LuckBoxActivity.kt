package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.random.Random

class LuckBoxActivity : BaseGameActivity() {

    private val boxes = ArrayList<Button>()
    private var prize = 0
    private var picked = false
    private var streak = 0
    private var rounds = 0
    private var hits = 0

    private lateinit var statusText: TextView
    private lateinit var streakText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎁 حظ الصندوق")

        content.addView(
            label("أحد الصناديق الثلاثة يحتوي على الجائزة. اختر بحظك!", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        streakText = label("", 20f, R.color.x_color)
        content.addView(streakText, lp(top = dp(14)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))

        statusText = label("", 20f)
        content.addView(statusText, lp(top = dp(18), bottom = dp(18)))

        val r = row()
        for (i in 0 until 3) {
            val b = button("🎁", false) { pick(i) }
            b.textSize = 40f
            boxes.add(b)
            r.addView(b, weightLp(dp(110)))
        }
        content.addView(r, lp(top = dp(8)))

        content.addView(button("جولة جديدة") { newRound() }, lp(height = dp(52), top = dp(28)))
        newRound()
    }

    private fun newRound() {
        prize = Random.nextInt(3)
        picked = false
        statusText.text = "اختر صندوقاً"
        boxes.forEach {
            it.text = "🎁"
            it.alpha = 1f
        }
        updateTexts()
    }

    private fun updateTexts() {
        streakText.text = "سلسلة الفوز: $streak   |   الإصابات: $hits من $rounds"
        val best = Scores.get(this, "luck_best")
        bestText.text = if (best == 0) "" else "أفضل سلسلة: $best"
    }

    private fun pick(i: Int) {
        if (picked) return
        picked = true
        rounds++

        for (k in 0 until 3) {
            boxes[k].text = if (k == prize) "🏆" else "💨"
            if (k != i) boxes[k].alpha = 0.55f
        }

        if (i == prize) {
            streak++
            hits++
            statusText.text = "🎉 وجدت الجائزة!"
            Feedback.win(this)
            val best = Scores.get(this, "luck_best")
            if (streak > best) Scores.put(this, "luck_best", streak)
        } else {
            streak = 0
            statusText.text = "😅 حظ أوفر في المرة القادمة"
            Feedback.lose(this)
        }
        updateTexts()
    }
}
