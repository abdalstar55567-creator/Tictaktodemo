package com.example.tictactoe

import android.content.Context
import android.graphics.drawable.GradientDrawable

/** Central glossy 3D-inspired palette shared by the home screen and game cards. */
object GameTheme {
    val cardPalettes = arrayOf(
        intArrayOf(0xFF35D9FF.toInt(), 0xFF0875E8.toInt(), 0xFF0649B5.toInt()),
        intArrayOf(0xFFFFE34F.toInt(), 0xFFFF9D16.toInt(), 0xFFE85A0A.toInt()),
        intArrayOf(0xFF9CFF36.toInt(), 0xFF45D900.toInt(), 0xFF159B13.toInt()),
        intArrayOf(0xFFE78BFF.toInt(), 0xFFAD43F2.toInt(), 0xFF7020C8.toInt()),
        intArrayOf(0xFFFF9BCB.toInt(), 0xFFFF579B.toInt(), 0xFFD92870.toInt()),
        intArrayOf(0xFF5DF5DE.toInt(), 0xFF10BFAF.toInt(), 0xFF07877D.toInt())
    )

    fun cardBackground(context: Context, index: Int): GradientDrawable {
        val palette = cardPalettes[index % cardPalettes.size]
        return GradientDrawable(GradientDrawable.Orientation.TL_BR, palette).apply {
            cornerRadius = Ui.dp(context, 24).toFloat()
            setStroke(Ui.dp(context, 2), 0xCCFFFFFF.toInt())
        }
    }

    fun headerBackground(context: Context): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFF078BFF.toInt(), 0xFF18C9F0.toInt(), 0xFF71E8D1.toInt())
        ).apply {
            cornerRadius = Ui.dp(context, 30).toFloat()
            setStroke(Ui.dp(context, 2), 0xEEFFFFFF.toInt())
        }
}
