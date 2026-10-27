package com.example.tictactoe

import android.os.Bundle

class CapitalQuizActivity : QuickChallengeActivity() {
    override val mode: String get() = "capital"
    override val titleArabic: String get() = "تحدي العواصم"
    override val emoji: String get() = "🌍"
}
