package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.activity.OnBackPressedCallback

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

        findViewById<Button>(R.id.menuAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@MenuActivity)
                    .setTitle("الخروج من التطبيق")
                    .setMessage("هل أنت متأكد أنك تريد الخروج من التطبيق؟")
                    .setPositiveButton("نعم، خروج") { _, _ -> finish() }
                    .setNegativeButton("البقاء", null)
                    .show()
            }
        })

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

        val favorites = FavoritesManager.all(this, Games.all)
        if (favorites.isNotEmpty()) {
            addSectionTitle("♥ ألعابي المفضلة  (${favorites.size})")
            addGameGrid(favorites)
        }

        // أقسام ديناميكية تعتمد على سجل الاستخدام المحلي على الجهاز.
        // قد تظهر اللعبة في هذه الأقسام وفي تصنيفها الأصلي لتسهيل الوصول إليها.
        val mostPlayed = GameUsageManager.mostPlayed(this, Games.all, 6)
        if (mostPlayed.isNotEmpty()) {
            addSectionTitle("🔥 الأكثر زيارة  (${mostPlayed.size})")
            addGameGrid(mostPlayed)
        }

        val recentlyPlayed = GameUsageManager.recentlyPlayed(this, Games.all, 6)
        if (recentlyPlayed.isNotEmpty()) {
            addSectionTitle("🕘 آخر الألعاب لعباً  (${recentlyPlayed.size})")
            addGameGrid(recentlyPlayed)
        }

        // تصنيفات ثابتة وواضحة، وتبقى جميع الألعاب داخل تصنيفاتها حتى
        // إن ظهرت أيضاً في المفضلة أو الأكثر زيارة أو آخر الألعاب لعباً.
        val gamesForCategories = Games.all

        val categoryRules = linkedMapOf(
            "🏃 ألعاب الحركة والمغامرة" to setOf(
                "SprintRunActivity", "HurdleRunActivity", "FlappyActivity", "FrogHopActivity",
                "CatchActivity", "DodgeActivity", "CarRaceActivity", "BikeRaceActivity",
                "MeteorShootingActivity", "GhostMazeActivity", "SpaceShooterActivity", "MazeEscapeActivity",
                "TargetTapActivity", "GemCollectorActivity", "MeteorDodgeActivity", "JumpRunnerActivity",
                "BallBounceActivity", "TreasureHuntActivity", "CrosswalkActivity", "BeeFlowerActivity",
                "ArcheryActivity", "FixedGoalFootballActivity", "DuckHuntActivity", "CatapultActivity",
                "MagnetActivity", "PongSoloActivity", "BasketShotActivity", "MiniGolfActivity",
                "BrickActivity", "WhackActivity", "StopBarActivity", "BubblePopActivity"
            ),
            "🧩 ألعاب العقل والألغاز" to setOf(
                "PuzzleActivity", "MinesweeperActivity", "LightsOutActivity", "TowerActivity",
                "Game2048Activity", "SokobanActivity", "HanoiActivity", "MastermindActivity",
                "NimStrategyActivity", "PegSolitaireActivity", "RiverCrossingActivity", "NonogramActivity",
                "PipeConnectActivity", "SudokuActivity", "PicturePuzzleActivity", "TetrisActivity"
            ),
            "🧠 ألعاب الذكاء والذاكرة والتركيز" to setOf(
                "MemoryActivity", "NumberMemoryActivity", "SequenceActivity", "OddOneActivity",
                "NumberOrderActivity", "ColorMatchActivity", "ColorQuizActivity", "SimonActivity",
                "MemoryPairsPlusActivity", "EmojiCountActivity", "ColorWordActivity", "BiggerNumberActivity",
                "MathCheckActivity", "MakeTenActivity", "MissingLetterActivity", "AntonymsActivity",
                "MemoryEmojiActivity", "CoinCountActivity", "ClockReadActivity", "OddWordActivity", "RealisticDifferencesActivity",
                "BalanceScaleActivity", "NumberLadderActivity", "LetterColoringActivity", "EyesightChallengeActivity"
            ),
            "❓ الأسئلة والمعرفة" to setOf(
                "TriviaActivity", "CapitalQuizActivity", "AnimalQuizActivity", "TrueFalseActivity",
                "HigherLowerActivity", "WeatherQuizActivity", "GuessNumberActivity", "HangmanActivity",
                "WordScrambleActivity", "ScrambleActivity", "QuickMathActivity", "MathActivity"
            ),
            "⚡ ألعاب السرعة والتحدي" to setOf(
                "TapSpeedActivity", "ReactionActivity", "WordSprintActivity", "QuickChallengeActivity",
                "OddEvenActivity", "ColorWordActivity"
            ),
            "🎲 الألعاب الكلاسيكية والاجتماعية" to setOf(
                "MainActivity", "RpsActivity", "DiceActivity", "DiceDuelActivity", "CoinFlipActivity",
                "SnakeActivity", "Connect4Activity", "PacManActivity", "LuckyWheelActivity",
                "LuckBoxActivity", "FallingActivity", "CheckersActivity"
            )
        )

        val assigned = mutableSetOf<Class<*>>()
        categoryRules.forEach { (title, classNames) ->
            val group = gamesForCategories.filter { it.cls.simpleName in classNames && assigned.add(it.cls) }
            if (group.isNotEmpty()) {
                addSectionTitle("$title  (${group.size})")
                addGameGrid(group)
            }
        }

        val uncategorized = gamesForCategories.filter { it.cls !in assigned }
        if (uncategorized.isNotEmpty()) {
            addSectionTitle("🎮 ألعاب متنوعة  (${uncategorized.size})")
            addGameGrid(uncategorized)
        }
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
