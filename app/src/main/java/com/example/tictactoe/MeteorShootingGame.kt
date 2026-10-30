package com.example.tictactoe 

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MeteorShootingGame : AppCompatActivity() {

    private lateinit var gameView: GameView
    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("صيد النيازك")

        val root = RelativeLayout(this)

        gameView = GameView(this)
        root.addView(gameView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        // ===== شريط التحكم =====
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 10, 20, 10)
        }

        val leftBtn = Button(this).apply {
            text = "◀ يسار"
            textSize = 16f
        }
        val fireBtn = Button(this).apply {
            text = "🔥 إطلاق"
            textSize = 18f
        }
        val rightBtn = Button(this).apply {
            text = "يمين ▶"
            textSize = 16f
        }

        val fireParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
        val sideParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        controls.addView(leftBtn, sideParams)
        controls.addView(fireBtn, fireParams)
        controls.addView(rightBtn, sideParams)

        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            bottomMargin = 20
            leftMargin = 20
            rightMargin = 20
        }
        root.addView(controls, lp)

        setContentView(root)

        // ===== أزرار الدوران مع الاستمرار عند الضغط =====
        leftBtn.setOnTouchListener { _, event -> handleHoldButton(event, -1f) }
        rightBtn.setOnTouchListener { _, event -> handleHoldButton(event, +1f) }
        fireBtn.setOnClickListener { gameView.fire() }
    }

    private fun handleHoldButton(event: MotionEvent, direction: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                gameView.rotateCannon(direction * 6f)
                val r = object : Runnable {
                    override fun run() {
                        gameView.rotateCannon(direction * 3f)
                        repeatHandler.postDelayed(this, 40)
                    }
                }
                repeatRunnable = r
                repeatHandler.postDelayed(r, 350)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                repeatRunnable?.let { repeatHandler.removeCallbacks(it) }
                repeatRunnable = null
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        repeatHandler.removeCallbacksAndMessages(null)
    }

    // ============================================================
    //                        محرك اللعبة
    // ============================================================
    inner class GameView(context: Context) : View(context) {

        // --- ثوابت اللعبة ---
        private val TOTAL_METEORS = 15
        private val TOTAL_HOUSES = 8
        private val TOTAL_MISSILES = 30
        private val MISSILE_SPEED = 850f
        private val METEOR_SPAWN_INTERVAL = 1.3f

        // --- حالة اللعبة ---
        private var meteorsSpawned = 0
        private var meteorsDestroyed = 0
        private var missilesLeft = TOTAL_MISSILES
        private var cannonAngle = 90f // 90 = للأعلى، 15 = يمين، 165 = يسار
        private var cannonX = 0f
        private var cannonY = 0f
        private val cannonLength = 75f
        private var gameOver = false
        private var won = false
        private var initialized = false
        private var lastFrameNano = 0L
        private var spawnTimer = 0f

        // --- كيانات اللعبة ---
        private class House(var x: Float, var y: Float, var width: Float, var height: Float) {
            var alive = true
        }
        private class Meteor(
            var x: Float, var y: Float,
            var vx: Float, var vy: Float,
            var radius: Float, var rot: Float, var rotSpeed: Float
        )
        private class Missile(var x: Float, var y: Float, var vx: Float, var vy: Float)

        private val houses = mutableListOf<House>()
        private val meteors = mutableListOf<Meteor>()
        private val missiles = mutableListOf<Missile>()
        private val stars = mutableListOf<Pair<Float, Float>>()

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()

        // ------------------------------------------------------------
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (!initialized) {
                initialized = true
                setupGame(w, h)
                postInvalidateOnAnimation()
            }
        }

        private fun setupGame(w: Int, h: Int) {
            houses.clear()
            meteors.clear()
            missiles.clear()
            stars.clear()

            val groundY = h - 140f
            val houseWidth = w / TOTAL_HOUSES.toFloat()
            for (i in 0 until TOTAL_HOUSES) {
                houses.add(
                    House(
                        x = i * houseWidth + houseWidth / 2f,
                        y = groundY,
                        width = houseWidth * 0.85f,
                        height = 75f
                    )
                )
            }
            cannonX = w / 2f
            cannonY = groundY - 25f

            repeat(70) {
                stars.add(Random.nextFloat() * w to Random.nextFloat() * (h * 0.75f))
            }
        }

        fun rotateCannon(delta: Float) {
            if (gameOver) return
            cannonAngle = (cannonAngle + delta).coerceIn(15f, 165f)
            invalidate()
        }

        fun fire() {
            if (gameOver || missilesLeft <= 0) return
            missilesLeft--
            val rad = Math.toRadians(cannonAngle.toDouble())
            val dx = cos(rad).toFloat()
            val dy = -sin(rad).toFloat()
            val startX = cannonX + dx * cannonLength
            val startY = cannonY + dy * cannonLength
            missiles.add(Missile(startX, startY, dx * MISSILE_SPEED, dy * MISSILE_SPEED))
            invalidate()
        }

        private fun resetGame() {
            meteorsSpawned = 0
            meteorsDestroyed = 0
            missilesLeft = TOTAL_MISSILES
            cannonAngle = 90f
            gameOver = false
            won = false
            spawnTimer = 0f
            lastFrameNano = 0L
            setupGame(width, height)
            invalidate()
        }

        private fun spawnMeteor() {
            meteorsSpawned++
            // استهداف أحد المنازل الحية
            val aliveHouses = houses.filter { it.alive }
            val targetX = if (aliveHouses.isNotEmpty()) {
                aliveHouses.random().x + (Random.nextFloat() - 0.5f) * 40f
            } else {
                Random.nextFloat() * width
            }
            val startX = Random.nextFloat() * width
            val targetY = height + 80f
            val dx = targetX - startX
            val dy = targetY - (-40f)
            val dist = sqrt(dx * dx + dy * dy)
            val speed = 130f + Random.nextFloat() * 90f

            meteors.add(
                Meteor(
                    x = startX,
                    y = -40f,
                    vx = dx / dist * speed,
                    vy = dy / dist * speed,
                    radius = 20f + Random.nextFloat() * 12f,
                    rot = 0f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 200f
                )
            )
        }

        // ------------------------------------------------------------
        override fun onDraw(canvas: Canvas) {
            val now = System.nanoTime()
            val dt = if (lastFrameNano == 0L) 0f
                     else ((now - lastFrameNano) / 1_000_000_000f).coerceAtMost(0.05f)
            lastFrameNano = now

            if (!gameOver) update(dt)

            // --- رسم المشهد ---
            canvas.drawColor(Color.parseColor("#0A0E27"))
            drawStars(canvas)
            drawGround(canvas)
            drawHouses(canvas)
            drawCannon(canvas)
            drawMeteors(canvas)
            drawMissiles(canvas)
            drawHUD(canvas)
            if (gameOver) drawGameOver(canvas)

            if (!gameOver) postInvalidateOnAnimation()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (gameOver && event.action == MotionEvent.ACTION_DOWN) {
                resetGame()
                return true
            }
            return super.onTouchEvent(event)
        }

        // ------------------------------------------------------------
        private fun update(dt: Float) {
            // إنشاء النيازك
            if (meteorsSpawned < TOTAL_METEORS) {
                spawnTimer += dt
                if (spawnTimer >= METEOR_SPAWN_INTERVAL) {
                    spawnTimer = 0f
                    spawnMeteor()
                }
            }

            // حركة النيازك
            val meteorIter = meteors.iterator()
            while (meteorIter.hasNext()) {
                val m = meteorIter.next()
                m.x += m.vx * dt
                m.y += m.vy * dt
                m.rot += m.rotSpeed * dt

                // تصادم مع المنازل
                var hitHouse = false
                for (h in houses) {
                    if (h.alive &&
                        m.y + m.radius > h.y - h.height &&
                        m.y - m.radius < h.y &&
                        m.x + m.radius > h.x - h.width / 2 &&
                        m.x - m.radius < h.x + h.width / 2
                    ) {
                        h.alive = false
                        hitHouse = true
                        break
                    }
                }
                if (hitHouse || m.y > height + 80) {
                    meteorIter.remove()
                }
            }

            // حركة القذائف
            val missileIter = missiles.iterator()
            while (missileIter.hasNext()) {
                val mm = missileIter.next()
                mm.x += mm.vx * dt
                mm.y += mm.vy * dt

                // تصادم مع نيزك
                var hit: Meteor? = null
                for (m in meteors) {
                    val dx = mm.x - m.x
                    val dy = mm.y - m.y
                    val r = m.radius + 10f
                    if (dx * dx + dy * dy < r * r) {
                        hit = m
                        break
                    }
                }
                if (hit != null) {
                    meteors.remove(hit)
                    meteorsDestroyed++
                    missileIter.remove()
                    continue
                }
                if (mm.x < -80 || mm.x > width + 80 || mm.y < -80 || mm.y > height + 80) {
                    missileIter.remove()
                }
            }

            // --- شروط الفوز/الخسارة ---
            if (houses.none { it.alive }) {
                gameOver = true
                won = false
            } else if (meteorsSpawned >= TOTAL_METEORS && meteors.isEmpty()) {
                gameOver = true
                won = true
            }
        }

        // ------------------------------------------------------------
        //                       دوال الرسم
        // ------------------------------------------------------------
        private fun drawStars(canvas: Canvas) {
            paint.color = Color.WHITE
            paint.alpha = 170
            for ((x, y) in stars) canvas.drawCircle(x, y, 1.6f, paint)
            paint.alpha = 255
        }

        private fun drawGround(canvas: Canvas) {
            paint.color = Color.parseColor("#1B2430")
            canvas.drawRect(0f, height - 60f, width.toFloat(), height.toFloat(), paint)
        }

        private fun drawHouses(canvas: Canvas) {
            for (h in houses) {
                val left = h.x - h.width / 2f
                val top = h.y - h.height

                if (h.alive) {
                    // جدار
                    paint.color = Color.parseColor("#8B4513")
                    canvas.drawRect(left, top, left + h.width, h.y, paint)
                    // سقف
                    paint.color = Color.parseColor("#C0392B")
                    path.reset()
                    path.moveTo(left - 6f, top)
                    path.lineTo(h.x, top - h.width * 0.42f)
                    path.lineTo(left + h.width + 6f, top)
                    path.close()
                    canvas.drawPath(path, paint)
                    // نافذة
                    paint.color = Color.parseColor("#FFEB3B")
                    canvas.drawRect(
                        h.x - h.width * 0.15f,
                        top + h.height * 0.25f,
                        h.x + h.width * 0.15f,
                        top + h.height * 0.55f, paint
                    )
                    // باب
                    paint.color = Color.parseColor("#3E2723")
                    canvas.drawRect(
                        h.x - h.width * 0.12f, h.y - h.height * 0.4f,
                        h.x + h.width * 0.12f, h.y, paint
                    )
                } else {
                    // بقايا الدمار
                    paint.color = Color.parseColor("#2C1A0F")
                    canvas.drawRect(left + 4f, h.y - h.height * 0.25f, left + h.width - 4f, h.y, paint)
                    paint.color = Color.parseColor("#FF5722")
                    canvas.drawCircle(h.x, h.y - h.height * 0.35f, 6f, paint)
                    paint.color = Color.parseColor("#FF9800")
                    paint.alpha = 120
                    canvas.drawCircle(h.x, h.y - h.height * 0.35f, 12f, paint)
                    paint.alpha = 255
                }
            }
        }

        private fun drawCannon(canvas: Canvas) {
            // ظل القاعدة
            paint.color = Color.parseColor("#000000")
            paint.alpha = 90
            canvas.drawCircle(cannonX + 5f, cannonY + 8f, 34f, paint)
            paint.alpha = 255

            // القاعدة
            paint.color = Color.parseColor("#37474F")
            canvas.drawCircle(cannonX, cannonY, 32f, paint)
            paint.color = Color.parseColor("#546E7A")
            canvas.drawCircle(cannonX, cannonY, 24f, paint)
            paint.color = Color.parseColor("#263238")
            canvas.drawCircle(cannonX, cannonY, 12f, paint)

            // الماسورة (تدور مع الزاوية)
            canvas.save()
            canvas.rotate(-(cannonAngle - 90f), cannonX, cannonY)
            paint.color = Color.parseColor("#263238")
            canvas.drawRect(
                cannonX - 11f, cannonY - cannonLength,
                cannonX + 11f, cannonY + 5f, paint
            )
            paint.color = Color.parseColor("#546E7A")
            canvas.drawRect(
                cannonX - 11f, cannonY - cannonLength,
                cannonX + 11f, cannonY - cannonLength + 10f, paint
            )
            // وميض الفوهة
            paint.color = Color.parseColor("#FFC107")
            canvas.drawCircle(cannonX, cannonY - cannonLength, 6f, paint)
            canvas.restore()

            // مؤشر الزاوية
            paint.color = Color.WHITE
            paint.textSize = 26f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${cannonAngle.toInt()}°", cannonX, cannonY + 52f, paint)
        }

        private fun drawMeteors(canvas: Canvas) {
            for (m in meteors) {
                // ذيل اللهب
                paint.color = Color.parseColor("#FF4500")
                paint.alpha = 110
                canvas.drawCircle(m.x - m.vx * 0.08f, m.y - m.vy * 0.08f, m.radius * 0.9f, paint)
                paint.alpha = 60
                canvas.drawCircle(m.x - m.vx * 0.16f, m.y - m.vy * 0.16f, m.radius * 0.7f, paint)
                paint.alpha = 255

                // النواة
                paint.color = Color.parseColor("#E65100")
                canvas.drawCircle(m.x, m.y, m.radius, paint)
                paint.color = Color.parseColor("#FF9800")
                canvas.drawCircle(m.x, m.y, m.radius * 0.82f, paint)
                paint.color = Color.parseColor("#FFC107")
                canvas.drawCircle(m.x - m.radius * 0.25f, m.y - m.radius * 0.25f, m.radius * 0.45f, paint)

                // فوهات
                paint.color = Color.parseColor("#BF360C")
                canvas.drawCircle(m.x + m.radius * 0.3f, m.y + m.radius * 0.2f, m.radius * 0.18f, paint)
                canvas.drawCircle(m.x - m.radius * 0.1f, m.y + m.radius * 0.4f, m.radius * 0.12f, paint)
            }
        }

        private fun drawMissiles(canvas: Canvas) {
            for (mm in missiles) {
                // ذيل
                paint.color = Color.parseColor("#FFEB3B")
                paint.alpha = 130
                canvas.drawCircle(mm.x - mm.vx * 0.03f, mm.y - mm.vy * 0.03f, 7f, paint)
                paint.alpha = 70
                canvas.drawCircle(mm.x - mm.vx * 0.06f, mm.y - mm.vy * 0.06f, 5f, paint)
                paint.alpha = 255
                // نواة
                paint.color = Color.parseColor("#FFF176")
                canvas.drawCircle(mm.x, mm.y, 6f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(mm.x, mm.y, 3f, paint)
            }
        }

        private fun drawHUD(canvas: Canvas) {
            // لوحة خلفية
            paint.color = Color.parseColor("#000000")
            paint.alpha = 130
            canvas.drawRect(0f, 0f, width.toFloat(), 90f, paint)
            paint.alpha = 255

            paint.color = Color.WHITE
            paint.textSize = 32f

            // قذائف متبقية (يسار)
            paint.textAlign = Paint.Align.LEFT
            paint.color = if (missilesLeft > 0) Color.parseColor("#4FC3F7") else Color.parseColor("#EF5350")
            canvas.drawText("🚀 $missilesLeft", 30f, 58f, paint)

            // نيازك مدمرة (يمين)
            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.parseColor("#FFB74D")
            canvas.drawText("☄️ $meteorsDestroyed / $TOTAL_METEORS", width - 30f, 58f, paint)

            // منازل (وسط)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 28f
            val aliveCount = houses.count { it.alive }
            paint.color = if (aliveCount > 3) Color.parseColor("#81C784") else Color.parseColor("#EF5350")
            canvas.drawText("🏠 $aliveCount / $TOTAL_HOUSES", width / 2f, 58f, paint)
        }

        private fun drawGameOver(canvas: Canvas) {
            paint.color = Color.parseColor("#000000")
            paint.alpha = 200
            canvas.drawRect(0f, height / 2f - 160f, width.toFloat(), height / 2f + 160f, paint)
            paint.alpha = 255

            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 70f
            paint.color = if (won) Color.parseColor("#66BB6A") else Color.parseColor("#EF5350")
            canvas.drawText(if (won) "🎉 فزت!" else "💥 خسرت", width / 2f, height / 2f, paint)

            paint.textSize = 26f
            paint.color = Color.WHITE
            val sub = if (won)
                "دمرت $meteorsDestroyed نيزك، و${houses.count { it.alive }} منازل نجت"
            else
                "تم تدمير ${TOTAL_HOUSES - houses.count { it.alive }} منزل"
            canvas.drawText(sub, width / 2f, height / 2f + 55f, paint)

            paint.textSize = 22f
            paint.color = Color.parseColor("#B0BEC5")
            canvas.drawText("اضغط في أي مكان لإعادة اللعب", width / 2f, height / 2f + 110f, paint)
        }

        private val alivecountHouse: Int get() = houses.count { it.alive }
    }
}
