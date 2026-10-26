package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import kotlin.random.Random

class StopBarView(context: Context) : GameLoopView(context) {

    override val aspect: Float get() = 0.22f

    private val bgP = paint(Color.parseColor("#273449"))
    private val zoneP = paint(Color.parseColor("#22C55E"))
    private val markP = paint(Color.WHITE)

    private var pos = 0f          // 0..1
    private var dir = 1f
    private var level = 0
    private var zoneCenter = 0.5f
    private var zoneWidth = 0.3f

    val currentLevel: Int get() = level

    fun start() {
        level = 0
        pos = 0f
        dir = 1f
        newZone()
        startLoop()
        invalidate()
    }

    private fun newZone() {
        zoneWidth = maxOf(0.05f, 0.30f - level * 0.02f)
        zoneCenter = zoneWidth / 2f + Random.nextFloat() * (1f - zoneWidth)
    }

    /** يرجع true إذا توقف المؤشر داخل المنطقة الخضراء. */
    fun press(): Boolean {
        if (!running) return false
        val inside = pos >= zoneCenter - zoneWidth / 2f && pos <= zoneCenter + zoneWidth / 2f
        if (inside) {
            level++
            newZone()
            invalidate()
        } else {
            stopLoop()
            invalidate()
        }
        return inside
    }

    override fun update(dt: Float) {
        val speed = 0.7f + level * 0.12f
        pos += dir * speed * dt
        if (pos >= 1f) {
            pos = 1f
            dir = -1f
        } else if (pos <= 0f) {
            pos = 0f
            dir = 1f
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRoundRect(0f, 0f, w, h, 20f, 20f, bgP)
        canvas.drawRect(
            (zoneCenter - zoneWidth / 2f) * w, 0f,
            (zoneCenter + zoneWidth / 2f) * w, h, zoneP
        )
        canvas.drawRoundRect(pos * w - 6f, 0f, pos * w + 6f, h, 6f, 6f, markP)
    }
}

class StopBarActivity : BaseGameActivity() {

    private lateinit var bar: StopBarView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎚️ أوقف المؤشر")

        content.addView(
            label("أوقف المؤشر داخل المنطقة الخضراء! تضيق المنطقة ويزداد السرعة مع كل مستوى.", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        scoreText = label("المستوى: 0", 24f)
        content.addView(scoreText, lp(top = dp(14)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2), bottom = dp(14)))
        updateBest()

        bar = StopBarView(this)
        content.addView(bar, lp())

        content.addView(button("✋ أوقف!") { pressStop() }, lp(height = dp(90), top = dp(24)))
        content.addView(button("ابدأ من جديد", false) { restart() }, lp(height = dp(48), top = dp(14)))
    }

    override fun onPause() {
        bar.stopLoop()
        super.onPause()
    }

    private fun updateBest() {
        val best = Scores.get(this, "stopbar_best")
        bestText.text = if (best == 0) "" else "أفضل مستوى: $best"
    }

    private fun restart() {
        scoreText.text = "المستوى: 0"
        bar.start()
    }

    private fun pressStop() {
        if (!bar.running) return
        val ok = bar.press()
        val level = bar.currentLevel
        if (ok) {
            Feedback.match(this)
            scoreText.text = "المستوى: $level"
        } else {
            Feedback.lose(this)
            val best = Scores.get(this, "stopbar_best")
            if (level > best) {
                Scores.put(this, "stopbar_best", level)
                scoreText.text = "أخطأت! رقم قياسي جديد 🏆 المستوى $level"
            } else {
                scoreText.text = "أخطأت! وصلت للمستوى $level"
            }
            updateBest()
        }
    }
}
