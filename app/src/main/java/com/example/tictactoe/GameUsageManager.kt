package com.example.tictactoe

import android.content.Context

object GameUsageManager {

    private const val PREFS_NAME = "game_usage_stats"

    data class Usage(
        val playCount: Int,
        val lastPlayed: Long
    )

    private fun preferences(context: Context) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private fun countKey(className: String) =
        "count_$className"

    private fun timeKey(className: String) =
        "time_$className"

    // تسجيل زيارة اللعبة
    fun recordGameOpened(
        context: Context,
        className: String
    ) {
        val prefs = preferences(context)

        val oldCount = prefs.getInt(
            countKey(className),
            0
        )

        prefs.edit()
            .putInt(countKey(className), oldCount + 1)
            .putLong(timeKey(className), System.currentTimeMillis())
            .apply()
    }

    // قراءة إحصائيات لعبة
    fun usage(
        context: Context,
        className: String
    ): Usage {
        val prefs = preferences(context)

        return Usage(
            playCount = prefs.getInt(countKey(className), 0),
            lastPlayed = prefs.getLong(timeKey(className), 0L)
        )
    }

    // الألعاب الأكثر زيارة
    fun mostPlayed(
        context: Context,
        games: List<GameInfo>,
        limit: Int = 3
    ): List<GameInfo> {

        return games
            .filter {
                usage(context, it.cls.name).playCount > 0
            }
            .sortedWith(
                compareByDescending<GameInfo> {
                    usage(context, it.cls.name).playCount
                }.thenByDescending {
                    usage(context, it.cls.name).lastPlayed
                }
            )
            .take(limit)
    }

    // الألعاب التي لعبها المستخدم مؤخراً
    fun recentlyPlayed(
        context: Context,
        games: List<GameInfo>,
        limit: Int = 5
    ): List<GameInfo> {

        return games
            .filter {
                usage(context, it.cls.name).lastPlayed > 0
            }
            .sortedByDescending {
                usage(context, it.cls.name).lastPlayed
            }
            .take(limit)
    }
}
