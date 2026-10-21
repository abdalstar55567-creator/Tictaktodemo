package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.random.Random

class MathActivity : BaseGameActivity() {

    override val gameTitle: String get() = "تحدي الحساب"

    private val total = 10
    private var index = 0
    private var score = 0
    private var correct = 0
    private var locked = false

    private val handler = Handler(Looper.getMainLooper())
    private val optionButtons = ArrayList<Button>()

    private lateinit var infoText: TextView
    private lateinit var questionText: TextView
    private lateinit var resultText: TextView
    private lateinit var replayButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val col = Ui.column(this)

        infoText = Ui.text(this, "", 17f, R.color.text_dim, top = 8)
        col.addView(infoText)

        questionText = Ui.text(this, "", 46f, R.color.x_color, true, top = 24)
        questionText.textDirection = View.TEXT_DIRECTION_LTR
        col.addView(questionText)

        resultText = Ui.text(this, "", 19f, R.color.text_main, true, top = 8)
        col.addView(resultText)

        for (i in 0 until 4) {
            val b = Ui.button(this, "", false, 14, 58) { onAnswer(i) }
            b.textSize = 22f
            b.textDirection = View.TEXT_DIRECTION_LTR
            optionButtons.add(b)
            col.addView(b)
        }

        replayButton = Ui.button(this, "العب مرة أخرى", true, 24, 52) { startGame() }
        replayButton.visibility = View.GONE
        col.addView(replayButton)

        setContentView(Ui.scroll(this, col))
        startGame()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun startGame() {
        index = 0
        score = 0
        locked = false
        replayButton.visibility = View.GONE
        optionButtons.forEach { it.visibility = View.VISIBLE }
        showQuestion()
    }

    private fun showQuestion() {
        if (index >= total) {
            endGame()
            return
        }

        val a = Random.nextInt(2, 13)
        val b = Random.nextInt(2, 13)
        val question: String
        when (Random.nextInt(3)) {
            0 -> {
                question = "$a + $b"
                correct = a + b
            }
            1 -> {
                val big = maxOf(a, b)
                val small = minOf(a, b)
                question = "$big - $small"
                correct = big - small
            }
            else -> {
                question = "$a × $b"
                correct = a * b
            }
        }

        val options = mutableSetOf(correct)
        while (options.size < 4) {
            val d = correct + Random.nextInt(-10, 11)
            if (d >= 0) options.add(d)
        }
        val shuffled = options.toList().shuffled()

        questionText.text = "$question = ؟"
        resultText.text = ""
        infoText.text = "السؤال ${index + 1} من $total   •   النقاط: $score"
        for (i in 0 until 4) {
            optionButtons[i].text = shuffled[i].toString()
            optionButtons[i].setBackgroundResource(R.drawable.bg_button_dim)
        }
    }

    private fun onAnswer(i: Int) {
        if (locked) return
        locked = true

        val chosen = optionButtons[i].text.toString().toIntOrNull()
        if (chosen == correct) {
            score++
            optionButtons[i].background = Ui.panelBg(this, Color.parseColor("#166534"), 24)
            resultText.text = "✔ صحيح"
            Feedback.match(this)
        } else {
            optionButtons[i].background = Ui.panelBg(this, Color.parseColor("#991B1B"), 24)
            resultText.text = "✘ الجواب الصحيح: $correct"
            Feedback.lose(this)
        }
        index++

        handler.postDelayed({
            locked = false
            showQuestion()
        }, 900)
    }

    private fun endGame() {
        optionButtons.forEach { it.visibility = View.GONE }
        replayButton.visibility = View.VISIBLE
        val record = Scores.saveHigh(this, "math_best", score)
        questionText.text = "$score / $total"
        infoText.text = "انتهى التحدي!"
        resultText.text = if (record) "🏆 رقم قياسي جديد!"
        else "أفضل نتيجة: ${Scores.get(this, "math_best")} / $total"
        Feedback.win(this)
    }
}
