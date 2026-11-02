package com.zero.neon

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.tictactoe.GameMusic
import com.example.tictactoe.GameUsageManager
import com.zero.neon.common.theme.NeonTheme

/**
 * نقطة دخول لعبة Neon داخل تطبيق 100+.
 * اللعبة الأصلية: https://github.com/mariodujic/Neon  (MIT, © 2022 Mario Dujić)
 * التعديلات موثّقة في THIRD_PARTY_NOTICES.md
 */
class NeonGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (savedInstanceState == null) {
            GameUsageManager.recordGameOpened(this, javaClass.name)
        }

        setContent {
            NeonTheme {
                NeonApp(onExit = { finish() })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GameMusic.playFor(this, "NeonGameActivity")
    }

    override fun onStop() {
        super.onStop()
        GameMusic.stopWhenBackgrounded()
    }

    override fun onDestroy() {
        GameMusic.stopNow()
        super.onDestroy()
    }
}
