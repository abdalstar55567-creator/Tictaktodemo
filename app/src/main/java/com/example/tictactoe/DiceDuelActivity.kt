package com.example.tictactoe

import android.os.Bundle

class DiceDuelActivity : QuickChallengeActivity() {
    override val mode: String get() = "dice"
    override val titleArabic: String get() = "مبارزة النرد"
    override val emoji: String get() = "🎲"
}
