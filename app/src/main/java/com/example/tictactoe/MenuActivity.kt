package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var soundButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<TextView>(R.id.menuCount).text =
            "${Games.all.size} لعبة متاحة الآن"

        val list = findViewById<LinearLayout>(R.id.gamesList)
        list.removeAllViews()

        // شبكة مرنة بثلاث بطاقات في الصف، دون تغيير بيانات الألعاب أو طريقة تشغيلها.
        val columns = 3
        var row: LinearLayout? = null
        Games.all.forEachIndexed { index, game ->
            if (index % columns == 0) {
                row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutDirection = View.LAYOUT_DIRECTION_LTR
                }
                val rowParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rowParams.bottomMargin = Ui.dp(this, 10)
                list.addView(row, rowParams)
            }

            val card = GameComponents.gameCard(this, game, index)
            val cardParams = LinearLayout.LayoutParams(0, Ui.dp(this, 126), 1f)
            cardParams.setMargins(Ui.dp(this, 4), Ui.dp(this, 3), Ui.dp(this, 4), Ui.dp(this, 3))
            row?.addView(card, cardParams)
            card.setOnClickListener {
                Feedback.tap(this)
                startActivity(Intent(this, game.cls))
            }
        }

        soundButton = findViewById(R.id.menuSound)
        soundButton.setOnClickListener {
            Feedback.setEnabled(this, !Feedback.enabled(this))
            Feedback.tap(this)
            updateSoundButton()
        }
        updateSoundButton()
    }

    private fun updateSoundButton() {
        soundButton.text =
            if (Feedback.enabled(this)) "🔊  الصوت والاهتزاز: تشغيل" else "🔇  الصوت والاهتزاز: إيقاف"
    }
}
