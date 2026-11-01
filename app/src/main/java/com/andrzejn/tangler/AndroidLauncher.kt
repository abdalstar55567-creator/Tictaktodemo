@file:androidx.compose.runtime.NoLiveLiterals
package com.andrzejn.tangler

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.example.tictactoe.GameMusic

/**
 * Android entry point for Tangler, embedded as a separate game screen in 100+.
 * Original game by Andrzej Novosiolov; see THIRD_PARTY_NOTICES.md.
 */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useWakelock = false
        }
        initialize(Main(), config)
    }

    override fun onResume() {
        super.onResume()
        GameMusic.playFor(this, "AndroidLauncher")
    }

    override fun onPause() {
        GameMusic.stopWhenBackgrounded()
        super.onPause()
    }

    override fun onDestroy() {
        GameMusic.stopNow()
        super.onDestroy()
    }
}
