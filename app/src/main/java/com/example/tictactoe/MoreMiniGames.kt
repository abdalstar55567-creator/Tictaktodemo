package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

/** يُبقي الأرقام والمعادلات من اليسار إلى اليمين داخل النص العربي. */
private fun ltrText(text: String): String = "‪$text‬"

/** جولة واحدة في الألعاب ذات الاختيارات. */
data class ChoiceRound(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val promptSize: Float = 32f,
    val promptColor: Int? = null,
    /** إن وُجد يُعرض لثانيتين ثم يختفي ويظهر السؤال. */
    val memorize: String? = null
)

/** أساس الألعاب الجديدة: نفس نظام النقاط الموجود في ExtraMiniGameActivity مع مؤقّت. */
abstract class MoreGameActivity : ExtraMiniGameActivity() {
    protected val handler = Handler(Looper.getMainLooper())

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

/** لعبة أسئلة باختيار الإجابة، عشر جولات في كل مرة. */
abstract class ChoiceGameActivity : MoreGameActivity() {
    private lateinit var area: LinearLayout
    protected open val totalRounds = 10

    protected abstract fun makeRound(): ChoiceRound

    protected fun startChoiceGame(title: String, hint: String) {
        prepare(title, hint)
        area = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(area, lp())
        content.addView(button("ابدأ من جديد") { restart() }, lp(top = dp(8)))
        restart()
    }

    private fun restart() {
        handler.removeCallbacksAndMessages(null)
        resetScore()
        nextRound()
    }

    private fun nextRound() {
        area.removeAllViews()
        if (rounds >= totalRounds) {
            ended = true
            area.addView(
                label("انتهت اللعبة 🎉\nنتيجتك: $points من $totalRounds", 22f),
                lp(top = dp(16))
            )
            return
        }
        val round = makeRound()
        val memo = round.memorize
        if (memo != null) {
            area.addView(label(memo, round.promptSize), lp(top = dp(8), bottom = dp(8)))
            handler.postDelayed({ showOptions(round) }, 2200L)
        } else {
            showOptions(round)
        }
    }

    private fun showOptions(round: ChoiceRound) {
        area.removeAllViews()
        val promptView = label(round.prompt, round.promptSize)
        round.promptColor?.let { promptView.setTextColor(it) }
        area.addView(promptView, lp(top = dp(8), bottom = dp(8)))
        round.options.forEach { option ->
            area.addView(
                button(option, false) { choose(round, option) },
                lp(height = dp(56), top = dp(8))
            )
        }
    }

    private fun choose(round: ChoiceRound, option: String) {
        if (ended) return
        rounds++
        val ok = option == round.answer
        if (ok) points++
        updateStatus(if (ok) "إجابة صحيحة ✅" else "إجابة خاطئة ❌ — الصحيح: ${round.answer}")
        nextRound()
    }
}

class EmojiCountActivity : ChoiceGameActivity() {
    private val pool = listOf("🍎", "🐱", "⭐", "🚗", "🎈", "🐟", "🌸", "⚽")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🔢 عدّ الرموز", "عدّ الرموز الظاهرة واختر العدد الصحيح. عشر جولات.")
    }

    override fun makeRound(): ChoiceRound {
        val n = Random.nextInt(3, 10)
        val icon = pool.random()
        val wrong = listOf(n - 2, n - 1, n + 1, n + 2).filter { it > 0 }.shuffled().take(2)
        val options = (wrong + n).shuffled().map { it.toString() }
        return ChoiceRound(List(n) { icon }.joinToString(" "), options, n.toString(), 30f)
    }
}

class ColorWordActivity : ChoiceGameActivity() {
    private val names = listOf("أحمر", "أزرق", "أخضر", "أصفر")
    private val inks = listOf("#EF4444", "#3B82F6", "#22C55E", "#FACC15").map { Color.parseColor(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🌈 لون الكلمة", "اختر لون الخط الذي كُتبت به الكلمة، وليس معناها!")
    }

    override fun makeRound(): ChoiceRound {
        val word = Random.nextInt(4)
        val ink = if (Random.nextInt(4) == 0) word else (word + Random.nextInt(1, 4)) % 4
        return ChoiceRound(names[word], names.shuffled(), names[ink], 44f, inks[ink])
    }
}

class BiggerNumberActivity : ChoiceGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("⚖️ أيّهما أكبر؟", "اختر الرقم الأكبر بأسرع ما تستطيع.")
    }

    override fun makeRound(): ChoiceRound {
        val a = Random.nextInt(10, 100)
        var b = Random.nextInt(10, 100)
        while (b == a) b = Random.nextInt(10, 100)
        return ChoiceRound("أيّهما أكبر؟", listOf(a.toString(), b.toString()), maxOf(a, b).toString(), 26f)
    }
}

