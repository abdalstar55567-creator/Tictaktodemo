package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.RectF
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
    private val pipeHighlight = paint(Color.parseColor("#86EFAC"))
    private val cloudP = paint(Color.argb(65, 255, 255, 255))
    private val groundP = paint(Color.parseColor("#854D0E"))
    private val birdP = paint(Color.parseColor("#FACC15"))
    private val eyeP = paint(Color.BLACK)
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG)

    private var by = 0f
    private var vy = 0f
    private var score = 0
    private var waiting = true
    private var elapsed = 0f
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
        elapsed = 0f
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
        elapsed += dt
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
        bgP.shader = LinearGradient(0f, 0f, 0f, h,
            intArrayOf(Color.rgb(13, 40, 78), Color.rgb(50, 126, 168), Color.rgb(164, 216, 224)),
            null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)
        bgP.shader = null
        // Soft distant clouds add depth without external image assets.
        for (i in 0..3) {
            val cx = ((i * .31f + .18f - elapsed * .018f) % 1.25f) * w
            val cy = h * (.16f + (i % 2) * .14f)
            canvas.drawOval(cx-w*.075f,cy-h*.025f,cx+w*.075f,cy+h*.025f,cloudP)
            canvas.drawCircle(cx-w*.025f,cy-h*.012f,h*.025f,cloudP)
            canvas.drawCircle(cx+w*.018f,cy-h*.018f,h*.031f,cloudP)
        }

        for (pipe in pipes) {
            val x = pipe[0]
            val top = pipe[1]
            val bottom = top + gapH
            pipeP.shader = LinearGradient(x,0f,x+pipeW,0f,
                intArrayOf(Color.rgb(14,105,65),Color.rgb(57,211,117),Color.rgb(19,133,77)),null,Shader.TileMode.CLAMP)
            canvas.drawRect(x, 0f, x + pipeW, top, pipeP)
            canvas.drawRect(x, bottom, x + pipeW, h * 0.92f, pipeP)
            pipeP.shader = null
            canvas.drawRoundRect(x-pipeW*.08f,top-pipeW*.10f,x+pipeW*1.08f,top+pipeW*.06f,pipeW*.08f,pipeW*.08f,pipeP)
            canvas.drawRoundRect(x-pipeW*.08f,bottom-pipeW*.06f,x+pipeW*1.08f,bottom+pipeW*.10f,pipeW*.08f,pipeW*.08f,pipeP)
            canvas.drawRect(x+pipeW*.18f,0f,x+pipeW*.27f,top-pipeW*.12f,pipeHighlight)
            canvas.drawRect(x+pipeW*.18f,bottom+pipeW*.12f,x+pipeW*.27f,h*.92f,pipeHighlight)
        }
        canvas.drawRect(0f, h * 0.92f, w, h, groundP)
        canvas.drawRect(0f,h*.92f,w,h*.94f,paint(Color.rgb(189,137,58)))
        for (i in 0..12) canvas.drawLine(i*w/12f,h*.945f,(i+1)*w/12f,h,paint(Color.argb(75,40,25,12)))

        val bx = w * 0.28f
        val br = w * 0.04f
        GameGraphics.drawBird(canvas, bx, by, br*1.25f, elapsed, vy/(h*2.6f)*18f)

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
