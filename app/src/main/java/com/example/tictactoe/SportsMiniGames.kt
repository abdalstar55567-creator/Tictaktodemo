package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Fixed-goal football: swipe from the ball; swipe speed controls the shot power. */
class FixedGoalFootballActivity : BaseGameActivity() {
    private lateinit var field: FootballShotView
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("⚽ تسديد الكرة")
        content.addView(label("اسحب إصبعك من الكرة باتجاه المرمى. سرعة السحب تحدد قوة التسديدة!", 15f, R.color.text_dim, false), lp(top = dp(8), bottom = dp(6)))
        status = label("الأهداف: 0  •  المحاولات: 0", 18f, R.color.x_color)
        content.addView(status, lp(bottom = dp(6)))
        field = FootballShotView(this) { goals, shots, message ->
            status.text = "$message  •  الأهداف: $goals / $shots"
        }
        content.addView(field, lp())
        content.addView(button("إعادة المباراة ↻") { field.reset() }, lp(top = dp(8), bottom = dp(10)))
        field.reset()
    }
    override fun onPause() { if (::field.isInitialized) field.stopLoop(); super.onPause() }
}

private class FootballShotView(context: Context, private val report: (Int, Int, String) -> Unit) : GameLoopView(context) {
    override val aspect: Float get() = 1.12f
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ball = Ball()
    private data class Ball(var x: Float = .5f, var y: Float = .82f, var vx: Float = 0f, var vy: Float = 0f, var spin: Float = 0f)
    private var moving = false
    private var goals = 0
    private var shots = 0
    private var message = "اسحب الكرة نحو المرمى"
    private var dragStart = false
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var gestureTime = 0L
    private var aimX = .5f
    private var aimY = .24f
    private var aimVisible = false
    private var goalChecked = false

    fun reset() {
        stopLoop(); goals = 0; shots = 0; moving = false; goalChecked = false
        ball.x = .5f; ball.y = .82f; ball.vx = 0f; ball.vy = 0f
        message = "اسحب الكرة نحو المرمى"; aimVisible = false
        report(goals, shots, message); invalidate()
    }

