package com.example.tictactoe

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout

/** لغز صورة منطقية 5×5: املأ الخانات لتكوين قلب. */
class NonogramActivity : BaseGameActivity() {
    override val gameTitle: String = "لغز الصورة المخفية"

    private val solution = listOf("01110", "11111", "11111", "01110", "00100")
    private val marks = Array(5) { IntArray(5) }
    private lateinit var board: LinearLayout
    private lateinit var info: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)

        info = label("املأ المربعات لتكوين قلب. اضغط مرة للتلوين ومرة أخرى لوضع علامة X.", 14f)
        content.addView(info, lp(bottom = 8))
        content.addView(label("أدلة الأعمدة: 2 | 5 | 5 | 3 | 2", 14f), lp(bottom = 4))

        board = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(board, lp())
        content.addView(button("مسح اللوحة", false) {
            for (y in 0..4) marks[y].fill(0)
            info.text = "املأ المربعات لتكوين قلب. اضغط مرة للتلوين ومرة أخرى لوضع علامة X."
            drawBoard()
        }, lp(top = 10))
        drawBoard()
    }

    private fun tap(x: Int, y: Int) {
        marks[y][x] = (marks[y][x] + 1) % 3
        drawBoard()
        val solved = (0..4).all { rowIndex ->
            (0..4).all { colIndex ->
                (marks[rowIndex][colIndex] == 1) == (solution[rowIndex][colIndex] == '1')
            }
        }
        if (solved) info.text = "أحسنت! اكتشفت الصورة المخفية ❤️"
    }

    private fun drawBoard() {
        board.removeAllViews()
        val rowClues = listOf("1 3 1", "5", "5", "3", "1")
        for (y in 0..4) {
            val line = row()
            line.addView(label(rowClues[y], 12f), LinearLayout.LayoutParams(dp(48), dp(44)))
            for (x in 0..4) {
                val symbol = when (marks[y][x]) {
                    1 -> "🟦"
                    2 -> "❌"
                    else -> "⬜"
                }
                val cell = button(symbol, false) { tap(x, y) }
                line.addView(cell, LinearLayout.LayoutParams(0, dp(44), 1f))
            }
            board.addView(line, lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }
}
