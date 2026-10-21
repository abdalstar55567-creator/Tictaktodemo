package com.example.tictactoe

import android.content.Context

/** حفظ أفضل النتائج على الجهاز. */
object Scores {

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("xozoom", Context.MODE_PRIVATE)

    fun get(ctx: Context, key: String): Int = prefs(ctx).getInt(key, 0)

    fun put(ctx: Context, key: String, value: Int) {
        prefs(ctx).edit().putInt(key, value).apply()
    }

    /** كلما زاد الرقم كان أفضل. يرجع true إذا كان رقماً قياسياً جديداً. */
    fun saveHigh(ctx: Context, key: String, value: Int): Boolean {
        val old = get(ctx, key)
        if (value > old) {
            prefs(ctx).edit().putInt(key, value).apply()
            return true
        }
        return false
    }

    /** كلما قلّ الرقم كان أفضل. يرجع true إذا كان رقماً قياسياً جديداً. */
    fun saveLow(ctx: Context, key: String, value: Int): Boolean {
        val old = get(ctx, key)
        if (old == 0 || value < old) {
            prefs(ctx).edit().putInt(key, value).apply()
            return true
        }
        return false
    }
}
