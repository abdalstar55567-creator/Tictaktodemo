package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.min

/** Lightweight, asset-free category background shared by simple game screens. */
class GameBackgroundDrawable(context: Context, activityName: String) : Drawable() {
    private val density = context.resources.displayMetrics.density
    private val palette = paletteFor(activityName)
    private val gradient = android.graphics.LinearGradient(
        0f, 0f, 0f, 1600f,
        palette.first, palette.second,
        android.graphics.Shader.TileMode.CLAMP
    )
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 24 }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 35
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-rounded", android.graphics.Typeface.BOLD)
    }
    private val icons = iconsFor(activityName)

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        canvas.drawRect(b, fill)
        val w = b.width().toFloat(); val h = b.height().toFloat()
        canvas.drawCircle(w * .92f, h * .12f, min(w, h) * .25f, glow)
        canvas.drawCircle(w * .05f, h * .82f, min(w, h) * .22f, glow)
        iconPaint.textSize = 42f * density
        val positions = arrayOf(
            Triple(w * .13f, h * .18f, 0f), Triple(w * .86f, h * .32f, 0f),
            Triple(w * .18f, h * .67f, 0f), Triple(w * .84f, h * .82f, 0f)
        )
        positions.forEachIndexed { i, pos ->
            canvas.save()
            canvas.rotate(if (i % 2 == 0) -13f else 12f, pos.first, pos.second)
            canvas.drawText(icons[i % icons.size], pos.first, pos.second, iconPaint)
            canvas.restore()
        }
        // Soft lower highlight adds the glossy toy-like atmosphere without covering gameplay.
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(0f, 0f, w, h,
                intArrayOf(0x18FFFFFF, 0x00FFFFFF, 0x12FFFFFF), null,
                android.graphics.Shader.TileMode.CLAMP)
        }
        canvas.drawRect(b, highlight)
    }

    override fun setAlpha(alpha: Int) { fill.alpha = alpha }
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) { fill.colorFilter = colorFilter }
    override fun getOpacity(): Int = android.graphics.PixelFormat.OPAQUE

    companion object {
        private fun paletteFor(name: String): Pair<Int, Int> {
            val n = name.lowercase()
            return when {
                listOf("math", "number", "sudoku", "puzzle", "memory", "mastermind", "nonogram", "lightsout", "hanoi", "scramble").any { it in n } -> 0xFF32C8F4.toInt() to 0xFF73E5D0.toInt()
                listOf("snake", "flappy", "bird", "frog", "hurdle", "sprint", "falling", "motion", "brick", "catapult").any { it in n } -> 0xFF26B8E8.toInt() to 0xFF67D8B0.toInt()
                listOf("trivia", "quiz", "higherlower", "truefalse", "capital", "animal", "color").any { it in n } -> 0xFF42B8F5.toInt() to 0xFF9ADCFB.toInt()
                listOf("dice", "coin", "rps", "luck", "odd", "reaction", "tap", "stopbar", "simon").any { it in n } -> 0xFF37C7E8.toInt() to 0xFF8CE8D2.toInt()
                else -> 0xFF279FEA.toInt() to 0xFF72DCCB.toInt()
            }
        }
        private fun iconsFor(name: String): List<String> {
            val n = name.lowercase()
            return when {
                listOf("math", "number", "sudoku", "puzzle", "memory", "mastermind", "nonogram", "lightsout", "hanoi", "scramble").any { it in n } -> listOf("🔢", "🧩", "⭐", "💡")
                listOf("snake", "flappy", "bird", "frog", "hurdle", "sprint", "falling", "motion", "brick", "catapult").any { it in n } -> listOf("🎮", "🚀", "🏃", "⭐")
                listOf("trivia", "quiz", "higherlower", "truefalse", "capital", "animal", "color").any { it in n } -> listOf("❓", "🌈", "🧠", "⭐")
                listOf("dice", "coin", "rps", "luck", "odd", "reaction", "tap", "stopbar", "simon").any { it in n } -> listOf("🎲", "✨", "🎯", "🏆")
                else -> listOf("🎮", "✨", "🧩", "⭐")
            }
        }
    }
}
