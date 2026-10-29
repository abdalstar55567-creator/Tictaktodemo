package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/** يُبقي الأرقام والمعادلات من اليسار إلى اليمين داخل النص العربي. */
private fun ltrText(text: String): String = "‪$text‬"

/** عدّ العملات: اجمع قيمة العملات الظاهرة واختر المجموع الصحيح. */
class CoinCountActivity : ChoiceGameActivity() {
    private data class Coin(val icon: String, val value: Int)
    private val coins = listOf(
        Coin("🟡", 5), Coin("🟠", 10), Coin("🔵", 25), Coin("🟣", 50)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🪙 عدّ العملات", "اجمع قيمة العملات. 🟡=5 🟠=10 🔵=25 🟣=50")
    }

    override fun makeRound(): ChoiceRound {
        val n = Random.nextInt(3, 6)
        val picked = List(n) { coins.random() }
        val total = picked.sumOf { it.value }
        val wrong = listOf(-15, -10, -5, 5, 10, 15, 25)
            .map { total + it }
            .filter { it > 0 && it != total }
            .distinct().shuffled().take(3)
        val options = (wrong + total).shuffled().map { it.toString() }
        return ChoiceRound(
            "ما مجموع قيمة العملات؟\n" + picked.joinToString("  ") { it.icon },
            options, total.toString(), 30f
        )
    }
}

/** عبور المشاة: عبر فقط عندما تكون الإشارة خضراء. */
class CrosswalkActivity : MoreGameActivity() {
    private lateinit var light: TextView
    private var current = "🔴"
    private val totalRounds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("🚦 عبور المشاة", "عبر الطريق عندما تكون الإشارة خضراء فقط. أمامك 10 إشارات.")
        light = label("🔴", 64f)
        content.addView(light, lp(top = dp(12), bottom = dp(12)))
        val line = row()
        line.addView(button("⛔ توقف", false) { decide(false) }, weightLp(dp(60)))
        line.addView(button("🚶 اذهب", false) { decide(true) }, weightLp(dp(60)))
        content.addView(line)
        content.addView(button("لعبة جديدة") { newGame() }, lp(top = dp(10)))
        newGame()
    }

    private fun newGame() {
        resetScore()
        nextLight()
    }

    private fun nextLight() {
        if (rounds >= totalRounds) {
            ended = true
            updateStatus(if (points >= 7) "عبرت بأمان! 🎉" else "انتهت الجولة — انتبه للإشارات")
            return
        }
        current = listOf("🔴", "🟡", "🟢")[Random.nextInt(3)]
        light.text = current
        updateStatus("الإشارة رقم ${rounds + 1} من $totalRounds")
    }

    private fun decide(go: Boolean) {
        if (ended) return
        rounds++
        val shouldGo = current == "🟢"
        if (go == shouldGo) points++ else points = (points - 1).coerceAtLeast(0)
        updateStatus(
            if (go == shouldGo) "قرار صحيح ✅"
            else if (shouldGo) "كان عليك العبور! الإشارة خضراء"
            else "خطير! الإشارة ليست خضراء"
        )
        nextLight()
    }
}

/** المغناطيس: قطبان متشابهان يتنافران ومتعاكسان يتجاذبان. */
class MagnetActivity : ChoiceGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🧲 المغناطيس", "قطبان متقابلان: المتشابهان يتنافران والمتعاكسان يتجاذبان.")
    }

    override fun makeRound(): ChoiceRound {
        val a = if (Random.nextBoolean()) "شمال (N)" else "جنوب (S)"
        val b = if (Random.nextBoolean()) "شمال (N)" else "جنوب (S)"
        val same = a == b
        val attract = "تجاذب 🧲"
        val repel = "تنافر 💥"
        return ChoiceRound(
            "قطبان متقابلان:\nالأول: $a\nالثاني: $b\nماذا يحدث؟",
            listOf(attract, repel),
            if (same) repel else attract,
            24f
        )
    }
}

