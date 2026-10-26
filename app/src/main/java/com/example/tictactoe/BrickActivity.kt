package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class BrickView(
    context: Context,
    private val onScore: (Int) -> Unit,
    private val onLives: (Int) -> Unit,
    private val onOver: (Int, Boolean) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 1.15f

    private val rows = 5
    private val cols = 7
    private val alive = BooleanArray(rows * cols)
    private val rowColors = intArrayOf(
        Color.parseColor("#FB7185"), Color.parseColor("#FB923C"), Color.parseColor("#FACC15"),
        Color.parseColor("#22C55E"), Color.parseColor("#38BDF8")
    )

    private val bgP = paint(Color.parseColor("#1E293B"))
    private val brickP = paint(Color.WHITE)
    private val paddleP = paint(Color.parseColor("#F8FAFC"))
    private val ballP = paint(Color.parseColor("#FACC15"))

    private var px = 0f
    private var bx = 0f
    private var by = 0f
    private var vx = 0f
    private var vy = 0f
    private var stuck = true
    private var score = 0
    private var lives = 3

    fun start() {
        alive.fill(true)
        score = 0
        lives = 3
        px = width / 2f
        stuck = true
        onScore(0)
        onLives(lives)
        startLoop()
        invalidate()
    }

    private fun speed(): Float = height * minOf(1.0f, 0.62f + score * 0.004f)

    private fun launch() {
        val sp = speed()
        vx = sp * 0.35f * (if (Random.nextBoolean()) 1f else -1f)
        vy = -sqrt(sp * sp - vx * vx)
        stuck = false
    }

    override fun update(dt: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pw = w * 0.22f
        val ph = h * 0.022f
        val paddleY = h * 0.92f
        val r = w * 0.022f

        if (stuck) {
            bx = px
            by = paddleY - r
            return
        }

        bx += vx * dt
        by += vy * dt

        if (bx < r) {
            bx = r
            vx = abs(vx)
        } else if (bx > w - r) {
            bx = w - r
            vx = -abs(vx)
        }
        if (by < r) {
            by = r
            vy = abs(vy)
        }

        // المضرب
        if (vy > 0f && by + r >= paddleY && by + r <= paddleY + ph * 2f && abs(bx - px) <= pw / 2f + r) {
            val rel = ((bx - px) / (pw / 2f)).coerceIn(-1f, 1f)
            val sp = speed()
            vx = rel * sp * 0.9f
            vy = -sqrt(sp * sp - vx * vx)
            by = paddleY - r
        }

        // الطوب
        val margin = w * 0.03f
        val bw = (w - margin * 2f) / cols
        val bh = h * 0.04f
        val top0 = h * 0.08f
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!alive[row * cols + col]) continue
                val left = margin + col * bw
                val top = top0 + row * (bh + 4f)
                val right = left + bw - 4f
                val bottom = top + bh
                if (bx + r > left && bx - r < right && by + r > top && by - r < bottom) {
                    alive[row * cols + col] = false
                    score++
                    onScore(score)
                    val dx = minOf(bx - left, right - bx)
                    val dy = minOf(by - top, bottom - by)
                    if (dx < dy) vx = -vx else vy = -vy
                    if (alive.none { it }) {
                        stopLoop()
                        invalidate()
                        onOver(score, true)
                    }
                    return
                }
            }
        }

        // سقوط الكرة
        if (by - r > h) {
            lives--
            onLives(lives)
            if (lives <= 0) {
                stopLoop()
                invalidate()
                onOver(score, false)
            } else {
                stuck = true
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)

        val margin = w * 0.03f
        val bw = (w - margin * 2f) / cols
        val bh = h * 0.04f
        val top0 = h * 0.08f
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!alive[row * cols + col]) continue
                brickP.color = rowColors[row]
                val left = margin + col * bw
                val top = top0 + row * (bh + 4f)
                canvas.drawRoundRect(left, top, left + bw - 4f, top + bh, 6f, 6f, brickP)
            }
        }

        val pw = w * 0.22f
        val ph = h * 0.022f
        canvas.drawRoundRect(px - pw / 2f, h * 0.92f, px + pw / 2f, h * 0.92f + ph, 8f, 8f, paddleP)
        canvas.drawCircle(bx, by, w * 0.022f, ballP)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            grab()
            px = event.x.coerceIn(width * 0.11f, width * 0.89f)
            if (event.action == MotionEvent.ACTION_DOWN && running && stuck) launch()
            if (!running) invalidate()
        }
        return true
    }
}

class BrickActivity : BaseGameActivity() {

    private lateinit var game: BrickView
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🧱 كسر الطوب")

        content.addView(
            label("حرّك المضرب بإصبعك، واضغط لإطلاق الكرة وكسر كل الطوب!", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        scoreText = label("النقاط: 0", 22f)
        content.addView(scoreText, lp(top = dp(6)))

        livesText = label("❤️❤️❤️", 20f)
        content.addView(livesText, lp(top = dp(2)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        content.addView(button("ابدأ") { game.start() }, lp(height = dp(48), top = dp(10), bottom = dp(10)))

        game = BrickView(
            this,
            { s ->
                scoreText.text = "النقاط: $s"
                if (s > 0) Feedback.tap(this)
            },
            { l -> livesText.text = "❤️".repeat(maxOf(l, 0)) + "🖤".repeat(3 - maxOf(l, 0)) },
            { s, won -> onGameOver(s, won) }
        )
        content.addView(game, lp())
    }

    override fun onPause() {
        game.stopLoop()
        super.onPause()
    }

    private fun updateBest() {
        val best = Scores.get(this, "brick_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun onGameOver(score: Int, won: Boolean) {
        if (won) Feedback.win(this) else Feedback.lose(this)
        val best = Scores.get(this, "brick_best")
        val prefix = if (won) "🎉 كسرت كل الطوب!" else "انتهت اللعبة!"
        if (score > best) {
            Scores.put(this, "brick_best", score)
            scoreText.text = "$prefix رقم قياسي جديد 🏆 $score"
        } else {
            scoreText.text = "$prefix نتيجتك: $score"
        }
        updateBest()
    }
}
