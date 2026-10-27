package com.example.tictactoe

import android.os.Bundle

class ColorQuizActivity : QuickChallengeActivity() {
    override val mode: String get() = "color"
    override val titleArabic: String get() = "اختبار الألوان"
    override val emoji: String get() = "🎨"
}