    override fun update(dt: Float) {
        if (!moving) return
        val oldY = ball.y
        ball.x += ball.vx * dt
        ball.y += ball.vy * dt
        ball.vy += 1.30f * dt
        ball.vx *= (1f - 0.10f * dt)
        ball.spin += dt * (ball.vx * 8f + 3f)
        // Goal line: judge the ball when it reaches the plane of the goal mouth.
        val goalLine = .205f
        if (!goalChecked && oldY > goalLine && ball.y <= goalLine) {
            goalChecked = true
            val postLeft = .31f + .035f
            val postRight = .69f - .035f
            val inside = ball.x > postLeft && ball.x < postRight && ball.y < .205f
            if (inside) { goals++; message = "⚽ هدف رائع!" } else message = "قريبة! حاول مرة أخرى"
            report(goals, shots, message)
        }
        if (ball.y < -.08f || ball.x < -.12f || ball.x > 1.12f || ball.y > 1.08f || abs(ball.vx) + abs(ball.vy) < .08f) {
            moving = false
            stopLoop()
            ball.x = .5f; ball.y = .82f; ball.vx = 0f; ball.vy = 0f
            aimVisible = false
            if (!goalChecked) message = "لم تصب المرمى — جرّب بزاوية مختلفة"
            report(goals, shots, message)
        }
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        // Sky and distant stadium.
        p.shader = LinearGradient(0f, 0f, 0f, h * .58f, intArrayOf(Color.rgb(91, 174, 224), Color.rgb(202, 231, 245)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h * .58f, p); p.shader = null
        p.color = Color.rgb(48, 75, 99); c.drawRoundRect(w * .04f, h * .09f, w * .96f, h * .29f, 18f, 18f, p)
        p.color = Color.rgb(226, 233, 239); c.drawRect(w * .06f, h * .12f, w * .94f, h * .255f, p)
        for (i in 0..23) { p.color = if (i % 2 == 0) Color.rgb(52, 78, 103) else Color.rgb(239, 245, 247); c.drawCircle(w * (.085f + i * .036f), h * .17f + (i % 3) * h * .012f, w * .009f, p) }
        // Turf with perspective stripes.
        p.shader = LinearGradient(0f, h * .30f, 0f, h, intArrayOf(Color.rgb(66, 157, 79), Color.rgb(24, 111, 57)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, h * .30f, w, h, p); p.shader = null
        for (i in 0..7) { p.color = if (i % 2 == 0) 0x22FFFFFF.toInt() else 0x11000000; val y1 = h * (.31f + i * .085f); c.drawRect(0f, y1, w, min(h, y1 + h * .085f), p) }
        p.color = Color.argb(170, 255, 255, 255); p.style = Paint.Style.STROKE; p.strokeWidth = 2.2f
        c.drawRect(w * .07f, h * .31f, w * .93f, h * .98f, p)
        c.drawOval(w * .28f, h * .67f, w * .72f, h * .95f, p); p.style = Paint.Style.FILL
        // Goal frame and net.
        val gl = w * .31f; val gr = w * .69f; val gt = h * .115f; val gb = h * .285f
        p.color = Color.argb(75, 0, 0, 0); c.drawRect(gl + 5f, gt + 5f, gr + 5f, gb + 5f, p)
        p.color = Color.rgb(231, 240, 245); p.style = Paint.Style.STROKE; p.strokeWidth = max(2f, w * .009f)
        c.drawRect(gl, gt, gr, gb, p)
        p.color = 0x88FFFFFF.toInt(); p.strokeWidth = 1f
        for (i in 1..9) { val x = gl + (gr - gl) * i / 10f; c.drawLine(x, gt, x, gb, p) }
        for (i in 1..4) { val y = gt + (gb - gt) * i / 5f; c.drawLine(gl, y, gr, y, p) }
        p.style = Paint.Style.FILL
        // Keeper silhouette.
        p.color = Color.rgb(242, 178, 49); c.drawCircle(w * .5f, h * .205f, w * .025f, p)
        p.color = Color.rgb(32, 66, 119); c.drawRoundRect(w * .475f, h * .224f, w * .525f, h * .273f, 7f, 7f, p)
        p.color = Color.rgb(242, 178, 49); c.drawLine(w * .48f, h * .24f, w * .445f, h * .263f, p); c.drawLine(w * .52f, h * .24f, w * .555f, h * .263f, p)
        // Aiming trail.
        if (aimVisible && !moving) {
            p.color = Color.argb(190, 255, 246, 125); p.style = Paint.Style.STROKE; p.strokeWidth = 4f; p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 9f), 0f)
            c.drawLine(ball.x * w, ball.y * h, aimX * w, aimY * h, p); p.pathEffect = null; p.style = Paint.Style.FILL
            p.color = Color.WHITE; c.drawCircle(aimX * w, aimY * h, 5f, p)
        }
        // Ball shadow and realistic paneled football.
        val bx = ball.x * w; val by = ball.y * h; val r = w * .047f
        p.color = Color.argb(65, 0, 0, 0); c.drawOval(bx - r * 1.1f, by + r * .72f, bx + r * 1.1f, by + r * 1.15f, p)
        p.shader = LinearGradient(bx - r, by - r, bx + r, by + r, intArrayOf(Color.WHITE, 0xFFE0E4E8.toInt(), 0xFF858F99.toInt()), null, Shader.TileMode.CLAMP)
        c.drawCircle(bx, by, r, p); p.shader = null
        p.color = Color.rgb(31, 40, 48)
        val pentagon = Path().apply { for (i in 0..5) { val a = (i * 72f + ball.spin * 20f) * Math.PI.toFloat() / 180f; val x = bx + cos(a) * r * .38f; val y = by + sin(a) * r * .38f; if (i == 0) moveTo(x, y) else lineTo(x, y) }; close() }
        c.drawPath(pentagon, p)
        for (i in 0..4) { val a = (i * 72f + ball.spin * 20f) * Math.PI.toFloat() / 180f; c.drawCircle(bx + cos(a) * r * .73f, by + sin(a) * r * .73f, r * .15f, p) }
        p.color = Color.WHITE; p.textAlign = Paint.Align.CENTER; p.textSize = w * .033f; p.typeface = android.graphics.Typeface.DEFAULT_BOLD
        c.drawText("اسحب من الكرة باتجاه المرمى", w / 2f, h * .055f, p)
        if (moving) { p.color = Color.argb(170, 255, 255, 255); p.textSize = w * .027f; c.drawText("قوة التسديدة تعتمد على سرعة السحب", w / 2f, h * .345f, p) }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w = width.toFloat().coerceAtLeast(1f); val h = height.toFloat().coerceAtLeast(1f)
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                if (moving) return true
                val bx = ball.x * w; val by = ball.y * h
                if (abs(e.x - bx) > w * .16f || abs(e.y - by) > h * .13f) return false
                grab(); dragStart = true; startX = e.x; startY = e.y; lastX = e.x; lastY = e.y; gestureTime = System.currentTimeMillis(); aimVisible = true; return true
            }
            MotionEvent.ACTION_MOVE -> if (dragStart) {
                lastX = e.x; lastY = e.y; aimX = (e.x / w).coerceIn(.02f, .98f); aimY = (e.y / h).coerceIn(.04f, .78f); invalidate(); return true
            }
            MotionEvent.ACTION_UP -> if (dragStart) {
                dragStart = false
                val elapsed = ((System.currentTimeMillis() - gestureTime).coerceAtLeast(35L)).toFloat() / 1000f
                val dx = e.x - startX; val dy = e.y - startY
                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                if (dy < -h * .035f && distance > w * .055f) {
                    val speed = (distance / elapsed).coerceIn(w * .35f, w * 3.4f)
                    val ux = dx / distance; val uy = dy / distance
                    val power = speed / (w * 2.2f)
                    ball.vx = ux * power * 1.30f
                    ball.vy = uy * power * 2.15f
                    moving = true; goalChecked = false; shots++; message = "تسديدة!"; aimVisible = false; report(goals, shots, message); startLoop()
                } else { aimVisible = false; message = "اسحب بسرعة أكبر نحو الأعلى"; report(goals, shots, message) }
                invalidate(); performClick(); return true
            }
            MotionEvent.ACTION_CANCEL -> { dragStart = false; aimVisible = false; invalidate(); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}

/** Duck shooting gallery: tap anywhere to launch a pellet from the lower hunter position. */
class DuckHuntActivity : BaseGameActivity() {
    private lateinit var game: DuckHuntView
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🦆 صيد البط")
        content.addView(label("البط يطير من اليمين إلى اليسار — اضغط على مكانه لإطلاق طلقة", 15f, R.color.text_dim, false), lp(top = dp(8), bottom = dp(6)))
        status = label("النقاط: 0  •  البط الفائت: 0", 18f, R.color.x_color)
        content.addView(status, lp(bottom = dp(6)))
        game = DuckHuntView(this) { score, missed, level, ended ->
            status.text = if (ended) "انتهت الجولة • النقاط: $score" else "النقاط: $score  •  الفائت: $missed  •  المرحلة: $level"
        }
        content.addView(game, lp())
        content.addView(button("ابدأ / أعد الجولة ↻") { game.restart() }, lp(top = dp(8), bottom = dp(10)))
        game.restart()
    }
    override fun onPause() { if (::game.isInitialized) game.stopLoop(); super.onPause() }
}

