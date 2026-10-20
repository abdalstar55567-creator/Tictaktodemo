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

class MemoryActivity : AppCompatActivity() {

    private val emojis = listOf("🐱", "🐶", "🦊", "🐼", "🐸", "🦁", "🐵", "🐙")

    private val cards = ArrayList<TextView>()
    private var values = IntArray(16)
    private val faceUp = BooleanArray(16)
    private val matched = BooleanArray(16)

    private var first = -1
    private var locked = false
    private var moves = 0
    private var pairs = 0

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var movesText: TextView
    private lateinit var bestText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        movesText = findViewById(R.id.memMoves)
        bestText = findViewById(R.id.memBest)
        statusText = findViewById(R.id.memStatus)

        buildBoard()

        findViewById<Button>(R.id.memNew).setOnClickListener {
            Feedback.tap(this)
            newGame()
        }

        newGame()
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
        val grid = findViewById<GridLayout>(R.id.memBoard)
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = dp(4)
        val size = minOf((screenWidth - dp(40)) / 4 - margin * 2, dp(84))

        for (i in 0 until 16) {
            val card = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = size
            params.height = size
            params.setMargins(margin, margin, margin, margin)
            card.layoutParams = params
            card.gravity = Gravity.CENTER
            card.cameraDistance = 8000f
            card.setOnClickListener { onCardClick(i) }
            grid.addView(card)
            cards.add(card)
        }
    }

    private fun showBack(card: TextView) {
        card.text = "؟"
        card.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
        card.setTextColor(ContextCompat.getColor(this, R.color.text_dim))
    }

    private fun showFront(card: TextView, index: Int) {
        card.text = emojis[values[index]]
        card.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f)
        card.setTextColor(ContextCompat.getColor(this, R.color.text_main))
    }

    private fun newGame() {
        handler.removeCallbacksAndMessages(null)
        values = (0 until 8).flatMap { listOf(it, it) }.shuffled().toIntArray()
        first = -1
        locked = false
        moves = 0
        pairs = 0

        for (i in 0 until 16) {
            faceUp[i] = false
            matched[i] = false
            val card = cards[i]
            card.animate().cancel()
            card.rotationY = 0f
            card.setBackgroundResource(R.drawable.bg_cell)
            showBack(card)
        }

        statusText.text = "اقلب بطاقتين متطابقتين"
        updateTexts()
    }

    private fun onCardClick(i: Int) {
        if (locked || faceUp[i] || matched[i]) return

        Feedback.tap(this)
        flip(i, true)

        if (first == -1) {
            first = i
            return
        }

        val a = first
        first = -1
        moves++
        updateTexts()
        locked = true

        if (values[a] == values[i]) {
            handler.postDelayed({
                matched[a] = true
                matched[i] = true
                cards[a].setBackgroundResource(R.drawable.bg_cell_match)
                cards[i].setBackgroundResource(R.drawable.bg_cell_match)
                pairs++
                locked = false
                if (pairs == 8) {
                    finishGame()
                } else {
                    Feedback.match(this)
                    statusText.text = "تطابق ممتاز! ✨"
                }
            }, 350)
        } else {
            handler.postDelayed({
                flip(a, false)
                flip(i, false)
                locked = false
            }, 800)
        }
    }

    private fun flip(i: Int, up: Boolean) {
        faceUp[i] = up
        val card = cards[i]
        card.animate().rotationY(90f).setDuration(120).withEndAction {
            if (up) showFront(card, i) else showBack(card)
            card.rotationY = -90f
            card.animate().rotationY(0f).setDuration(120).start()
        }.start()
    }

    private fun finishGame() {
        val prefs = getSharedPreferences("xozoom", MODE_PRIVATE)
        val best = prefs.getInt("memory_best", 0)
        if (best == 0 || moves < best) {
            prefs.edit().putInt("memory_best", moves).apply()
            statusText.text = "رقم قياسي جديد! 🏆 ($moves حركة)"
        } else {
            statusText.text = "أحسنت! أنهيت اللعبة في $moves حركة 🎉"
        }
        Feedback.win(this)
        updateTexts()

        cards.forEach { card ->
            card.animate().scaleX(1.12f).scaleY(1.12f).setDuration(200).withEndAction {
                card.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }.start()
        }
    }

    private fun updateTexts() {
        val best = getSharedPreferences("xozoom", MODE_PRIVATE).getInt("memory_best", 0)
        movesText.text = "الحركات\n$moves"
        bestText.text = "أفضل نتيجة\n" + (if (best == 0) "-" else best.toString())
    }
}
