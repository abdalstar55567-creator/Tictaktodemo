package com.example.tictactoe

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseGameActivity : AppCompatActivity() {

    open val gameTitle: String get() = ""

    protected lateinit var content: LinearLayout

    private var screenTitle: String? = null
    private var backInstalled = false

    override fun onResume() {
        super.onResume()
        GameMusic.playFor(this, javaClass.simpleName)
    }

    override fun onStop() {
        super.onStop()
        GameMusic.stopWhenBackgrounded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // تسجيل فتح اللعبة مرة واحدة عند بدء النشاط
        if (savedInstanceState == null) {
            GameUsageManager.recordGameOpened(this, javaClass.name)
        }
    }

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
        root.background = GameBackgroundDrawable(this, javaClass.simpleName)

        val bar = layoutInflater.inflate(R.layout.view_topbar, root, false)
        bar.setPadding(dp(16), dp(12), dp(16), dp(4))
        root.addView(bar)

        // Shared glossy 3D frame around the game body. The top bar stays outside;
        // the play surface and its in-game controls keep their original layout.
        val boardShell = FrameLayout(this).apply {
            background = ContextCompat.getDrawable(this@BaseGameActivity, R.drawable.bg_game_board_frame)
            elevation = dp(8).toFloat()
            clipToOutline = true
            setPadding(dp(5), dp(5), dp(5), dp(8))
        }
        boardShell.addView(view, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        val boardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply {
            setMargins(dp(9), dp(4), dp(9), dp(10))
        }
        root.addView(boardShell, boardParams)

        super.setContentView(root)

        bar.findViewById<TextView>(R.id.topTitle).text =
            screenTitle ?: gameTitle

        bar.findViewById<View>(R.id.backButton)
            .setOnClickListener { finish() }

        val favoriteButton = bar.findViewById<Button>(R.id.favoriteButton)
        fun refreshFavorite() {
            val selected = FavoritesManager.contains(this, javaClass.name)
            favoriteButton.text = if (selected) "♥" else "♡"
            favoriteButton.setTextColor(color(if (selected) R.color.x_color else R.color.text_main))
            favoriteButton.contentDescription = if (selected) "إزالة من المفضلة" else "إضافة إلى المفضلة"
        }
        refreshFavorite()
        favoriteButton.setOnClickListener {
            FavoritesManager.toggle(this, javaClass.name)
            refreshFavorite()
            Feedback.tap(this)
        }

        installBackHandler()
    }

    private fun installBackHandler() {
        if (backInstalled) return

        backInstalled = true

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
    }

    // أدوات مساعدة لبناء الواجهات

    protected fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    protected fun color(res: Int): Int =
        ContextCompat.getColor(this, res)

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

        if (bold) {
            tv.setTypeface(tv.typeface, Typeface.BOLD)
        }

        return tv
    }

    protected fun button(
        text: String,
        primary: Boolean = true,
        onClick: () -> Unit
    ): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.setTextColor(android.graphics.Color.WHITE)
        b.setShadowLayer(2.5f, 0f, dp(2).toFloat(), 0x99004B86.toInt())
        b.setBackgroundResource(if (primary) R.drawable.bg_button else R.drawable.bg_button_dim)
        b.elevation = dp(5).toFloat()
        b.stateListAnimator = null
        b.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> view.animate().scaleX(.97f).scaleY(.97f).translationY(dp(2).toFloat()).setDuration(65).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(100).start()
            }
            false
        }
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

    protected fun row(topMarginDp: Int): LinearLayout {
        val r = row()

        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        p.topMargin = dp(topMarginDp)
        r.layoutParams = p

        return r
    }
}
