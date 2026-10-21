package com.example.tictactoe

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class TapSpeedActivity : BaseGameActivity() {

    override val gameTitle: String get() = "سرعة الضغط"

    private var taps = 0
    private var running = false
    private var done = false
    private var timer: CountDownTimer? = null

    private lateinit var timeText: TextView
    private lateinit var countText: TextView
    private lateinit var statusText: TextView
    private lateinit var tapButton: Button
    private lateinit var againButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = Ui.column(this)

        timeText = Ui.text(this, "10.0", 34f, R.color.gold, true, top = 8)
        col.addView(timeText)

        countText = Ui.text(this, "0", 56f, R.color.x_color, true, top = 4)
        col.addView(countText)

        statusText = Ui.text(this, "", 16f, R.color.text_dim, top = 4)
        col.addView(statusText)

        tapButton = Ui.button(this, "اضغط!", true, 24, 200) { onTap() }
        tapButton.textSize = 34f
        col.addView(tapButton)

        againButton = Ui.button(this, "إعادة", false, 16, 48) { reset() }
        againButton.visibility = View.GONE
        col.addView(againButton)

        setContentView(Ui.scroll(this, col))
        reset()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun reset() {
        timer?.cancel()
        taps = 0
        running = false
        done = false
        timeText.text = "10.0"
        countText.text = "0"
        val best = Scores.get(this, "tap_best")
        statusText.text = "اضغط أول مرة لبدء العدّ — لديك 10 ثوانٍ" +
                (if (best > 0) "\n🏆 أفضل نتيجة: $best" else "")
        tapButton.isEnabled = true
        againButton.visibility = View.GONE
    }

    private fun onTap() {
        if (done) return
        if (!running) {
            running = true
            timer = object : CountDownTimer(10000, 100) {
                override fun onTick(ms: Long) {
                    timeText.text = String.format(Locale.US, "%.1f", ms / 1000.0)
                }

                override fun onFinish() {
                    finishGame()
                }
            }.start()
        }
        taps++
        countText.text = taps.toString()
    }

    private fun finishGame() {
        done = true
        running = false
        timeText.text = "0.0"
        tapButton.isEnabled = false
        againButton.visibility = View.VISIBLE
        val record = Scores.saveHigh(this, "tap_best", taps)
        val rate = String.format(Locale.US, "%.1f", taps / 10.0)
        statusText.text = (if (record) "🏆 رقم قياسي جديد!\n" else "") +
                "$taps ضغطة — بمعدل $rate في الثانية"
        Feedback.win(this)
    }
}