private class DuckHuntView(context: Context, private val changed: (Int, Int, Int, Boolean) -> Unit) : GameLoopView(context) {
    override val aspect: Float get() = 1.10f
    private data class Duck(var x: Float, var y: Float, var speed: Float, var phase: Float, var size: Float)
    private data class Pellet(var x: Float, var y: Float, var vx: Float, var vy: Float)
    private data class Puff(var x: Float, var y: Float, var age: Float)
    private val ducks = ArrayList<Duck>()
    private val pellets = ArrayList<Pellet>()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val puffs = ArrayList<Puff>()
    private var score = 0
    private var missed = 0
    private var level = 1
    private var spawn = .4f
    private var elapsed = 0f
    private var ended = false
    private var started = false
    private var shooterX = .5f
    private var shotFlash = 0f
    private var lastAimX = .5f
    private var lastAimY = .5f

    fun restart() {
        stopLoop(); ducks.clear(); pellets.clear(); puffs.clear(); score = 0; missed = 0; level = 1; spawn = .25f; elapsed = 0f; ended = false; started = true; shotFlash = 0f
        changed(score, missed, level, false); startLoop(); invalidate()
    }
    private fun spawnDuck() {
        val y = Random.nextFloat() * .43f + .17f
        val speed = (.27f + level * .035f + Random.nextFloat() * .12f).coerceAtMost(.85f)
        ducks.add(Duck(1.12f, y, speed, Random.nextFloat() * 6.28f, .055f + Random.nextFloat() * .012f))
    }
    override fun update(dt: Float) {
        if (!started || ended) return
        elapsed += dt; spawn -= dt; shotFlash = max(0f, shotFlash - dt * 4f)
        if (spawn <= 0f) { spawnDuck(); spawn = (1.15f - level * .055f).coerceAtLeast(.38f) }
        val iterator = ducks.iterator()
        while (iterator.hasNext()) {
            val d = iterator.next(); d.x -= d.speed * dt; d.phase += dt * 9f
            if (d.x < -.14f) { iterator.remove(); missed++; changed(score, missed, level, false); if (missed >= 8) finish() }
        }
        val pelletsIt = pellets.iterator()
        while (pelletsIt.hasNext()) {
            val b = pelletsIt.next(); b.x += b.vx * dt; b.y += b.vy * dt; b.vy += .16f * dt
            var hit: Duck? = null
            for (d in ducks) {
                val dx = (b.x - d.x) * width; val dy = (b.y - d.y) * height
                if (dx * dx + dy * dy < (d.size * width * 1.25f) * (d.size * width * 1.25f)) { hit = d; break }
            }
            val struckDuck = hit
            if (struckDuck != null) {
                ducks.remove(struckDuck); pelletsIt.remove(); puffs.add(Puff(struckDuck.x, struckDuck.y, 0f)); score += 10; val newLevel = 1 + score / 60
                if (newLevel != level) level = newLevel
                changed(score, missed, level, false)
            } else if (b.x < -.1f || b.x > 1.1f || b.y < -.1f || b.y > 1.1f) pelletsIt.remove()
        }
        val puffIt = puffs.iterator(); while (puffIt.hasNext()) { val puff = puffIt.next(); puff.age += dt; if (puff.age > .45f) puffIt.remove() }
    }
    private fun finish() { ended = true; started = false; stopLoop(); changed(score, missed, level, true); invalidate() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); if (w <= 0f || h <= 0f) return
        // Dawn sky, distant hills and meadow.
        p.shader = LinearGradient(0f, 0f, 0f, h * .75f, intArrayOf(Color.rgb(96, 177, 221), Color.rgb(205, 231, 235), Color.rgb(246, 226, 174)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p); p.shader = null
        p.color = Color.argb(190, 255, 255, 255); c.drawCircle(w * .78f, h * .16f, w * .055f, p)
        val hill1 = Path().apply { moveTo(0f, h * .66f); cubicTo(w * .25f, h * .48f, w * .48f, h * .73f, w * .72f, h * .55f); cubicTo(w * .85f, h * .48f, w * .93f, h * .55f, w, h * .51f); lineTo(w, h); lineTo(0f, h); close() }
        p.color = Color.rgb(119, 164, 106); c.drawPath(hill1, p)
        val hill2 = Path().apply { moveTo(0f, h * .76f); cubicTo(w * .28f, h * .61f, w * .54f, h * .82f, w * .77f, h * .68f); cubicTo(w * .89f, h * .62f, w * .96f, h * .67f, w, h * .65f); lineTo(w, h); lineTo(0f, h); close() }
        p.color = Color.rgb(62, 130, 72); c.drawPath(hill2, p)
        // Foreground reeds and grass.
        p.color = Color.rgb(37, 102, 54)
        for (i in 0..26) { val x = i * w / 26f; val base = h * (.91f + (i % 3) * .018f); c.drawLine(x, base, x - w * .012f, base - h * (.05f + (i % 4) * .012f), p); c.drawLine(x + w * .012f, base, x + w * .022f, base - h * .04f, p) }
        // Ducks fly from right to left, with flapping wings and visible beaks.
        ducks.forEach { d -> drawDuck(c, d, w, h) }
        // Pellets and soft hit feathers (cartoon puff; no gore).
        pellets.forEach { b -> val x = b.x * w; val y = b.y * h; p.color = Color.rgb(54, 58, 63); c.drawCircle(x, y, w * .008f, p); p.color = Color.argb(90, 255, 244, 194); c.drawCircle(x, y, w * .018f, p) }
        puffs.forEach { puff -> val radius = w * (.018f + puff.age * .10f); p.color = Color.argb((170 * (1f - puff.age / .45f)).toInt().coerceIn(0, 170), 255, 249, 224); for (i in 0..5) { val a = i * Math.PI.toFloat() / 3f; c.drawCircle(puff.x * w + cos(a) * radius, puff.y * h + sin(a) * radius, radius * .35f, p) } }
        // Hunter position, simple detailed shotgun silhouette, pointed toward latest target.
        val sx = shooterX * w; val sy = h * .91f
        p.color = Color.argb(55, 0, 0, 0); c.drawOval(sx - w * .10f, sy - h * .015f, sx + w * .10f, sy + h * .025f, p)
        p.color = Color.rgb(45, 62, 48); c.drawRoundRect(sx - w * .052f, sy - h * .07f, sx + w * .052f, sy + h * .025f, 12f, 12f, p)
        p.color = Color.rgb(122, 76, 42); c.drawRoundRect(sx - w * .025f, sy - h * .055f, sx + w * .025f, sy + h * .018f, 7f, 7f, p)
        val angle = atan2(lastAimY * h - sy, lastAimX * w - sx)
        val barrelEndX = sx + cos(angle) * w * .16f; val barrelEndY = sy + sin(angle) * w * .16f
        p.color = Color.rgb(61, 70, 75); p.strokeWidth = w * .022f; p.strokeCap = Paint.Cap.ROUND; c.drawLine(sx, sy - h * .055f, barrelEndX, barrelEndY, p)
        p.color = Color.rgb(180, 190, 190); p.strokeWidth = w * .009f; c.drawLine(sx, sy - h * .061f, barrelEndX, barrelEndY, p)
        if (shotFlash > 0f) { p.color = Color.argb((180 * shotFlash).toInt(), 255, 222, 120); c.drawCircle(barrelEndX, barrelEndY, w * (.025f + shotFlash * .035f), p) }
        p.color = Color.WHITE; p.textAlign = Paint.Align.CENTER; p.textSize = w * .033f; p.typeface = android.graphics.Typeface.DEFAULT_BOLD
        c.drawText("اضغط على مكان البط لإطلاق الطلقة", w / 2f, h * .07f, p)
        if (!started || ended) {
            p.color = Color.argb(205, 20, 45, 35); c.drawRoundRect(w * .12f, h * .39f, w * .88f, h * .59f, 20f, 20f, p)
            p.color = Color.WHITE; p.textSize = w * .052f; c.drawText(if (ended) "انتهت الجولة — $score نقطة" else "ابدأ الصيد!", w / 2f, h * .49f, p)
        }
    }

