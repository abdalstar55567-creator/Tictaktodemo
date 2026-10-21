package com.example.tictactoe

data class GameInfo(val emoji: String, val title: String, val cls: Class<*>)

/**
 * قائمة الألعاب. لإضافة لعبة جديدة: أنشئ Activity جديدة ترث BaseGameActivity،
 * وسجّلها في AndroidManifest.xml، ثم أضف سطراً هنا.
 */
object Games {
    val all: List<GameInfo> = listOf(
        GameInfo("❌⭕", "إكس أو", MainActivity::class.java),
        GameInfo("🧠", "لعبة الذاكرة", MemoryActivity::class.java),
        GameInfo("🔢", "تخمين الرقم", GuessNumberActivity::class.java),
        GameInfo("✊✋✌️", "حجر ورقة مقص", RpsActivity::class.java),
        GameInfo("➕", "تحدي الحساب", MathActivity::class.java),
        GameInfo("🐹", "اضرب الخلد", WhackActivity::class.java),
        GameInfo("👆", "سرعة الضغط", TapSpeedActivity::class.java),
        GameInfo("⚡", "سرعة رد الفعل", ReactionActivity::class.java),
        GameInfo("🧩", "لغز الأرقام", PuzzleActivity::class.java),
        GameInfo("🎲", "رمي النرد", DiceActivity::class.java),
        GameInfo("🎨", "تحدي الألوان", ColorMatchActivity::class.java),
        GameInfo("🐍", "الثعبان", SnakeActivity::class.java),
        GameInfo("🔴", "لعبة سيمون", SimonActivity::class.java)
    )
}
