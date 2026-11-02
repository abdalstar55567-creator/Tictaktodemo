package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import kotlin.math.sin
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

    /** Tap anywhere on the game screen: start the run if needed, then jump. */
    fun tapAnywhere() {
        if (!running) start()
        jump()
        grab()
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
            // Keep a fair landing window: gap is measured in travel distance, not pixels.
            val obstacleWidth = w * (0.055f + Random.nextFloat() * 0.018f)
            val obstacleHeight = h * (0.085f + Random.nextFloat() * 0.045f)
            hurdles.add(floatArrayOf(w, obstacleWidth, obstacleHeight))
            nextGap = speed * (1.05f + Random.nextFloat() * 0.48f)
        }

        dist += speed * dt
        val newScore = (dist / (w * 0.12f)).toInt()
        if (newScore != score) {
            score = newScore
            onScore(score)
        }

        // الاصطدام
        val groundY = h * 0.8f
        val s = w * 0.105f
        val rx = w * 0.2f
        val rLeft = rx - s * 0.3f
        val rRight = rx + s * 0.3f
        val rBottom = groundY - offset
        val rTop = rBottom - s * 1.1f
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
        // Bright layered sky and distant hills use the same polished arcade language as Flappy.
        bgP.shader = LinearGradient(0f, 0f, 0f, groundY,
            intArrayOf(Color.rgb(22, 78, 125), Color.rgb(79, 166, 190), Color.rgb(190, 225, 217)),
            null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)
        bgP.shader = null
        val cloud = paint(Color.argb(115, 255, 255, 255))
        for (i in 0..3) {
            val cx = ((i * .31f + .12f - dist / (w * 22f)) % 1.3f) * w
            val cy = h * (.13f + (i % 2) * .09f)
            canvas.drawOval(cx-w*.07f,cy-h*.018f,cx+w*.07f,cy+h*.018f,cloud)
            canvas.drawCircle(cx-w*.025f,cy-h*.01f,h*.018f,cloud)
            canvas.drawCircle(cx+w*.025f,cy-h*.012f,h*.022f,cloud)
        }
        val hill = paint(Color.rgb(66, 143, 116))
        val hillPath = android.graphics.Path().apply {
            moveTo(0f, groundY-h*.12f)
            cubicTo(w*.22f,groundY-h*.29f,w*.34f,groundY-h*.04f,w*.55f,groundY-h*.16f)
            cubicTo(w*.76f,groundY-h*.31f,w*.86f,groundY-h*.10f,w,groundY-h*.20f)
            lineTo(w,groundY);lineTo(0f,groundY);close()
        }
        canvas.drawPath(hillPath,hill)
        canvas.drawRect(0f, groundY, w, h, paint(Color.rgb(43, 113, 74)))
        canvas.drawRect(0f, groundY+h*.035f, w, h, groundP)

        // Animated track dashes make speed and the running surface readable.
        val dash = paint(Color.rgb(228, 236, 230))
        val dashOffset = (dist * 1.8f) % (w*.18f)
        var markX = -dashOffset
        while (markX < w) {
            canvas.drawRoundRect(markX, groundY+h*.13f, markX+w*.075f, groundY+h*.145f, h*.01f, h*.01f, dash)
            markX += w*.18f
        }
        canvas.drawRect(0f,groundY,w,groundY+h*.012f,paint(Color.rgb(190,230,215)))
        for (o in hurdles) {
            val top = groundY - o[2]
            GameGraphics.drawHurdle(canvas,o[0],top,o[0]+o[1],groundY,dist)
        }

        val s = w * 0.105f
        val runnerX = w * 0.2f
        GameGraphics.drawRunner(canvas,runnerX,groundY,s,dist,offset)
        // The dotted arc shows the useful jump window relative to the next obstacle.
        val next = hurdles.firstOrNull { it[0] > runnerX }
        if (next != null && next[0]-runnerX < w*.43f) {
            val jumpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(150,250,204,21); style=Paint.Style.STROKE; strokeWidth=w*.004f; pathEffect=android.graphics.DashPathEffect(floatArrayOf(w*.018f,w*.014f),0f) }
            canvas.drawArc(RectF(runnerX-s*.45f,groundY-s*2.1f,runnerX+s*2.8f,groundY+s*.1f),205f,130f,false,jumpPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            tapAnywhere()
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

        val instructionText = label("المس الشاشة في أي مكان للقفز!", 16f, R.color.text_dim, false)
        content.addView(instructionText, lp(top = dp(4)))

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

        // The information area is also a touch target, not just the canvas.
        // Keep the start button's normal click behavior separate.
        instructionText.setOnClickListener { game.tapAnywhere() }
        scoreText.setOnClickListener { game.tapAnywhere() }
        bestText.setOnClickListener { game.tapAnywhere() }
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
