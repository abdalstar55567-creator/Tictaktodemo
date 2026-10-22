package com.example.tictactoe

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

class Connect4Activity : BaseGameActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val rows = 6
    private val cols = 7
    private val board = Array(rows) { IntArray(cols) }
    private val cells = ArrayList<View>()
    private var over = false
    private var busy = false
    private var wins = 0
    private var losses = 0

    private lateinit var status: TextView
    private lateinit var scoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔵 أربعة في صف")

        scoreText = label("", 16f, R.color.gold)
        content.addView(scoreText, lp(top = dp(4)))

        status = label("", 20f)
        content.addView(status, lp(top = dp(10), bottom = dp(12)))

        val grid = GridLayout(this)
        grid.columnCount = cols
        grid.rowCount = rows
        grid.setBackgroundResource(R.drawable.bg_panel)
        grid.setPadding(dp(6), dp(6), dp(6), dp(6))

        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / cols - dp(6), dp(48))
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = View(this)
                val p = GridLayout.LayoutParams()
                p.width = size
                p.height = size
                p.setMargins(dp(3), dp(3), dp(3), dp(3))
                cell.layoutParams = p
                cell.background = circle(0xFF273449.toInt())
                cell.setOnClickListener { onColumn(c) }
                grid.addView(cell)
                cells.add(cell)
            }
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        content.addView(
            label("اضغط على أي عمود لإسقاط قطعتك 🔵", 14f, R.color.text_dim, false),
            lp(top = dp(10))
        )
        content.addView(button("لعبة جديدة") { newGame() }, lp(height = dp(50), top = dp(14)))

        newGame()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun circle(c: Int): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        d.setColor(c)
        return d
    }

    private fun newGame() {
        handler.removeCallbacksAndMessages(null)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                board[r][c] = 0
                cells[r * cols + c].background = circle(0xFF273449.toInt())
            }
        }
        over = false
        busy = false
        status.text = "دورك 🔵"
        scoreText.text = "فوز: $wins   |   خسارة: $losses"
    }

    private fun place(c: Int, who: Int): Int {
        for (r in rows - 1 downTo 0) {
            if (board[r][c] == 0) {
                board[r][c] = who
                return r
            }
        }
        return -1
    }

    private fun drop(c: Int, who: Int): Boolean {
        val r = place(c, who)
        if (r < 0) return false
        val colorInt = if (who == 1) 0xFF38BDF8.toInt() else 0xFFFB7185.toInt()
        cells[r * cols + c].background = circle(colorInt)
        return true
    }

    private fun wouldWin(c: Int, who: Int): Boolean {
        val r = place(c, who)
        if (r < 0) return false
        val w = hasWin(who)
        board[r][c] = 0
        return w
    }

    private fun hasWin(who: Int): Boolean {
        val dirs = arrayOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, -1))
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c] != who) continue
                for (d in dirs) {
                    var k = 1
                    while (k < 4) {
                        val nr = r + d[0] * k
                        val nc = c + d[1] * k
                        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || board[nr][nc] != who) break
                        k++
                    }
                    if (k == 4) return true
                }
            }
        }
        return false
    }

    private fun full(): Boolean = (0 until cols).all { board[0][it] != 0 }

    private fun onColumn(c: Int) {
        if (over || busy) return
        if (!drop(c, 1)) return
        Feedback.tap(this)

        if (hasWin(1)) {
            endGame("🎉 فزت!", true)
            return
        }
        if (full()) {
            over = true
            status.text = "تعادل 🤝"
            Feedback.draw(this)
            return
        }

        status.text = "الكمبيوتر يفكر... 🔴"
        busy = true
        handler.postDelayed({
            busy = false
            cpuMove()
        }, 500)
    }

    private fun cpuMove() {
        if (over) return
        val valid = (0 until cols).filter { board[0][it] == 0 }
        val pick = valid.firstOrNull { wouldWin(it, 2) }
            ?: valid.firstOrNull { wouldWin(it, 1) }
            ?: valid.sortedBy { abs(it - 3) * 10 + Random.nextInt(10) }.first()
        drop(pick, 2)

        if (hasWin(2)) {
            endGame("فاز الكمبيوتر 🤖", false)
            return
        }
        if (full()) {
            over = true
            status.text = "تعادل 🤝"
            Feedback.draw(this)
            return
        }
        status.text = "دورك 🔵"
    }

    private fun endGame(message: String, playerWon: Boolean) {
        over = true
        status.text = message
        if (playerWon) {
            wins++
            Feedback.win(this)
        } else {
            losses++
            Feedback.lose(this)
        }
        scoreText.text = "فوز: $wins   |   خسارة: $losses"
    }
}
