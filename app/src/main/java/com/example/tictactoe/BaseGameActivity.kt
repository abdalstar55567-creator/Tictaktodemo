package com.example.tictactoe

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * أساس كل الألعاب.
 *
 * أي شاشة لعبة تُمرَّر إلى setContentView (سواء ملف تصميم أو View مبنية بالكود)
 * تُلَفّ تلقائياً بشريط علوي فيه زر "رجوع" وعنوان اللعبة، وعند الضغط على
 * الزر (أو زر الرجوع في الهاتف) تظهر رسالة: هل أنت متأكد أنك تريد المغادرة؟
 *
 * العنوان: إما override لـ gameTitle أو الاستدعاء initScreen("العنوان").
 */
abstract class BaseGameActivity : AppCompatActivity() {

    open val gameTitle: String get() = ""

    /** حاوية المحتوى للألعاب التي تستخدم initScreen. */
    protected lateinit var content: LinearLayout

    private var screenTitle: String? = null
    private var backInstalled = false

    /** للألعاب التي تبني واجهتها بالكود على حاوية جاهزة اسمها content. */
    protected fun initScreen(title: String) {
        screenTitle = title
        val col = Ui.column(this)
        content = col
        setContentView(Ui.scroll(this, col))
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, null))
    }

    override fun setContentView(view: View) {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(color(R.color.bg))

        val bar = layoutInflater.inflate(R.layout.view_topbar, root, false)
        bar.setPadding(dp(16), dp(12), dp(16), dp(4))
        root.addView(bar)
        root.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        super.setContentView(root)

        bar.findViewById<TextView>(R.id.topTitle).text = screenTitle ?: gameTitle
        bar.findViewById<View>(R.id.backButton).setOnClickListener { confirmExit() }
        installBackHandler()
    }

    private fun installBackHandler() {
        if (backInstalled) return
        backInstalled = true
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })
    }

    protected fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("مغادرة اللعبة")
            .setMessage("هل أنت متأكد أنك تريد المغادرة؟")
            .setPositiveButton("نعم، غادر") { _, _ -> finish() }
            .setNegativeButton("لا، ابقَ", null)
            .show()
    }

    // ---------- أدوات مساعدة لبناء الواجهات بالكود ----------

    protected fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()

    protected fun color(res: Int): Int = ContextCompat.getColor(this, res)

    protected fun label(
        text: String,
        sp: Float = 18f,
        colorRes: Int = R.color.text_main,
        bold: Boolean = true
    ): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        tv.setTextColor(color(colorRes))
        tv.gravity = Gravity.CENTER
        if (bold) tv.setTypeface(tv.typeface, Typeface.BOLD)
        return tv
    }

    protected fun button(text: String, primary: Boolean = true, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.setTextColor(color(R.color.text_main))
        b.setBackgroundResource(if (primary) R.drawable.bg_button else R.drawable.bg_button_dim)
        b.setOnClickListener { onClick() }
        return b
    }

    protected fun lp(
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(width, height)
        p.topMargin = top
        p.bottomMargin = bottom
        return p
    }

    protected fun weightLp(height: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(0, height, 1f)
        p.setMargins(dp(4), 0, dp(4), 0)
        return p
    }

    protected fun row(): LinearLayout {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER
        return r
    }
}
