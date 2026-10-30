package com.example.tictactoe

import android.content.Context

/**
 * يحفظ إحصاءات استخدام الألعاب محلياً على الجهاز.
 * لا يحتاج إلى إنترنت أو قاعدة بيانات أو مكتبات إضافية.
 */
object GameUsageManager {

    private const val PREFS_NAME = "game_usage_stats"
    private const val KEY_PREFIX = "game."
    private const val COUNT_SUFFIX = ".count"
    private const val LAST_PLAYED_SUFFIX = ".last_played"

    data class Usage(
        val playCount: Int,
        val lastPlayed: Long
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** يسجل زيارة واحدة عند إنشاء شاشة اللعبة لأول مرة في دورة فتحها. */
    fun recordGameOpened(context: Context, activityClassName: String) {
        val p = prefs(context)
        val countKey = KEY_PREFIX + activityClassName + COUNT_SUFFIX
        val lastKey = KEY_PREFIX + activityClassName + LAST_PLAYED_SUFFIX

        p.edit()
            .putInt(countKey, p.getInt(countKey, 0) + 1)
            .putLong(lastKey, System.currentTimeMillis())
            .apply()
    }

    fun usage(context: Context, activityClassName: String): Usage {
        val p = prefs(context)
        val prefix = KEY_PREFIX + activityClassName
        return Usage(
            playCount = p.getInt(prefix + COUNT_SUFFIX, 0),
            lastPlayed = p.getLong(prefix + LAST_PLAYED_SUFFIX, 0L)
        )
    }

    /** الأعلى زيارة أولاً، وعند التعادل تكون الأحدث استخداماً أولاً. */
    fun mostPlayed(context: Context, games: List<GameInfo>, limit: Int): List<GameInfo> {
        return games
            .filter { usage(context, it.cls.name).playCount > 0 }
            .sortedWith(
                compareByDescending<GameInfo> { usage(context, it.cls.name).playCount }
                    .thenByDescending { usage(context, it.cls.name).lastPlayed }
            )
            .take(limit)
    }

    /** الأحدث استخداماً أولاً. الألعاب التي لم تستخدم تأتي بعد المستخدمة. */
    fun recentlyPlayed(context: Context, games: List<GameInfo>): List<GameInfo> {
        return games.sortedWith(
            compareByDescending<GameInfo> { usage(context, it.cls.name).lastPlayed }
        )
    }
}
