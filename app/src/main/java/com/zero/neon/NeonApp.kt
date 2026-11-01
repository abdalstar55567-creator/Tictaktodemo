package com.zero.neon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.zero.neon.game.GameScreen
import com.zero.neon.splash.SplashScreen

/**
 * يحل محل NavHost الأصلي: شاشة افتتاحية ثم اللعبة.
 * إعادة اللعب تتم بتغيير [session] فتُبنى حالة اللعبة من جديد.
 */
@Composable
fun NeonApp(onExit: () -> Unit) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var session by rememberSaveable { mutableStateOf(0) }

    if (showSplash) {
        SplashScreen { showSplash = false }
    } else {
        key(session) {
            GameScreen(
                onRestart = { session += 1 },
                onExit = onExit
            )
        }
    }
}
