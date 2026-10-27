package com.example.tictactoe

import android.os.Bundle

class TrueFalseActivity : QuickChallengeActivity() {
    override val mode: String get() = "truefalse"
    override val titleArabic: String get() = "صح أم خطأ"
    override val emoji: String get() = "🤔"
}
