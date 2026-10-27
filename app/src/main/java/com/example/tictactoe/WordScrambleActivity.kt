package com.example.tictactoe

import android.os.Bundle

class WordScrambleActivity : QuickChallengeActivity() {
    override val mode: String get() = "word"
    override val titleArabic: String get() = "خلط الكلمات"
    override val emoji: String get() = "🔤"
}
