package com.example.tictactoe

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

/** موسيقى خلفية أصلية منخفضة الصوت، تتغير حسب تصنيف اللعبة. */
object GameMusic {
    private const val PREFS = "100plus_music"
    private const val KEY_ENABLED = "music_enabled"
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var currentTrack: Int = 0

    fun enabled(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, enabled).apply()
        if (!enabled) stopNow()
    }

    fun playFor(context: Context, activityName: String) {
        handler.removeCallbacksAndMessages(null)
        if (!enabled(context)) { stopNow(); return }
        val track = trackFor(activityName)
        if (currentTrack == track && player?.isPlaying == true) return
        stopNow()
        try {
            player = MediaPlayer.create(context.applicationContext, track)?.apply {
                isLooping = true
                setVolume(0.22f, 0.22f)
                start()
            }
            currentTrack = track
        } catch (_: Exception) { stopNow() }
    }

    fun stopWhenBackgrounded() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ stopNow() }, 900)
    }

    fun stopNow() {
        handler.removeCallbacksAndMessages(null)
        try { player?.stop() } catch (_: Exception) { }
        try { player?.release() } catch (_: Exception) { }
        player = null
        currentTrack = 0
    }

    private fun trackFor(name: String): Int = when (name) {
        in ACTION -> R.raw.bg_action
        in PUZZLES -> R.raw.bg_puzzle
        in FOCUS -> R.raw.bg_focus
        in QUIZ -> R.raw.bg_quiz
        in CHALLENGE -> R.raw.bg_challenge
        else -> R.raw.bg_classic
    }

    private val ACTION = setOf("SprintRunActivity", "HurdleRunActivity", "FlappyActivity", "FrogHopActivity", "CatchActivity", "DodgeActivity", "CarRaceActivity", "BikeRaceActivity", "MeteorShootingActivity", "GhostMazeActivity", "SpaceShooterActivity", "MazeEscapeActivity", "TargetTapActivity", "GemCollectorActivity", "MeteorDodgeActivity", "JumpRunnerActivity", "BallBounceActivity", "TreasureHuntActivity", "CrosswalkActivity", "BeeFlowerActivity", "ArcheryActivity", "FixedGoalFootballActivity", "DuckHuntActivity", "CatapultActivity", "NeonGameActivity", "MagnetActivity", "PongSoloActivity", "BasketShotActivity", "MiniGolfActivity", "BrickActivity", "WhackActivity", "StopBarActivity", "BubblePopActivity")
    private val PUZZLES = setOf("PuzzleActivity", "MinesweeperActivity", "LightsOutActivity", "TowerActivity", "Game2048Activity", "SokobanActivity", "HanoiActivity", "MastermindActivity", "NimStrategyActivity", "PegSolitaireActivity", "RiverCrossingActivity", "NonogramActivity", "PipeConnectActivity", "SudokuActivity", "PicturePuzzleActivity", "TetrisActivity", "AndroidLauncher")
    private val FOCUS = setOf("MemoryActivity", "NumberMemoryActivity", "SequenceActivity", "OddOneActivity", "NumberOrderActivity", "ColorMatchActivity", "ColorQuizActivity", "SimonActivity", "MemoryPairsPlusActivity", "EmojiCountActivity", "ColorWordActivity", "BiggerNumberActivity", "MathCheckActivity", "MakeTenActivity", "MissingLetterActivity", "AntonymsActivity", "MemoryEmojiActivity", "CoinCountActivity", "ClockReadActivity", "OddWordActivity", "RealisticDifferencesActivity", "BalanceScaleActivity", "NumberLadderActivity", "LetterColoringActivity", "EyesightChallengeActivity")
    private val QUIZ = setOf("TriviaActivity", "CapitalQuizActivity", "AnimalQuizActivity", "TrueFalseActivity", "HigherLowerActivity", "WeatherQuizActivity", "GuessNumberActivity", "HangmanActivity", "WordScrambleActivity", "ScrambleActivity", "QuickMathActivity", "MathActivity")
    private val CHALLENGE = setOf("TapSpeedActivity", "ReactionActivity", "WordSprintActivity", "QuickChallengeActivity", "OddEvenActivity")
}
