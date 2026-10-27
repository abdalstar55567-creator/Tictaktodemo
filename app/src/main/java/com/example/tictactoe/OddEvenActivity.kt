package com.example.tictactoe

import android.os.Bundle

class OddEvenActivity : QuickChallengeActivity() {
    override val mode: String get() = "parity"
    override val titleArabic: String get() = "زوجي أم فردي"
    override val emoji: String get() = "🔢"
}