class MathCheckActivity : ChoiceGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("✅ المعادلة الصحيحة", "هل المعادلة المعروضة صحيحة أم خاطئة؟")
    }

    override fun makeRound(): ChoiceRound {
        val a = Random.nextInt(2, 20)
        val b = Random.nextInt(2, 10)
        val op = listOf("+", "-", "×").random()
        val real = when (op) {
            "+" -> a + b
            "-" -> a - b
            else -> a * b
        }
        val isTrue = Random.nextBoolean()
        val shown = if (isTrue) real else real + listOf(-3, -2, -1, 1, 2, 3).random()
        val yes = "صحيحة ✅"
        val no = "خاطئة ❌"
        return ChoiceRound(ltrText("$a $op $b = $shown"), listOf(yes, no), if (isTrue) yes else no, 34f)
    }
}

class MakeTenActivity : ChoiceGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🔟 كمّل للعشرة", "اختر الرقم الذي يكمّل المعادلة ليصبح الناتج 10.")
    }

    override fun makeRound(): ChoiceRound {
        val a = Random.nextInt(1, 10)
        val need = 10 - a
        val wrong = (0..10).filter { it != need }.shuffled().take(3)
        val options = (wrong + need).shuffled().map { it.toString() }
        return ChoiceRound(ltrText("$a + ? = 10"), options, need.toString(), 36f)
    }
}

class MissingLetterActivity : ChoiceGameActivity() {
    private val words = listOf(
        "كتاب", "مدرسة", "سودان", "شمس", "قمر", "نخلة",
        "حديقة", "مفتاح", "برتقال", "ساعة", "دجاجة", "مكتبة"
    )
    private val letters = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("🔤 الحرف الناقص", "اختر الحرف الذي يكمّل الكلمة.")
    }

    override fun makeRound(): ChoiceRound {
        val w = words.random()
        val idx = w.indices.filter { letters.contains(w[it]) }.random()
        val answer = w[idx].toString()
        val wrong = letters.map { it.toString() }.filter { it != answer }.shuffled().take(3)
        val shown = w.substring(0, idx) + "_" + w.substring(idx + 1)
        return ChoiceRound("ما الحرف الناقص؟\n$shown", (wrong + answer).shuffled(), answer, 28f)
    }
}

class AntonymsActivity : ChoiceGameActivity() {
    private val pairs = listOf(
        "كبير" to "صغير", "طويل" to "قصير", "حار" to "بارد", "سريع" to "بطيء",
        "نهار" to "ليل", "قوي" to "ضعيف", "غني" to "فقير", "فرح" to "حزن",
        "جديد" to "قديم", "ثقيل" to "خفيف", "مفتوح" to "مغلق", "قريب" to "بعيد",
        "سهل" to "صعب", "نظيف" to "متسخ", "ممتلئ" to "فارغ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("↔️ عكس الكلمة", "اختر الكلمة المعاكسة في المعنى.")
    }

    override fun makeRound(): ChoiceRound {
        val p = pairs.random()
        val flip = Random.nextBoolean()
        val word = if (flip) p.second else p.first
        val answer = if (flip) p.first else p.second
        val others = pairs.filter { it != p }.flatMap { listOf(it.first, it.second) }.shuffled().take(2)
        return ChoiceRound("ما عكس كلمة «$word»؟", (others + answer).shuffled(), answer, 24f)
    }
}

