package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.activity.OnBackPressedCallback

class MenuActivity : AppCompatActivity() {
    private lateinit var pageHost: FrameLayout
    private lateinit var homeTab: TextView
    private lateinit var favoritesTab: TextView
    private lateinit var settingsTab: TextView
    private var selectedTab = TAB_HOME

    companion object {
        private const val TAB_FAVORITES = 0
        private const val TAB_HOME = 1
        private const val TAB_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildShell()
        showTab(TAB_HOME)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (selectedTab != TAB_HOME) showTab(TAB_HOME)
                else AlertDialog.Builder(this@MenuActivity)
                    .setTitle("الخروج من التطبيق")
                    .setMessage("هل أنت متأكد أنك تريد الخروج من التطبيق؟")
                    .setPositiveButton("نعم، خروج") { _, _ -> finish() }
                    .setNegativeButton("البقاء", null).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::pageHost.isInitialized) showTab(selectedTab)
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_menu_sky)
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(7), dp(12), dp(7))
            background = gradient(0xFF073A83.toInt(), 0xFF0879D8.toInt(), 18)
        }
        val brand = TextView(this).apply {
            text = "100+"
            textSize = 23f
            setTextColor(0xFFFFD84D.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val overflow = TextView(this).apply {
            text = "⋮"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(-1)
            setPadding(dp(12), 0, dp(12), 0)
            contentDescription = "القائمة الجانبية"
            setOnClickListener { showOverflow(this) }
        }
        toolbar.addView(overflow, LinearLayout.LayoutParams(dp(48), dp(46)))
        toolbar.addView(brand, LinearLayout.LayoutParams(0, dp(46), 1f))
        root.addView(toolbar)

        pageHost = FrameLayout(this)
        root.addView(pageHost, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(6), dp(7), dp(6))
            background = gradient(0xFF052D68.toInt(), 0xFF0876D2.toInt(), 20)
            elevation = dp(12).toFloat()
        }
        favoritesTab = navItem("♥\nالمفضلة", TAB_FAVORITES)
        homeTab = navItem("⌂\nالرئيسية", TAB_HOME)
        settingsTab = navItem("⚙\nالضبط", TAB_SETTINGS)
        // RTL order: right = favorites, center = home, left = settings.
        nav.addView(favoritesTab, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(homeTab, LinearLayout.LayoutParams(0, dp(58), 1f))
        nav.addView(settingsTab, LinearLayout.LayoutParams(0, dp(58), 1f))
        root.addView(nav)
        setContentView(root)
    }

    private fun navItem(label: String, tab: Int) = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 13f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setTextColor(-1)
        setOnClickListener { showTab(tab) }
    }

    private fun showTab(tab: Int) {
        selectedTab = tab
        pageHost.removeAllViews()
        val page = when (tab) {
            TAB_FAVORITES -> buildFavoritesPage()
            TAB_SETTINGS -> buildSettingsPage()
            else -> buildHomePage()
        }
        pageHost.addView(page)
        listOf(favoritesTab to TAB_FAVORITES, homeTab to TAB_HOME, settingsTab to TAB_SETTINGS).forEach { (view, id) ->
            view.alpha = if (id == tab) 1f else .68f
            view.background = if (id == tab) gradient(0xFFFFB52E.toInt(), 0xFFFF7A18.toInt(), 16) else null
        }
    }

    private fun buildHomePage(): View {
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(18)) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(14), dp(13), dp(14), dp(15)); background = gradient(0xFF0647A5.toInt(), 0xFF00A7E8.toInt(), 22); elevation = dp(7).toFloat()
        }
        header.addView(TextView(this).apply { text = "🎮  ✨  🧩"; textSize = 24f; gravity = Gravity.CENTER })
        val logo = ImageView(this).apply { setImageResource(R.drawable.logo_100plus_glossy); scaleType = ImageView.ScaleType.FIT_CENTER; contentDescription = "100+" }
        header.addView(logo, LinearLayout.LayoutParams(-1, dp(154)))
        header.addView(TextView(this).apply { text = "عالم من المرح والتحدي"; textSize = 20f; setTextColor(-1); setTypeface(typeface, 1); gravity = Gravity.CENTER })
        header.addView(TextView(this).apply { text = "${Games.all.size} لعبة متاحة الآن"; textSize = 14f; setTextColor(0xFFEAF6FF.toInt()); gravity = Gravity.CENTER; setPadding(0, dp(5), 0, 0) })
        content.addView(header, LinearLayout.LayoutParams(-1, -2))
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        gamesListProxy = list
        content.addView(list, LinearLayout.LayoutParams(-1, -2))
        buildGamesMenu()
        return ScrollView(this).apply { clipToPadding = false; addView(content) }
    }

    private lateinit var gamesListProxy: LinearLayout

    private fun buildGamesMenu() {
        if (!::gamesListProxy.isInitialized) return
        gamesListProxy.removeAllViews()
        val categoryRules = linkedMapOf(
            "🏃 ألعاب الحركة والمغامرة" to setOf("SprintRunActivity", "HurdleRunActivity", "FlappyActivity", "FrogHopActivity", "CatchActivity", "DodgeActivity", "CarRaceActivity", "BikeRaceActivity", "MeteorShootingActivity", "GhostMazeActivity", "SpaceShooterActivity", "MazeEscapeActivity", "TargetTapActivity", "GemCollectorActivity", "MeteorDodgeActivity", "JumpRunnerActivity", "BallBounceActivity", "TreasureHuntActivity", "CrosswalkActivity", "BeeFlowerActivity", "ArcheryActivity", "FixedGoalFootballActivity", "DuckHuntActivity", "CatapultActivity", "NeonGameActivity", "MagnetActivity", "PongSoloActivity", "BasketShotActivity", "MiniGolfActivity", "BrickActivity", "WhackActivity", "StopBarActivity", "BubblePopActivity"),
            "🧩 ألعاب العقل والألغاز" to setOf("PuzzleActivity", "MinesweeperActivity", "LightsOutActivity", "TowerActivity", "Game2048Activity", "SokobanActivity", "HanoiActivity", "MastermindActivity", "NimStrategyActivity", "PegSolitaireActivity", "RiverCrossingActivity", "NonogramActivity", "PipeConnectActivity", "SudokuActivity", "PicturePuzzleActivity", "TetrisActivity"),
            "🧠 ألعاب الذكاء والذاكرة والتركيز" to setOf("MemoryActivity", "NumberMemoryActivity", "SequenceActivity", "OddOneActivity", "NumberOrderActivity", "ColorMatchActivity", "ColorQuizActivity", "SimonActivity", "MemoryPairsPlusActivity", "EmojiCountActivity", "ColorWordActivity", "BiggerNumberActivity", "MathCheckActivity", "MakeTenActivity", "MissingLetterActivity", "AntonymsActivity", "MemoryEmojiActivity", "CoinCountActivity", "ClockReadActivity", "OddWordActivity", "RealisticDifferencesActivity", "BalanceScaleActivity", "NumberLadderActivity", "LetterColoringActivity", "EyesightChallengeActivity"),
            "❓ الأسئلة والمعرفة" to setOf("TriviaActivity", "CapitalQuizActivity", "AnimalQuizActivity", "TrueFalseActivity", "HigherLowerActivity", "WeatherQuizActivity", "GuessNumberActivity", "HangmanActivity", "WordScrambleActivity", "ScrambleActivity", "QuickMathActivity", "MathActivity"),
            "⚡ ألعاب السرعة والتحدي" to setOf("TapSpeedActivity", "ReactionActivity", "WordSprintActivity", "QuickChallengeActivity", "OddEvenActivity", "ColorWordActivity"),
            "🎲 الألعاب الكلاسيكية والاجتماعية" to setOf("MainActivity", "RpsActivity", "DiceActivity", "DiceDuelActivity", "CoinFlipActivity", "SnakeActivity", "Connect4Activity", "PacManActivity", "LuckyWheelActivity", "LuckBoxActivity", "FallingActivity", "CheckersActivity")
        )
        val assigned = mutableSetOf<Class<*>>()
        categoryRules.forEach { (title, names) ->
            val group = Games.all.filter { it.cls.simpleName in names && assigned.add(it.cls) }
            if (group.isNotEmpty()) { addSectionTitle(title, gamesListProxy); addGameGrid(group, gamesListProxy) }
        }
        val remaining = Games.all.filter { it.cls !in assigned }
        if (remaining.isNotEmpty()) { addSectionTitle("🎮 ألعاب متنوعة", gamesListProxy); addGameGrid(remaining, gamesListProxy) }
    }

    private fun buildFavoritesPage(): View {
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(14), dp(12), dp(20)) }
        addPageTitle(list, "♥ المفضلة وآخر الألعاب")
        val fav = FavoritesManager.all(this, Games.all)
        addSectionTitle("♥ ألعابي المفضلة (${fav.size})", list)
        if (fav.isEmpty()) addEmptyMessage(list, "لم تضف ألعاباً للمفضلة بعد. اضغط القلب داخل أي لعبة لإضافتها هنا.") else addGameGrid(fav, list)
        val recent = GameUsageManager.recentlyPlayed(this, Games.all, Games.all.size)
        addSectionTitle("🕘 آخر الألعاب استخداماً (${recent.size})", list)
        if (recent.isEmpty()) addEmptyMessage(list, "ستظهر هنا الألعاب التي تلعبها، مرتبة من الأحدث إلى الأقدم.") else addGameGrid(recent, list)
        return ScrollView(this).apply { addView(list) }
    }

    private fun buildSettingsPage(): View {
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(18), dp(16), dp(22)) }
        addPageTitle(list, "⚙ الضبط")
        addSectionTitle("🎵 إعدادات الصوت والإيقاع", list)
        list.addView(settingRow("الموسيقى والإيقاع", "تشغيل أو إيقاف موسيقى أقسام الألعاب", GameMusic.enabled(this)) {
            GameMusic.setEnabled(this, !GameMusic.enabled(this)); Feedback.tap(this); showTab(TAB_SETTINGS)
        })
        list.addView(settingRow("المؤثرات الصوتية", "أصوات الضغط والفوز والخسارة", Feedback.enabled(this)) {
            Feedback.setEnabled(this, !Feedback.enabled(this)); showTab(TAB_SETTINGS)
        })
        list.addView(TextView(this).apply {
            text = "تُحفظ هذه الإعدادات على جهازك وتُطبّق على الألعاب دون التأثير على تقدمك أو نتائجك."
            textSize = 14f; setTextColor(0xFFEAF4FF.toInt()); setPadding(dp(8), dp(18), dp(8), 0)
        })
        return ScrollView(this).apply { addView(list) }
    }

    private fun settingRow(title: String, subtitle: String, enabled: Boolean, action: () -> Unit): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = gradient(0xFF0878D8.toInt(), 0xFF064B9C.toInt(), 18); isClickable = true; isFocusable = true; setOnClickListener { action() } }
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        labels.addView(TextView(this).apply { text = title; textSize = 17f; setTextColor(-1); setTypeface(typeface, 1) })
        labels.addView(TextView(this).apply { text = subtitle; textSize = 12f; setTextColor(0xFFD8ECFF.toInt()) })
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(this).apply { text = if (enabled) "● يعمل" else "○ متوقف"; textSize = 14f; setTextColor(if (enabled) 0xFF8CFFB0.toInt() else 0xFFFFD18A.toInt()); setTypeface(typeface, 1) })
        row.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
        return row
    }

    private fun showOverflow(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add("🏆 أعلى النتائج لكل لعبة").setOnMenuItemClickListener { startActivity(Intent(this@MenuActivity, HighScoresActivity::class.java)); true }
            menu.add("ℹ عن التطبيق والخصوصية").setOnMenuItemClickListener { startActivity(Intent(this@MenuActivity, AboutActivity::class.java)); true }
            show()
        }
    }

    private fun addSectionTitle(title: String, target: LinearLayout) {
        val text = TextView(this).apply {
            this.text = title; textSize = 17f; setTextColor(-1); setPadding(dp(16), dp(9), dp(16), dp(9)); gravity = Gravity.CENTER
            setTypeface(typeface, 1); background = gradient(0xFF0878E8.toInt(), 0xFF0752B8.toInt(), 24); elevation = dp(5).toFloat()
        }
        target.addView(text, LinearLayout.LayoutParams(-2, -2).apply { gravity = Gravity.END; topMargin = dp(13); bottomMargin = dp(8) })
    }

    private fun addGameGrid(games: List<GameInfo>, target: LinearLayout) {
        val columns = 3
        var row: LinearLayout? = null
        games.forEachIndexed { index, game ->
            if (index % columns == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; layoutDirection = View.LAYOUT_DIRECTION_LTR }
                target.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
            }
            val card = GameComponents.gameCard(this, game, Games.all.indexOf(game))
            row?.addView(card, LinearLayout.LayoutParams(0, dp(126), 1f).apply { setMargins(dp(4), dp(3), dp(4), dp(3)) })
            card.setOnClickListener { Feedback.tap(this); startActivity(Intent(this, game.cls)) }
        }
        val remainder = games.size % columns
        if (remainder != 0 && row != null) repeat(columns - remainder) { row?.addView(View(this), LinearLayout.LayoutParams(0, dp(126), 1f)) }
    }

    private fun addPageTitle(target: LinearLayout, title: String) {
        target.addView(TextView(this).apply { text = title; textSize = 23f; setTextColor(-1); setTypeface(typeface, 1); gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(12)) })
    }
    private fun addEmptyMessage(target: LinearLayout, message: String) {
        target.addView(TextView(this).apply { text = message; textSize = 15f; setTextColor(0xFFEAF4FF.toInt()); gravity = Gravity.CENTER; setPadding(dp(18), dp(18), dp(18), dp(18)); background = gradient(0xFF0752A8.toInt(), 0xFF063B7D.toInt(), 18) })
    }
    private fun gradient(a: Int, b: Int, radius: Int) = android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(a, b)).apply { cornerRadius = dp(radius).toFloat(); setStroke(dp(1), 0x66FFFFFF) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
