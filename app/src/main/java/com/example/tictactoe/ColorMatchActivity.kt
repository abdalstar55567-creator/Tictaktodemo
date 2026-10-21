package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import kotlin.random.Random

class ColorMatchActivity : BaseGameActivity() {

    private val names = arrayOf("أحمر", "أزرق", "أخضر", "أصفر")
    private val colors = intArrayOf(
        Color.parseColor("#EF4444"),
        Color.parseColor("#3B82F6"),
        Color.parseColor("#22C55E"),
        Color.parseColor("#FACC15")
    )

    private var running = false
    private var score = 0
    private var correct = 0
    private var timer: CountDownTimer? = null

    private lateinit var wordText: TextView
    private lateinit var timeText: TextView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView
    private lateinit var startButton: Button
    private val options = ArrayList<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎨 تحدي الألوان")

        content.addView(
            label("اضغط على اسم اللون الذي كُتبت به الكلمة\n(وليس على معنى الكلمة!)", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        timeText = label("الوقت: 30", 22f, R.color.x_color)
        content.addView(timeText, lp(top = dp(14)))

        scoreText = label("النقاط: 0", 24f)
        content.addView(scoreText, lp(top = dp(4)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        wordText = label("جاهز؟", 54f)
        content.addView(wordText, lp(top = dp(22), bottom = dp(22)))

        val row1 = row()
        val row2 = row()
        for (i in 0 until 4) {
            val b = button(names[i], false) { choose(i) }
            b.textSize = 20f
            b.isEnabled = false
            b.alpha = 0.5f
            options.add(b)
            (if (i < 2) row1 else row2).addView(b, weightLp(dp(58)))
        }
        content.addView(row1, lp(top = dp(4)))
        content.addView(row2, lp(top = dp(8)))

        startButton = button("ابدأ") { startGame() }
        content.addView(startButton, lp(height = dp(52), top = dp(24)))
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun updateBest() {
        val best = Scores.get(this, "color_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun setOptionsEnabled(on: Boolean) {
        options.forEach {
            it.isEnabled = on
            it.alpha = if (on) 1f else 0.5f
        }
    }

    private fun startGame() {
        if (running) return
        running = true
        score = 0
        scoreText.text = "النقاط: 0"
        startButton.text = "جارٍ اللعب..."
        setOptionsEnabled(true)
        nextWord()

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                timeText.text = "الوقت: ${ms / 1000}"
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
    }

    private fun nextWord() {
        val w = Random.nextInt(4)
        val c = Random.nextInt(4)
        wordText.text = names[w]
        wordText.setTextColor(colors[c])
        correct = c
    }

    private fun choose(i: Int) {
        if (!running) return
        if (i == correct) {
            score++
            Feedback.tap(this)
        } else {
            score = maxOf(0, score - 1)
            Feedback.lose(this)
        }
        scoreText.text = "النقاط: $score"
        nextWord()
    }

    private fun endGame() {
        running = false
        setOptionsEnabled(false)
        timeText.text = "انتهى الوقت!"
        wordText.text = "انتهى!"
        wordText.setTextColor(color(R.color.text_main))
        startButton.text = "العب من جديد"
        Feedback.win(this)

        val best = Scores.get(this, "color_best")
        if (score > best) {
            Scores.put(this, "color_best", score)
            scoreText.text = "رقم قياسي جديد! 🏆 $score"
        }
        updateBest()
    }
}