/** قراءة الساعة: اقرأ الوصف واختر الوقت الصحيح. */
class ClockReadActivity : ChoiceGameActivity() {
    private val arabic = listOf(
        "", "واحدة", "اثنتين", "ثلاث", "أربع", "خمس",
        "ست", "سبع", "ثمان", "تسع", "عشر", "أحد عشر", "اثنتي عشر"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🕐 قراءة الساعة", "اقرأ الوصف واختر الوقت الصحيح على الساعة.")
    }

    override fun makeRound(): ChoiceRound {
        val h = Random.nextInt(1, 12)
        val kind = Random.nextInt(5)
        val m = when (kind) {
            0 -> 0
            1 -> 5
            2 -> 10
            3 -> 25
            else -> 55
        }
        val prompt = when (kind) {
            0 -> "الساعة ${arabic[h]} بالضبط"
            1 -> "خمس دقائق بعد ${arabic[h]}"
            2 -> "عشر دقائق بعد ${arabic[h]}"
            3 -> "خمسة وعشرون دقيقة بعد ${arabic[h]}"
            else -> "خمس دقائق قبل ${arabic[(h % 12) + 1]}"
        }
        val correct = ltrText(String.format("%d:%02d", h, m))
        val wrong = mutableListOf<String>()
        for (mm in listOf(0, 15, 30, 45, 50, 55)) {
            if (mm != m) wrong.add(ltrText(String.format("%d:%02d", h, mm)))
        }
        wrong.add(ltrText(String.format("%d:%02d", (h % 12) + 1, m)))
        val options = (wrong.shuffled().take(3) + correct).shuffled()
        return ChoiceRound(prompt, options, correct, 26f)
    }
}

/** الغريب عن المجموعة: اختر الكلمة التي لا تنتمي إلى مجموعتها. */
class OddWordActivity : ChoiceGameActivity() {
    private val sets = listOf(
        listOf("تفاح", "موز", "عنب", "برتقال", "كرسي"),
        listOf("قطة", "كلب", "أسد", "حصان", "شجرة"),
        listOf("شمس", "قمر", "نجمة", "كوكب", "بحر"),
        listOf("سيارة", "حافلة", "قطار", "شاحنة", "مقص"),
        listOf("كتاب", "قلم", "دفتر", "مسطرة", "تفاحة"),
        listOf("أحمر", "أزرق", "أخضر", "أصفر", "موز"),
        listOf("يد", "قدم", "رأس", "عين", "مفتاح"),
        listOf("بيت", "قصر", "خيمة", "شقة", "ساعة")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🎪 الغريب عن المجموعة", "اختر الكلمة التي لا تنتمي إلى مجموعتها.")
    }

    override fun makeRound(): ChoiceRound {
        val s = sets.random()
        val odd = s.last()
        val options = (s.dropLast(1).shuffled().take(3) + odd).shuffled()
        return ChoiceRound("أي كلمة غريبة عن المجموعة؟", options, odd, 26f)
    }
}

