package com.example.tictactoe

import android.content.Context
import android.graphics.drawable.GradientDrawable

/**
 * لوحة ألوان مركزية لواجهات الألعاب.
 * تعديل الألوان هنا ينعكس على بطاقات القائمة والعناصر التي تستخدم هذا النظام.
 */
object GameTheme {
    val cardPalettes = arrayOf(
        intArrayOf(0xFF1688E8.toInt(), 0xFF0756B5.toInt()),
        intArrayOf(0xFFFF8A24.toInt(), 0xFFE85A18.toInt()),
        intArrayOf(0xFF16BFA5.toInt(), 0xFF078A82.toInt()),
        intArrayOf(0xFFB65CE8.toInt(), 0xFF7938C9.toInt()),
        intArrayOf(0xFFFFC928.toInt(), 0xFFE99412.toInt()),
        intArrayOf(0xFFFF648D.toInt(), 0xFFE83F70.toInt())
    )

    fun cardBackground(context: Context, index: Int): GradientDrawable {
        val palette = cardPalettes[index % cardPalettes.size]
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            palette
        ).apply {
            cornerRadius = Ui.dp(context, 18).toFloat()
            setStroke(Ui.dp(context, 1), 0x66FFFFFF)
        }
    }

    fun headerBackground(context: Context): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFF1476DB.toInt(), 0xFF26B9ED.toInt(), 0xFF65DCCB.toInt())
        ).apply {
            cornerRadius = Ui.dp(context, 24).toFloat()
            setStroke(Ui.dp(context, 1), 0x5538BDF8)
        }
}
