package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

/** mode 0: التقط الفواكه، mode 1: تفادى القنابل. */
class FallingView(
    context: Context,
    private val mode: Int,
    private val onState: (Int, Int) -> Unit,
    private val onOver: (Int) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 1.1f

    private val bgP = paint(Color.parseColor("#1E293B"))
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG)

    private val goodItems = arrayOf("🍎", "🍌", "🍇", "🍊")
    private val badItems = arrayOf("💣", "🔥")
    private val items = ArrayList<FloatArray>()   // x, y, نوع
    private var px = 0f
    private var score = 0
    private var lives = 3
    private var spawn = 0f

    init {
        textP.textAlign = Paint.Align.CENTER
    }

    fun start() {
        items.clear()
        px = width / 2f
        score = 0
        lives = 3
        spawn = 0.3f
        onState(score, lives)
        startLoop()
        invalidate()
    }

    private fun types(): Array<String> = if (mode == 0) goodItems else badItems

    override fun update(dt: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val isz = w * 0.09f
        val ps = w * 0.13f
        val py = h * 0.88f

        spawn -= dt
        if (spawn <= 0f) {
            items.add(floatArrayOf(isz + Random.nextFloat() * (w - 2f * isz), -isz, Random.nextInt(types().size).toFloat()))
            spawn = maxOf(0.35f, 0.95f - score * 0.02f)
        }

        val fall = h * minOf(0.95f, 0.35f + score * 0.01f)
        var changed = false
        val iter = items.iterator()
        while (iter.hasNext()) {
            val o = iter.next()
            o[1] += fall * dt
            val hit = abs(o[0] - px) < ps * 0.5f + isz * 0.4f && abs(o[1] - py) < ps * 0.5f + isz * 0.4f
            if (hit) {
                iter.remove()
                if (mode == 0) score++ else lives--
                changed = true
            } else if (o[1] > h + isz) {
                iter.remove()
                if (mode == 0) lives-- else score++
                changed = true
            }
        }

        if (changed) onState(score, lives)
        if (lives <= 0) {
            stopLoop()
            invalidate()
            onOver(score)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRoundRect(0f, 0f, w, h, 24f, 24f, bgP)
        val speckle=paint(Color.argb(42,255,255,255))
        for(i in 0..22) canvas.drawCircle(w*((i*41%97)/100f),h*((i*29%89)/100f),w*.0035f,speckle)
        canvas.drawRoundRect(px-w*.14f,h*.85f,px+w*.14f,h*.91f,12f,12f,paint(Color.argb(55,0,0,0)))

        textP.textSize = w * 0.09f
        val list = types()
        for (o in items) {
            canvas.drawCircle(o[0]-w*.012f,o[1]-w*.012f,w*.027f,paint(Color.argb(45,255,255,255)))
            canvas.drawText(list[o[2].toInt()], o[0], o[1] + w * 0.03f, textP)
        }

        textP.textSize = w * 0.13f
        canvas.drawText(if (mode == 0) "🧺" else "🚀", px, h * 0.88f + w * 0.04f, textP)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            grab()
            px = event.x.coerceIn(width * 0.07f, width * 0.93f)
            if (!running) invalidate()
        }
        return true
    }
}

abstract class FallingGameActivity : BaseGameActivity() {

    protected abstract val mode: Int
    protected abstract val titleText: String
    protected abstract val hint: String
    protected abstract val bestKey: String

    private lateinit var game: FallingView
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(titleText)

        content.addView(label(hint, 16f, R.color.text_dim, false), lp(top = dp(4)))

        scoreText = label("النقاط: 0", 22f)
        content.addView(scoreText, lp(top = dp(6)))

        livesText = label("❤️❤️❤️", 20f)
        content.addView(livesText, lp(top = dp(2)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()

        content.addView(button("ابدأ") { game.start() }, lp(height = dp(48), top = dp(10), bottom = dp(10)))

        game = FallingView(
            this,
            mode,
            { s, l ->
                scoreText.text = "النقاط: $s"
                livesText.text = "❤️".repeat(maxOf(l, 0)) + "🖤".repeat(3 - maxOf(l, 0))
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
        val best = Scores.get(this, bestKey)
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }

    private fun onGameOver(score: Int) {
        Feedback.lose(this)
        val best = Scores.get(this, bestKey)
        if (score > best) {
            Scores.put(this, bestKey, score)
            scoreText.text = "انتهت! رقم قياسي جديد 🏆 $score"
        } else {
            scoreText.text = "انتهت اللعبة! نتيجتك: $score"
        }
        updateBest()
    }
}

class CatchActivity : FallingGameActivity() {
    override val mode: Int get() = 0
    override val titleText: String get() = "🧺 التقط الفواكه"
    override val hint: String get() = "حرّك السلة بإصبعك والتقط الفواكه! لديك 3 فرص."
    override val bestKey: String get() = "catch_best"
}

class DodgeActivity : FallingGameActivity() {
    override val mode: Int get() = 1
    override val titleText: String get() = "🚀 تفادى القنابل"
    override val hint: String get() = "حرّك الصاروخ بإصبعك وتفادى القنابل! لديك 3 فرص."
    override val bestKey: String get() = "dodge_best"
}
