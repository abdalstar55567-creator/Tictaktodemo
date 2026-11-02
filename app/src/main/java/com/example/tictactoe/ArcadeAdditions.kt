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
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** A compact, touch-first Tetris implementation. */
private class TetrisBoardView(
    context: Context,
    private val scoreChanged: (Int, Int) -> Unit,
    private val finished: (Int) -> Unit
) : GameLoopView(context) {
    override val aspect: Float get() = 1.18f
    private val cols = 10
    private val rows = 20
    private val board = Array(rows) { IntArray(cols) }
    private val colors = intArrayOf(
        Color.TRANSPARENT, Color.rgb(53, 210, 225), Color.rgb(70, 125, 245),
        Color.rgb(255, 165, 65), Color.rgb(255, 215, 75), Color.rgb(75, 205, 125),
        Color.rgb(175, 105, 245), Color.rgb(245, 85, 110)
    )
    private val shapes = arrayOf(
        arrayOf(intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(2, 0), intArrayOf(3, 0)),
        arrayOf(intArrayOf(0, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(2, 1)),
        arrayOf(intArrayOf(2, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(2, 1)),
        arrayOf(intArrayOf(1, 0), intArrayOf(2, 0), intArrayOf(1, 1), intArrayOf(2, 1)),
        arrayOf(intArrayOf(1, 0), intArrayOf(2, 0), intArrayOf(0, 1), intArrayOf(1, 1)),
        arrayOf(intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(2, 1)),
        arrayOf(intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(2, 1))
    )
    private data class Piece(var type: Int, var x: Int, var y: Int, var cells: Array<IntArray>)
    private var piece = newPiece()
    private var score = 0
    private var lines = 0
    private var elapsed = 0f
    private var fallEvery = .72f
    private var started = false
    private var over = false
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }

    private fun newPiece(): Piece {
        val type = Random.nextInt(shapes.size)
        return Piece(type, 3, 0, shapes[type].map { it.copyOf() }.toTypedArray())
    }
    fun start() {
        stopLoop()
        for (r in 0 until rows) board[r].fill(0)
        score = 0; lines = 0; elapsed = 0f; fallEvery = .72f; over = false; started = true
        piece = newPiece(); scoreChanged(score, lines)
        if (!valid(piece.x, piece.y, piece.cells)) { over = true; finished(score) } else startLoop()
        invalidate()
    }
    private fun valid(x: Int, y: Int, cells: Array<IntArray>): Boolean = cells.all {
        val bx = x + it[0]; val by = y + it[1]
        bx in 0 until cols && by < rows && (by < 0 || board[by][bx] == 0)
    }
    private fun move(dx: Int, dy: Int): Boolean {
        if (!started || over) return false
        if (valid(piece.x + dx, piece.y + dy, piece.cells)) { piece.x += dx; piece.y += dy; return true }
        if (dy > 0) lockPiece()
        return false
    }
    fun left() { move(-1, 0); invalidate() }
    fun right() { move(1, 0); invalidate() }
    fun down() { if (move(0, 1)) { score += 1; scoreChanged(score, lines) }; invalidate() }
    fun rotate() {
        if (!started || over) return
        val raw = piece.cells.map { intArrayOf(piece.cells.maxOf { cell -> cell[1] } - it[1], it[0]) }
        val minX = raw.minOf { it[0] }; val minY = raw.minOf { it[1] }
        val rotated = raw.map { intArrayOf(it[0] - minX, it[1] - minY) }.toTypedArray()
        for (kick in intArrayOf(0, -1, 1, -2, 2)) if (valid(piece.x + kick, piece.y, rotated)) {
            piece.x += kick; piece.cells = rotated; break
        }
        invalidate()
    }
    private fun lockPiece() {
        piece.cells.forEach { cell ->
            val x = piece.x + cell[0]; val y = piece.y + cell[1]
            if (y in 0 until rows && x in 0 until cols) board[y][x] = piece.type + 1
        }
        var cleared = 0
        var y = rows - 1
        while (y >= 0) {
            if (board[y].all { it != 0 }) {
                for (r in y downTo 1) board[r] = board[r - 1].copyOf()
                board[0].fill(0); cleared++
            } else y--
        }
        if (cleared > 0) {
            lines += cleared
            score += when (cleared) { 1 -> 100; 2 -> 300; 3 -> 500; else -> 800 }
            fallEvery = max(.16f, .72f - (lines / 5) * .07f)
        }
        scoreChanged(score, lines)
        piece = newPiece()
        if (!valid(piece.x, piece.y, piece.cells)) {
            over = true; started = false; stopLoop(); finished(score)
        }
    }
    override fun update(dt: Float) {
        if (!started || over) return
        elapsed += dt
        if (elapsed >= fallEvery) { elapsed = 0f; move(0, 1) }
    }
    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        p.shader = LinearGradient(0f, 0f, w, h, intArrayOf(Color.rgb(10, 20, 48), Color.rgb(30, 35, 82), Color.rgb(43, 24, 75)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p); p.shader = null
        val cell = min((w * .72f) / cols, (h * .88f) / rows)
        val bw = cell * cols; val bh = cell * rows
        val ox = (w - bw) / 2f; val oy = (h - bh) / 2f + h * .025f
        p.color = Color.argb(130, 2, 8, 25); c.drawRoundRect(ox - 7, oy - 7, ox + bw + 7, oy + bh + 7, 16f, 16f, p)
        for (r in 0 until rows) for (col in 0 until cols) {
            val value = board[r][col]
            drawCell(c, ox + col * cell, oy + r * cell, cell, if (value == 0) Color.rgb(35, 47, 79) else colors[value])
        }
        if (started && !over) piece.cells.forEach { drawCell(c, ox + (piece.x + it[0]) * cell, oy + (piece.y + it[1]) * cell, cell, colors[piece.type + 1]) }
        text.textSize = w * .045f; c.drawText("TETRIS", w / 2f, h * .055f, text)
        if (!started || over) {
            p.color = Color.argb(205, 5, 10, 30); c.drawRoundRect(w * .12f, h * .40f, w * .88f, h * .61f, 18f, 18f, p)
            text.textSize = w * .052f; c.drawText(if (over) "انتهت اللعبة" else "اضغط ابدأ للعب", w / 2f, h * .49f, text)
        }
    }
    private fun drawCell(c: Canvas, x: Float, y: Float, size: Float, color: Int) {
        p.color = color; c.drawRoundRect(x + 1.5f, y + 1.5f, x + size - 1.5f, y + size - 1.5f, size * .13f, size * .13f, p)
        p.color = Color.argb(75, 255, 255, 255); c.drawRoundRect(x + size * .16f, y + size * .12f, x + size * .84f, y + size * .24f, size * .06f, size * .06f, p)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { grab(); touchX = event.x; touchY = event.y; return true }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchX; val dy = event.y - touchY
                if (abs(dx) > abs(dy) && abs(dx) > width * .06f) { if (dx > 0) right() else left() }
                else if (dy > height * .05f) down() else if (dy < -height * .05f) rotate()
                return true
            }
        }
        return true
    }
    private var touchX = 0f
    private var touchY = 0f
}

