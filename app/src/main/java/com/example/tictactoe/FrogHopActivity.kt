package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

/** لعبة قفز الضفدع بين أوراق اللوتس. اللمس يساراً/وسطاً/يميناً يحدد مسار القفزة. */
class FrogHopView(
    context: Context,
    private val onScore: (Int) -> Unit,
    private val onOver: (Int) -> Unit
) : GameLoopView(context) {

    override val aspect: Float get() = 0.94f

    private val water = Paint(Paint.ANTI_ALIAS_FLAG)
    private val waterLine = Paint(Paint.ANTI_ALIAS_FLAG)
    private val padPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val padLight = Paint(Paint.ANTI_ALIAS_FLAG)
    private val frogPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bellyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyeWhite = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pupil = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 34f
        setShadowLayer(5f, 1f, 2f, Color.BLACK)
    }
    private val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x44001B25 }
    private val pads = ArrayList<Int>()
    private var lane = 1
    private var score = 0
    private var hopProgress = 0f
    private var hopping = false
    private var fromLane = 1
    private var toLane = 1
    private var landingGood = true
    private var gameStarted = false
    private var elapsed = 0f
    private var message = "اضغط للبدء ثم اختر اتجاه القفزة"

    init {
        water.color = Color.rgb(19, 105, 133)
        waterLine.color = 0x337DE3E8
        padPaint.color = Color.rgb(47, 160, 94)
        padLight.color = Color.rgb(111, 220, 139)
        frogPaint.color = Color.rgb(83, 205, 74)
        bellyPaint.color = Color.rgb(190, 239, 125)
        eyeWhite.color = Color.WHITE
        pupil.color = Color.rgb(20, 35, 27)
    }

    fun startGame() {
        stopLoop()
        pads.clear()
        pads.add(1)
        lane = 1
        score = 0
        hopProgress = 0f
        hopping = false
        gameStarted = true
        elapsed = 0f
        message = "يسار: يسار  •  وسط: مستقيم  •  يمين: يمين"
        repeat(5) { pads.add(nextSafeLane(pads.last())) }
        onScore(0)
        startLoop()
        invalidate()
    }

    private fun nextSafeLane(previous: Int): Int {
        val options = (0..2).filter { abs(it - previous) <= 1 }
        return options.random()
    }

    override fun update(dt: Float) {
        elapsed += dt
        if (!hopping || !gameStarted) return
        hopProgress += dt / 0.34f
        if (hopProgress >= 1f) {
            hopping = false
            if (!landingGood) {
                gameStarted = false
                stopLoop()
                onOver(score)
                return
            }
            lane = toLane
            score++
            onScore(score)
            pads.removeAt(0)
            pads.add(nextSafeLane(pads.last()))
            message = if (score % 10 == 0) "ممتاز! وصلت إلى $score قفزة ⭐" else "قفزة ناجحة! استمر 🐸"
        }
    }

    fun resumeGame() {
        if (gameStarted && !running) startLoop()
    }

    private fun attemptHop(x: Float) {
        if (!gameStarted || hopping) return
        fromLane = lane
        val wanted = when {
            x < width * 0.34f -> lane - 1
            x > width * 0.66f -> lane + 1
            else -> lane
        }
        toLane = wanted.coerceIn(0, 2)
        // لا يمكن تغيير المسار بأكثر من حارة واحدة، ويجب أن توجد ورقة في المسار المختار.
        landingGood = wanted in 0..2 && pads.size > 1 && pads[1] == wanted
        hopProgress = 0f
        hopping = true
        if (!landingGood) message = "أوه! لا توجد ورقة في هذا الاتجاه!"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        water.shader = LinearGradient(0f, 0f, w, h,
            intArrayOf(Color.rgb(13, 74, 111), Color.rgb(23, 137, 151), Color.rgb(15, 91, 126)),
            null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(0f, 0f, w, h, 28f, 28f, water)
        water.shader = null

        // تموجات الماء
        for (i in 0..8) {
            val y = ((i * h / 8f + elapsed * 22f) % h)
            canvas.drawArc(w * .08f, y, w * .34f, y + h * .035f, 195f, 150f, false, waterLine)
            canvas.drawArc(w * .58f, y + h * .03f, w * .91f, y + h * .065f, 195f, 150f, false, waterLine)
        }

        val padW = w * .25f
        val padH = h * .095f
        val laneX = floatArrayOf(w * .20f, w * .50f, w * .80f)
        val yCurrent = h * .73f
        val yNext = h * .43f
        val yThird = h * .25f

        // أوراق اللوتس أمام اللاعب
        if (pads.size > 1) drawLilyPad(canvas, laneX[pads[1]], yNext, padW, padH)
        if (pads.size > 2) drawLilyPad(canvas, laneX[pads[2]], yThird, padW, padH)
        if (pads.isNotEmpty()) drawLilyPad(canvas, laneX[pads[0]], yCurrent, padW, padH)

        var frogX = laneX[lane]
        var frogY = yCurrent - padH * .32f
        if (hopping) {
            val t = hopProgress.coerceIn(0f, 1f)
            val eased = t * t * (3f - 2f * t)
            frogX = laneX[fromLane] + (laneX[toLane] - laneX[fromLane]) * eased
            frogY = yCurrent - (yCurrent - yNext) * eased - kotlin.math.sin(t * Math.PI).toFloat() * h * .16f - padH * .32f
        }
        drawFrog(canvas, frogX, frogY, w * .12f)

        // مناطق اللمس
        val zonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x22FFFFFF }
        canvas.drawRoundRect(w*.035f,h*.86f,w*.315f,h*.97f,18f,18f,zonePaint)
        canvas.drawRoundRect(w*.36f,h*.86f,w*.64f,h*.97f,18f,18f,zonePaint)
        canvas.drawRoundRect(w*.685f,h*.86f,w*.965f,h*.97f,18f,18f,zonePaint)
        textPaint.textSize = h * .045f
        canvas.drawText("◀ يسار", w*.175f, h*.93f, textPaint)
        canvas.drawText("⬆ وسط", w*.50f, h*.93f, textPaint)
        canvas.drawText("يمين ▶", w*.825f, h*.93f, textPaint)
        textPaint.textSize = h * .045f
        canvas.drawText("⭐ $score", w*.5f, h*.075f, textPaint)
        textPaint.textSize = h * .026f
        canvas.drawText(message, w*.5f, h*.145f, textPaint)
    }

    private fun drawLilyPad(canvas: Canvas, cx: Float, cy: Float, pw: Float, ph: Float) {
        canvas.drawOval(cx-pw*.48f, cy-ph*.22f, cx+pw*.48f, cy+ph*.55f, shadow)
        val oval = RectF(cx-pw*.5f, cy-ph*.48f, cx+pw*.5f, cy+ph*.42f)
        canvas.drawOval(oval, padPaint)
        canvas.drawArc(oval, 305f, 110f, true, paint(Color.rgb(19, 93, 66)))
        canvas.drawOval(cx-pw*.34f, cy-ph*.34f, cx+pw*.27f, cy+ph*.18f, padLight)
        val vein = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x7788E8A0; strokeWidth = 2.5f }
        canvas.drawLine(cx, cy, cx+pw*.35f, cy-ph*.22f, vein)
    }

    private fun drawFrog(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        // ظل الجسم وأرجله
        canvas.drawOval(cx-r*1.0f, cy+r*.45f, cx+r*1.0f, cy+r*.78f, shadow)
        canvas.drawOval(cx-r*.95f, cy+r*.08f, cx-r*.42f, cy+r*.55f, frogPaint)
        canvas.drawOval(cx+r*.42f, cy+r*.08f, cx+r*.95f, cy+r*.55f, frogPaint)
        canvas.drawOval(cx-r*.78f, cy-r*.52f, cx+r*.78f, cy+r*.58f, frogPaint)
        canvas.drawOval(cx-r*.52f, cy-r*.25f, cx+r*.52f, cy+r*.45f, bellyPaint)
        // الرأس والعينان البارزتان
        canvas.drawCircle(cx-r*.43f, cy-r*.43f, r*.34f, frogPaint)
        canvas.drawCircle(cx+r*.43f, cy-r*.43f, r*.34f, frogPaint)
        canvas.drawCircle(cx-r*.43f, cy-r*.47f, r*.205f, eyeWhite)
        canvas.drawCircle(cx+r*.43f, cy-r*.47f, r*.205f, eyeWhite)
        canvas.drawCircle(cx-r*.39f, cy-r*.45f, r*.09f, pupil)
        canvas.drawCircle(cx+r*.47f, cy-r*.45f, r*.09f, pupil)
        val smile = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(28, 91, 47); style = Paint.Style.STROKE; strokeWidth = r*.055f }
        canvas.drawArc(cx-r*.25f, cy-r*.03f, cx+r*.25f, cy+r*.30f, 15f, 150f, false, smile)
        canvas.drawCircle(cx-r*.20f, cy+r*.05f, r*.055f, paint(Color.rgb(239, 115, 111)))
        canvas.drawCircle(cx+r*.20f, cy+r*.05f, r*.055f, paint(Color.rgb(239, 115, 111)))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { grab(); return true }
            MotionEvent.ACTION_UP -> {
                if (!gameStarted) startGame() else attemptHop(event.x)
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

class FrogHopActivity : BaseGameActivity() {
    private lateinit var game: FrogHopView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🐸 نطّة الضفدع")
        content.addView(label("اقفز بين أوراق اللوتس! اختر يساراً أو وسطاً أو يميناً.", 15f, R.color.text_dim, false), lp(top = dp(2)))
        scoreText = label("النقاط: 0", 21f)
        content.addView(scoreText, lp(top = dp(4)))
        bestText = label("", 14f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))
        updateBest()
        game = FrogHopView(this,
            { score -> scoreText.text = "النقاط: $score"; if (score > 0) Feedback.tap(this) },
            { score ->
                Feedback.lose(this)
                val best = Scores.get(this, "frog_hop_best")
                if (score > best) Scores.put(this, "frog_hop_best", score)
                scoreText.text = "انتهت الجولة! نتيجتك: $score 🐸"
                updateBest()
            }
        )
        content.addView(game, lp(top = dp(8)))
        content.addView(button("ابدأ اللعب من جديد") { game.startGame() }, lp(height = dp(48), top = dp(8), bottom = dp(12)))
    }

    private fun updateBest() {
        val best = Scores.get(this, "frog_hop_best")
        bestText.text = if (best > 0) "أفضل نتيجة: $best 🏆" else ""
    }

    override fun onPause() {
        if (::game.isInitialized) game.stopLoop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::game.isInitialized) game.resumeGame()
    }
}
