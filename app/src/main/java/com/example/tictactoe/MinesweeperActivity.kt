package com.example.tictactoe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import kotlin.random.Random

class MinesweeperActivity : BaseGameActivity() {

    private val n = 8
    private val mineCount = 10
    private val mine = BooleanArray(n * n)
    private val revealed = BooleanArray(n * n)
    private val flagged = BooleanArray(n * n)
    private val adj = IntArray(n * n)
    private val cells = ArrayList<TextView>()

    private var started = false
    private var over = false
    private var flagMode = false

    private lateinit var statusText: TextView
    private lateinit var modeButton: Button

    private val numberColors = intArrayOf(
        Color.TRANSPARENT, Color.parseColor("#38BDF8"), Color.parseColor("#22C55E"),
        Color.parseColor("#FB7185"), Color.parseColor("#A78BFA"), Color.parseColor("#FB923C"),
        Color.parseColor("#2DD4BF"), Color.WHITE, Color.GRAY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("💣 كاسحة الألغام")

        content.addView(
            label("اكشف كل الخلايا الآمنة. الرقم = عدد الألغام المجاورة. اضغط مطولاً لوضع علم 🚩", 15f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        statusText = label("", 20f)
        content.addView(statusText, lp(top = dp(10), bottom = dp(10)))

        val grid = GridLayout(this)
        grid.columnCount = n
        grid.rowCount = n
        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / n - dp(4), dp(42))
        for (i in 0 until n * n) {
            val t = TextView(this)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(2), dp(2), dp(2), dp(2))
            t.layoutParams = p
            t.gravity = Gravity.CENTER
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            t.setTypeface(t.typeface, android.graphics.Typeface.BOLD)
            t.setOnClickListener { onTap(i) }
            t.setOnLongClickListener {
                toggleFlag(i)
                true
            }
            grid.addView(t)
            cells.add(t)
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        modeButton = button("الوضع: كشف ⛏️", false) {
            flagMode = !flagMode
            updateModeButton()
        }
        content.addView(modeButton, lp(height = dp(46), top = dp(14)))

        content.addView(button("لعبة جديدة") { newGame() }, lp(height = dp(50), top = dp(10)))
        newGame()
    }

    private fun updateModeButton() {
        modeButton.text = if (flagMode) "الوضع: علم 🚩" else "الوضع: كشف ⛏️"
    }

    private fun neighbors(i: Int): List<Int> {
        val r = i / n
        val c = i % n
        val out = ArrayList<Int>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until n && nc in 0 until n) out.add(nr * n + nc)
            }
        }
        return out
    }

    private fun newGame() {
        mine.fill(false)
        revealed.fill(false)
        flagged.fill(false)
        adj.fill(0)
        started = false
        over = false
        flagMode = false
        updateModeButton()
        render()
    }

    private fun placeMines(avoid: Int) {
        var placed = 0
        while (placed < mineCount) {
            val i = Random.nextInt(n * n)
            if (i != avoid && !mine[i]) {
                mine[i] = true
                placed++
            }
        }
        for (i in 0 until n * n) {
            adj[i] = neighbors(i).count { mine[it] }
        }
    }

    private fun onTap(i: Int) {
        if (over) return
        if (flagMode) toggleFlag(i) else reveal(i)
    }

    private fun toggleFlag(i: Int) {
        if (over || revealed[i]) return
        flagged[i] = !flagged[i]
        Feedback.tap(this)
        render()
    }

    private fun reveal(start: Int) {
        if (revealed[start] || flagged[start]) return
        if (!started) {
            placeMines(start)
            started = true
        }

        if (mine[start]) {
            over = true
            for (i in 0 until n * n) if (mine[i]) revealed[i] = true
            render()
            statusText.text = "💥 انفجر لغم! خسرت"
            Feedback.lose(this)
            return
        }

        val stack = ArrayList<Int>()
        stack.add(start)
        while (stack.isNotEmpty()) {
            val i = stack.removeAt(stack.size - 1)
            if (revealed[i] || flagged[i]) continue
            revealed[i] = true
            if (adj[i] == 0) {
                for (nb in neighbors(i)) {
                    if (!revealed[nb] && !mine[nb]) stack.add(nb)
                }
            }
        }
        Feedback.tap(this)
        render()

        val safeRevealed = (0 until n * n).count { revealed[it] && !mine[it] }
        if (safeRevealed == n * n - mineCount) {
            over = true
            statusText.text = "🎉 أحسنت! كشفت كل الخلايا الآمنة"
            Feedback.win(this)
            for (i in 0 until n * n) if (mine[i]) flagged[i] = true
            render()
        }
    }

    private fun render() {
        for (i in 0 until n * n) {
            val t = cells[i]
            val d = GradientDrawable()
            d.cornerRadius = dp(6).toFloat()
            if (revealed[i]) {
                d.setColor(Color.parseColor(if (mine[i]) "#7F1D1D" else "#1E293B"))
                t.text = if (mine[i]) "💣" else if (adj[i] > 0) adj[i].toString() else ""
                t.setTextColor(numberColors[adj[i].coerceIn(0, 8)])
            } else {
                d.setColor(Color.parseColor("#475569"))
                t.text = if (flagged[i]) "🚩" else ""
            }
            t.background = d
        }
        if (!over) {
            val flags = flagged.count { it }
            statusText.text = "الألغام المتبقية: ${mineCount - flags}"
        }
    }
}
