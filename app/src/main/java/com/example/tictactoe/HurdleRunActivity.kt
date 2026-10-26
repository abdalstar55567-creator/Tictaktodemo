package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.random.Random

class HurdleView(
    context: Context,
    private val onScore: (Int) -> Unit,
    private val onOver: (Int) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 0.7f

    private val bgP = paint(Color.parseColor("#1E293B"))
    private val groundP = paint(Color.parseColor("#475569"))
    private val hurdleP = paint(Color.parseColor("#FB7185"))
    private val stripeP = paint(Color.parseColor("#FACC15"))
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG)

    private var offset = 0f      // ارتفاع العدّاء عن الأرض
    private var vy = 0f
    private var dist = 0f
    private var score = 0
    private var nextGap = 0f
    private val hurdles = ArrayList<FloatArray>()   // x, عرض, ارتفاع

    init {
        textP.textAlign = Paint.Align.CENTER
    }

    fun start() {
        hurdles.clear()
        offset = 0f
        vy = 0f
        dist = 0f
        score = 0
        nextGap = width * 0.9f
        onScore(0)
        startLoop()
        invalidate()
    }

    private fun jump() {
        if (running && offset <= 0f) vy = height * 1.85f
    }

    override fun update(dt: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val speed = w * minOf(1.5f, 0.6f + score * 0.012f)

        // القفز
        if (offset > 0f || vy > 0f) {
            vy -= h * 6.2f * dt
            offset += vy * dt
            if (offset <= 0f) {
                offset = 0f
                vy = 0f
            }
        }

        // الحواجز
        for (o in hurdles) o[0] -= speed * dt
        hurdles.removeAll { it[0] + it[1] < 0f }

        val lastRight = if (hurdles.isEmpty()) -1f else hurdles.last()[0] + hurdles.last()[1]
        if (hurdles.isEmpty() || w - lastRight >= nextGap) {
            hurdles.add(floatArrayOf(w, w * 0.05f, h * (0.09f + Random.nextFloat() * 0.07f)))
            nextGap = speed * (0.8f + Random.nextFloat() * 0.55f)
        }

        dist += speed * dt
        val newScore = (dist / (w * 0.12f)).toInt()
        if (newScore != score) {
            score = newScore
            onScore(score)
        }

        // الاصطدام
        val groundY = h * 0.8f
        val s = w * 0.09f
        val rx = w * 0.2f
        val rLeft = rx - s * 0.3f
        val rRight = rx + s * 0.3f
        val rBottom = groundY - offset
        val rTop = rBottom - s * 0.9f
        for (o in hurdles) {
            val oLeft = o[0]
            val oRight = o[0] + o[1]
            val oTop = groundY - o[2]
            if (rLeft < oRight && rRight > oLeft && rTop < groundY && rBottom > oTop) {
                stopLoop()
                onOver(score)
                return
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val groundY = h * 0.8f
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)
        canvas.drawRect(0f, groundY, w, h, groundP)

        for (o in hurdles) {
            val top = groundY - o[2]
            canvas.drawRoundRect(o[0], top, o[0] + o[1], groundY, 6f, 6f, hurdleP)
            canvas.drawRect(o[0], top + o[2] * 0.25f, o[0] + o[1], top + o[2] * 0.45f, stripeP)
        }

        val s = w * 0.09f
        textP.textSize = s * 1.3f
        canvas.drawText("🏃", w * 0.2f, groundY - offset - s * 0.05f, textP)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            grab()
            jump()
        }
        return true
    }
}

class HurdleRunActivity : BaseGameActivity() {

    private lateinit var game: HurdleView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🏃 سباق الحواجز")

        content.addView(
            label("اضغط على الشاشة للقفز فوق الحواجز!", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        scoreText = label("المسافة: 0 متر", 22f)
        content.addView(scoreText, lp(top = dp(6)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        content.addView(button("ابدأ السباق") { game.start() }, lp(height = dp(48), top = dp(10), bottom = dp(10)))

        game = HurdleView(
            this,
            { s -> scoreText.text = "المسافة: $s متر" },
            { s -> onGameOver(s) }
        )
        content.addView(game, lp())
    }

    override fun onPause() {
        game.stopLoop()
        super.onPause()
    }

    private fun updateBest() {
        val best = Scores.get(this, "hurdle_best")
        bestText.text = if (best == 0) "" else "أفضل مسافة: $best متر"
    }

    private fun onGameOver(score: Int) {
        Feedback.lose(this)
        val best = Scores.get(this, "hurdle_best")
        if (score > best) {
            Scores.put(this, "hurdle_best", score)
            scoreText.text = "اصطدمت! رقم قياسي جديد 🏆 $score متر"
        } else {
            scoreText.text = "اصطدمت! وصلت إلى $score متر"
        }
        updateBest()
    }
}
