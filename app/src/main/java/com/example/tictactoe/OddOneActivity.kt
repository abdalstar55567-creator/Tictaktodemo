package com.example.tictactoe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import kotlin.random.Random

class OddOneActivity : BaseGameActivity() {

    private var running = false
    private var score = 0
    private var oddIndex = 0
    private var timer: CountDownTimer? = null

    private lateinit var grid: GridLayout
    private lateinit var timeText: TextView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎯 ابحث عن المختلف")

        content.addView(
            label("اضغط على المربع المختلف قليلاً في اللون! لديك 30 ثانية.", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        timeText = label("الوقت: 30", 22f, R.color.x_color)
        content.addView(timeText, lp(top = dp(12)))

        scoreText = label("النقاط: 0", 24f)
        content.addView(scoreText, lp(top = dp(4)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2), bottom = dp(12)))
        updateBest()

        grid = GridLayout(this)
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        startButton = button("ابدأ") { startGame() }
        content.addView(startButton, lp(height = dp(52), top = dp(20)))

        buildGrid(2, Color.parseColor("#273449"), Color.parseColor("#273449"))
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun updateBest() {
        val best = Scores.get(this, "odd_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun squareBg(c: Int): GradientDrawable {
        val d = GradientDrawable()
        d.cornerRadius = dp(10).toFloat()
        d.setColor(c)
        return d
    }

    private fun buildGrid(n: Int, base: Int, odd: Int) {
        grid.removeAllViews()
        grid.columnCount = n
        grid.rowCount = n
        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / n - dp(8), dp(100))
        oddIndex = Random.nextInt(n * n)
        for (i in 0 until n * n) {
            val v = View(this)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(4), dp(4), dp(4), dp(4))
            v.layoutParams = p
            v.background = squareBg(if (i == oddIndex) odd else base)
            v.setOnClickListener { onSquare(i) }
            grid.addView(v)
        }
    }

    private fun nextRound() {
        val n = minOf(2 + score / 3, 6)
        val hue = Random.nextFloat() * 360f
        val delta = maxOf(0.05f, 0.30f - score * 0.015f)
        val base = Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.90f))
        val odd = Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.90f - delta))
        buildGrid(n, base, odd)
    }

    private fun startGame() {
        if (running) return
        running = true
        score = 0
        scoreText.text = "النقاط: 0"
        startButton.text = "جارٍ اللعب..."
        nextRound()

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                timeText.text = "الوقت: ${ms / 1000}"
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
    }

    private fun onSquare(i: Int) {
        if (!running) return
        if (i == oddIndex) {
            score++
            scoreText.text = "النقاط: $score"
            Feedback.tap(this)
            nextRound()
        } else {
            Feedback.lose(this)
        }
    }

    private fun endGame() {
        running = false
        timeText.text = "انتهى الوقت!"
        startButton.text = "العب من جديد"
        Feedback.win(this)
        val best = Scores.get(this, "odd_best")
        if (score > best) {
            Scores.put(this, "odd_best", score)
            scoreText.text = "رقم قياسي جديد! 🏆 $score"
        }
        updateBest()
    }
}
