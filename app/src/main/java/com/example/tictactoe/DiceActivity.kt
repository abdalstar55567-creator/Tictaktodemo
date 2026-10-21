package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

class DiceActivity : BaseGameActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val faces = arrayOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

    private var wins = 0
    private var losses = 0
    private var draws = 0
    private var rolling = false

    private lateinit var playerDie: TextView
    private lateinit var cpuDie: TextView
    private lateinit var resultText: TextView
    private lateinit var scoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎲 رمي النرد")

        scoreText = label("", 17f, R.color.gold)
        content.addView(scoreText, lp(top = dp(8)))

        resultText = label("ارمِ النرد! صاحب الرقم الأكبر يفوز", 18f)
        content.addView(resultText, lp(top = dp(24)))

        val r = row()
        playerDie = label("🎲", 84f, R.color.x_color)
        cpuDie = label("🎲", 84f, R.color.o_color)
        r.addView(column("أنت", playerDie), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(column("الكمبيوتر", cpuDie), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        content.addView(r, lp(top = dp(24)))

        content.addView(button("🎲  ارمِ النرد") { roll() }, lp(height = dp(60), top = dp(32)))

        content.addView(
            button("تصفير النتيجة", false) {
                wins = 0
                losses = 0
                draws = 0
                updateScore()
            },
            lp(height = dp(46), top = dp(12))
        )

        updateScore()
    }

    private fun column(title: String, die: TextView): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.gravity = Gravity.CENTER
        c.addView(label(title, 16f, R.color.text_dim))
        c.addView(die)
        return c
    }

    private fun roll() {
        if (rolling) return
        rolling = true
        resultText.text = "..."
        var steps = 0

        handler.post(object : Runnable {
            override fun run() {
                if (steps < 8) {
                    playerDie.text = faces.random()
                    cpuDie.text = faces.random()
                    steps++
                    Feedback.tap(this@DiceActivity)
                    handler.postDelayed(this, 80)
                } else {
                    val p = Random.nextInt(6)
                    val c = Random.nextInt(6)
                    playerDie.text = faces[p]
                    cpuDie.text = faces[c]
                    when {
                        p > c -> {
                            wins++
                            resultText.text = "فزت! ${p + 1} ضد ${c + 1} 🎉"
                            Feedback.win(this@DiceActivity)
                        }
                        p < c -> {
                            losses++
                            resultText.text = "خسرت! ${p + 1} ضد ${c + 1} 😅"
                            Feedback.lose(this@DiceActivity)
                        }
                        else -> {
                            draws++
                            resultText.text = "تعادل! ${p + 1} ضد ${c + 1} 🤝"
                            Feedback.draw(this@DiceActivity)
                        }
                    }
                    updateScore()
                    rolling = false
                }
            }
        })
    }

    private fun updateScore() {
        scoreText.text = "فوز: $wins   |   خسارة: $losses   |   تعادل: $draws"
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
