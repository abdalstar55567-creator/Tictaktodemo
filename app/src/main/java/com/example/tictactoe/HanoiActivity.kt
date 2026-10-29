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
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** أبراج هانوي بقواعد صحيحة ورسم ثلاثي الأبعاد تقريبي للأقراص الخشبية. */
class HanoiActivity : BaseGameActivity() {
    override val gameTitle = "أبراج هانوي"

    // ترتيب كل عمود من الأسفل إلى الأعلى؛ آخر عنصر هو القرص الموجود في القمة.
    private val towers = mutableListOf(mutableListOf(4, 3, 2, 1), mutableListOf<Int>(), mutableListOf<Int>())
    private var selected = -1
    private var moves = 0
    private lateinit var info: TextView
    private lateinit var hint: TextView
    private lateinit var board: HanoiBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)
        info = label("الحركات: 0  •  الحد الأدنى: 15", 17f, R.color.gold)
        hint = label("اختر العمود الذي تريد نقل القرص العلوي منه.", 14f, R.color.text_dim, false)
        content.addView(info, lp(top = dp(8), bottom = dp(3)))
        content.addView(hint, lp(bottom = dp(8)))
        board = HanoiBoardView(this)
        content.addView(board, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(button("إعادة اللعبة", false) { reset() }, lp(top = dp(8), bottom = dp(8)))
        updateText()
    }

    private fun tap(col: Int) {
        if (towers[2].size == 4) return
        if (selected == -1) {
            if (towers[col].isNotEmpty()) {
                selected = col
                hint.text = "الآن اختر العمود الذي ستضع القرص فيه."
            } else {
                hint.text = "هذا العمود فارغ؛ اختر عموداً يحتوي على أقراص."
            }
        } else if (selected == col) {
            selected = -1
            hint.text = "تم إلغاء الاختيار. اختر قرصاً من عمود آخر."
        } else {
            val from = towers[selected]
            val to = towers[col]
            if (from.isNotEmpty()) {
                val disk = from.last()
                if (to.isEmpty() || to.last() > disk) {
                    to.add(from.removeAt(from.lastIndex))
                    moves++
                    hint.text = "نقلة صحيحة!"
                    if (towers[2].size == 4) {
                        hint.text = if (moves == 15) "ممتاز! أنهيتها بأقل عدد ممكن من الحركات 🎉"
                        else "أحسنت! أكملت اللغز في $moves حركة. حاول الوصول إلى 15 حركة."
                        Feedback.win(this)
                    } else if (to.isNotEmpty() && to.last() == disk) {
                        // لا شيء إضافياً؛ رسالة القواعد أدناه تظهر عند الحركة غير الصحيحة.
                    }
                } else {
                    hint.text = "حركة غير مسموحة: لا تضع قرصاً كبيراً فوق قرص أصغر."
                    Feedback.lose(this)
                }
            }
            selected = -1
        }
        updateText()
    }

    private fun reset() {
        towers.forEach { it.clear() }
        towers[0].addAll(listOf(4, 3, 2, 1))
        selected = -1
        moves = 0
        hint.text = "انقل الأقراص الأربعة إلى العمود الثالث، ولا تضع كبيراً فوق صغير."
        updateText()
    }

    private fun updateText() {
        info.text = "الحركات: $moves  •  الحد الأدنى: 15"
        board.invalidate()
    }

    private inner class HanoiBoardView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val base = RectF()
        private val diskColors = intArrayOf(0xFFEF5350.toInt(), 0xFFFFA726.toInt(), 0xFF42A5F5.toInt(), 0xFF66BB6A.toInt())
        private fun colorPaint(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            if (w <= 0f || h <= 0f) return
            // خلفية هادئة مع إضاءة في أعلى اللوحة.
            canvas.drawColor(Color.rgb(15, 23, 42))
            paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(30, 50, 75), Color.rgb(12, 19, 34), Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null

            val floorY = h * .80f
            val centers = floatArrayOf(w * .19f, w * .50f, w * .81f)
            val rodWidth = (w * .025f).coerceAtLeast(8f)
            val rodTop = h * .19f
            val diskH = (h * .105f).coerceAtLeast(24f).coerceAtMost(48f)
            val maxDiskW = w * .255f
            val minDiskW = w * .105f

            // ظل القاعدة ثم قاعدة خشبية ذات حواف مضيئة.
            canvas.drawOval(w * .06f, floorY - 2f, w * .94f, floorY + h * .06f, colorPaint(0x66000000))
            base.set(w * .055f, floorY - h * .025f, w * .945f, floorY + h * .025f)
            paint.shader = LinearGradient(0f, base.top, 0f, base.bottom, Color.rgb(190, 123, 62), Color.rgb(83, 45, 26), Shader.TileMode.CLAMP)
            canvas.drawRoundRect(base, 14f, 14f, paint); paint.shader = null
            canvas.drawRoundRect(base.left + 4f, base.top + 3f, base.right - 4f, base.top + 7f, 4f, 4f, colorPaint(0x88FFE0A3.toInt()))

            for (i in 0..2) {
                val cx = centers[i]
                // توهج خفيف للعمود المختار.
                if (selected == i) canvas.drawRoundRect(cx - maxDiskW * .63f, rodTop - 10f, cx + maxDiskW * .63f, floorY, 20f, 20f, colorPaint(0x2238BDF8))
                val rodRect = RectF(cx - rodWidth / 2, rodTop, cx + rodWidth / 2, floorY - h * .015f)
                paint.shader = LinearGradient(rodRect.left, 0f, rodRect.right, 0f, intArrayOf(0xFF70452C.toInt(), 0xFFE5B477.toInt(), 0xFF77452A.toInt()), null, Shader.TileMode.CLAMP)
                canvas.drawRoundRect(rodRect, rodWidth / 2, rodWidth / 2, paint); paint.shader = null
                canvas.drawCircle(cx, rodTop, rodWidth * .70f, colorPaint(0xFFE7C18D.toInt()))

                val disks = towers[i]
                disks.forEachIndexed { level, disk ->
                    val dw = minDiskW + (disk - 1) * ((maxDiskW - minDiskW) / 3f)
                    val yBottom = floorY - h * .025f - level * diskH
                    val lift = if (selected == i && level == disks.lastIndex) diskH * .38f else 0f
                    val top = yBottom - diskH - lift
                    val rect = RectF(cx - dw / 2, top, cx + dw / 2, yBottom - lift)
                    // ظل واقعي مبسط وحافة سفلية داكنة.
                    canvas.drawRoundRect(RectF(rect.left + 3f, rect.top + 5f, rect.right + 4f, rect.bottom + 7f), diskH * .20f, diskH * .20f, colorPaint(0x66000000))
                    val baseColor = diskColors[disk - 1]
                    paint.shader = LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                        intArrayOf(lighten(baseColor, 1.22f), baseColor, darken(baseColor, .62f)), null, Shader.TileMode.CLAMP)
                    canvas.drawRoundRect(rect, diskH * .20f, diskH * .20f, paint); paint.shader = null
                    canvas.drawRoundRect(rect.left + 4f, rect.top + 3f, rect.right - 4f, rect.top + diskH * .20f,
                        diskH * .10f, diskH * .10f, colorPaint(0x77FFFFFF))
                    canvas.drawRoundRect(rect.left + 4f, rect.bottom - diskH * .18f, rect.right - 4f, rect.bottom - 3f,
                        diskH * .08f, diskH * .08f, colorPaint(0x55000000))
                }
                paint.color = if (selected == i) 0xFF7DD3FC.toInt() else Color.rgb(226, 232, 240)
                paint.textSize = (w * .042f).coerceIn(14f, 23f)
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                canvas.drawText("العمود ${i + 1}", cx, h * .93f, paint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) {
                val col = when {
                    event.x < width / 3f -> 0
                    event.x < width * 2f / 3f -> 1
                    else -> 2
                }
                performClick()
                tap(col)
                return true
            }
            return true
        }

        override fun performClick(): Boolean { super.performClick(); return true }

        private fun lighten(c: Int, factor: Float): Int = Color.rgb(
            (Color.red(c) * factor).toInt().coerceAtMost(255),
            (Color.green(c) * factor).toInt().coerceAtMost(255),
            (Color.blue(c) * factor).toInt().coerceAtMost(255))
        private fun darken(c: Int, factor: Float): Int = Color.rgb(
            (Color.red(c) * factor).toInt().coerceAtLeast(0),
            (Color.green(c) * factor).toInt().coerceAtLeast(0),
            (Color.blue(c) * factor).toInt().coerceAtLeast(0))
    }
}
