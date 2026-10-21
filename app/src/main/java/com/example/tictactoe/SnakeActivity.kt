package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

class SnakeView(
    context: Context,
    private val onScore: (Int) -> Unit,
    private val onOver: (Int) -> Unit
) : View(context) {

    private val n = 16
    private val body = ArrayList<IntArray>()
    private var dx = 1
    private var dy = 0
    private var nextDx = 1
    private var nextDy = 0
    private var foodX = 0
    private var foodY = 0
    private var score = 0
    private var delayMs = 220L
    private var downX = 0f
    private var downY = 0f

    var running = false
        private set

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foodPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            step()
            invalidate()
            if (running) postDelayed(this, delayMs)
        }
    }

    init {
        bgPaint.color = Color.parseColor("#1E293B")
        bodyPaint.color = Color.parseColor("#22C55E")
        headPaint.color = Color.parseColor("#86EFAC")
        foodPaint.color = Color.parseColor("#FB7185")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    fun start() {
        body.clear()
        body.add(intArrayOf(8, 8))
        body.add(intArrayOf(7, 8))
        body.add(intArrayOf(6, 8))
        dx = 1
        dy = 0
        nextDx = 1
        nextDy = 0
        score = 0
        delayMs = 220L
        placeFood()
        running = true
        onScore(0)
        removeCallbacks(loop)
        postDelayed(loop, delayMs)
        invalidate()
    }

    fun stop() {
        running = false
        removeCallbacks(loop)
    }

    fun turn(ndx: Int, ndy: Int) {
        if (!running) return
        if (ndx == -dx && ndy == -dy) return
        nextDx = ndx
        nextDy = ndy
    }

    private fun placeFood() {
        while (true) {
            val x = Random.nextInt(n)
            val y = Random.nextInt(n)
            if (body.none { it[0] == x && it[1] == y }) {
                foodX = x
                foodY = y
                return
            }
        }
    }

    private fun step() {
        dx = nextDx
        dy = nextDy
        val nx = body[0][0] + dx
        val ny = body[0][1] + dy

        if (nx < 0 || ny < 0 || nx >= n || ny >= n || body.any { it[0] == nx && it[1] == ny }) {
            running = false
            onOver(score)
            return
        }

        body.add(0, intArrayOf(nx, ny))
        if (nx == foodX && ny == foodY) {
            score++
            onScore(score)
            delayMs = maxOf(90L, delayMs - 6L)
            placeFood()
        } else {
            body.removeAt(body.size - 1)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val c = w / n
        canvas.drawRoundRect(0f, 0f, w, w, 24f, 24f, bgPaint)
        canvas.drawCircle((foodX + 0.5f) * c, (foodY + 0.5f) * c, c * 0.38f, foodPaint)
        for (i in body.indices) {
            val p = body[i]
            canvas.drawRoundRect(
                p[0] * c + 2f, p[1] * c + 2f,
                (p[0] + 1) * c - 2f, (p[1] + 1) * c - 2f,
                10f, 10f,
                if (i == 0) headPaint else bodyPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val mx = event.x - downX
                val my = event.y - downY
                if (abs(mx) > 40f || abs(my) > 40f) {
                    if (abs(mx) > abs(my)) {
                        turn(if (mx > 0) 1 else -1, 0)
                    } else {
                        turn(0, if (my > 0) 1 else -1)
                    }
                }
                return true
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(loop)
        super.onDetachedFromWindow()
    }
}

class SnakeActivity : BaseGameActivity() {

    private lateinit var snake: SnakeView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🐍 الثعبان")

        scoreText = label("النقاط: 0", 22f)
        content.addView(scoreText, lp(top = dp(4)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2), bottom = dp(10)))
        updateBest()

        snake = SnakeView(
            this,
            { s -> scoreText.text = "النقاط: $s"; if (s > 0) Feedback.tap(this) },
            { s -> onGameOver(s) }
        )
        content.addView(snake, lp())

        content.addView(
            label("اسحب بإصبعك على اللوحة أو استخدم الأسهم", 14f, R.color.text_dim, false),
            lp(top = dp(8))
        )

        // أسهم التحكم
        val up = button("⬆", false) { snake.turn(0, -1) }
        val down = button("⬇", false) { snake.turn(0, 1) }
        val left = button("⬅", false) { snake.turn(-1, 0) }
        val right = button("➡", false) { snake.turn(1, 0) }

        val pad = LinearLayout(this)
        pad.orientation = LinearLayout.VERTICAL
        pad.gravity = android.view.Gravity.CENTER

        pad.addView(up, LinearLayout.LayoutParams(dp(80), dp(52)))

        val mid = row()
        mid.addView(left, LinearLayout.LayoutParams(dp(80), dp(52)))
        mid.addView(View(this), LinearLayout.LayoutParams(dp(60), dp(52)))
        mid.addView(right, LinearLayout.LayoutParams(dp(80), dp(52)))
        pad.addView(mid)

        pad.addView(down, LinearLayout.LayoutParams(dp(80), dp(52)))
        content.addView(pad, lp(top = dp(8)))

        content.addView(button("ابدأ") { snake.start() }, lp(height = dp(50), top = dp(12)))
    }

    override fun onPause() {
        snake.stop()
        super.onPause()
    }

    private fun updateBest() {
        val best = Scores.get(this, "snake_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun onGameOver(score: Int) {
        Feedback.lose(this)
        val best = Scores.get(this, "snake_best")
        if (score > best) {
            Scores.put(this, "snake_best", score)
            scoreText.text = "انتهت! رقم قياسي جديد 🏆 $score"
        } else {
            scoreText.text = "انتهت اللعبة! نتيجتك: $score"
        }
        updateBest()
    }
}
