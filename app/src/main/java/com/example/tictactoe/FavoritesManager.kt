package com.example.tictactoe

import android.content.Context

object FavoritesManager {
    private const val PREFS = "user_game_preferences"
    private const val KEY = "favorite_games"

    fun contains(context: Context, className: String): Boolean =
        preferences(context).getStringSet(KEY, emptySet())?.contains(className) == true

    fun toggle(context: Context, className: String): Boolean {
        val values = preferences(context).getStringSet(KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val added = if (values.contains(className)) { values.remove(className); false }
                    else { values.add(className); true }
        preferences(context).edit().putStringSet(KEY, values).apply()
        return added
    }

    fun all(context: Context, games: List<GameInfo>): List<GameInfo> =
        games.filter { contains(context, it.cls.name) }

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
