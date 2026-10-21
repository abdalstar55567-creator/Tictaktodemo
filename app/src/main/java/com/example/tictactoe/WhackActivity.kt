package com.example.tictactoe

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

class WhackActivity : BaseGameActivity() {

    override val gameTitle: String get() = "اضرب الخلد"

    private val cells = ArrayList<TextView>()
    private var score = 0
    private var running = false
    private var mole = -1
    private var timer: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var timeText: TextView
    private lateinit var scoreText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button

    private val moleRunnable = object : Runnable {
        override fun run() {
            moveMole()
            handler.postDelayed(this, 750)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = Ui.column(this)

        val row = Ui.row(this, 4)
        timeText = Ui.text(this, "⏱ 30", 20f, R.color.gold, true)
        scoreText = Ui.text(this, "النقاط: 0", 20f, R.color.x_color, true)
        timeText.layoutParams = LinearLayout.LayoutParams(0, Ui.WRAP, 1f)
        scoreText.layoutParams = LinearLayout.LayoutParams(0, Ui.WRAP, 1f)
        row.addView(timeText)
        row.addView(scoreText)
        col.addView(row)

        statusText = Ui.text(this, "اضغط على الخلد 🐹 بأسرع ما يمكن!", 16f, R.color.text_dim, top = 12)
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
            cell.textSize = 40f
            cell.setBackgroundResource(R.drawable.bg_cell)
            cell.setOnClickListener { onCellClick(i) }
            grid.addView(cell)
            cells.add(cell)
        }
        val gridLp = LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP)
        gridLp.topMargin = Ui.dp(this, 18)
        grid.layoutParams = gridLp
        col.addView(grid)

        startButton = Ui.button(this, "ابدأ", true, 24, 52) { start() }
        col.addView(startButton)

        setContentView(Ui.scroll(this, col))
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun start() {
        score = 0
        running = true
        mole = -1
        scoreText.text = "النقاط: 0"
        statusText.text = "هيا!"
        startButton.isEnabled = false
        cells.forEach { it.text = "" }

        timer?.cancel()
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                timeText.text = "⏱ " + (ms / 1000)
            }

            override fun onFinish() {
                end()
            }
        }.start()

        handler.removeCallbacks(moleRunnable)
        handler.post(moleRunnable)
    }

    private fun moveMole() {
        if (!running) return
        if (mole >= 0) cells[mole].text = ""
        var next = Random.nextInt(9)
        while (next == mole) next = Random.nextInt(9)
        mole = next
        cells[mole].text = "🐹"
    }

    private fun onCellClick(i: Int) {
        if (!running || i != mole) return
        score++
        scoreText.text = "النقاط: $score"
        cells[i].text = "💥"
        mole = -1
        Feedback.tap(this)
    }

    private fun end() {
        running = false
        handler.removeCallbacks(moleRunnable)
        cells.forEach { it.text = "" }
        mole = -1
        timeText.text = "⏱ 0"
        val record = Scores.saveHigh(this, "whack_best", score)
        statusText.text = if (record) "🏆 رقم قياسي جديد: $score"
        else "انتهى الوقت! نتيجتك $score (الأفضل ${Scores.get(this, "whack_best")})"
        startButton.text = "العب مرة أخرى"
        startButton.isEnabled = true
        Feedback.win(this)
    }
}