/** النحلة والزهور: قُد النحلة إلى الزهرة المطلوبة بالضبط. */
class BeeFlowerActivity : MoreGameActivity() {
    private data class Flower(val icon: String, val name: String)
    private val kinds = listOf(
        Flower("🌹", "الحمراء"),
        Flower("🌻", "الصفراء"),
        Flower("🌷", "الوردية"),
        Flower("🌸", "البيضاء")
    )
    private lateinit var grid: LinearLayout
    private var targetName = ""
    private var gridFlowers: List<Flower> = emptyList()
    private val totalRounds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("🐝 النحلة والزهور", "قُد النحلة إلى الزهرة المطلوبة بالضبط. 10 جولات.")
        grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(grid, lp())
        content.addView(button("لعبة جديدة") { newGame() }, lp(top = dp(8)))
        newGame()
    }

    private fun newGame() {
        resetScore()
        nextRound()
    }

    private fun nextRound() {
        if (rounds >= totalRounds) {
            ended = true
            updateStatus(if (points >= 7) "وصلت النحلة إلى منزلها! 🎉" else "انتهت الجولة")
            return
        }
        gridFlowers = List(9) { kinds.random() }
        val target = gridFlowers.distinct().random()
        targetName = target.name
        draw()
        updateStatus("قُد النحلة إلى الزهرة $targetName (الجولة ${rounds + 1} من $totalRounds)")
    }

    private fun draw() {
        grid.removeAllViews()
        for (r in 0..2) {
            val line = row(4)
            for (c in 0..2) {
                val f = gridFlowers[r * 3 + c]
                line.addView(button(f.icon, false) { tap(f) }, weightLp(dp(66)))
            }
            grid.addView(line)
        }
    }

    private fun tap(f: Flower) {
        if (ended) return
        rounds++
        if (f.name == targetName) points++ else points = (points - 1).coerceAtLeast(0)
        updateStatus(if (f.name == targetName) "رائع! زهرة $targetName 🌸" else "ليست هي — المطلوبة $targetName")
        nextRound()
    }
}

/** الميزان: قارن الأوزان واختر الأجسام الأثقل. */
class BalanceScaleActivity : ChoiceGameActivity() {
    private data class Item(val icon: String, val name: String, val kg: Int)
    private val items = listOf(
        Item("🪶", "ريشة", 1),
        Item("🍌", "موزة", 2),
        Item("🍎", "تفاحة", 3),
        Item("🧸", "دب قماشي", 5),
        Item("🪨", "صخرة", 8),
        Item("🍉", "بطيخة", 12),
        Item("🛢️", "برميل زيت", 40),
        Item("🚗", "سيارة", 1200),
        Item("🐘", "فيل", 5000)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("⚖️ الميزان", "قارن الأوزان ثم اختر الأجسام الأثقل.")
    }

    override fun makeRound(): ChoiceRound {
        val pair = items.shuffled().take(2)
        val a = pair[0]
        val b = pair[1]
        val heavier = if (a.kg > b.kg) a else b
        val first = if (Random.nextBoolean()) a else b
        val second = if (first == a) b else a
        val prompt =
            "أيّهما أثقل؟\n" +
            ltrText("${first.icon} ${first.name} (${first.kg} كغ)") + "\n" +
            ltrText("${second.icon} ${second.name} (${second.kg} كغ)")
        return ChoiceRound(
            prompt,
            listOf("${first.icon} ${first.name}", "${second.icon} ${second.name}"),
            "${heavier.icon} ${heavier.name}",
            24f
        )
    }
}

/** تحدّي الطقس: اقرأ الوصف واختر رمز الطقس الصحيح. */
class WeatherQuizActivity : ChoiceGameActivity() {
    private val pairs = listOf(
        "السماء صافية والشمس دافئة" to "☀️",
        "تسقط قطرات ماء من الغيوم" to "🌧️",
        "السماء ملبدة بالغيوم" to "☁️",
        "تسقط حبات من الجليد" to "🌨️",
        "البرق يضيء والرياح تعوي" to "⛈️",
        "الشمس تلمع خلف الغيوم" to "⛅",
        "الثلج يغطي الأرض بيضاء" to "❄️",
        "قوس ملون في السماء بعد المطر" to "🌈"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🌦️ تحدّي الطقس", "اقرأ الوصف واختر رمز الطقس الصحيح.")
    }

    override fun makeRound(): ChoiceRound {
        val p = pairs.random()
        val others = pairs.filter { it != p }.map { it.second }.shuffled().take(3)
        return ChoiceRound(p.first, (others + p.second).shuffled(), p.second, 26f)
    }
}

