package com.example.tictactoe

import android.os.Bundle

class QuickMathActivity : QuickChallengeActivity() {
    override val mode: String get() = "math"
    override val titleArabic: String get() = "الحساب السريع"
    override val emoji: String get() = "⚡"
}
