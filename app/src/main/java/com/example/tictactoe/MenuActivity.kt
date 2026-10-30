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

    private lateinit var gamesList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        gamesList = findViewById(R.id.gamesList)

        findViewById<TextView>(R.id.menuCount).text =
            "${Games.all.size} لعبة متاحة الآن"

        soundButton = findViewById(R.id.menuSound)

        soundButton.setOnClickListener {
            Feedback.setEnabled(
                this,
                !Feedback.enabled(this)
            )

            Feedback.tap(this)
            updateSoundButton()
        }

        updateSoundButton()
        buildGamesMenu()
    }

    override fun onResume() {
        super.onResume()

        // تحديث الترتيب عند العودة من اللعبة
        if (::gamesList.isInitialized) {
            buildGamesMenu()
        }
    }

    private fun buildGamesMenu() {

        gamesList.removeAllViews()

        // أولاً: أكثر ثلاث ألعاب زيارة
        val topThree = GameUsageManager.mostPlayed(
            this,
            Games.all,
            3
        )

        // ثانياً: آخر خمس ألعاب لعبها المستخدم
        // مع استبعاد الألعاب الموجودة في القسم الأول
        val recentGames = GameUsageManager
            .recentlyPlayed(this, Games.all, 100)
            .filter { recent ->
                topThree.none { it.cls == recent.cls }
            }
            .take(5)

        // ثالثاً: بقية الألعاب
        // الألعاب التي سبق لعبها ترتب حسب آخر زيارة
        // الألعاب الجديدة تحتفظ بترتيبها الأصلي
        val excluded = (topThree + recentGames)
            .map { it.cls }
            .toSet()

        val remainingGames = Games.all
            .filter { it.cls !in excluded }
            .sortedWith(
                compareByDescending<GameInfo> {
                    GameUsageManager.usage(
                        this,
                        it.cls.name
                    ).lastPlayed > 0
                }.thenByDescending {
                    GameUsageManager.usage(
                        this,
                        it.cls.name
                    ).lastPlayed
                }
            )

        // عرض القسم الأول
        if (topThree.isNotEmpty()) {
            addSectionTitle("🏆 الألعاب الأكثر زيارة")
            addGameGrid(topThree)
        }

        // عرض القسم الثاني
        if (recentGames.isNotEmpty()) {
            addSectionTitle("🕹️ آخر الألعاب التي لعبتها")
            addGameGrid(recentGames)
        }

        // عرض جميع الألعاب المتبقية
        addSectionTitle("🎮 جميع الألعاب")
        addGameGrid(remainingGames)
    }

    private fun addSectionTitle(title: String) {

        val text = TextView(this).apply {
            this.text = title
            textSize = 17f
            setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    this@MenuActivity,
                    R.color.text_main
                )
            )
            setPadding(
                Ui.dp(this@MenuActivity, 8),
                Ui.dp(this@MenuActivity, 16),
                Ui.dp(this@MenuActivity, 8),
                Ui.dp(this@MenuActivity, 10)
            )
            gravity = Gravity.CENTER_VERTICAL
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        gamesList.addView(
            text,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addGameGrid(games: List<GameInfo>) {

        val columns = 3
        var row: LinearLayout? = null

        games.forEachIndexed { index, game ->

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

                gamesList.addView(row, rowParams)
            }

            val originalIndex = Games.all.indexOf(game)

            val card = GameComponents.gameCard(
                this,
                game,
                originalIndex
            )

            val cardParams = LinearLayout.LayoutParams(
                0,
                Ui.dp(this, 126),
                1f
            )

            cardParams.setMargins(
                Ui.dp(this, 4),
                Ui.dp(this, 3),
                Ui.dp(this, 4),
                Ui.dp(this, 3)
            )

            row?.addView(card, cardParams)

            card.setOnClickListener {
                Feedback.tap(this)
                startActivity(
                    Intent(this, game.cls)
                )
            }
        }

        // إكمال الصف الأخير بعناصر فارغة للمحافظة على توزيع الشبكة
        val remainder = games.size % columns

        if (remainder != 0 && row != null) {
            repeat(columns - remainder) {
                val spacer = View(this)

                row?.addView(
                    spacer,
                    LinearLayout.LayoutParams(0, Ui.dp(this, 126), 1f)
                )
            }
        }
    }

    private fun updateSoundButton() {

        soundButton.text =
            if (Feedback.enabled(this)) {
                "🔊  الصوت والاهتزاز: تشغيل"
            } else {
                "🔇  الصوت والاهتزاز: إيقاف"
            }
    }
}