/** الرماية: اضغط على الجهة التي يظهر فيها الهدف. */
class ArcheryActivity : MoreGameActivity() {
    private lateinit var scene: TextView
    private var pos = 1
    private val totalRounds = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("🏹 الرماية", "اضغط على الجهة التي يظهر فيها الهدف. لديك 10 أسهم.")
        scene = label("", 40f)
        content.addView(scene, lp(top = dp(12), bottom = dp(12)))
        val line = row()
        line.addView(button("⬅ يسار", false) { shoot(0) }, weightLp(dp(60)))
        line.addView(button("وسط", false) { shoot(1) }, weightLp(dp(60)))
        line.addView(button("يمين ➡", false) { shoot(2) }, weightLp(dp(60)))
        content.addView(line)
        content.addView(button("جولة جديدة") { newGame() }, lp(top = dp(10)))
        newGame()
    }

    private fun newGame() {
        resetScore()
        nextShot()
    }

    private fun nextShot() {
        if (rounds >= totalRounds) {
            ended = true
            updateStatus(if (points >= 7) "رامي ماهر! 🎯" else "انتهت الأسهم — تدرب أكثر")
            return
        }
        pos = Random.nextInt(3)
        scene.text = when (pos) {
            0 -> "🎯  ⚪  ⚪"
            1 -> "⚪  🎯  ⚪"
            else -> "⚪  ⚪  🎯"
        }
        updateStatus("السهم ${rounds + 1} من $totalRounds")
    }

    private fun shoot(p: Int) {
        if (ended) return
        rounds++
        if (p == pos) points++ else points = (points - 1).coerceAtLeast(0)
        updateStatus(if (p == pos) "إصابة مباشرة! 🎯" else "فاتك الهدف")
        nextShot()
    }
}

/** سلّم الأرقام: أجب عن السؤال لتصعد درجاً حتى تصل القمة. */
class NumberLadderActivity : MoreGameActivity() {
    private val rungs = 8
    private var rung = 0
    private lateinit var ladderView: TextView
    private lateinit var area: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("🪜 سلّم الأرقام", "أجب عن السؤال لتصعد درجاً. اجمع الثمانية درجات لتصل القمة.")
        ladderView = label("", 20f)
        content.addView(ladderView, lp(top = dp(4), bottom = dp(8)))
        area = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(area, lp())
        content.addView(button("ابدأ من جديد") { newGame() }, lp(top = dp(8)))
        newGame()
    }

    private fun newGame() {
        handler.removeCallbacksAndMessages(null)
        resetScore()
        rung = 0
        drawLadder()
        showStep()
    }

    private fun drawLadder() {
        val s = StringBuilder()
        for (i in rungs downTo 1) {
            s.append(if (i <= rung) "🟢" else "⚪").append(" ")
        }
        ladderView.text = ltrText(s.toString())
    }

    private fun showStep() {
        area.removeAllViews()
        if (rung >= rungs) {
            ended = true
            updateStatus("وصلت إلى القمة! 🎉 صعدت $rungs درجاً")
            return
        }
        val plus = Random.nextBoolean()
        val a = Random.nextInt(5, 20)
        val b = if (plus) Random.nextInt(1, 15) else Random.nextInt(1, a)
        val ans = if (plus) a + b else a - b
        val wrong = listOf(a + b, a - b, a + 1, b + 1, ans + 2, ans - 2)
            .filter { it != ans && it >= 0 }
            .distinct().shuffled().take(3)
        val options = (wrong + ans).shuffled().map { it.toString() }
        area.addView(
            label(if (plus) ltrText("$a + $b = ?") else ltrText("$a - $b = ?"), 30f),
            lp(top = dp(8), bottom = dp(8))
        )
        options.forEach { o ->
            area.addView(button(o, false) { answer(ans, o) }, lp(height = dp(52), top = dp(6)))
        }
    }

    private fun answer(ans: Int, o: String) {
        if (ended) return
        if (o == ans.toString()) {
            rung++
            points++
            updateStatus("درج $rung من $rungs ⬆")
        } else {
            points = (points - 1).coerceAtLeast(0)
            updateStatus("إجابة خاطئة — الصحيح $ans")
        }
        drawLadder()
        showStep()
    }
}
