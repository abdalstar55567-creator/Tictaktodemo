package com.example.tictactoe

import android.os.Bundle

class SequenceActivity : QuickChallengeActivity() {
    override val mode: String get() = "sequence"
    override val titleArabic: String get() = "أكمل المتتالية"
    override val emoji: String get() = "🧠"
}
