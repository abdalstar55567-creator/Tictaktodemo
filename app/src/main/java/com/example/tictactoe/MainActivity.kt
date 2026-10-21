package com.example.tictactoe

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : BaseGameActivity() {

    override val gameTitle: String get() = "إكس أو"

    private val cells = ArrayList<TextView>()
    private val board = Array(9) { "" }
    private var current = "X"
    private var gameOver = false
    private var busy = false
    private var vsComputer = true

    // 0 = سهل، 1 = متوسط، 2 = صعب
    private var difficulty = 1
    private val levelNames = arrayOf("سهل", "متوسط", "صعب")

    private var scoreX = 0
    private var scoreO = 0
    private var draws = 0

    private val handler = Handler(Looper.getMainLooper())
    private val winAnimators = ArrayList<ObjectAnimator>()

    private lateinit var statusText: TextView
    private lateinit var scoreXText: TextView
    private lateinit var scoreOText: TextView
    private lateinit var scoreDrawText: TextView
    private lateinit var modeButton: Button
    private lateinit var levelButton: Button
    private lateinit var soundButton: Button

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
        levelButton = findViewById(R.id.difficultyButton)
        soundButton = findViewById(R.id.soundButton)

        buildBoard()

        findViewById<Button>(R.id.restartButton).setOnClickListener { newRound() }

        modeButton.setOnClickListener {
            vsComputer = !vsComputer
            resetScores()
            newRound()
        }

        levelButton.setOnClickListener {
            if (!vsComputer) return@setOnClickListener
            difficulty = (difficulty + 1) % 3
            resetScores()
            newRound()
        }

        soundButton.setOnClickListener {
            Feedback.setEnabled(this, !Feedback.enabled(this))
            Feedback.tap(this)
            updateButtons()
        }

        findViewById<Button>(R.id.resetScoreButton).setOnClickListener {
            resetScores()
            newRound()
        }

        updateScores()
        updateButtons()
        newRound()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        winAnimators.forEach { it.cancel() }
        super.onDestroy()
    }

    private fun buildBoard() {
        val grid = findViewById<GridLayout>(R.id.board)
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = dp(5)
        val cellSize = minOf((screenWidth - dp(40)) / 3 - margin * 2, dp(110))

        for (i in 0 until 9) {
            val cell = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = cellSize
            params.height = cellSize
            params.setMargins(margin, margin, margin, margin)
            cell.layoutParams = params
            cell.gravity = Gravity.CENTER
            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
            cell.setTypeface(cell.typeface, Typeface.BOLD)
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

        // حركة ظهور الرمز
        cell.scaleX = 0.2f
        cell.scaleY = 0.2f
        cell.animate().scaleX(1f).scaleY(1f).setDuration(240)
            .setInterpolator(OvershootInterpolator()).start()

        val win = lineOf()
        if (win != null) {
            gameOver = true
            win.forEach {
                cells[it].setBackgroundResource(R.drawable.bg_cell_win)
                pulse(cells[it])
            }
            if (current == "X") scoreX++ else scoreO++
            updateScores()

            val computerWon = vsComputer && current == "O"
            if (computerWon) Feedback.lose(this) else Feedback.win(this)
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
            Feedback.draw(this)
            statusText.text = "تعادل 🤝"
            return
        }

        Feedback.tap(this)
        current = if (current == "X") "O" else "X"
        updateStatus()

        if (vsComputer && current == "O") {
            busy = true
            handler.postDelayed({
                busy = false
                if (!gameOver) play(computerMove())
            }, 500)
        }
    }

    private fun pulse(view: View) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.12f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.12f)
        )
        animator.duration = 350
        animator.repeatCount = 5
        animator.repeatMode = ValueAnimator.REVERSE
        animator.start()
        winAnimators.add(animator)
    }

    /** يرجع الخط الفائز إن وُجد. */
    private fun lineOf(): IntArray? {
        for (line in lines) {
            val a = line[0]
            val b = line[1]
            val c = line[2]
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return line
            }
        }
        return null
    }

    private fun computerMove(): Int {
        val empty = board.indices.filter { board[it].isEmpty() }
        return when (difficulty) {
            0 -> empty.random()
            1 -> findWinning("O", empty) ?: findWinning("X", empty) ?: empty.random()
            else -> bestMove(empty)
        }
    }

    private fun findWinning(player: String, empty: List<Int>): Int? {
        for (i in empty) {
            board[i] = player
            val wins = lineOf() != null
            board[i] = ""
            if (wins) return i
        }
        return null
    }

    /** المستوى الصعب: خوارزمية Minimax لا تُهزم. */
    private fun bestMove(empty: List<Int>): Int {
        var bestScore = -1000
        val best = ArrayList<Int>()
        for (i in empty) {
            board[i] = "O"
            val score = minimax(false, 1)
            board[i] = ""
            if (score > bestScore) {
                bestScore = score
                best.clear()
                best.add(i)
            } else if (score == bestScore) {
                best.add(i)
            }
        }
        return best.random()
    }

    private fun minimax(isO: Boolean, depth: Int): Int {
        val line = lineOf()
        if (line != null) {
            return if (board[line[0]] == "O") 10 - depth else depth - 10
        }
        if (board.none { it.isEmpty() }) return 0

        var best = if (isO) -1000 else 1000
        for (i in board.indices) {
            if (board[i].isEmpty()) {
                board[i] = if (isO) "O" else "X"
                val score = minimax(!isO, depth + 1)
                board[i] = ""
                best = if (isO) maxOf(best, score) else minOf(best, score)
            }
        }
        return best
    }

    private fun newRound() {
        handler.removeCallbacksAndMessages(null)
        winAnimators.forEach { it.cancel() }
        winAnimators.clear()
        busy = false
        gameOver = false
        current = "X"
        for (i in board.indices) {
            board[i] = ""
            val cell = cells[i]
            cell.animate().cancel()
            cell.scaleX = 1f
            cell.scaleY = 1f
            cell.text = ""
            cell.setBackgroundResource(R.drawable.bg_cell)
        }
        updateStatus()
    }

    private fun resetScores() {
        scoreX = 0
        scoreO = 0
        draws = 0
        updateScores()
        updateButtons()
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

    private fun updateButtons() {
        modeButton.text = if (vsComputer) "ضد الكمبيوتر" else "لاعبان"
        levelButton.text = "المستوى: " + levelNames[difficulty]
        levelButton.alpha = if (vsComputer) 1f else 0.4f
        soundButton.text = if (Feedback.enabled(this)) "🔊 الصوت: تشغيل" else "🔇 الصوت: إيقاف"
        updateScores()
    }
}
