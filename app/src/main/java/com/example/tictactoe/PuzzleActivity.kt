package com.example.tictactoe

import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

class PuzzleActivity : BaseGameActivity() {

    override val gameTitle: String get() = "لغز الأرقام"

    private val tiles = IntArray(9)
    private val cells = ArrayList<TextView>()
    private var moves = 0
    private var won = false

    private lateinit var infoText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = Ui.column(this)
        col.addView(Ui.text(this, "🧩 رتّب الأرقام من 1 إلى 8", 22f, bold = true, top = 8))

        infoText = Ui.text(this, "", 17f, R.color.text_dim, top = 8)
        col.addView(infoText)

        statusText = Ui.text(this, "", 19f, R.color.x_color, true, top = 8)
        col.addView(statusText)

        val grid = GridLayout(this)
        grid.columnCount = 3
        grid.rowCount = 3
        val margin = Ui.dp(this, 5)
        val size = minOf(
            (resources.displayMetrics.widthPixels - Ui.dp(this, 40)) / 3 - margin * 2,
            Ui.dp(this, 100)
        )
        for (i in 0 until 9) {
            val cell = TextView(this)
            val lp = GridLayout.LayoutParams()
            lp.width = size
            lp.height = size
            lp.setMargins(margin, margin, margin, margin)
            cell.layoutParams = lp
            cell.gravity = Gravity.CENTER
            cell.textSize = 36f
            cell.setTextColor(Ui.color(this, R.color.x_color))
            cell.setOnClickListener { onTileClick(i) }
            grid.addView(cell)
            cells.add(cell)
        }
        val gridLp = LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP)
        gridLp.topMargin = Ui.dp(this, 18)
        grid.layoutParams = gridLp
        col.addView(grid)

        col.addView(Ui.button(this, "لغز جديد", true, 24, 52) { newGame() })

        setContentView(Ui.scroll(this, col))
        newGame()
    }

    private fun isSolved(): Boolean {
        for (i in 0 until 8) if (tiles[i] != i + 1) return false
        return tiles[8] == 0
    }

    private fun neighbors(index: Int): List<Int> {
        val r = index / 3
        val c = index % 3
        val list = ArrayList<Int>()
        if (r > 0) list.add(index - 3)
        if (r < 2) list.add(index + 3)
        if (c > 0) list.add(index - 1)
        if (c < 2) list.add(index + 1)
        return list
    }

    private fun newGame() {
        do {
            for (i in 0 until 8) tiles[i] = i + 1
            tiles[8] = 0
            var blank = 8
            // نخلط بحركات صحيحة فقط، فيكون اللغز قابلاً للحل دائماً
            repeat(150) {
                val next = neighbors(blank).random()
                tiles[blank] = tiles[next]
                tiles[next] = 0
                blank = next
            }
        } while (isSolved())

        moves = 0
        won = false
        statusText.text = "اضغط على رقم بجوار الخانة الفارغة"
        render()
    }

    private fun onTileClick(i: Int) {
        if (won || tiles[i] == 0) return
        val blank = tiles.indexOf(0)
        if (i !in neighbors(blank)) return

        tiles[blank] = tiles[i]
        tiles[i] = 0
        moves++
        Feedback.tap(this)
        render()

        if (isSolved()) {
            won = true
            val record = Scores.saveLow(this, "puzzle_best", moves)
            statusText.text = if (record) "🏆 رقم قياسي! حللته في $moves حركة"
            else "🎉 أحسنت! حللته في $moves حركة"
            Feedback.win(this)
            render()
        }
    }

    private fun render() {
        for (i in 0 until 9) {
            val cell = cells[i]
            if (tiles[i] == 0) {
                cell.text = ""
                cell.setBackgroundColor(0)
            } else {
                cell.text = tiles[i].toString()
                cell.setBackgroundResource(if (won) R.drawable.bg_cell_match else R.drawable.bg_cell)
            }
        }
        val best = Scores.get(this, "puzzle_best")
        infoText.text = "الحركات: $moves" + (if (best > 0) "   •   🏆 الأفضل: $best" else "")
    }
}
