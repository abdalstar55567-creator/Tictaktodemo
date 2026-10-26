package com.example.tictactoe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.GridLayout
import android.widget.TextView
import kotlin.random.Random

class LightsOutActivity : BaseGameActivity() {

    private val n = 5
    private val lit = BooleanArray(n * n)
    private val cells = ArrayList<TextView>()
    private var moves = 0
    private var solved = false

    private lateinit var movesText: TextView
    private lateinit var bestText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("💡 أطفئ الأضواء")

        content.addView(
            label("اضغط على مربع لتبديل حالته وحالة جيرانه. أطفئ كل الأضواء!", 16f, R.color.text_dim, false),
            lp(top = dp(4))
        )

        movesText = label("", 22f)
        content.addView(movesText, lp(top = dp(10)))

        bestText = label("", 15f, R.color.gold)
        content.addView(bestText, lp(top = dp(2)))

        statusText = label("", 18f)
        content.addView(statusText, lp(top = dp(6), bottom = dp(10)))

        val grid = GridLayout(this)
        grid.columnCount = n
        grid.rowCount = n
        val size = minOf((resources.displayMetrics.widthPixels - dp(72)) / n - dp(8), dp(64))
        for (i in 0 until n * n) {
            val t = TextView(this)
            val p = GridLayout.LayoutParams()
            p.width = size
            p.height = size
            p.setMargins(dp(4), dp(4), dp(4), dp(4))
            t.layoutParams = p
            t.setOnClickListener { press(i) }
            grid.addView(t)
            cells.add(t)
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

        content.addView(button("لعبة جديدة") { newGame() }, lp(height = dp(50), top = dp(20)))
        newGame()
    }

    private fun toggle(i: Int) {
        val r = i / n
        val c = i % n
        lit[i] = !lit[i]
        if (r > 0) lit[i - n] = !lit[i - n]
        if (r < n - 1) lit[i + n] = !lit[i + n]
        if (c > 0) lit[i - 1] = !lit[i - 1]
        if (c < n - 1) lit[i + 1] = !lit[i + 1]
    }

    private fun newGame() {
        lit.fill(false)
        val presses = Random.nextInt(8, 14)
        for (k in 0 until presses) toggle(Random.nextInt(n * n))
        if (lit.none { it }) toggle(12)
        moves = 0
        solved = false
        statusText.text = ""
        render()
    }

    private fun press(i: Int) {
        if (solved) return
        toggle(i)
        moves++
        Feedback.tap(this)
        render()

        if (lit.none { it }) {
            solved = true
            statusText.text = "🎉 أطفأت كل الأضواء!"
            Feedback.win(this)
            val best = Scores.get(this, "lights_best")
            if (best == 0 || moves < best) {
                Scores.put(this, "lights_best", moves)
                statusText.text = "🏆 رقم قياسي! $moves حركة"
            }
            render()
        }
    }

    private fun render() {
        for (i in 0 until n * n) {
            val d = GradientDrawable()
            d.cornerRadius = dp(10).toFloat()
            d.setColor(if (lit[i]) Color.parseColor("#FACC15") else Color.parseColor("#273449"))
            cells[i].background = d
        }
        movesText.text = "الحركات: $moves"
        val best = Scores.get(this, "lights_best")
        bestText.text = if (best == 0) "" else "أقل عدد حركات: $best"
    }
}