class TetrisActivity : BaseGameActivity() {
    override val gameTitle = "تيترس"
    private lateinit var board: TetrisBoardView
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)
        content.addView(label("رتّب القطع وأكمل الصفوف قبل أن تمتلئ اللوحة", 14f, R.color.text_dim, false), lp(bottom = dp(4)))
        status = label("النقاط: 0   •   الصفوف: 0", 18f)
        content.addView(status, lp(bottom = dp(5)))
        board = TetrisBoardView(this, { s, l -> status.text = "النقاط: $s   •   الصفوف: $l" }, { s -> status.text = "انتهت اللعبة • نتيجتك: $s" })
        content.addView(board, lp())
        val controls = row(dp(5))
        controls.addView(button("◀", true) { board.left() }, weightLp(dp(48)))
        controls.addView(button("⟳", true) { board.rotate() }, weightLp(dp(48)))
        controls.addView(button("▼", true) { board.down() }, weightLp(dp(48)))
        controls.addView(button("▶", true) { board.right() }, weightLp(dp(48)))
        content.addView(controls)
        content.addView(button("ابدأ / العب من جديد") { board.start() }, lp(top = dp(5), bottom = dp(8)))
        board.start()
    }
    override fun onPause() { if (::board.isInitialized) board.stopLoop(); super.onPause() }
}

