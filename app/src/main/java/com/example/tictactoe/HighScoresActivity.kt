package com.example.tictactoe

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HighScoresActivity : AppCompatActivity() {
    private data class Record(val title: String, val key: String, val lowerIsBetter: Boolean = false)

    private val scoreKeys = mapOf(
        "WhackActivity" to "whack_best", "TapSpeedActivity" to "tap_best",
        "ReactionActivity" to "reaction_best", "SprintRunActivity" to "sprint_run_best_ms",
        "BrickActivity" to "brick_best", "SnakeActivity" to "snake_best",
        "FlappyActivity" to "flappy_best", "TowerActivity" to "tower_best",
        "MathActivity" to "math_best", "TriviaActivity" to "trivia_best",
        "Game2048Activity" to "g2048_best", "ColorMatchActivity" to "color_best",
        "SimonActivity" to "simon_best", "NumberMemoryActivity" to "nummem_best",
        "OddOneActivity" to "odd_best", "NumberOrderActivity" to "order_best",
        "StopBarActivity" to "stopbar_best", "LuckBoxActivity" to "luck_best",
        "HurdleRunActivity" to "hurdle_best", "FrogHopActivity" to "frog_hop_best",
        "GuessNumberActivity" to "guess_best", "HigherLowerActivity" to "hl_best",
        "LightsOutActivity" to "lights_best", "ScrambleActivity" to "scramble_best",
        "CatchActivity" to "catch_best", "DodgeActivity" to "dodge_best",
        "CatapultActivity" to "catapult_best_1"
    )
    private val lowerIsBetter = setOf("ReactionActivity", "SprintRunActivity", "NumberOrderActivity")
    private val millisecondScores = setOf("ReactionActivity", "SprintRunActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(24)); setBackgroundResource(R.drawable.bg_menu_sky) }
        content.addView(TextView(this).apply { text = "🏆 أعلى النتائج لكل لعبة"; textSize = 23f; setTextColor(-1); gravity = Gravity.CENTER; setTypeface(typeface, 1); setPadding(0, 0, 0, dp(16)) })
        Games.all.forEach { game ->
            val key = scoreKeys[game.cls.simpleName]
            val value = if (key != null) Scores.get(this, key) else 0
            val display = when {
                key == null -> "السجل غير متاح لهذه اللعبة حالياً"
                value == 0 -> "لم تسجل نتيجة بعد"
                game.cls.simpleName in millisecondScores -> "$value ms"
                else -> value.toString()
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12))
                background = android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFF0878D8.toInt(), 0xFF06458F.toInt())).apply { cornerRadius = dp(16).toFloat(); setStroke(dp(1), 0x66FFFFFF) }
            }
            row.addView(TextView(this).apply { text = "${game.emoji}  ${game.title}"; textSize = 15f; setTextColor(-1); setTypeface(typeface, 1) }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(TextView(this).apply { text = "🏅 $display"; textSize = 12f; setTextColor(0xFFFFE28A.toInt()); setTypeface(typeface, 1); gravity = Gravity.END })
            content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
        content.addView(TextView(this).apply { text = "تُعرض النتائج المحفوظة على هذا الجهاز. بعض الألعاب لم يُضف إليها حفظ الرقم القياسي بعد."; textSize = 13f; setTextColor(0xFFEAF4FF.toInt()); gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) })
        setContentView(ScrollView(this).apply { addView(content) })
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
