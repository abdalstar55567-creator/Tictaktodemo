package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var buttons: List<Button>
    private lateinit var statusText: TextView
    private val board = Array(9) { "" }
    private var currentPlayer = "X"
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2,
            R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8
        ).map { findViewById<Button>(it) }

        buttons.forEachIndexed { i, button ->
            button.setOnClickListener { onCellClick(i) }
        }

        findViewById<Button>(R.id.restartButton).setOnClickListener { resetGame() }
    }

    private fun onCellClick(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return

        board[index] = currentPlayer
        buttons[index].text = currentPlayer

        val winner = checkWinner()
        if (winner != null) {
            statusText.text = "فاز $winner"
            gameOver = true
            return
        }

        if (board.none { it.isEmpty() }) {
            statusText.text = "تعادل"
            gameOver = true
            return
        }

        currentPlayer = if (currentPlayer == "X") "O" else "X"
        statusText.text = "دور $currentPlayer"
    }

    private fun checkWinner(): String? {
        val lines = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        for (line in lines) {
            val (a, b, c) = line
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }
        return null
    }

    private fun resetGame() {
        for (i in board.indices) {
            board[i] = ""
            buttons[i].text = ""
        }
        currentPlayer = "X"
        gameOver = false
        statusText.text = "دور X"
    }
}