/** Defend the three houses by launching interceptors at incoming meteors. */
private class MeteorDefenseView(
    context: Context,
    private val scoreChanged: (Int, Int) -> Unit,
    private val gameOver: (Int) -> Unit
) : GameLoopView(context) {
    override val aspect: Float get() = 1.20f
    private data class Meteor(var x: Float, var y: Float, var vx: Float, var vy: Float, var radius: Float, var hp: Int = 1)
    private data class Missile(var x: Float, var y: Float, var tx: Float, var ty: Float, var speed: Float = .95f)
    private data class Blast(var x: Float, var y: Float, var age: Float = 0f)
    private data class House(val x: Float, var hp: Int = 3)
    private val meteors = mutableListOf<Meteor>()
    private val missiles = mutableListOf<Missile>()
    private val blasts = mutableListOf<Blast>()
    private val houses = mutableListOf(House(.18f), House(.50f), House(.82f))
    private val stars = List(48) { Random.nextFloat() to Random.nextFloat() }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val white = Color.WHITE
    private var score = 0
    private var stage = 1
    private var spawned = 0
    private var spawnTimer = 0f
    private var elapsed = 0f
    private var started = false
    private var ended = false
    private var cannonX = .5f
    private var lastShot = 0f
    private val totalPerStage = 12

    fun start() {
        stopLoop(); meteors.clear(); missiles.clear(); blasts.clear(); houses.forEach { it.hp = 3 }
        score = 0; stage = 1; spawned = 0; spawnTimer = .2f; elapsed = 0f; started = true; ended = false; cannonX = .5f; lastShot = 0f
        scoreChanged(score, stage); startLoop(); invalidate()
    }
    private fun finish() { if (ended) return; ended = true; started = false; stopLoop(); gameOver(score); invalidate() }
    private fun shoot(x: Float, y: Float) {
        if (!started || ended || missiles.size >= 7) return
        val tx = x.coerceIn(.06f, .94f); val ty = y.coerceIn(.12f, .70f)
        missiles.add(Missile(cannonX, .88f, tx, ty)); lastShot = elapsed
    }
    override fun update(dt: Float) {
        if (!started || ended) return
        elapsed += dt; spawnTimer += dt
        val interval = max(.38f, 1.18f - stage * .075f)
        if (spawned < totalPerStage && spawnTimer >= interval) {
            spawnTimer = 0f; spawned++
            val tx = houses.filter { it.hp > 0 }.randomOrNull()?.x ?: .5f
            val sx = Random.nextFloat() * .84f + .08f
            val targetY = .84f
            val dx = tx - sx
            val dy = targetY - .04f
            val mag = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(.01f)
            val speed = .18f + stage * .018f
            meteors.add(Meteor(sx, -.03f, dx / mag * speed, dy / mag * speed, .018f + Random.nextFloat() * .012f, if (stage >= 4 && Random.nextFloat() < .22f) 2 else 1))
        }
        meteors.forEach { it.x += it.vx * dt; it.y += it.vy * dt }
        missiles.forEach { m ->
            val dx = m.tx - m.x; val dy = m.ty - m.y; val d = kotlin.math.sqrt(dx * dx + dy * dy)
            if (d < m.speed * dt + .018f) { m.x = m.tx; m.y = m.ty; blasts.add(Blast(m.x, m.y)) }
            else { m.x += dx / d * m.speed * dt; m.y += dy / d * m.speed * dt }
        }
        missiles.removeAll { it.x == it.tx && it.y == it.ty }
        blasts.forEach { it.age += dt }
        val iterator = meteors.iterator()
        while (iterator.hasNext()) {
            val m = iterator.next()
            val blast = blasts.firstOrNull { b ->
                val dx = m.x - b.x; val dy = m.y - b.y
                kotlin.math.sqrt(dx * dx + dy * dy) < (.12f + b.age * .10f) && b.age < .55f
            }
            if (blast != null) { m.hp--; if (m.hp <= 0) { iterator.remove(); score += 10; scoreChanged(score, stage) } }
            if (m.y >= .86f) {
                val house = houses.minByOrNull { abs(it.x - m.x) }
                if (house != null && abs(house.x - m.x) < .22f && house.hp > 0) house.hp--
                iterator.remove()
            }
        }
        blasts.removeAll { it.age > .58f }
        if (houses.all { it.hp <= 0 }) { finish(); return }
        if (spawned >= totalPerStage && meteors.isEmpty() && missiles.isEmpty()) {
            stage++; spawned = 0; spawnTimer = -.35f; score += 25; scoreChanged(score, stage)
        }
    }
    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        p.shader = LinearGradient(0f, 0f, w, h, intArrayOf(Color.rgb(4, 10, 34), Color.rgb(20, 25, 72), Color.rgb(49, 23, 78)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p); p.shader = null
        stars.forEachIndexed { i, s -> p.color = if (i % 5 == 0) Color.rgb(255, 224, 150) else Color.rgb(190, 211, 255); c.drawCircle(s.first * w, s.second * h, if (i % 5 == 0) 2.4f else 1.3f, p) }
        // Deep-space moon, layered mountain silhouettes and atmospheric horizon.
        p.shader = LinearGradient(w * .68f, h * .08f, w * .88f, h * .34f,
            intArrayOf(Color.rgb(226, 224, 210), Color.rgb(112, 126, 151), Color.rgb(54, 67, 103)), null, Shader.TileMode.CLAMP)
        c.drawCircle(w * .79f, h * .20f, w * .105f, p); p.shader = null
        p.color = Color.argb(42, 40, 47, 67)
        c.drawCircle(w * .75f, h * .17f, w * .018f, p); c.drawCircle(w * .83f, h * .23f, w * .026f, p)
        val ridge = Path().apply { moveTo(0f, h * .82f); lineTo(w * .12f, h * .70f); lineTo(w * .25f, h * .80f); lineTo(w * .42f, h * .67f); lineTo(w * .57f, h * .79f); lineTo(w * .74f, h * .69f); lineTo(w, h * .81f); lineTo(w, h); lineTo(0f, h); close() }
        p.color = Color.rgb(22, 35, 62); c.drawPath(ridge, p)
        val farRidge = Path().apply { moveTo(0f, h * .86f); lineTo(w * .18f, h * .77f); lineTo(w * .34f, h * .85f); lineTo(w * .60f, h * .76f); lineTo(w * .82f, h * .84f); lineTo(w, h * .78f); lineTo(w, h); lineTo(0f, h); close() }
        p.color = Color.rgb(35, 47, 66); c.drawPath(farRidge, p)
        p.shader = LinearGradient(0f, h * .88f, 0f, h, intArrayOf(Color.rgb(73, 77, 79), Color.rgb(22, 31, 43)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, h * .88f, w, h, p); p.shader = null
        p.color = Color.rgb(111, 105, 94)
        for (i in 0..24) { val x = (i * 47 % 101) / 101f * w; val y = h * (.90f + (i % 5) * .018f); c.drawOval(x, y, x + w * .018f, y + h * .006f, p) }
        // Three defended homes with visible health indicators.
        houses.forEach { house ->
            val x = house.x * w; val base = h * .91f; val bw = w * .13f; val bh = h * .10f
            p.shader = LinearGradient(x - bw / 2, base - bh, x + bw / 2, base,
                if (house.hp > 0) intArrayOf(Color.rgb(159, 177, 181), Color.rgb(72, 104, 119), Color.rgb(37, 58, 75)) else intArrayOf(Color.DKGRAY, Color.GRAY), null, Shader.TileMode.CLAMP)
            c.drawRoundRect(x - bw / 2, base - bh, x + bw / 2, base, 5f, 5f, p); p.shader = null
            // lit windows and masonry seams
            for (row in 0..1) for (col in 0..1) {
                p.color = if (house.hp > 0) Color.rgb(255, 205, 111) else Color.rgb(55, 59, 65)
                val wx = x - bw * .30f + col * bw * .40f; val wy = base - bh * (.76f - row * .32f)
                c.drawRoundRect(wx, wy, wx + bw * .18f, wy + bh * .16f, 2f, 2f, p)
            }
            p.color = if (house.hp > 0) Color.rgb(255, 191, 105) else Color.rgb(95, 75, 78)
            val roof = Path().apply { moveTo(x - bw * .62f, base - bh); lineTo(x, base - bh * 1.65f); lineTo(x + bw * .62f, base - bh); close() }
            c.drawPath(roof, p)
            p.color = Color.rgb(28, 43, 70); c.drawRect(x - bw * .10f, base - bh * .48f, x + bw * .10f, base, p)
            p.color = Color.rgb(255, 220, 95); c.drawRoundRect(x - bw * .42f, base - bh * 1.88f, x + bw * .42f, base - bh * 1.72f, 4f, 4f, p)
            for (i in 0 until 3) { p.color = if (i < house.hp) Color.rgb(83, 235, 155) else Color.rgb(80, 83, 103); c.drawCircle(x - w * .035f + i * w * .035f, base - bh * 2.03f, w * .009f, p) }
        }
        // Incoming asteroids: incandescent atmospheric tail, irregular rocky body and craters.
        meteors.forEach { m ->
            val x = m.x * w; val y = m.y * h; val r = m.radius * w
            val tail = Path().apply { moveTo(x - m.vx * w * .34f, y - m.vy * h * .34f); lineTo(x - r * .42f, y - r * .38f); lineTo(x + r * .42f, y + r * .38f); close() }
            p.shader = LinearGradient(x, y - m.vy * h * .34f, x, y, intArrayOf(Color.TRANSPARENT, Color.argb(95, 255, 91, 35), Color.argb(210, 255, 192, 91)), null, Shader.TileMode.CLAMP)
            c.drawPath(tail, p); p.shader = null
            p.color = Color.argb(45, 255, 88, 32); c.drawCircle(x, y, r * 2.25f, p)
            val rock = Path().apply {
                val factors = floatArrayOf(1f, .82f, 1.08f, .88f, 1.02f, .78f, 1.05f, .91f)
                for (i in factors.indices) { val a = (Math.PI * 2.0 * i / factors.size).toFloat(); val px = x + kotlin.math.cos(a) * r * factors[i]; val py = y + kotlin.math.sin(a) * r * factors[i]; if (i == 0) moveTo(px, py) else lineTo(px, py) }; close()
            }
            p.shader = LinearGradient(x - r, y - r, x + r, y + r, intArrayOf(Color.rgb(255, 202, 119), Color.rgb(183, 91, 57), Color.rgb(65, 48, 53)), null, Shader.TileMode.CLAMP)
            c.drawPath(rock, p); p.shader = null
            p.color = Color.argb(125, 55, 36, 39); c.drawCircle(x + r * .27f, y + r * .18f, r * .22f, p)
            p.color = Color.argb(105, 255, 185, 117); c.drawCircle(x - r * .30f, y - r * .27f, r * .13f, p)
            if (m.hp > 1) { p.color = Color.WHITE; p.style = Paint.Style.STROKE; p.strokeWidth = 2f; c.drawCircle(x, y, r * 1.22f, p); p.style = Paint.Style.FILL }
        }
        missiles.forEach { m ->
            p.color = Color.argb(100, 90, 220, 255); c.drawCircle(m.x * w, m.y * h, w * .018f, p)
            p.color = Color.rgb(255, 235, 125); c.drawRoundRect(m.x * w - w * .004f, m.y * h, m.x * w + w * .004f, m.y * h + h * .045f, 4f, 4f, p)
        }
        blasts.forEach { b ->
            val radius = w * (.025f + b.age * .23f); p.color = Color.argb((190 * (1f - b.age / .58f)).toInt().coerceIn(0, 190), 255, 177, 75); c.drawCircle(b.x * w, b.y * h, radius, p)
            p.color = Color.argb(100, 255, 245, 190); c.drawCircle(b.x * w, b.y * h, radius * .48f, p)
        }
        // Detailed mobile interceptor turret with armored base, barrel and glowing core.
        val cx = cannonX * w; val cy = h * .90f
        p.color = Color.argb(45, 0, 0, 0); c.drawOval(cx - w * .075f, cy - h * .015f, cx + w * .075f, cy + h * .025f, p)
        p.shader = LinearGradient(cx - w * .05f, cy - h * .05f, cx + w * .05f, cy + h * .04f, intArrayOf(Color.rgb(180, 205, 214), Color.rgb(49, 83, 107), Color.rgb(18, 34, 54)), null, Shader.TileMode.CLAMP)
        c.drawRoundRect(cx - w * .055f, cy - h * .045f, cx + w * .055f, cy + h * .025f, 10f, 10f, p); p.shader = null
        p.color = Color.rgb(122, 157, 172); c.drawRoundRect(cx - w * .014f, cy - h * .12f, cx + w * .014f, cy - h * .025f, 5f, 5f, p)
        p.color = Color.rgb(24, 41, 58); c.drawRoundRect(cx - w * .009f, cy - h * .115f, cx + w * .009f, cy - h * .03f, 3f, 3f, p)
        p.color = Color.rgb(255, 196, 93); c.drawCircle(cx, cy - h * .035f, w * .012f, p)
        p.color = Color.rgb(90, 223, 255); c.drawCircle(cx, cy - h * .035f, w * .005f, p)
        p.color = Color.rgb(170, 198, 211); c.drawCircle(cx - w * .04f, cy - h * .012f, w * .006f, p); c.drawCircle(cx + w * .04f, cy - h * .012f, w * .006f, p)
        p.color = white; p.textSize = w * .035f; p.textAlign = Paint.Align.CENTER; p.typeface = android.graphics.Typeface.DEFAULT_BOLD
        c.drawText("المس مكان النيزك لإطلاق صاروخ", w / 2f, h * .075f, p)
        if (!started || ended) {
            p.color = Color.argb(205, 4, 8, 28); c.drawRoundRect(w * .10f, h * .39f, w * .90f, h * .60f, 18f, 18f, p)
            p.color = white; p.textSize = w * .052f; c.drawText(if (ended) "انتهت المهمة" else "احمِ المنازل الثلاثة", w / 2f, h * .48f, p)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { grab(); touchX = event.x; touchY = event.y; return true }
            MotionEvent.ACTION_UP -> { val nx = (event.x / width).coerceIn(.08f, .92f); cannonX = nx; shoot(nx, (event.y / height).coerceIn(.12f, .72f)); invalidate(); return true }
            MotionEvent.ACTION_MOVE -> { cannonX = (event.x / width).coerceIn(.08f, .92f); invalidate(); return true }
        }
        return true
    }
    private var touchX = 0f
    private var touchY = 0f
}

class MeteorShootingActivity : BaseGameActivity() {
    override val gameTitle = "صيد النيازك"
    private lateinit var game: MeteorDefenseView
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)
        content.addView(label("أطلق الصواريخ واعترض النيازك قبل أن تصيب المنازل", 14f, R.color.text_dim, false), lp(bottom = dp(4)))
        status = label("النقاط: 0   •   المرحلة: 1", 18f); content.addView(status, lp(bottom = dp(5)))
        game = MeteorDefenseView(this, { s, level -> status.text = "النقاط: $s   •   المرحلة: $level" }, { s -> status.text = "انتهت المهمة • النقاط: $s" })
        content.addView(game, lp())
        content.addView(button("ابدأ / أعد المهمة") { game.start() }, lp(top = dp(6), bottom = dp(8)))
        game.start()
    }
    override fun onPause() { if (::game.isInitialized) game.stopLoop(); super.onPause() }
}

/** Existing arcade renderer reused for two additional driving variations. */
class BikeRaceActivity : MotionGameActivity() { override val mode = MotionGame.BIKE }
