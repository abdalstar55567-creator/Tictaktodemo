package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

class ReactionActivity : BaseGameActivity() {

    override val gameTitle: String get() = "سرعة رد الفعل"

    // 0 = جاهز، 1 = انتظار، 2 = اضغط الآن
    private var state = 0
    private var startTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var panel: TextView
    private lateinit var bestText: TextView

    private val readyRunnable = Runnable {
        state = 2
        startTime = SystemClock.elapsedRealtime()
        setPanel("#16A34A", "اضغط الآن! ⚡")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = Ui.column(this)
        col.addView(Ui.text(this, "⚡ اضغط بأسرع ما يمكن عندما يصبح اللون أخضر", 18f, top = 8))

        panel = TextView(this)
        panel.gravity = android.view.Gravity.CENTER
        panel.textSize = 24f
        panel.setTextColor(Color.WHITE)
        val lp = LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 320))
        lp.topMargin = Ui.dp(this, 24)
        panel.layoutParams = lp
        panel.setOnClickListener { onPanelClick() }
        col.addView(panel)

        bestText = Ui.text(this, "", 16f, R.color.gold, top = 20)
        col.addView(bestText)

        setContentView(Ui.scroll(this, col))
        setPanel("#2563EB", "اضغط للبدء")
        updateBest()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun setPanel(hex: String, text: String) {
        panel.background = Ui.panelBg(this, Color.parseColor(hex), 24)
        panel.text = text
    }

    private fun updateBest() {
        val best = Scores.get(this, "reaction_best")
        bestText.text = if (best == 0) "" else "🏆 أفضل زمن: $best ملي ثانية"
    }

    private fun onPanelClick() {
        when (state) {
            0 -> {
                state = 1
                setPanel("#DC2626", "انتظر اللون الأخضر...")
                handler.postDelayed(readyRunnable, Random.nextLong(1500, 4500))
            }
            1 -> {
                handler.removeCallbacks(readyRunnable)
                state = 0
                setPanel("#EA580C", "مبكر جداً! 😅\nاضغط للمحاولة من جديد")
                Feedback.lose(this)
            }
            else -> {
                val ms = (SystemClock.elapsedRealtime() - startTime).toInt()
                state = 0
                val record = Scores.saveLow(this, "reaction_best", ms)
                setPanel(
                    "#2563EB",
                    "$ms ملي ثانية" + (if (record) "\n🏆 رقم قياسي!" else "") +
                            "\nاضغط للمحاولة مجدداً"
                )
                Feedback.win(this)
                updateBest()
            }
        }
    }
}
