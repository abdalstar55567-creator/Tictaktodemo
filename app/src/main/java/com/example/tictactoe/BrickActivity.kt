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
    private val onStage: (Int) -> Unit,
    private val onOver: (Int, Boolean) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 1.15f

    private var rows = 5
    private val cols = 7
    private var alive = BooleanArray(rows * cols)
    private var hitPoints = IntArray(rows * cols) { 1 }
    private var stage = 1
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
        score = 0
        lives = 3
        stage = 1
        px = width / 2f
        stuck = true
        setupStage()
        onScore(0)
        onLives(lives)
        startLoop()
        invalidate()
    }

    private fun setupStage() {
        rows = (4 + stage).coerceAtMost(8)
        alive = BooleanArray(rows * cols) { index ->
            val row = index / cols
            val col = index % cols
            // Later stages add gaps/patterns, but never remove all reachable bricks.
            stage == 1 || stage == 2 || (row + col + stage) % 5 != 0
        }
        hitPoints = IntArray(rows * cols) { index ->
            if (alive[index] && stage >= 3 && (index + stage) % 3 == 0) 2 else 1
        }
        stuck = true
        vx = 0f; vy = 0f
        onStage(stage)
    }

    private fun speed(): Float = height * (0.58f + (stage - 1) * 0.10f + minOf(0.30f, score * 0.002f)).coerceAtMost(1.28f)

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
                    val index = row * cols + col
                    hitPoints[index]--
                    if (hitPoints[index] <= 0) {
                        alive[index] = false
                        score++
                        onScore(score)
                    }
                    val dx = minOf(bx - left, right - bx)
                    val dy = minOf(by - top, bottom - by)
                    if (dx < dy) vx = -vx else vy = -vy
                    if (alive.none { it }) {
                        if (stage < 5) {
                            stage++
                            setupStage()
                            onScore(score)
                        } else {
                            stopLoop()
                            invalidate()
                            onOver(score, true)
                        }
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
        val gridPaint=paint(Color.argb(22,148,163,184)); gridPaint.strokeWidth=1f
        for(i in 1..7) canvas.drawLine(w*i/8f,0f,w*i/8f,h,gridPaint)
        for(i in 1..8) canvas.drawLine(0f,h*i/9f,w,h*i/9f,gridPaint)

        val margin = w * 0.03f
        val bw = (w - margin * 2f) / cols
        val bh = h * 0.04f
        val top0 = h * 0.08f
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!alive[row * cols + col]) continue
                brickP.color = if (hitPoints[row * cols + col] > 1) Color.rgb(174, 92, 255) else rowColors[row % rowColors.size]
                val left = margin + col * bw
                val top = top0 + row * (bh + 4f)
                canvas.drawRoundRect(left, top, left + bw - 4f, top + bh, 6f, 6f, paint(Color.argb(65,0,0,0)))
                canvas.drawRoundRect(left, top, left + bw - 4f, top + bh, 6f, 6f, brickP)
                canvas.drawRoundRect(left+3f,top+2f,left+bw-8f,top+bh*.30f,4f,4f,paint(Color.argb(85,255,255,255)))
            }
        }

        val pw = w * 0.22f
        val ph = h * 0.022f
        canvas.drawRoundRect(px - pw / 2f, h * 0.92f, px + pw / 2f, h * 0.92f + ph, 8f, 8f, paint(Color.argb(80,0,0,0)))
        canvas.drawRoundRect(px - pw / 2f, h * 0.92f, px + pw / 2f, h * 0.92f + ph, 8f, 8f, paddleP)
        canvas.drawRoundRect(px-pw*.40f,h*.922f,px+pw*.40f,h*.926f,3f,3f,paint(Color.rgb(125,211,252)))
        canvas.drawCircle(bx, by, w * 0.032f, paint(Color.argb(55,250,204,21)))
        canvas.drawCircle(bx, by, w * 0.022f, ballP)
        canvas.drawCircle(bx-w*.006f,by-w*.006f,w*.006f,paint(Color.WHITE))
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
    private lateinit var stageText: TextView

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
        stageText = label("المرحلة: 1 / 5", 17f, R.color.x_color)
        content.addView(stageText, lp(top = dp(2)))

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
            { level -> stageText.text = "المرحلة: $level / 5" },
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
