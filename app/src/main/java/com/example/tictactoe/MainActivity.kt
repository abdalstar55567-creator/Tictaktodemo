package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val cells = ArrayList<TextView>()
    private val board = Array(9) { "" }
    private var current = "X"
    private var gameOver = false
    private var busy = false
    private var vsComputer = true

    private var scoreX = 0
    private var scoreO = 0
    private var draws = 0

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var statusText: TextView
    private lateinit var scoreXText: TextView
    private lateinit var scoreOText: TextView
    private lateinit var scoreDrawText: TextView
    private lateinit var modeButton: Button

    private val lines = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        scoreXText = findViewById(R.id.scoreX)
        scoreOText = findViewById(R.id.scoreO)
        scoreDrawText = findViewById(R.id.scoreDraw)
        modeButton = findViewById(R.id.modeButton)

        buildBoard()

        findViewById<Button>(R.id.restartButton).setOnClickListener { newRound() }

        modeButton.setOnClickListener {
            vsComputer = !vsComputer
            resetScores()
            newRound()
        }

        findViewById<Button>(R.id.resetScoreButton).setOnClickListener {
            resetScores()
            newRound()
        }

        updateScores()
        updateModeButton()
        newRound()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()

    private fun buildBoard() {
        val grid = findViewById<GridLayout>(R.id.board)
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = dp(5)
        val cellSize = minOf((screenWidth - dp(40)) / 3 - margin * 2, dp(120))

        for (i in 0 until 9) {
            val cell = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = cellSize
            params.height = cellSize
            params.setMargins(margin, margin, margin, margin)
            cell.layoutParams = params
            cell.gravity = Gravity.CENTER
            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
            cell.setTypeface(cell.typeface, android.graphics.Typeface.BOLD)
            cell.setBackgroundResource(R.drawable.bg_cell)
            cell.setOnClickListener { onCellClick(i) }
            grid.addView(cell)
            cells.add(cell)
        }
    }

    private fun onCellClick(index: Int) {
        if (gameOver || busy || board[index].isNotEmpty()) return
        play(index)
    }

    private fun play(index: Int) {
        board[index] = current
        val cell = cells[index]
        cell.text = current
        cell.setTextColor(
            ContextCompat.getColor(this, if (current == "X") R.color.x_color else R.color.o_color)
        )

        val win = winningLine()
        if (win != null) {
            gameOver = true
            win.forEach { cells[it].setBackgroundResource(R.drawable.bg_cell_win) }
            if (current == "X") scoreX++ else scoreO++
            updateScores()
            statusText.text = when {
                vsComputer && current == "X" -> "أحسنت! فزت 🎉"
                vsComputer -> "فاز الكمبيوتر 🤖"
                else -> "فاز اللاعب $current 🎉"
            }
            return
        }

        if (board.none { it.isEmpty() }) {
            gameOver = true
            draws++
            updateScores()
            statusText.text = "تعادل 🤝"
            return
        }

        current = if (current == "X") "O" else "X"
        updateStatus()

        if (vsComputer && current == "O") {
            busy = true
            handler.postDelayed({
                busy = false
                if (!gameOver) play(computerMove())
            }, 450)
        }
    }

    private fun winningLine(): IntArray? {
        for (line in lines) {
            val (a, b, c) = line
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return line
            }
        }
        return null
    }

    private fun computerMove(): Int {
        val empty = board.indices.filter { board[it].isEmpty() }

        fun findWinning(player: String): Int? = empty.firstOrNull { i ->
            board[i] = player
            val wins = winningLine() != null
            board[i] = ""
            wins
        }

        return findWinning("O")
            ?: findWinning("X")
            ?: (if (4 in empty) 4 else null)
            ?: listOf(0, 2, 6, 8).filter { it in empty }.randomOrNull()
            ?: empty.random()
    }

    private fun newRound() {
        handler.removeCallbacksAndMessages(null)
        busy = false
        gameOver = false
        current = "X"
        for (i in board.indices) {
            board[i] = ""
            cells[i].text = ""
            cells[i].setBackgroundResource(R.drawable.bg_cell)
        }
        updateStatus()
    }

    private fun resetScores() {
        scoreX = 0
        scoreO = 0
        draws = 0
        updateScores()
        updateModeButton()
    }

    private fun updateStatus() {
        statusText.text = when {
            vsComputer && current == "X" -> "دورك (X)"
            vsComputer -> "الكمبيوتر يفكر..."
            else -> "دور اللاعب $current"
        }
    }

    private fun updateScores() {
        scoreXText.text = (if (vsComputer) "أنت (X)" else "X") + "\n" + scoreX
        scoreOText.text = (if (vsComputer) "الكمبيوتر (O)" else "O") + "\n" + scoreO
        scoreDrawText.text = "تعادل\n$draws"
    }

    private fun updateModeButton() {
        modeButton.text = if (vsComputer) "ضد الكمبيوتر" else "لاعبان"
    }
}
