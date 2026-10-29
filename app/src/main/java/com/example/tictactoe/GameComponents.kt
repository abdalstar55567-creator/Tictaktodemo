package com.example.tictactoe

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/** مكونات واجهة موحّدة يمكن إعادة استخدامها في جميع الألعاب. */
object GameComponents {

    fun gameCard(context: Context, game: GameInfo, index: Int): LinearLayout {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GameTheme.cardBackground(context, index)
            isClickable = true
            isFocusable = true
            clipToOutline = true
            elevation = Ui.dp(context, 3).toFloat()
            setPadding(Ui.dp(context, 4), Ui.dp(context, 8), Ui.dp(context, 4), Ui.dp(context, 8))
            contentDescription = game.title
        }

        val emoji = TextView(context).apply {
            text = game.emoji
            textSize = if (game.emoji.length > 3) 27f else 32f
            gravity = Gravity.CENTER
            includeFontPadding = true
            setTextColor(Ui.color(context, R.color.text_main))
        }
        card.addView(emoji, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.15f
        ))

        val title = TextView(context).apply {
            text = game.title
            textSize = 12.5f
            gravity = Gravity.CENTER
            maxLines = 2
            setLineSpacing(0f, 0.95f)
            setTextColor(Ui.color(context, R.color.text_main))
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
        }
        card.addView(title, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(context, 35)
        ))

        card.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            // نترك نظام النقر الافتراضي ينفذ onClick.
            false
        }
        return card
    }
}
