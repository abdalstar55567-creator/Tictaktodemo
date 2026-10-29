package com.example.tictactoe

import android.content.Context
import android.graphics.drawable.GradientDrawable

/**
 * لوحة ألوان مركزية لواجهات الألعاب.
 * تعديل الألوان هنا ينعكس على بطاقات القائمة والعناصر التي تستخدم هذا النظام.
 */
object GameTheme {
    val cardPalettes = arrayOf(
        intArrayOf(0xFF1E3A5F.toInt(), 0xFF1E293B.toInt()),
        intArrayOf(0xFF312E81.toInt(), 0xFF1E293B.toInt()),
        intArrayOf(0xFF164E63.toInt(), 0xFF1E293B.toInt()),
        intArrayOf(0xFF4C1D4F.toInt(), 0xFF1E293B.toInt()),
        intArrayOf(0xFF3F3F46.toInt(), 0xFF1E293B.toInt()),
        intArrayOf(0xFF1F4D3A.toInt(), 0xFF1E293B.toInt())
    )

    fun cardBackground(context: Context, index: Int): GradientDrawable {
        val palette = cardPalettes[index % cardPalettes.size]
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            palette
        ).apply {
            cornerRadius = Ui.dp(context, 18).toFloat()
            setStroke(Ui.dp(context, 1), 0x3348BFFF)
        }
    }

    fun headerBackground(context: Context): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFF172554.toInt(), 0xFF1E293B.toInt(), 0xFF0F172A.toInt())
        ).apply {
            cornerRadius = Ui.dp(context, 24).toFloat()
            setStroke(Ui.dp(context, 1), 0x5538BDF8)
        }
}
