package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class HangmanActivity : BaseGameActivity() {

    private val words = listOf(
        "سيارة", "حديقة", "مكتبة", "شجرة", "قطار", "سمكة", "برتقال", "موزة",
        "كتاب", "نجمة", "جبل", "دجاجة", "فراشة", "سلحفاة", "مدرسة", "مطبخ",
        "حصان", "تفاحة", "شمس", "وردة", "مفتاح", "ساعة", "جمل", "سفينة"
    )
    private val alphabet = "ابتثجحخدذرزسشصضطظعغفقكلمنهوية"
    private val maxLives = 6

    private var word = ""
    private val guessed = HashSet<Char>()
    private var lives = maxLives
    private var over = false
    private var wins = 0

    private lateinit var wordText: TextView
    private lateinit var livesText: TextView
    private lateinit var statusText: TextView
    private val keys = HashMap<Char, Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔤 حبل المشنقة")

        livesText = label("", 24f)
        content.addView(livesText, lp(top = dp(4)))

        wordText = label("", 36f, R.color.x_color)
        content.addView(wordText, lp(top = dp(14)))

        statusText = label("", 17f, R.color.gold)
        content.addView(statusText, lp(top = dp(8), bottom = dp(12)))

        var i = 0
        while (i < alphabet.length) {
            val r = row()
            var j = 0
            while (j < 6 && i < alphabet.length) {
                val ch = alphabet[i]
                val b = button(ch.toString(), false) { onLetter(ch) }
                b.textSize = 18f
                b.setPadding(0, 0, 0, 0)
                keys[ch] = b
                r.addView(b, weightLp(dp(46)))
                i++
                j++
            }
            content.addView(r, lp(top = dp(4)))
        }

        content.addView(button("كلمة جديدة") { newGame() }, lp(height = dp(48), top = dp(16)))
        newGame()
    }

    private fun newGame() {
        word = words.random()
        guessed.clear()
        lives = maxLives
        over = false
        keys.values.forEach {
            it.isEnabled = true
            it.alpha = 1f
        }
        statusText.text = "الفوز حتى الآن: $wins"
        render()
    }

    private fun render() {
        livesText.text = "❤️".repeat(lives) + "🖤".repeat(maxLives - lives)
        wordText.text = word.map { if (guessed.contains(it)) it.toString() else "_" }.joinToString(" ")
    }

    private fun onLetter(ch: Char) {
        if (over) return
        val b = keys[ch] ?: return
        if (guessed.contains(ch)) return
        guessed.add(ch)
        b.isEnabled = false
        b.alpha = 0.35f

        if (word.contains(ch)) {
            Feedback.tap(this)
        } else {
            lives--
            Feedback.lose(this)
        }
        render()

        if (word.all { guessed.contains(it) }) {
            over = true
            wins++
            statusText.text = "🎉 أحسنت! الفوز حتى الآن: $wins"
            Feedback.win(this)
        } else if (lives <= 0) {
            over = true
            statusText.text = "خسرت! الكلمة كانت: $word"
            wordText.text = word.map { it.toString() }.joinToString(" ")
        }
    }
}
