package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.random.Random

class FlappyView(
    context: Context,
    private val onScore: (Int) -> Unit,
    private val onOver: (Int) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 1.1f

    private val bgP = paint(Color.parseColor("#1E3A5F"))
    private val pipeP = paint(Color.parseColor("#22C55E"))
    private val groundP = paint(Color.parseColor("#854D0E"))
    private val birdP = paint(Color.parseColor("#FACC15"))
    private val eyeP = paint(Color.BLACK)
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG)

    private var by = 0f
    private var vy = 0f
    private var score = 0
    private var waiting = true
    private val pipes = ArrayList<FloatArray>()   // x, أعلى الفتحة, تم احتسابه

    init {
        textP.color = Color.WHITE
        textP.textAlign = Paint.Align.CENTER
    }

    fun start() {
        pipes.clear()
        by = height * 0.45f
        vy = 0f
        score = 0
        waiting = true
        onScore(0)
        startLoop()
        invalidate()
    }

    private fun flap() {
        if (!running) return
        waiting = false
        vy = -height * 0.8f
    }

    override fun update(dt: Float) {
        if (waiting) return
        val w = width.toFloat()
        val h = height.toFloat()
        val pipeW = w * 0.16f
        val gapH = h * 0.30f
        val speed = w * 0.42f
        val bx = w * 0.28f
        val br = w * 0.04f

        vy += h * 2.6f * dt
        by += vy * dt
        if (by < br) {
            by = br
            vy = 0f
        }

        for (p in pipes) p[0] -= speed * dt
        pipes.removeAll { it[0] + pipeW < 0f }
        if (pipes.isEmpty() || pipes.last()[0] <= w - w * 0.62f) {
            val top = h * 0.12f + Random.nextFloat() * (h * 0.92f - gapH - h * 0.24f)
            pipes.add(floatArrayOf(w, top, 0f))
        }

        for (p in pipes) {
            if (p[2] == 0f && p[0] + pipeW < bx) {
                p[2] = 1f
                score++
                onScore(score)
            }
            val overlapX = bx + br * 0.8f > p[0] && bx - br * 0.8f < p[0] + pipeW
            if (overlapX && (by - br * 0.8f < p[1] || by + br * 0.8f > p[1] + gapH)) {
                die()
                return
            }
        }

        if (by + br > h * 0.92f) die()
    }

    private fun die() {
        stopLoop()
        invalidate()
        onOver(score)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pipeW = w * 0.16f
        val gapH = h * 0.30f
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)

        for (p in pipes) {
            canvas.drawRect(p[0], 0f, p[0] + pipeW, p[1], pipeP)
            canvas.drawRect(p[0], p[1] + gapH, p[0] + pipeW, h * 0.92f, pipeP)
        }
        canvas.drawRect(0f, h * 0.92f, w, h, groundP)

        val bx = w * 0.28f
        val br = w * 0.04f
        canvas.drawCircle(bx, by, br, birdP)
        canvas.drawCircle(bx + br * 0.4f, by - br * 0.25f, br * 0.2f, eyeP)

        if (waiting && running) {
            textP.textSize = w * 0.05f
            canvas.drawText("اضغط للطيران!", w / 2f, h * 0.3f, textP)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            grab()
            flap()
        }
        return true
    }
}

class FlappyActivity : BaseGameActivity() {

    private lateinit var game: FlappyView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🐤 الطائر الطائر")

        content.addView(
            label("اضغط للطيران وتفادى الأنابيب!", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        scoreText = label("النقاط: 0", 22f)
        content.addView(scoreText, lp(top = dp(6)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        content.addView(button("ابدأ") { game.start() }, lp(height = dp(48), top = dp(10), bottom = dp(10)))

        game = FlappyView(
            this,
            { s ->
                scoreText.text = "النقاط: $s"
                if (s > 0) Feedback.tap(this)
            },
            { s -> onGameOver(s) }
        )
        content.addView(game, lp())
    }

    override fun onPause() {
        game.stopLoop()
        super.onPause()
    }

    private fun updateBest() {
        val best = Scores.get(this, "flappy_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun onGameOver(score: Int) {
        Feedback.lose(this)
        val best = Scores.get(this, "flappy_best")
        if (score > best) {
            Scores.put(this, "flappy_best", score)
            scoreText.text = "انتهت! رقم قياسي جديد 🏆 $score"
        } else {
            scoreText.text = "انتهت اللعبة! نتيجتك: $score"
        }
        updateBest()
    }
}
