package com.example.tictactoe

import android.os.Bundle

class CoinFlipActivity : QuickChallengeActivity() {
    override val mode: String get() = "coin"
    override val titleArabic: String get() = "توقّع العملة"
    override val emoji: String get() = "🪙"
}
