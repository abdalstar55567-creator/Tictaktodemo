package com.example.tictactoe

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Glossy, playful game tiles shared by every category on the home screen. */
object GameComponents {

    fun gameCard(context: Context, game: GameInfo, index: Int): FrameLayout {
        val shell = FrameLayout(context).apply {
            isClickable = true
            isFocusable = true
            clipToOutline = true
            elevation = Ui.dp(context, 8).toFloat()
            background = GameTheme.cardBackground(context, index)
            contentDescription = game.title
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(Ui.dp(context, 4), Ui.dp(context, 8), Ui.dp(context, 4), Ui.dp(context, 7))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x18FFFFFF, 0x00FFFFFF, 0x22000000)
            ).apply { cornerRadius = Ui.dp(context, 23).toFloat() }
        }
        val emoji = TextView(context).apply {
            text = game.emoji
            textSize = if (game.emoji.length > 3) 29f else 36f
            gravity = Gravity.CENTER
            includeFontPadding = true
            setShadowLayer(5f, 0f, Ui.dp(context, 3).toFloat(), 0x66000000)
        }
        content.addView(emoji, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.15f
        ))

        val title = TextView(context).apply {
            text = game.title
            textSize = 13f
            gravity = Gravity.CENTER
            maxLines = 2
            setLineSpacing(0f, 0.96f)
            setTextColor(0xFFFFF1C2.toInt())
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
            setShadowLayer(3f, 0f, Ui.dp(context, 2).toFloat(), 0x99000000.toInt())
        }
        content.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(context, 36)
        ))
        shell.addView(content, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        ))

        // Small glossy streak at the upper edge, like a molded toy-game button.
        val shine = View(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0xAAFFFFFF.toInt(), 0x44FFFFFF, 0x00FFFFFF)
            ).apply { cornerRadius = Ui.dp(context, 30).toFloat() }
            alpha = 0.9f
        }
        val shineParams = FrameLayout.LayoutParams(
            Ui.dp(context, 48), Ui.dp(context, 5), Gravity.TOP or Gravity.START
        ).apply { topMargin = Ui.dp(context, 8); marginStart = Ui.dp(context, 12) }
        shell.addView(shine, shineParams)

        shell.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.96f).scaleY(0.96f).translationY(Ui.dp(context, 2).toFloat()).setDuration(75).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(110).start()
            }
            false
        }
        return shell
    }
}