    private fun drawDuck(c: Canvas, d: Duck, w: Float, h: Float) {
        val x = d.x * w; val y = d.y * h; val s = d.size * w
        c.save(); c.scale(-1f, 1f, x, y) // mirror: the bird faces its flight direction (left)
        val flap = sin(d.phase) * s * .62f
        // Tail and body with a subtle feather gradient.
        p.color = Color.rgb(48, 61, 48)
        val tail = Path().apply { moveTo(x - s * .55f, y); lineTo(x - s * 1.05f, y - s * .28f); lineTo(x - s * .91f, y + s * .22f); close() }; c.drawPath(tail, p)
        p.shader = LinearGradient(x - s, y - s, x + s, y + s, intArrayOf(Color.rgb(113, 133, 92), Color.rgb(61, 83, 57), Color.rgb(36, 54, 42)), null, Shader.TileMode.CLAMP)
        c.drawOval(x - s * .72f, y - s * .43f, x + s * .66f, y + s * .43f, p); p.shader = null
        // Wing animation.
        val wing = Path().apply { moveTo(x - s * .25f, y); quadTo(x - s * .10f, y - s * 1.12f - flap, x + s * .55f, y - s * .18f); quadTo(x + s * .15f, y + s * .18f, x - s * .25f, y); close() }
        p.color = Color.rgb(133, 151, 102); c.drawPath(wing, p)
        p.color = Color.rgb(184, 194, 148); p.style = Paint.Style.STROKE; p.strokeWidth = max(1.5f, s * .045f); c.drawLine(x - s * .12f, y - s * .08f, x + s * .34f, y - s * .28f - flap * .45f, p); p.style = Paint.Style.FILL
        // Neck, head, eye and orange bill.
        p.color = Color.rgb(72, 91, 61); c.drawRoundRect(x + s * .28f, y - s * .48f, x + s * .54f, y - s * .03f, s * .12f, s * .12f, p)
        p.color = Color.rgb(65, 91, 57); c.drawCircle(x + s * .55f, y - s * .45f, s * .25f, p)
        p.color = Color.WHITE; c.drawCircle(x + s * .62f, y - s * .50f, s * .075f, p)
        p.color = Color.rgb(20, 25, 20); c.drawCircle(x + s * .65f, y - s * .50f, s * .035f, p)
        p.color = Color.rgb(230, 153, 48); val bill = Path().apply { moveTo(x + s * .75f, y - s * .43f); lineTo(x + s * 1.10f, y - s * .34f); lineTo(x + s * .76f, y - s * .25f); close() }; c.drawPath(bill, p)
        c.restore()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { if (ended || !started) return true; grab(); lastAimX = (e.x / width).coerceIn(0f, 1f); lastAimY = (e.y / height).coerceIn(0f, 1f); shooterX = (lastAimX * .72f + .14f).coerceIn(.18f, .82f); shootAt(lastAimX, lastAimY); invalidate(); performClick(); return true }
        }
        return true
    }
    private fun shootAt(tx: Float, ty: Float) {
        val sx = shooterX; val sy = .91f
        val dx = tx - sx; val dy = ty - sy
        val length = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(.01f)
        val speed = 1.65f
        pellets.add(Pellet(sx, sy, dx / length * speed, dy / length * speed))
        shotFlash = 1f
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}
