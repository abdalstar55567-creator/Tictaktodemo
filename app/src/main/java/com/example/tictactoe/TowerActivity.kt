package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

class TowerView(
    context: Context,
    private val onScore: (Int, Boolean) -> Unit,
    private val onOver: (Int) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 1.1f

    private val bgP = paint(Color.parseColor("#1E293B"))
    private val blockP = paint(Color.WHITE)

    private val stack = ArrayList<FloatArray>()   // x, عرض
    private var cw = 0f
    private var mx = 0f
    private var my = 0f
    private var dir = 1f
    private var state = 2       // 0 يتحرك، 1 يسقط، 2 انتهت
    private var camera = 0f
    private var score = 0

    fun start() {
        val w = width.toFloat()
        stack.clear()
        cw = w * 0.4f
        stack.add(floatArrayOf((w - cw) / 2f, cw))
        score = 0
        mx = 0f
        dir = 1f
        state = 0
        camera = 0f
        my = height * 0.1f
        onScore(0, false)
        startLoop()
        invalidate()
    }

    private fun blockH(): Float = height * 0.055f

    override fun update(dt: Float) {
        val w = width.toFloat()
        val h = height.toFloat()

        val target = maxOf(0, stack.size - 7) * blockH()
        camera += (target - camera) * minOf(1f, dt * 4f)

        if (state == 0) {
            val speed = w * minOf(1.6f, 0.55f + stack.size * 0.03f)
            mx += dir * speed * dt
            if (mx < 0f) {
                mx = 0f
                dir = 1f
            } else if (mx > w - cw) {
                mx = w - cw
                dir = -1f
            }
        } else if (state == 1) {
            my += h * 1.8f * dt
            val landY = h - (stack.size + 1) * blockH() + camera
            if (my >= landY) land()
        }
    }

    private fun land() {
        val w = width.toFloat()
        val top = stack.last()
        val left = maxOf(mx, top[0])
        val right = minOf(mx + cw, top[0] + top[1])

        if (right - left < w * 0.01f) {
            state = 2
            stopLoop()
            invalidate()
            onOver(score)
            return
        }

        var nx = left
        var nw = right - left
        var perfect = false
        if (abs(mx - top[0]) < w * 0.012f) {
            nx = top[0]
            nw = top[1]
            perfect = true
        }

        stack.add(floatArrayOf(nx, nw))
        cw = nw
        score++
        onScore(score, perfect)

        state = 0
        my = height * 0.1f
        dir = if (Random.nextBoolean()) 1f else -1f
        mx = if (dir > 0f) 0f else w - cw
    }

    private fun blockColor(i: Int): Int =
        Color.HSVToColor(floatArrayOf(((i * 18) % 360).toFloat(), 0.6f, 0.95f))

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val bh = blockH()
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)

        for (i in stack.indices) {
            val y = h - (i + 1) * bh + camera
            if (y > h || y + bh < 0f) continue
            blockP.color = blockColor(i)
            canvas.drawRoundRect(stack[i][0], y, stack[i][0] + stack[i][1], y + bh - 2f, 8f, 8f, blockP)
        }

        if (state == 0 || state == 1) {
            blockP.color = blockColor(stack.size)
            canvas.drawRoundRect(mx, my, mx + cw, my + bh - 2f, 8f, 8f, blockP)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            grab()
            if (running && state == 0) state = 1
        }
        return true
    }
}

class TowerActivity : BaseGameActivity() {

    private lateinit var game: TowerView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView
    private lateinit var infoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🏗️ برج الصناديق")

        content.addView(
            label("اضغط لإسقاط الصندوق المتحرك فوق البرج. كل جزء زائد يُقطع!", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        scoreText = label("الطوابق: 0", 22f)
        content.addView(scoreText, lp(top = dp(6)))

        infoText = label("", 15f, R.color.x_color)
        content.addView(infoText, lp(top = dp(2)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        content.addView(button("ابدأ البناء") { game.start() }, lp(height = dp(48), top = dp(10), bottom = dp(10)))

        game = TowerView(
            this,
            { s, perfect ->
                scoreText.text = "الطوابق: $s"
                infoText.text = if (perfect) "✨ تطابق تام!" else ""
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
        val best = Scores.get(this, "tower_best")
        bestText.text = if (best == 0) "" else "أفضل ارتفاع: $best طابق"
    }

    private fun onGameOver(score: Int) {
        Feedback.lose(this)
        val best = Scores.get(this, "tower_best")
        if (score > best) {
            Scores.put(this, "tower_best", score)
            scoreText.text = "سقط البرج! رقم قياسي جديد 🏆 $score طابق"
        } else {
            scoreText.text = "سقط الصندوق! ارتفاع برجك: $score طابق"
        }
        infoText.text = ""
        updateBest()
    }
}
