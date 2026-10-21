package com.example.tictactoe

import android.os.Bundle
import android.widget.TextView
import kotlin.random.Random

class RpsActivity : BaseGameActivity() {

    private val icons = arrayOf("✊", "✋", "✌️")
    private val names = arrayOf("حجر", "ورقة", "مقص")

    private var wins = 0
    private var losses = 0
    private var draws = 0

    private lateinit var resultText: TextView
    private lateinit var shownText: TextView
    private lateinit var scoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("✊ حجر ورقة مقص")

        scoreText = label("", 17f, R.color.gold)
        content.addView(scoreText, lp(top = dp(8)))

        resultText = label("اختر حركتك", 26f)
        content.addView(resultText, lp(top = dp(28)))

        shownText = label("", 44f)
        content.addView(shownText, lp(top = dp(16), bottom = dp(24)))

        val r = row()
        for (i in 0 until 3) {
            val b = button(icons[i]) { play(i) }
            b.textSize = 32f
            r.addView(b, weightLp(dp(80)))
        }
        content.addView(r, lp(top = dp(8)))

        content.addView(
            button("تصفير النتيجة", false) {
                wins = 0
                losses = 0
                draws = 0
                resultText.text = "اختر حركتك"
                shownText.text = ""
                updateScore()
            },
            lp(height = dp(46), top = dp(28))
        )

        updateScore()
    }

    private fun play(player: Int) {
        val computer = Random.nextInt(3)
        shownText.text = "أنت ${icons[player]}   ضد   ${icons[computer]} الكمبيوتر"

        when {
            player == computer -> {
                draws++
                resultText.text = "تعادل 🤝"
                Feedback.draw(this)
            }
            (player - computer + 3) % 3 == 1 -> {
                wins++
                resultText.text = "فزت! ${names[player]} تغلب ${names[computer]} 🎉"
                Feedback.win(this)
            }
            else -> {
                losses++
                resultText.text = "خسرت! ${names[computer]} تغلب ${names[player]} 😅"
                Feedback.lose(this)
            }
        }
        updateScore()
    }

    private fun updateScore() {
        scoreText.text = "فوز: $wins   |   خسارة: $losses   |   تعادل: $draws"
    }
}
