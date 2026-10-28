package com.example.tictactoe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

class Game2048Activity : BaseGameActivity() {

    private val grid = IntArray(16)
    private val cells = ArrayList<TextView>()
    private var score = 0
    private var over = false
    private var downX = 0f
    private var downY = 0f

    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView
    private lateinit var statusText: TextView

    private val tileColors = intArrayOf(
        Color.parseColor("#273449"), Color.parseColor("#64748B"), Color.parseColor("#38BDF8"),
        Color.parseColor("#22C55E"), Color.parseColor("#FACC15"), Color.parseColor("#FB923C"),
        Color.parseColor("#FB7185"), Color.parseColor("#E879F9"), Color.parseColor("#A78BFA"),
        Color.parseColor("#34D399"), Color.parseColor("#F472B6"), Color.parseColor("#EAB308")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔢 لعبة 2048")

        scoreText = label("النقاط: 0", 22f)
        content.addView(scoreText, lp(top = dp(4)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))

        statusText = label("اسحب على اللوحة لدمج الأرقام المتشابهة", 14f, R.color.text_dim, false)
        content.addView(statusText, lp(top = dp(6), bottom = dp(10)))

        val board = GridLayout(this)
        board.columnCount = 4
        board.rowCount = 4
        board.setBackgroundResource(R.drawable.bg_panel)
        board.setPadding(dp(6), dp(6), dp(6), dp(6))

        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / 4 - dp(8), dp(76))
        for (i in 0 until 16) {
            val t = TextView(this)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(4), dp(4), dp(4), dp(4))
            t.layoutParams = p
            t.gravity = Gravity.CENTER
            t.setTextColor(Color.WHITE)
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            t.setTypeface(t.typeface, android.graphics.Typeface.BOLD)
            board.addView(t)
            cells.add(t)
        }

        board.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.x
                    downY = e.y
                }
                MotionEvent.ACTION_UP -> {
                    val mx = e.x - downX
                    val my = e.y - downY
                    if (abs(mx) > 40f || abs(my) > 40f) {
                        if (abs(mx) > abs(my)) move(if (mx > 0) 1 else 0)
                        else move(if (my > 0) 3 else 2)
                    }
                }
            }
            true
        }
        content.addView(board, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        content.addView(Ui.directionPad(this) { dx, dy ->
            when {
                dx < 0 -> move(0)
                dx > 0 -> move(1)
                dy < 0 -> move(2)
                else -> move(3)
            }
        }, lp(top = dp(6)))

        content.addView(button("لعبة جديدة") { newGame() }, lp(height = dp(48), top = dp(12)))
        newGame()
    }

    private fun newGame() {
        grid.fill(0)
        score = 0
        over = false
        statusText.text = "اسحب على اللوحة لدمج الأرقام المتشابهة"
        addTile()
        addTile()
        render()
    }

    private fun addTile() {
        val empty = grid.indices.filter { grid[it] == 0 }
        if (empty.isEmpty()) return
        grid[empty.random()] = if (Random.nextInt(10) == 0) 4 else 2
    }

    private fun slide(line: IntArray): IntArray {
        val nz = line.filter { it != 0 }
        val out = ArrayList<Int>()
        var i = 0
        while (i < nz.size) {
            if (i + 1 < nz.size && nz[i] == nz[i + 1]) {
                out.add(nz[i] * 2)
                score += nz[i] * 2
                i += 2
            } else {
                out.add(nz[i])
                i++
            }
        }
        while (out.size < 4) out.add(0)
        return out.toIntArray()
    }

    /** dir: 0 يسار، 1 يمين، 2 أعلى، 3 أسفل */
    private fun move(dir: Int) {
        if (over) return
        var changed = false
        for (k in 0 until 4) {
            val idx = IntArray(4) { j ->
                when (dir) {
                    0 -> k * 4 + j
                    1 -> k * 4 + (3 - j)
                    2 -> j * 4 + k
                    else -> (3 - j) * 4 + k
                }
            }
            val line = IntArray(4) { grid[idx[it]] }
            val result = slide(line)
            for (j in 0 until 4) {
                if (grid[idx[j]] != result[j]) changed = true
                grid[idx[j]] = result[j]
            }
        }
        if (!changed) return

        Feedback.tap(this)
        addTile()
        render()

        if (grid.any { it >= 2048 }) {
            statusText.text = "🎉 وصلت إلى 2048! يمكنك المتابعة"
            Feedback.win(this)
        }
        if (!canMove()) {
            over = true
            statusText.text = "انتهت اللعبة! لا توجد حركات"
            Feedback.lose(this)
            val best = Scores.get(this, "g2048_best")
            if (score > best) {
                Scores.put(this, "g2048_best", score)
                statusText.text = "انتهت! رقم قياسي جديد 🏆 $score"
            }
            render()
        }
    }

    private fun canMove(): Boolean {
        if (grid.any { it == 0 }) return true
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val v = grid[r * 4 + c]
                if (c < 3 && grid[r * 4 + c + 1] == v) return true
                if (r < 3 && grid[(r + 1) * 4 + c] == v) return true
            }
        }
        return false
    }

    private fun render() {
        for (i in 0 until 16) {
            val v = grid[i]
            val t = cells[i]
            t.text = if (v == 0) "" else v.toString()
            var level = 0
            var x = v
            while (x > 1) {
                x /= 2
                level++
            }
            val d = GradientDrawable()
            d.cornerRadius = dp(10).toFloat()
            d.setColor(tileColors[minOf(level, tileColors.size - 1)])
            t.background = d
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (v >= 1024) 16f else if (v >= 128) 19f else 22f)
        }
        scoreText.text = "النقاط: $score"
        val best = Scores.get(this, "g2048_best")
        bestText.text = if (best == 0) "" else "أفضل نتيجة: $best"
    }
}
