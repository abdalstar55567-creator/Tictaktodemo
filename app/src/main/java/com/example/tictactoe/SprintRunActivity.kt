package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min
import kotlin.math.sin
import java.util.ArrayDeque

class SprintRunActivity : BaseGameActivity() {
    override val gameTitle: String get() = "الركض السريع"

    private val handler = Handler(Looper.getMainLooper())
    private val taps = ArrayDeque<Long>()
    private lateinit var track: SprintTrackView
    private lateinit var timeText: TextView
    private lateinit var distanceText: TextView
    private lateinit var speedText: TextView
    private lateinit var statusText: TextView
    private var running = false
    private var finished = false
    private var startedAt = 0L
    private var lastFrame = 0L
    private var progress = 0f
    private var remaining = GAME_TIME
    private var tapRate = 0f

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            val now = SystemClock.elapsedRealtime()
            val dt = ((now - lastFrame).coerceAtMost(50L)) / 1000f
            lastFrame = now
            remaining = (GAME_TIME - (now - startedAt)).coerceAtLeast(0L)
            while (taps.isNotEmpty() && now - taps.peekFirst() > 1000L) taps.removeFirst()
            tapRate = taps.size.toFloat()
            // معدل الضغط يحدد سرعة التقدم: الضغط الأسرع يقرب اللاعب من خط النهاية أسرع.
            progress = min(1f, progress + (0.35f + tapRate * 0.72f) * dt / 100f)
            updateLabels()
            track.setRaceState(progress, tapRate, true)
            when {
                progress >= 1f -> finishRace(true)
                remaining <= 0L -> finishRace(false)
                else -> handler.postDelayed(this, 16L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🏃 الركض السريع")
        content.addView(label("اضغط بسرعة وبانتظام لزيادة سرعة العداء والوصول إلى خط النهاية قبل نفاد الوقت.", 15f, R.color.text_dim, false), lp(top = dp(2), bottom = dp(6)))
        timeText = label("⏱ 20.0 ثانية", 25f, R.color.gold)
        content.addView(timeText, lp(top = dp(2)))
        distanceText = label("المسافة: 0%", 19f)
        content.addView(distanceText, lp(top = dp(2)))
        speedText = label("السرعة: 0 ضغطة/ث", 15f, R.color.text_dim, false)
        content.addView(speedText, lp(top = dp(2), bottom = dp(8)))
        track = SprintTrackView(this)
        content.addView(track, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(300)))
        track.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onRunTap()
            true
        }
        statusText = label("المس مسار الركض لبدء السباق!", 17f)
        content.addView(statusText, lp(top = dp(10)))
        content.addView(button("إعادة السباق", false, ::resetRace), lp(height = dp(50), top = dp(10)))
        resetRace()
    }

    override fun onPause() {
        handler.removeCallbacks(frame)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (running) {
            lastFrame = SystemClock.elapsedRealtime()
            handler.post(frame)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun onRunTap() {
        if (finished) return
        val now = SystemClock.elapsedRealtime()
        if (!running) {
            running = true
            startedAt = now
            lastFrame = now
            taps.clear()
            statusText.text = "انطلق! استمر في الضغط بسرعة! 💨"
            handler.removeCallbacks(frame)
            handler.post(frame)
        }
        taps.addLast(now)
        track.bumpRunner()
        Feedback.tap(this)
    }

    private fun resetRace() {
        handler.removeCallbacks(frame)
        running = false
        finished = false
        progress = 0f
        remaining = GAME_TIME
        tapRate = 0f
        taps.clear()
        timeText.text = "⏱ 20.0 ثانية"
        distanceText.text = "المسافة: 0%"
        speedText.text = "السرعة: 0 ضغطة/ث"
        statusText.text = "المس مسار الركض لبدء السباق!"
        track.setRaceState(0f, 0f, false)
    }

    private fun updateLabels() {
        timeText.text = "⏱ %.1f ثانية".format(remaining / 1000f)
        distanceText.text = "المسافة: ${(progress * 100).toInt()}%"
        speedText.text = "السرعة: %.1f ضغطة/ث".format(tapRate)
    }

    private fun finishRace(won: Boolean) {
        if (finished) return
        finished = true
        running = false
        handler.removeCallbacks(frame)
        if (won) {
            val elapsed = (GAME_TIME - remaining).toInt().coerceAtLeast(1)
            val record = Scores.saveLow(this, "sprint_run_best_ms", elapsed)
            val best = Scores.get(this, "sprint_run_best_ms")
            statusText.text = (if (record) "🏆 رقم قياسي جديد! " else "وصلت إلى خط النهاية! 🎉 ") +
                "زمنك: %.1f ثانية".format(elapsed / 1000f) +
                "\nأفضل زمن: %.1f ثانية".format(best / 1000f)
            Feedback.win(this)
        } else {
            statusText.text = "انتهى الوقت! وصلت إلى ${(progress * 100).toInt()}%.\nحاول الضغط بسرعة أكبر."
            Feedback.lose(this)
        }
        track.setRaceState(progress, tapRate, false)
    }

    companion object { private const val GAME_TIME = 20_000L }
}

/** مشهد أفقي مرسوم بالكامل بالكود، دون صور أو موارد إضافية. */
private class SprintTrackView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress = 0f
    private var rate = 0f
    private var active = false
    private var phase = 0f

    fun setRaceState(value: Float, tapsPerSecond: Float, isActive: Boolean) {
        progress = value.coerceIn(0f, 1f)
        rate = tapsPerSecond
        active = isActive
        phase += 0.2f + rate * 0.08f
        invalidate()
    }

    fun bumpRunner() { phase += 0.7f; invalidate() }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        p.style = Paint.Style.FILL
        p.shader = android.graphics.LinearGradient(0f, 0f, 0f, h, Color.rgb(92, 184, 224), Color.rgb(218, 241, 244), android.graphics.Shader.TileMode.CLAMP)
        c.drawRoundRect(0f, 0f, w, h, 22f, 22f, p); p.shader = null
        p.color = Color.argb(220, 255, 255, 255)
        c.drawOval(w*.12f,h*.13f,w*.34f,h*.22f,p); c.drawOval(w*.60f,h*.09f,w*.82f,h*.18f,p)
        val hills = Path().apply {
            moveTo(0f,h*.62f); cubicTo(w*.18f,h*.36f,w*.36f,h*.62f,w*.52f,h*.47f)
            cubicTo(w*.72f,h*.28f,w*.82f,h*.62f,w,h*.42f); lineTo(w,h*.78f); lineTo(0f,h*.78f); close()
        }
        p.color = Color.rgb(82,166,112); c.drawPath(hills,p)
        p.color = Color.rgb(52,115,76); c.drawRect(0f,h*.60f,w,h,p)
        p.color = Color.rgb(48,57,68); c.drawRoundRect(0f,h*.65f,w,h*.91f,12f,12f,p)
        p.color = Color.rgb(245,245,225); p.strokeWidth = h*.012f
        c.drawLine(0f,h*.67f,w,h*.67f,p); c.drawLine(0f,h*.89f,w,h*.89f,p)
        p.color = Color.rgb(246,220,125)
        val dash = w*.10f; val gap = w*.12f; var x = -((progress*w*5f)%(dash+gap))
        while (x < w) { c.drawLine(x,h*.78f,x+dash,h*.78f,p); x += dash+gap }
        for (i in 0..5) {
            val tx = (((i*.22f+.05f-progress*.75f)%1.15f+1.15f)%1.15f)*w
            p.color = Color.rgb(111,76,48); c.drawRect(tx-w*.009f,h*.48f,tx+w*.009f,h*.58f,p)
            p.color = Color.rgb(31,126,73); c.drawCircle(tx,h*.46f,h*.055f,p)
        }
        val finishX = w*(1.02f-progress*.79f)
        drawFlag(c,finishX,h*.65f,h)
        val rx = w*.22f; val gy = h*.84f
        p.color = Color.argb(80,0,0,0); c.drawOval(rx-w*.055f,gy+h*.025f,rx+w*.065f,gy+h*.045f,p)
        drawRunner(c,rx,gy,h)
        p.color = Color.argb(155,15,23,42); c.drawRoundRect(w*.06f,h*.035f,w*.94f,h*.075f,12f,12f,p)
        p.color = Color.rgb(250,204,21); c.drawRoundRect(w*.06f,h*.035f,w*(.06f+.88f*progress),h*.075f,12f,12f,p)
    }

    private fun drawFlag(c: Canvas, x: Float, ground: Float, h: Float) {
        p.color = Color.WHITE; p.strokeWidth = h*.012f; c.drawLine(x,ground,x,ground-h*.43f,p)
        val flag = Path().apply { moveTo(x,ground-h*.43f); lineTo(x+h*.19f,ground-h*.37f); lineTo(x,ground-h*.30f); close() }
        p.color = Color.rgb(239,68,68); c.drawPath(flag,p)
        p.color = Color.WHITE; c.drawRect(x+h*.045f,ground-h*.405f,x+h*.09f,ground-h*.385f,p)
    }

    private fun drawRunner(c: Canvas, x: Float, ground: Float, h: Float) {
        val u = h*.16f; val swing = sin(phase).toFloat()
        p.style = Paint.Style.FILL; p.color = Color.rgb(245,190,145); c.drawCircle(x,ground-u*2.25f,u*.27f,p)
        p.color = Color.rgb(48,42,39); c.drawArc(RectF(x-u*.28f,ground-u*2.55f,x+u*.28f,ground-u*2.05f),190f,175f,false,p)
        p.color = Color.rgb(29,91,166); c.drawRoundRect(x-u*.22f,ground-u*1.95f,x+u*.22f,ground-u*.95f,u*.15f,u*.15f,p)
        p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND; p.strokeWidth = u*.15f
        p.color = Color.rgb(245,190,145)
        c.drawLine(x-u*.12f,ground-u*1.72f,x-u*.48f-swing*u*.25f,ground-u*1.25f,p)
        c.drawLine(x+u*.12f,ground-u*1.72f,x+u*.45f+swing*u*.25f,ground-u*1.35f,p)
        p.color = Color.rgb(35,42,55)
        c.drawLine(x-u*.08f,ground-u,x-u*.28f+swing*u*.52f,ground-u*.35f,p)
        c.drawLine(x+u*.08f,ground-u,x+u*.28f-swing*u*.52f,ground-u*.35f,p)
        p.color = Color.WHITE
        c.drawLine(x-u*.28f+swing*u*.52f,ground-u*.35f,x-u*.05f+swing*u*.52f,ground-u*.30f,p)
        c.drawLine(x+u*.28f-swing*u*.52f,ground-u*.35f,x+u*.50f-swing*u*.52f,ground-u*.30f,p)
        p.style = Paint.Style.FILL
    }
}
