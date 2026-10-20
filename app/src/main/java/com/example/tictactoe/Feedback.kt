package com.example.tictactoe

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/** أصوات واهتزاز بسيطة بدون أي ملفات صوتية. */
object Feedback {

    private const val PREFS = "xozoom"
    private const val KEY_SOUND = "sound"

    private var tone: ToneGenerator? = null

    fun enabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SOUND, true)

    fun setEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SOUND, on).apply()
    }

    private fun beep(type: Int, ms: Int) {
        try {
            if (tone == null) tone = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            tone?.startTone(type, ms)
        } catch (e: Exception) {
            tone = null
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate(ctx: Context, ms: Long) {
        try {
            val v = ctx.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(ms)
            }
        } catch (e: Exception) {
            // تجاهل أي خطأ في الاهتزاز
        }
    }

    fun tap(ctx: Context) {
        if (!enabled(ctx)) return
        beep(ToneGenerator.TONE_PROP_BEEP, 80)
        vibrate(ctx, 20)
    }

    fun match(ctx: Context) {
        if (!enabled(ctx)) return
        beep(ToneGenerator.TONE_PROP_PROMPT, 150)
        vibrate(ctx, 60)
    }

    fun win(ctx: Context) {
        if (!enabled(ctx)) return
        beep(ToneGenerator.TONE_PROP_ACK, 300)
        vibrate(ctx, 250)
    }

    fun lose(ctx: Context) {
        if (!enabled(ctx)) return
        beep(ToneGenerator.TONE_PROP_NACK, 300)
        vibrate(ctx, 150)
    }

    fun draw(ctx: Context) {
        if (!enabled(ctx)) return
        beep(ToneGenerator.TONE_PROP_BEEP2, 200)
        vibrate(ctx, 80)
    }
}
