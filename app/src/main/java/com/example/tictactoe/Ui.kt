package com.example.tictactoe

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat

/** أدوات مساعدة لبناء واجهات الألعاب البسيطة بالكود. */
object Ui {

    const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    fun dp(ctx: Context, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), ctx.resources.displayMetrics
        ).toInt()

    fun color(ctx: Context, res: Int): Int = ContextCompat.getColor(ctx, res)

    fun panelBg(ctx: Context, colorInt: Int, radiusDp: Int = 18): GradientDrawable {
        val d = GradientDrawable()
        d.cornerRadius = dp(ctx, radiusDp).toFloat()
        d.setColor(colorInt)
        return d
    }

    fun text(
        ctx: Context,
        s: String,
        sp: Float = 18f,
        colorRes: Int = R.color.text_main,
        bold: Boolean = false,
        top: Int = 0
    ): TextView {
        val t = TextView(ctx)
        t.text = s
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        t.setTextColor(color(ctx, colorRes))
        t.gravity = Gravity.CENTER
        if (bold) t.setTypeface(t.typeface, Typeface.BOLD)
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.topMargin = dp(ctx, top)
        t.layoutParams = lp
        return t
    }

    fun button(
        ctx: Context,
        s: String,
        primary: Boolean = true,
        top: Int = 12,
        height: Int = 52,
        onClick: () -> Unit
    ): Button {
        val b = Button(ctx)
        b.text = s
        b.isAllCaps = false
        b.setTextColor(color(ctx, R.color.text_main))
        b.setBackgroundResource(if (primary) R.drawable.bg_button else R.drawable.bg_button_dim)
        val lp = LinearLayout.LayoutParams(MATCH, dp(ctx, height))
        lp.topMargin = dp(ctx, top)
        b.layoutParams = lp
        b.setOnClickListener { onClick() }
        return b
    }

    /** زر يأخذ حصة متساوية داخل صف أفقي. */
    fun rowButton(ctx: Context, s: String, primary: Boolean = true, onClick: () -> Unit): Button {
        val b = Button(ctx)
        b.text = s
        b.isAllCaps = false
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        b.setTextColor(color(ctx, R.color.text_main))
        b.setBackgroundResource(if (primary) R.drawable.bg_button else R.drawable.bg_button_dim)
        val lp = LinearLayout.LayoutParams(0, dp(ctx, 68), 1f)
        lp.setMargins(dp(ctx, 4), 0, dp(ctx, 4), 0)
        b.layoutParams = lp
        b.setOnClickListener { onClick() }
        return b
    }

    fun row(ctx: Context, top: Int = 16): LinearLayout {
        val r = LinearLayout(ctx)
        r.orientation = LinearLayout.HORIZONTAL
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.topMargin = dp(ctx, top)
        r.layoutParams = lp
        return r
    }

    fun column(ctx: Context): LinearLayout {
        val c = LinearLayout(ctx)
        c.orientation = LinearLayout.VERTICAL
        c.gravity = Gravity.CENTER_HORIZONTAL
        val p = dp(ctx, 20)
        c.setPadding(p, p, p, p)
        return c
    }

    /** لوحة اتجاهات موحدة لكل الألعاب التي تحتاج حركة في أربعة اتجاهات. */
    fun directionPad(ctx: Context, onDirection: (dx: Int, dy: Int) -> Unit): LinearLayout {
        val pad = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(ctx, 4), dp(ctx, 2), dp(ctx, 4), dp(ctx, 2))
        }
        fun arrow(symbol: String, dx: Int, dy: Int): Button = Button(ctx).apply {
            text = symbol
            isAllCaps = false
            textSize = 22f
            setTextColor(color(ctx, R.color.text_main))
            background = panelBg(ctx, android.graphics.Color.rgb(39, 52, 73), 14)
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 62), dp(ctx, 48)).apply {
                setMargins(dp(ctx, 4), dp(ctx, 3), dp(ctx, 4), dp(ctx, 3))
            }
            setOnClickListener { onDirection(dx, dy) }
        }
        pad.addView(arrow("⬆", 0, -1))
        val middle = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        middle.addView(arrow("⬅", -1, 0))
        middle.addView(View(ctx), LinearLayout.LayoutParams(dp(ctx, 48), dp(ctx, 48)))
        middle.addView(arrow("➡", 1, 0))
        pad.addView(middle)
        pad.addView(arrow("⬇", 0, 1))
        return pad
    }

    fun scroll(ctx: Context, inner: View): ScrollView {
        val s = ScrollView(ctx)
        s.isFillViewport = true
        s.addView(inner)
        return s
    }
}
