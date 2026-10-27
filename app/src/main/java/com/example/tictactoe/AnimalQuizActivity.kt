package com.example.tictactoe

import android.os.Bundle

class AnimalQuizActivity : QuickChallengeActivity() {
    override val mode: String get() = "animal"
    override val titleArabic: String get() = "أصوات الحيوانات"
    override val emoji: String get() = "🐾"
}
