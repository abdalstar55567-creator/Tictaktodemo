package com.example.tictactoe

import android.content.Context
import android.graphics.Paint
import android.os.SystemClock
import android.view.View

/**
 * أساس ألعاب الحركة: حلقة تحديث (~60 إطار/ثانية) ورسم على Canvas.
 * الألعاب ترث هذا الصنف وتكتب update(dt) و onDraw().
 */
abstract class GameLoopView(context: Context) : View(context) {

    var running = false
        private set

    private var last = 0L

    /** نسبة الارتفاع إلى العرض. */
    protected open val aspect: Float get() = 1.0f

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            val now = SystemClock.uptimeMillis()
            var dt = (now - last) / 1000f
            last = now
            if (dt > 0.05f) dt = 0.05f
            update(dt)
            invalidate()
            if (running) postDelayed(this, 16)
        }
    }

    protected abstract fun update(dt: Float)

    fun startLoop() {
        running = true
        last = SystemClock.uptimeMillis()
        removeCallbacks(loop)
        postDelayed(loop, 16)
    }

    fun stopLoop() {
        running = false
        removeCallbacks(loop)
    }

    protected fun paint(c: Int): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = c
        return p
    }

    /** يمنع الـ ScrollView من سرقة اللمس أثناء اللعب. */
    protected fun grab() {
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, (w * aspect).toInt())
    }

    override fun onDetachedFromWindow() {
        stopLoop()
        super.onDetachedFromWindow()
    }
}
