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
        GameInfo("🔴", "لعبة سيمون", SimonActivity::class.java),
        GameInfo("🔵", "أربعة في صف", Connect4Activity::class.java),
        GameInfo("🔤", "حبل المشنقة", HangmanActivity::class.java),
        GameInfo("🔢", "لعبة 2048", Game2048Activity::class.java),
        GameInfo("🧩", "رتّب الحروف", ScrambleActivity::class.java),
        GameInfo("❓", "أسئلة عامة", TriviaActivity::class.java),
        GameInfo("⬆⬇", "أعلى أم أقل؟", HigherLowerActivity::class.java),
        GameInfo("🎯", "ابحث عن المختلف", OddOneActivity::class.java),
        GameInfo("🔟", "رتّب الأرقام", NumberOrderActivity::class.java),
        GameInfo("🎁", "حظ الصندوق", LuckBoxActivity::class.java),
        GameInfo("🧠", "ذاكرة الأرقام", NumberMemoryActivity::class.java),
        GameInfo("🏃", "سباق الحواجز", HurdleRunActivity::class.java),
        GameInfo("🏗️", "برج الصناديق", TowerActivity::class.java),
        GameInfo("🐤", "الطائر الطائر", FlappyActivity::class.java),
        GameInfo("🧺", "التقط الفواكه", CatchActivity::class.java),
        GameInfo("🚀", "تفادى القنابل", DodgeActivity::class.java),
        GameInfo("🧱", "كسر الطوب", BrickActivity::class.java),
        GameInfo("💣", "كاسحة الألغام", MinesweeperActivity::class.java),
        GameInfo("💡", "أطفئ الأضواء", LightsOutActivity::class.java),
        GameInfo("🎚️", "أوقف المؤشر", StopBarActivity::class.java)
    )
}
