package com.example.tictactoe

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

class NumberOrderActivity : BaseGameActivity() {

    private val buttons = ArrayList<Button>()
    private var next = 1
    private var startTime = 0L
    private var running = false

    private lateinit var infoText: TextView
    private lateinit var bestText: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔟 رتّب الأرقام")

        content.addView(
            label("اضغط الأرقام من 1 إلى 16 بالترتيب بأسرع ما يمكن!", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        infoText = label("اضغط ابدأ", 24f, R.color.x_color)
        content.addView(infoText, lp(top = dp(12)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2), bottom = dp(12)))
        updateBest()

        val grid = GridLayout(this)
        grid.columnCount = 4
        grid.rowCount = 4
        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / 4 - dp(8), dp(76))
        for (i in 0 until 16) {
            val b = button("", false) { }
            b.textSize = 22f
            b.setPadding(0, 0, 0, 0)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(4), dp(4), dp(4), dp(4))
            b.layoutParams = p
            b.isEnabled = false
            grid.addView(b)
            buttons.add(b)
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        startButton = button("ابدأ") { startGame() }
        content.addView(startButton, lp(height = dp(52), top = dp(20)))
    }

    private fun updateBest() {
        val best = Scores.get(this, "order_best")
        bestText.text = if (best == 0) "" else "أفضل وقت: ${best / 10}.${best % 10} ثانية"
    }

    private fun startGame() {
        val numbers = (1..16).shuffled()
        next = 1
        running = true
        startTime = SystemClock.elapsedRealtime()
        infoText.text = "ابحث عن 1"
        startButton.text = "إعادة"

        for (i in 0 until 16) {
            val b = buttons[i]
            val n = numbers[i]
            b.text = n.toString()
            b.isEnabled = true
            b.alpha = 1f
            b.setOnClickListener { onNumber(b, n) }
        }
    }

    private fun onNumber(b: Button, n: Int) {
        if (!running) return
        if (n != next) {
            Feedback.lose(this)
            return
        }
        Feedback.tap(this)
        b.isEnabled = false
        b.alpha = 0.25f
        next++

        if (next > 16) {
            running = false
            val tenths = ((SystemClock.elapsedRealtime() - startTime) / 100).toInt()
            infoText.text = "🎉 أنهيت في ${tenths / 10}.${tenths % 10} ثانية"
            startButton.text = "العب من جديد"
            Feedback.win(this)

            val best = Scores.get(this, "order_best")
            if (best == 0 || tenths < best) {
                Scores.put(this, "order_best", tenths)
                infoText.text = "🏆 رقم قياسي! ${tenths / 10}.${tenths % 10} ثانية"
            }
            updateBest()
        } else {
            infoText.text = "ابحث عن $next"
        }
    }
}
