package com.example.tictactoe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.GridLayout
import android.widget.TextView
import kotlin.random.Random

class SimonActivity : BaseGameActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val padColors = intArrayOf(
        Color.parseColor("#22C55E"),
        Color.parseColor("#EF4444"),
        Color.parseColor("#FACC15"),
        Color.parseColor("#3B82F6")
    )

    private val sequence = ArrayList<Int>()
    private val pads = ArrayList<TextView>()
    private var userIndex = 0
    private var accepting = false
    private var playing = false

    private lateinit var statusText: TextView
    private lateinit var levelText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔴 لعبة سيمون")

        content.addView(
            label("شاهد تسلسل الألوان ثم كرّره بنفس الترتيب!", 16f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        levelText = label("المستوى: 0", 22f, R.color.x_color)
        content.addView(levelText, lp(top = dp(14)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        statusText = label("اضغط ابدأ", 20f)
        content.addView(statusText, lp(top = dp(10), bottom = dp(14)))

        val grid = GridLayout(this)
        grid.columnCount = 2
        grid.rowCount = 2
        val size = minOf((resources.displayMetrics.widthPixels - dp(80)) / 2, dp(140))
        for (i in 0 until 4) {
            val pad = TextView(this)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(6), dp(6), dp(6), dp(6))
            pad.layoutParams = p
            val bg = GradientDrawable()
            bg.cornerRadius = dp(20).toFloat()
            bg.setColor(padColors[i])
            pad.background = bg
            pad.alpha = 0.45f
            pad.setOnClickListener { onPad(i) }
            grid.addView(pad)
            pads.add(pad)
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        content.addView(button("ابدأ") { startGame() }, lp(height = dp(52), top = dp(20)))
    }

    private fun updateBest() {
        val best = Scores.get(this, "simon_best")
        bestText.text = if (best == 0) "" else "أفضل مستوى: $best"
    }

    private fun flash(i: Int) {
        pads[i].alpha = 1f
        handler.postDelayed({ pads[i].alpha = 0.45f }, 350)
    }

    private fun startGame() {
        handler.removeCallbacksAndMessages(null)
        sequence.clear()
        playing = true
        nextRound()
    }

    private fun nextRound() {
        sequence.add(Random.nextInt(4))
        userIndex = 0
        accepting = false
        levelText.text = "المستوى: ${sequence.size}"
        statusText.text = "شاهد التسلسل..."

        for (k in sequence.indices) {
            handler.postDelayed({
                flash(sequence[k])
                Feedback.tap(this)
            }, 700L * k + 600L)
        }

        handler.postDelayed({
            accepting = true
            statusText.text = "دورك!"
        }, 700L * sequence.size + 600L)
    }

    private fun onPad(i: Int) {
        if (!accepting || !playing) return
        flash(i)

        if (i != sequence[userIndex]) {
            accepting = false
            playing = false
            val reached = sequence.size - 1
            statusText.text = "❌ خطأ! وصلت للمستوى $reached"
            Feedback.lose(this)
            val best = Scores.get(this, "simon_best")
            if (reached > best) {
                Scores.put(this, "simon_best", reached)
                statusText.text = "🏆 رقم قياسي جديد! وصلت للمستوى $reached"
            }
            updateBest()
            return
        }

        Feedback.tap(this)
        userIndex++
        if (userIndex == sequence.size) {
            accepting = false
            statusText.text = "ممتاز! ✨"
            Feedback.match(this)
            handler.postDelayed({ nextRound() }, 900)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