class MemoryEmojiActivity : ChoiceGameActivity() {
    private val pool = listOf("🍎", "🐱", "⭐", "🚗", "🎈", "🐟", "🌸", "⚽", "🍌", "🐶", "🌙", "🎁")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startChoiceGame("👀 تذكّر الرمز", "احفظ ترتيب الرموز من اليسار إلى اليمين قبل أن تختفي.")
    }

    override fun makeRound(): ChoiceRound {
        val seq = pool.shuffled().take(4)
        val pos = Random.nextInt(4)
        return ChoiceRound(
            prompt = "ما الرمز رقم ${pos + 1} من اليسار؟",
            options = seq.shuffled(),
            answer = seq[pos],
            promptSize = 26f,
            memorize = ltrText(seq.joinToString("   "))
        )
    }
}

class TreasureHuntActivity : MoreGameActivity() {
    private lateinit var grid: LinearLayout
    private var treasures: Set<Int> = emptySet()
    private val opened = mutableSetOf<Int>()
    private var taps = 0
    private val maxTaps = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("💰 صيد الكنز", "تحت المربعات ثلاثة كنوز مخبأة. لديك 8 محاولات فقط.")
        grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(grid, lp())
        content.addView(button("لعبة جديدة") { newGame() }, lp(top = dp(8)))
        newGame()
    }

    private fun newGame() {
        resetScore()
        taps = 0
        opened.clear()
        treasures = (0..15).shuffled().take(3).toSet()
        draw()
        updateStatus("المحاولات المتبقية: $maxTaps")
    }

    private fun draw() {
        grid.removeAllViews()
        for (r in 0..3) {
            val line = row(4)
            for (c in 0..3) {
                val i = r * 4 + c
                val text = if (i in opened) (if (i in treasures) "💰" else "💨") else "❔"
                line.addView(button(text, false) { dig(i) }, weightLp(dp(60)))
            }
            grid.addView(line)
        }
    }

    private fun dig(i: Int) {
        if (ended || i in opened) return
        opened.add(i)
        taps++
        val hit = i in treasures
        if (hit) points += 3
        val found = opened.count { it in treasures }
        val left = maxTaps - taps
        if (found == 3) {
            ended = true
            updateStatus("وجدت كل الكنوز! 🎉")
        } else if (left <= 0) {
            ended = true
            updateStatus("انتهت المحاولات — وجدت $found من 3")
        } else {
            updateStatus(if (hit) "كنز! 💰 المحاولات المتبقية: $left" else "لا شيء هنا. المحاولات المتبقية: $left")
        }
        draw()
    }
}

class LuckyWheelActivity : MoreGameActivity() {
    private data class Slice(val icon: String, val text: String, val value: Int)

    private val slices = listOf(
        Slice("💎", "جوهرة +10", 10), Slice("⭐", "نجمة +5", 5), Slice("🍀", "حظ +3", 3),
        Slice("🍒", "كرز +2", 2), Slice("😅", "لا شيء", 0), Slice("💣", "قنبلة -5", -5)
    )
    private val totalSpins = 6
    private lateinit var wheel: TextView
    private var spinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare("🎡 عجلة الحظ", "أدر العجلة $totalSpins مرات واجمع أكبر عدد من النقاط.")
        wheel = label("🎡", 56f)
        content.addView(wheel, lp(top = dp(16), bottom = dp(16)))
        content.addView(button("أدر العجلة") { spin() }, lp(height = dp(60)))
        content.addView(button("لعبة جديدة", false) {
            handler.removeCallbacksAndMessages(null)
            spinning = false
            resetScore()
            wheel.text = "🎡"
        }, lp(top = dp(8)))
    }

    private fun spin() {
        if (ended || spinning) return
        spinning = true
        val result = slices.random()
        var step = 0
        val tick = object : Runnable {
            override fun run() {
                step++
                if (step <= 10) {
                    wheel.text = slices.random().icon
                    handler.postDelayed(this, 90L)
                } else {
                    wheel.text = result.icon
                    points = (points + result.value).coerceAtLeast(0)
                    rounds++
                    spinning = false
                    if (rounds >= totalSpins) {
                        ended = true
                        updateStatus("${result.text}\nانتهت اللعبة! نتيجتك $points")
                    } else {
                        updateStatus("${result.text} — تبقّت ${totalSpins - rounds} محاولات")
                    }
                }
            }
        }
        handler.post(tick)
    }
}
