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
import android.widget.Button
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * منجنيق الكرات: اسحب الكرة للخلف ثم اتركها لتنطلق وتُسقط الأهداف الموضوعة
 * فوق الألواح الأفقية والأبراج. الفيزياء مبسّطة ومكتوبة بالكامل بالكود.
 * إحداثيات العالم ثابتة (1000 × 700) ويُغيَّر حجمها تلقائياً حسب عرض الشاشة.
 */
class CatapultView(
    context: Context,
    private val onHud: (Int, Int, Int) -> Unit,
    private val onEnd: (Boolean, Int, Int) -> Unit
) : GameLoopView(context) {

    companion object {
        const val LEVELS = 6

        const val WW = 1000f
        const val WH = 700f
        const val GROUND = 600f
        const val GRAV = 900f
        const val BALL_R = 17f
        const val ANCHOR_X = 185f
        const val ANCHOR_Y = 470f
        const val MAX_PULL = 115f
        const val LAUNCH_K = 7.2f

        const val WOOD = 0
        const val STONE = 1
        const val ICE = 2

        const val S_READY = 0
        const val S_AIM = 1
        const val S_FLY = 2
        const val S_WAIT = 3
        const val S_CLEAR = 4
        const val S_WON = 5
        const val S_LOST = 6
    }

    override val aspect: Float get() = WH / WW

    private class CatBlock(var cx: Float, var cy: Float, val hw: Float, val hh: Float, val kind: Int) {
        var vx = 0f
        var vy = 0f
        var dead = false
        var supported = false
        val maxHp: Float = when (kind) {
            1 -> 9f
            2 -> 1.8f
            else -> 4f
        }
        var hp: Float = maxHp
        val mass: Float = hw * hh * 4f / 1200f * when (kind) {
            1 -> 2f
            2 -> 0.6f
            else -> 1f
        }
        val l: Float get() = cx - hw
        val r: Float get() = cx + hw
        val t: Float get() = cy - hh
        val b: Float get() = cy + hh
    }

    private class CatTarget(var cx: Float, var cy: Float, val r: Float) {
        var vy = 0f
        var alive = true
    }

    private class CatParticle(
        var x: Float, var y: Float, var vx: Float, var vy: Float,
        var life: Float, val color: Int, val size: Float
    )

    private val blocks = ArrayList<CatBlock>()
    private val targets = ArrayList<CatTarget>()
    private val particles = ArrayList<CatParticle>()

    private var level = 0
    private var state = S_READY
    private var ballsLeft = 0
    private var score = 0
    private var hudDirty = false
    private var timer = 0f
    private var settleTime = 0f

    // الكرة الحالية
    private var bx = ANCHOR_X
    private var by = ANCHOR_Y
    private var bvx = 0f
    private var bvy = 0f
    private var ballActive = false
    private var ballTime = 0f
    private var restTime = 0f

    // أدوات الرسم
    private val pt = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val skyShader = LinearGradient(
        0f, 0f, 0f, GROUND,
        intArrayOf(Color.rgb(70, 150, 215), Color.rgb(150, 205, 235), Color.rgb(222, 240, 232)),
        null, Shader.TileMode.CLAMP
    )
    private val clipPath = Path().apply {
        addRoundRect(RectF(0f, 0f, WW, WH), 24f, 24f, Path.Direction.CW)
    }
    private val hillPath = Path().apply {
        moveTo(0f, GROUND - 70f)
        cubicTo(180f, GROUND - 190f, 340f, GROUND - 20f, 520f, GROUND - 110f)
        cubicTo(700f, GROUND - 200f, 860f, GROUND - 60f, WW, GROUND - 130f)
        lineTo(WW, GROUND)
        lineTo(0f, GROUND)
        close()
    }
    private val basePath = Path().apply {
        moveTo(ANCHOR_X - 38f, GROUND)
        lineTo(ANCHOR_X + 38f, GROUND)
        lineTo(ANCHOR_X + 9f, ANCHOR_Y + 70f)
        lineTo(ANCHOR_X - 9f, ANCHOR_Y + 70f)
        close()
    }

    // ------------------------------------------------------------------
    // التحكم العام
    // ------------------------------------------------------------------

    fun loadLevel(index: Int) {
        level = index.coerceIn(0, LEVELS - 1)
        blocks.clear()
        targets.clear()
        particles.clear()
        buildLevel(level)
        ballsLeft = ballsForLevel(level)
        score = 0
        state = S_READY
        ballActive = false
        timer = 0f
        settleTime = 0f
        bx = ANCHOR_X
        by = ANCHOR_Y
        pushHud()
        startLoop()
        invalidate()
    }

    fun resume() {
        if (state != S_WON && state != S_LOST && !running) startLoop()
    }

    private fun pushHud() {
        onHud(ballsLeft, targets.count { it.alive }, score)
    }

    private fun ballsForLevel(l: Int): Int = when (l) {
        0, 1 -> 4
        2, 3, 4 -> 5
        else -> 6
    }

    // ------------------------------------------------------------------
    // بناء المستويات
    // ------------------------------------------------------------------

    private fun addBlock(cx: Float, bottomY: Float, w: Float, h: Float, kind: Int) {
        blocks.add(CatBlock(cx, bottomY - h / 2f, w / 2f, h / 2f, kind))
    }

    /** عمود رأسي يقف على السطح baseY. */
    private fun pillar(cx: Float, h: Float, baseY: Float, kind: Int = WOOD) {
        addBlock(cx, baseY, 24f, h, kind)
    }

    /** لوح أفقي يرتكز أسفله على الارتفاع baseY. */
    private fun plank(cx: Float, w: Float, baseY: Float, kind: Int = WOOD) {
        addBlock(cx, baseY, w, 20f, kind)
    }

    /** هدف دائري يقف على السطح baseY. */
    private fun bullseye(cx: Float, baseY: Float) {
        targets.add(CatTarget(cx, baseY - 20f, 20f))
    }

    private fun buildLevel(l: Int) {
        val g = GROUND
        when (l) {
            0 -> {
                pillar(640f, 90f, g)
                pillar(780f, 90f, g)
                plank(710f, 200f, g - 90f)
                bullseye(710f, g - 110f)
                bullseye(880f, g)
            }
            1 -> {
                pillar(600f, 80f, g)
                pillar(740f, 80f, g)
                plank(670f, 200f, g - 80f)
                pillar(635f, 70f, g - 100f)
                pillar(705f, 70f, g - 100f)
                plank(670f, 130f, g - 170f)
                bullseye(670f, g - 190f)
                bullseye(588f, g - 100f)
                bullseye(755f, g - 100f)
                bullseye(860f, g)
            }
            2 -> {
                pillar(580f, 100f, g)
                pillar(740f, 100f, g, STONE)
                pillar(900f, 100f, g)
                plank(740f, 360f, g - 100f)
                bullseye(640f, g - 120f)
                bullseye(740f, g - 120f)
                bullseye(840f, g - 120f)
                bullseye(660f, g)
            }
            3 -> {
                pillar(560f, 110f, g, ICE)
                pillar(640f, 110f, g, ICE)
                plank(600f, 120f, g - 110f)
                bullseye(600f, g - 130f)
                pillar(800f, 70f, g, STONE)
                pillar(900f, 70f, g, STONE)
                plank(850f, 160f, g - 70f)
                pillar(830f, 60f, g - 90f)
                pillar(870f, 60f, g - 90f)
                plank(850f, 100f, g - 150f, ICE)
                bullseye(850f, g - 170f)
                bullseye(785f, g - 90f)
                bullseye(700f, g)
            }
            4 -> {
                pillar(560f, 70f, g)
                pillar(740f, 70f, g)
                pillar(920f, 70f, g)
                plank(740f, 400f, g - 70f)
                pillar(640f, 70f, g - 90f)
                pillar(840f, 70f, g - 90f)
                plank(740f, 240f, g - 160f)
                pillar(740f, 60f, g - 180f, STONE)
                plank(740f, 100f, g - 240f)
                bullseye(600f, g - 90f)
                bullseye(880f, g - 90f)
                bullseye(700f, g - 180f)
                bullseye(780f, g - 180f)
                bullseye(740f, g - 260f)
            }
            else -> {
                pillar(600f, 100f, g, STONE)
                pillar(700f, 100f, g)
                pillar(800f, 100f, g)
                pillar(900f, 100f, g, STONE)
                plank(750f, 340f, g - 100f)
                pillar(700f, 60f, g - 120f)
                pillar(800f, 60f, g - 120f)
                plank(750f, 160f, g - 180f)
                bullseye(650f, g)
                bullseye(850f, g)
                bullseye(640f, g - 120f)
                bullseye(860f, g - 120f)
                bullseye(750f, g - 200f)
            }
        }
    }

    // ------------------------------------------------------------------
    // الحلقة والفيزياء
    // ------------------------------------------------------------------

    override fun update(dt: Float) {
        val steps = 4
        val h = dt / steps
        for (i in 0 until steps) stepPhysics(h)
        updateParticles(dt)
        updateState(dt)
        if (hudDirty) {
            hudDirty = false
            pushHud()
        }
    }

    private fun stepPhysics(h: Float) {
        if (ballActive) stepBall(h)
        for (b in blocks) stepBlock(b, h)
        resolveBlockPairs()
        for (t in targets) stepTarget(t, h)
        blocks.removeAll { it.dead }
        targets.removeAll { !it.alive }
    }

    private fun stepBall(h: Float) {
        bvy += GRAV * h
        bx += bvx * h
        by += bvy * h
        ballTime += h
        var onGround = false
        if (by + BALL_R >= GROUND) {
            by = GROUND - BALL_R
            if (bvy > 0f) bvy = -bvy * 0.42f
            if (abs(bvy) < 45f) bvy = 0f
            bvx *= max(0f, 1f - 3f * h)
            onGround = true
        }
        for (b in blocks) {
            if (!b.dead) collideBallBlock(b)
        }
        for (t in targets) {
            if (t.alive) collideBallTarget(t)
        }
        val speed = hypot(bvx, bvy)
        if (speed < 45f && (onGround || ballTime > 1f)) {
            restTime += h
        } else {
            restTime = 0f
        }
        if (restTime > 0.6f || ballTime > 7f || bx < -80f || bx > WW + 160f || by > WH + 80f) {
            endBall()
        }
    }

    private fun collideBallBlock(b: CatBlock) {
        val nearX = bx.coerceIn(b.l, b.r)
        val nearY = by.coerceIn(b.t, b.b)
        val dx = bx - nearX
        val dy = by - nearY
        val d2 = dx * dx + dy * dy
        if (d2 >= BALL_R * BALL_R) return

        var nx: Float
        var ny: Float
        val pen: Float
        if (d2 > 0.0001f) {
            val d = sqrt(d2)
            nx = dx / d
            ny = dy / d
            pen = BALL_R - d
        } else {
            // مركز الكرة داخل الكتلة: ادفعها للخارج من أقرب جهة
            val toLeft = bx - b.l
            val toRight = b.r - bx
            val toTop = by - b.t
            val toBottom = b.b - by
            val m = min(min(toLeft, toRight), min(toTop, toBottom))
            nx = 0f
            ny = 0f
            if (m == toLeft) nx = -1f
            else if (m == toRight) nx = 1f
            else if (m == toTop) ny = -1f
            else ny = 1f
            pen = m + BALL_R
        }
        bx += nx * pen
        by += ny * pen

        val rel = (bvx - b.vx) * nx + (bvy - b.vy) * ny
        if (rel < 0f) {
            val impact = -rel
            val invBall = 1f
            // كتلة تستند إلى ما تحتها لا تتحرك للأسفل عندما تسقط الكرة عليها
            val invBlock = if (b.supported && ny < -0.5f) 0f else 1f / b.mass
            var j = -(1f + 0.28f) * rel / (invBall + invBlock)
            damageBlock(b, impact / 100f * 0.6f)
            if (b.dead) j *= 0.45f
            bvx += nx * j * invBall
            bvy += ny * j * invBall
            b.vx -= nx * j * invBlock
            b.vy -= ny * j * invBlock
        }
    }

    private fun collideBallTarget(t: CatTarget) {
        val dx = bx - t.cx
        val dy = by - t.cy
        val rr = BALL_R + t.r
        val d2 = dx * dx + dy * dy
        if (d2 >= rr * rr) return
        val d = max(sqrt(d2), 0.001f)
        val nx = dx / d
        val ny = dy / d
        bx += nx * (rr - d)
        by += ny * (rr - d)
        val speed = hypot(bvx, bvy)
        if (speed > 110f) {
            killTarget(t)
            bvx *= 0.8f
            bvy *= 0.8f
        } else {
            val rel = bvx * nx + bvy * ny
            if (rel < 0f) {
                bvx -= 1.3f * rel * nx
                bvy -= 1.3f * rel * ny
            }
        }
    }

    private fun damageBlock(b: CatBlock, d: Float) {
        if (b.dead) return
        b.hp -= d
        if (b.hp <= 0f) {
            b.dead = true
            score += 100
            hudDirty = true
            debris(b.cx, b.cy, blockColor(b.kind), 9)
        }
    }

    private fun killTarget(t: CatTarget) {
        if (!t.alive) return
        t.alive = false
        score += 500
        hudDirty = true
        debris(t.cx, t.cy, Color.rgb(220, 50, 60), 10)
        debris(t.cx, t.cy, Color.WHITE, 6)
        Feedback.match(context)
    }

    private fun blockColor(kind: Int): Int = when (kind) {
        1 -> Color.rgb(148, 154, 168)
        2 -> Color.rgb(175, 228, 248)
        else -> Color.rgb(182, 128, 72)
    }

    private fun supportTop(b: CatBlock): Float? {
        if (b.b >= GROUND - 3f) return GROUND
        var best: Float? = null
        for (o in blocks) {
            if (o === b || o.dead) continue
            if (b.r - 4f <= o.l || b.l + 4f >= o.r) continue
            val gap = o.t - b.b
            if (gap < -3f || gap > 3f) continue
            if (best == null || o.t < best) best = o.t
        }
        return best
    }

    private fun stepBlock(b: CatBlock, h: Float) {
        if (b.dead) return
        val support = supportTop(b)
        if (support != null && b.vy >= 0f) {
            b.supported = true
            b.vy = 0f
            b.cy = support - b.hh
            b.vx *= max(0f, 1f - 5f * h)
            if (abs(b.vx) < 6f) b.vx = 0f
            b.cx += b.vx * h
        } else {
            b.supported = false
            val prevBottom = b.b
            b.vy += GRAV * h
            b.cx += b.vx * h
            b.cy += b.vy * h
            landBlock(b, prevBottom)
        }
        if (b.cx < -300f || b.cx > WW + 400f) b.dead = true
    }

    private fun landBlock(b: CatBlock, prevBottom: Float) {
        if (b.b >= GROUND) {
            if (b.vy > 150f) damageBlock(b, b.vy / 100f * 0.35f)
            b.cy = GROUND - b.hh
            b.vy = 0f
            return
        }
        for (o in blocks) {
            if (o === b || o.dead) continue
            if (b.r - 4f <= o.l || b.l + 4f >= o.r) continue
            if (b.vy > 0f && prevBottom <= o.t + 4f && b.b >= o.t) {
                val v = b.vy
                if (v > 150f) {
                    val d = v / 100f * 0.35f
                    val total = o.mass + b.mass
                    damageBlock(b, d * o.mass / total)
                    damageBlock(o, d * b.mass / total)
                }
                b.cy = o.t - b.hh
                b.vy = 0f
                return
            }
        }
    }

    /** تصادم جانبي بين الكتل المنزلقة أو المتساقطة. */
    private fun resolveBlockPairs() {
        val n = blocks.size
        for (i in 0 until n) {
            val a = blocks[i]
            if (a.dead) continue
            for (j in i + 1 until n) {
                val b = blocks[j]
                if (b.dead) continue
                val ox = min(a.r, b.r) - max(a.l, b.l)
                val oy = min(a.b, b.b) - max(a.t, b.t)
                if (ox <= 0f || oy <= 6f || ox > oy) continue
                val s = if (a.cx < b.cx) 1f else -1f
                val total = a.mass + b.mass
                a.cx -= s * ox * b.mass / total
                b.cx += s * ox * a.mass / total
                val approach = (a.vx - b.vx) * s
                if (approach > 0f) {
                    val imp = 1.2f * approach / (1f / a.mass + 1f / b.mass)
                    a.vx -= s * imp / a.mass
                    b.vx += s * imp / b.mass
                    damageBlock(a, approach / 100f * 0.25f)
                    damageBlock(b, approach / 100f * 0.25f)
                }
            }
        }
    }

    private fun targetSupport(t: CatTarget): Float? {
        val bottom = t.cy + t.r
        if (bottom >= GROUND - 3f) return GROUND
        var best: Float? = null
        for (o in blocks) {
            if (o.dead) continue
            if (t.cx < o.l - t.r * 0.5f || t.cx > o.r + t.r * 0.5f) continue
            val gap = o.t - bottom
            if (gap < -3f || gap > 3f) continue
            if (best == null || o.t < best) best = o.t
        }
        return best
    }

    private fun stepTarget(t: CatTarget, h: Float) {
        if (!t.alive) return
        val support = targetSupport(t)
        if (support != null && t.vy >= 0f) {
            t.vy = 0f
            t.cy = support - t.r
        } else {
            val prevBottom = t.cy + t.r
            t.vy += GRAV * h
            t.cy += t.vy * h
            landTarget(t, prevBottom)
        }
        if (!t.alive) return
        for (b in blocks) {
            if (b.dead) continue
            if (hypot(b.vx, b.vy) > 90f && blockTouchesTarget(b, t)) {
                killTarget(t)
                break
            }
        }
    }

    private fun landTarget(t: CatTarget, prevBottom: Float) {
        val bottom = t.cy + t.r
        if (bottom >= GROUND) {
            if (t.vy > 330f) killTarget(t)
            t.cy = GROUND - t.r
            t.vy = 0f
            return
        }
        for (o in blocks) {
            if (o.dead) continue
            if (t.cx < o.l - t.r * 0.5f || t.cx > o.r + t.r * 0.5f) continue
            if (t.vy > 0f && prevBottom <= o.t + 4f && bottom >= o.t) {
                if (t.vy > 330f) killTarget(t)
                t.cy = o.t - t.r
                t.vy = 0f
                return
            }
        }
    }

    private fun blockTouchesTarget(b: CatBlock, t: CatTarget): Boolean {
        val nearX = t.cx.coerceIn(b.l, b.r)
        val nearY = t.cy.coerceIn(b.t, b.b)
        val dx = t.cx - nearX
        val dy = t.cy - nearY
        val rr = t.r - 2f
        return dx * dx + dy * dy < rr * rr
    }

    private fun debris(x: Float, y: Float, color: Int, n: Int) {
        for (i in 0 until n) {
            particles.add(
                CatParticle(
                    x, y,
                    Random.nextFloat() * 360f - 180f,
                    -Random.nextFloat() * 320f - 40f,
                    0.5f + Random.nextFloat() * 0.6f,
                    color,
                    3f + Random.nextFloat() * 4f
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        var i = particles.size - 1
        while (i >= 0) {
            val q = particles[i]
            q.life -= dt
            if (q.life <= 0f) {
                particles.removeAt(i)
            } else {
                q.vy += GRAV * 0.8f * dt
                q.x += q.vx * dt
                q.y += q.vy * dt
            }
            i--
        }
    }

    // ------------------------------------------------------------------
    // حالة اللعبة
    // ------------------------------------------------------------------

    private fun endBall() {
        ballActive = false
        debris(bx.coerceIn(0f, WW), min(by, GROUND - 4f), Color.rgb(250, 190, 90), 6)
        if (state == S_FLY) {
            state = S_WAIT
            timer = 0.7f
            settleTime = 0f
        }
    }

    private fun isSettled(): Boolean =
        blocks.all { abs(it.vx) < 12f && it.vy == 0f } && targets.all { it.vy == 0f }

    private fun updateState(dt: Float) {
        if (state != S_WON && state != S_LOST && state != S_CLEAR && targets.isEmpty()) {
            state = S_CLEAR
            timer = 1.0f
        }
        when (state) {
            S_CLEAR -> {
                timer -= dt
                if (timer <= 0f) finishWin()
            }
            S_WAIT -> {
                timer -= dt
                if (timer <= 0f) {
                    if (ballsLeft > 0) {
                        state = S_READY
                        bx = ANCHOR_X
                        by = ANCHOR_Y
                    } else {
                        settleTime += dt
                        if (settleTime > 4f || isSettled()) finishLose()
                    }
                }
            }
        }
    }

    private fun finishWin() {
        ballActive = false
        score += ballsLeft * 400
        val stars = when {
            ballsLeft >= 2 -> 3
            ballsLeft == 1 -> 2
            else -> 1
        }
        state = S_WON
        stopLoop()
        pushHud()
        onEnd(true, score, stars)
    }

    private fun finishLose() {
        ballActive = false
        state = S_LOST
        stopLoop()
        pushHud()
        onEnd(false, score, 0)
    }

    // ------------------------------------------------------------------
    // اللمس
    // ------------------------------------------------------------------

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val s = width / WW
        if (s <= 0f) return false
        val x = event.x / s
        val y = event.y / s
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (state == S_READY && ballsLeft > 0 && hypot(x - ANCHOR_X, y - ANCHOR_Y) < 180f) {
                    state = S_AIM
                    grab()
                    pull(x, y)
                    invalidate()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (state == S_AIM) {
                    pull(x, y)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (state == S_AIM) releaseBall()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (state == S_AIM) cancelAim()
            }
        }
        return true
    }

    private fun pull(x: Float, y: Float) {
        var dx = min(x - ANCHOR_X, 10f)
        var dy = y - ANCHOR_Y
        val len = hypot(dx, dy)
        if (len > MAX_PULL) {
            dx *= MAX_PULL / len
            dy *= MAX_PULL / len
        }
        bx = ANCHOR_X + dx
        by = min(ANCHOR_Y + dy, GROUND - BALL_R - 2f)
    }

    private fun cancelAim() {
        state = S_READY
        bx = ANCHOR_X
        by = ANCHOR_Y
        invalidate()
    }

    private fun releaseBall() {
        val dx = ANCHOR_X - bx
        val dy = ANCHOR_Y - by
        if (hypot(dx, dy) < 22f) {
            cancelAim()
            return
        }
        bvx = dx * LAUNCH_K
        bvy = dy * LAUNCH_K
        ballsLeft -= 1
        ballActive = true
        ballTime = 0f
        restTime = 0f
        state = S_FLY
        Feedback.tap(context)
        pushHud()
    }

    // ------------------------------------------------------------------
    // الرسم
    // ------------------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        val s = width / WW
        if (s <= 0f) return
        canvas.save()
        canvas.scale(s, s)
        canvas.clipPath(clipPath)
        drawBackground(canvas)
        drawCatapultBase(canvas)
        drawQueuedBalls(canvas)

        val onSling = state == S_READY || state == S_AIM
        val bandX = if (state == S_AIM) bx else ANCHOR_X
        val bandY = if (state == S_AIM) by else ANCHOR_Y - 2f
        drawBand(canvas, ANCHOR_X - 20f, ANCHOR_Y - 12f, bandX, bandY)

        for (b in blocks) drawBlock(canvas, b)
        for (t in targets) drawTarget(canvas, t)

        if (state == S_AIM) drawTrajectory(canvas)
        if (onSling || ballActive) drawBall(canvas, bx, by, BALL_R)
        drawBand(canvas, ANCHOR_X + 20f, ANCHOR_Y - 12f, bandX, bandY)

        drawParticles(canvas)
        canvas.restore()
    }

    private fun drawBackground(c: Canvas) {
        pt.style = Paint.Style.FILL
        pt.shader = skyShader
        c.drawRect(0f, 0f, WW, GROUND, pt)
        pt.shader = null
        pt.color = Color.rgb(255, 236, 150)
        c.drawCircle(880f, 100f, 42f, pt)
        pt.color = Color.argb(205, 255, 255, 255)
        drawCloud(c, 230f, 110f, 1f)
        drawCloud(c, 560f, 70f, 0.8f)
        drawCloud(c, 760f, 190f, 1.1f)
        pt.color = Color.rgb(96, 170, 130)
        c.drawPath(hillPath, pt)
        pt.color = Color.rgb(70, 150, 80)
        c.drawRect(0f, GROUND, WW, WH, pt)
        pt.color = Color.rgb(120, 84, 56)
        c.drawRect(0f, GROUND + 16f, WW, WH, pt)
    }

    private fun drawCloud(c: Canvas, cx: Float, cy: Float, k: Float) {
        rect.set(cx - 60f * k, cy - 16f * k, cx + 60f * k, cy + 16f * k)
        c.drawOval(rect, pt)
        c.drawCircle(cx - 20f * k, cy - 14f * k, 20f * k, pt)
        c.drawCircle(cx + 18f * k, cy - 18f * k, 24f * k, pt)
    }

    private fun drawCatapultBase(c: Canvas) {
        pt.style = Paint.Style.FILL
        pt.color = Color.rgb(110, 72, 40)
        c.drawPath(basePath, pt)
        pt.style = Paint.Style.STROKE
        pt.strokeCap = Paint.Cap.ROUND
        pt.color = Color.rgb(139, 90, 50)
        pt.strokeWidth = 16f
        c.drawLine(ANCHOR_X, ANCHOR_Y + 72f, ANCHOR_X, ANCHOR_Y + 8f, pt)
        pt.strokeWidth = 11f
        c.drawLine(ANCHOR_X, ANCHOR_Y + 12f, ANCHOR_X - 20f, ANCHOR_Y - 12f, pt)
        c.drawLine(ANCHOR_X, ANCHOR_Y + 12f, ANCHOR_X + 20f, ANCHOR_Y - 12f, pt)
        pt.style = Paint.Style.FILL
    }

    private fun drawBand(c: Canvas, tipX: Float, tipY: Float, endX: Float, endY: Float) {
        pt.style = Paint.Style.STROKE
        pt.strokeCap = Paint.Cap.ROUND
        pt.strokeWidth = 7f
        pt.color = Color.rgb(62, 36, 22)
        c.drawLine(tipX, tipY, endX, endY, pt)
        pt.style = Paint.Style.FILL
    }

    private fun drawQueuedBalls(c: Canvas) {
        val queued = if (state == S_READY || state == S_AIM) ballsLeft - 1 else ballsLeft
        for (i in 0 until queued) {
            drawBall(c, ANCHOR_X - 48f - i * 28f, GROUND - 12f, 12f)
        }
    }

    private fun drawBall(c: Canvas, x: Float, y: Float, r: Float) {
        pt.style = Paint.Style.FILL
        pt.color = Color.rgb(239, 130, 40)
        c.drawCircle(x, y, r, pt)
        pt.color = Color.rgb(250, 190, 100)
        c.drawCircle(x - r * 0.25f, y - r * 0.25f, r * 0.55f, pt)
        pt.color = Color.argb(200, 255, 255, 255)
        c.drawCircle(x - r * 0.38f, y - r * 0.4f, r * 0.2f, pt)
        pt.style = Paint.Style.STROKE
        pt.strokeWidth = 2.5f
        pt.color = Color.rgb(150, 70, 20)
        c.drawCircle(x, y, r, pt)
        pt.style = Paint.Style.FILL
    }

    private fun drawTrajectory(c: Canvas) {
        val vx0 = (ANCHOR_X - bx) * LAUNCH_K
        val vy0 = (ANCHOR_Y - by) * LAUNCH_K
        pt.style = Paint.Style.FILL
        for (i in 1..16) {
            val t = i * 0.055f
            val px = bx + vx0 * t
            val py = by + vy0 * t + 0.5f * GRAV * t * t
            if (py > GROUND) break
            val alpha = (230 - i * 11).coerceAtLeast(40)
            pt.color = Color.argb(alpha, 255, 255, 255)
            c.drawCircle(px, py, 5.5f - i * 0.12f, pt)
        }
    }

    private fun drawBlock(c: Canvas, b: CatBlock) {
        val fill: Int
        val edge: Int
        when (b.kind) {
            1 -> {
                fill = Color.rgb(148, 154, 168)
                edge = Color.rgb(92, 98, 112)
            }
            2 -> {
                fill = Color.argb(225, 175, 228, 248)
                edge = Color.rgb(110, 180, 212)
            }
            else -> {
                fill = Color.rgb(182, 128, 72)
                edge = Color.rgb(112, 72, 38)
            }
        }
        rect.set(b.l, b.t, b.r, b.b)
        pt.style = Paint.Style.FILL
        pt.color = fill
        c.drawRoundRect(rect, 5f, 5f, pt)
        pt.style = Paint.Style.STROKE
        pt.strokeWidth = 3f
        pt.color = edge
        c.drawRoundRect(rect, 5f, 5f, pt)
        if (b.kind == WOOD) {
            pt.strokeWidth = 1.5f
            if (b.hw > b.hh) {
                c.drawLine(b.l + 8f, b.cy, b.r - 8f, b.cy, pt)
            } else {
                c.drawLine(b.cx, b.t + 8f, b.cx, b.b - 8f, pt)
            }
        }
        val ratio = b.hp / b.maxHp
        pt.strokeWidth = 2f
        pt.color = Color.argb(210, 40, 30, 20)
        if (ratio < 0.67f) {
            c.drawLine(b.cx - b.hw * 0.4f, b.t + 2f, b.cx, b.cy, pt)
            c.drawLine(b.cx, b.cy, b.cx + b.hw * 0.3f, b.b - 2f, pt)
        }
        if (ratio < 0.34f) {
            c.drawLine(b.cx + b.hw * 0.5f, b.t + 2f, b.cx - b.hw * 0.1f, b.cy + b.hh * 0.2f, pt)
            c.drawLine(b.cx - b.hw * 0.6f, b.b - 2f, b.cx - b.hw * 0.2f, b.cy, pt)
        }
        pt.style = Paint.Style.FILL
    }

    private fun drawTarget(c: Canvas, t: CatTarget) {
        pt.style = Paint.Style.FILL
        pt.color = Color.rgb(205, 40, 50)
        c.drawCircle(t.cx, t.cy, t.r, pt)
        pt.color = Color.WHITE
        c.drawCircle(t.cx, t.cy, t.r * 0.72f, pt)
        pt.color = Color.rgb(205, 40, 50)
        c.drawCircle(t.cx, t.cy, t.r * 0.46f, pt)
        pt.color = Color.WHITE
        c.drawCircle(t.cx, t.cy, t.r * 0.22f, pt)
        pt.style = Paint.Style.STROKE
        pt.strokeWidth = 2.5f
        pt.color = Color.rgb(120, 20, 30)
        c.drawCircle(t.cx, t.cy, t.r, pt)
        pt.style = Paint.Style.FILL
    }

    private fun drawParticles(c: Canvas) {
        pt.style = Paint.Style.FILL
        for (q in particles) {
            pt.color = q.color
            pt.alpha = (min(1f, q.life * 2f) * 255f).toInt()
            c.drawCircle(q.x, q.y, q.size, pt)
        }
        pt.alpha = 255
    }
}

class CatapultActivity : BaseGameActivity() {

    override val gameTitle: String get() = "منجنيق الكرات"

    private lateinit var game: CatapultView
    private lateinit var levelText: TextView
    private lateinit var statsText: TextView
    private lateinit var statusText: TextView
    private lateinit var prevBtn: Button
    private lateinit var nextBtn: Button

    private var level = 0
    private var unlocked = 1

    private companion object {
        const val KEY_UNLOCKED = "catapult_unlocked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎯 منجنيق الكرات")

        unlocked = Scores.get(this, KEY_UNLOCKED).coerceIn(1, CatapultView.LEVELS)
        level = unlocked - 1

        content.addView(
            label("اسحب الكرة للخلف ثم اتركها لتنطلق من المنجنيق وتُسقط جميع الأهداف!", 15f, R.color.text_dim, false),
            lp(top = dp(2), bottom = dp(6))
        )
        levelText = label("", 20f, R.color.gold)
        content.addView(levelText, lp(top = dp(2)))
        statsText = label("", 16f)
        content.addView(statsText, lp(top = dp(2), bottom = dp(8)))

        game = CatapultView(
            this,
            { balls, targets, score -> onHud(balls, targets, score) },
            { won, score, stars -> onEnd(won, score, stars) }
        )
        content.addView(game, lp())

        statusText = label("", 16f)
        content.addView(statusText, lp(top = dp(10)))

        val nav = row(10)
        prevBtn = button("المستوى السابق", false) { changeLevel(-1) }
        val restartBtn = button("إعادة المحاولة") { loadLevel() }
        nextBtn = button("المستوى التالي", false) { changeLevel(1) }
        nav.addView(prevBtn, weightLp(dp(50)))
        nav.addView(restartBtn, weightLp(dp(50)))
        nav.addView(nextBtn, weightLp(dp(50)))
        content.addView(nav)

        loadLevel()
    }

    override fun onPause() {
        game.stopLoop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        game.resume()
    }

    private fun bestKey(): String = "catapult_best_${level + 1}"

    private fun loadLevel() {
        statusText.text = "اسحب الكرة من المنجنيق نحو الخلف لتصويب الرمية."
        refreshHeader()
        game.loadLevel(level)
        updateNav()
    }

    private fun changeLevel(delta: Int) {
        val target = level + delta
        if (target < 0 || target >= unlocked) return
        level = target
        loadLevel()
    }

    private fun refreshHeader() {
        val best = Scores.get(this, bestKey())
        levelText.text = "المستوى ${level + 1} / ${CatapultView.LEVELS}" +
            if (best > 0) "  •  أفضل نتيجة: $best" else ""
    }

    private fun updateNav() {
        prevBtn.isEnabled = level > 0
        nextBtn.isEnabled = level + 1 < unlocked
        prevBtn.alpha = if (prevBtn.isEnabled) 1f else 0.4f
        nextBtn.alpha = if (nextBtn.isEnabled) 1f else 0.4f
    }

    private fun onHud(balls: Int, targets: Int, score: Int) {
        statsText.text = "🎯 الأهداف: $targets     ⚾ الكرات: $balls     ⭐ النقاط: $score"
    }

    private fun onEnd(won: Boolean, score: Int, stars: Int) {
        if (won) {
            val record = Scores.saveHigh(this, bestKey(), score)
            if (level + 2 > unlocked && level + 1 < CatapultView.LEVELS) {
                unlocked = level + 2
                Scores.put(this, KEY_UNLOCKED, unlocked)
            }
            val starsText = "⭐".repeat(stars)
            val last = level + 1 >= CatapultView.LEVELS
            statusText.text = (if (record) "🏆 رقم قياسي جديد! " else "🎉 أحسنت! ") +
                "أسقطت جميع الأهداف $starsText\nالنقاط: $score" +
                if (last) "\n🏆 أنهيت جميع المستويات!" else "\nانتقل إلى المستوى التالي."
            Feedback.win(this)
        } else {
            statusText.text = "نفدت الكرات وبقيت أهداف! جرّب زاوية أو قوة مختلفة."
            Feedback.lose(this)
        }
        refreshHeader()
        updateNav()
    }
}
