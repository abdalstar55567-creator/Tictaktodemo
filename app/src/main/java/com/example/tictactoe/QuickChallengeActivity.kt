package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlin.random.Random

/**
 * محرك صغير مشترك لعشر ألعاب أسئلة سريعة.
 * كل لعبة لها مولّد سؤال وقواعد مختلفة عبر mode.
 */
abstract class QuickChallengeActivity : BaseGameActivity() {
    protected abstract val mode: String
    protected abstract val titleArabic: String
    protected abstract val emoji: String

    private var round = 0
    private var score = 0
    private var correctAnswer = ""
    private var locked = false
    private lateinit var status: TextView
    private lateinit var question: TextView
    private lateinit var feedback: TextView
    private lateinit var choices: List<android.widget.Button>
    private lateinit var replay: android.widget.Button

    private data class Challenge(val prompt: String, val options: List<String>, val answer: String)

    override val gameTitle: String get() = "$emoji $titleArabic"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)
        status = label("", 16f, R.color.text_dim)
        question = label("", 28f, R.color.x_color)
        feedback = label("", 18f)
        content.addView(status, lp(top = dp(10), bottom = dp(8)))
        content.addView(question, lp(top = dp(12), bottom = dp(12)))
        content.addView(feedback, lp(bottom = dp(8)))
        choices = (0..3).map { index ->
            button("...", false) { choose(index) }.also {
                it.textSize = 18f
                content.addView(it, lp(height = dp(56), top = dp(5), bottom = dp(5)))
            }
        }
        replay = button("العب من جديد", true) { startGame() }
        replay.visibility = View.GONE
        content.addView(replay, lp(top = dp(14)))
        startGame()
    }

    private fun startGame() {
        round = 0
        score = 0
        locked = false
        replay.visibility = View.GONE
        choices.forEach { it.visibility = View.VISIBLE }
        nextChallenge()
    }

    private fun nextChallenge() {
        if (round >= 10) {
            choices.forEach { it.visibility = View.GONE }
            replay.visibility = View.VISIBLE
            question.text = "نتيجتك $score من 10"
            status.text = "انتهت اللعبة!"
            feedback.text = if (Scores.saveHigh(this, "quick_${mode}_best", score))
                "🏆 حققت رقماً قياسياً جديداً!" else "أفضل نتيجة: ${Scores.get(this, "quick_${mode}_best")} من 10"
            Feedback.win(this)
            return
        }
        val c = makeChallenge()
        correctAnswer = c.answer
        val shuffled = c.options.shuffled()
        question.text = c.prompt
        status.text = "الجولة ${round + 1} من 10     •     النقاط: $score"
        feedback.text = ""
        choices.forEachIndexed { i, b ->
            b.text = shuffled[i]
            b.isEnabled = true
            b.setBackgroundResource(R.drawable.bg_button_dim)
        }
    }

    private fun choose(index: Int) {
        if (locked || round >= 10) return
        locked = true
        val chosen = choices[index].text.toString()
        choices.forEach { it.isEnabled = false }
        if (chosen == correctAnswer) {
            score++
            feedback.text = "✅ إجابة صحيحة!"
            Feedback.match(this)
        } else {
            feedback.text = "❌ الإجابة الصحيحة: $correctAnswer"
            Feedback.lose(this)
        }
        round++
        status.text = "الجولة $round من 10     •     النقاط: $score"
        question.postDelayed({
            locked = false
            nextChallenge()
        }, 750)
    }

    private fun makeChallenge(): Challenge {
        val c: Challenge = when (mode) {
            "coin" -> {
                val answer = listOf("صورة", "كتابة").random()
                Challenge("🪙 توقّع نتيجة رمي العملة", listOf("صورة", "كتابة", "نرد", "لا شيء"), answer)
            }
            "dice" -> {
                val a = Random.nextInt(1, 7); val b = Random.nextInt(1, 7)
                val answer = if (a > b) "الأول ($a)" else if (b > a) "الثاني ($b)" else "تعادل ($a)"
                Challenge("🎲 رمية أولى: $a   |   رمية ثانية: $b\nمن الأعلى؟", listOf("الأول ($a)", "الثاني ($b)", "تعادل ($a)", "إعادة الرمي"), answer)
            }
            "parity" -> {
                val n = Random.nextInt(1, 100)
                val answer = if (n % 2 == 0) "زوجي" else "فردي"
                Challenge("🔢 هل العدد $n زوجي أم فردي؟", listOf("زوجي", "فردي", "أولي", "سالب"), answer)
            }
            "capital" -> {
                val data = listOf(
                    Triple("السودان", "الخرطوم", listOf("القاهرة", "أديس أبابا", "الخرطوم")),
                    Triple("مصر", "القاهرة", listOf("القاهرة", "الرباط", "دمشق")),
                    Triple("السعودية", "الرياض", listOf("جدة", "الرياض", "مكة")),
                    Triple("المغرب", "الرباط", listOf("تونس", "الجزائر", "الرباط")),
                    Triple("اليابان", "طوكيو", listOf("سيول", "طوكيو", "بكين")),
                    Triple("تركيا", "أنقرة", listOf("إسطنبول", "أنقرة", "إزمير"))
                ).random()
                Challenge("🌍 ما عاصمة ${data.first}؟", (data.third + listOf("لندن")).distinct().take(4), data.second)
            }
            "animal" -> {
                val data = listOf(
                    Triple("مواء", "القط", listOf("القط", "الكلب", "الحصان", "البقرة")),
                    Triple("نهيق", "الحمار", listOf("الأسد", "الحمار", "الخروف", "القرد")),
                    Triple("صهيل", "الحصان", listOf("الحصان", "القط", "الدجاجة", "الذئب")),
                    Triple("خوار", "البقرة", listOf("البقرة", "الثعلب", "الحمامة", "السمكة"))
                ).random()
                Challenge("🐾 أي حيوان يصدر صوت «${data.first}»؟", data.third, data.second)
            }
            "truefalse" -> {
                val facts = listOf(
                    Triple("الشمس نجم", "صحيح", true),
                    Triple("للأخطبوط ثمانية أذرع", "صحيح", true),
                    Triple("الخفاش من الطيور", "خطأ", false),
                    Triple("الأسبوع فيه عشرة أيام", "خطأ", false),
                    Triple("الماء يتجمد قرب صفر مئوي", "صحيح", true),
                    Triple("القمر يصدر ضوءه بنفسه", "خطأ", false)
                ).random()
                Challenge("🤔 ${facts.first}؟", listOf("صحيح", "خطأ", "لا أعرف", "كلاهما"), facts.second)
            }
            "sequence" -> {
                val start = Random.nextInt(1, 15); val step = Random.nextInt(2, 6)
                val answer = (start + step * 3).toString()
                Challenge("🧠 أكمل المتتالية:\n$start، ${start+step}، ${start+2*step}، ؟",
                    listOf(answer, (start+step*3+1).toString(), (start+step*2).toString(), (start+step*4).toString()), answer)
            }
            "color" -> {
                val data = listOf(
                    Triple("🔴", "أحمر", listOf("أحمر", "أزرق", "أخضر", "أصفر")),
                    Triple("🔵", "أزرق", listOf("أحمر", "أزرق", "بنفسجي", "أسود")),
                    Triple("🟢", "أخضر", listOf("أبيض", "برتقالي", "أخضر", "وردي")),
                    Triple("🟡", "أصفر", listOf("أصفر", "بني", "رمادي", "أزرق"))
                ).random()
                Challenge("🎨 ما لون الرمز ${data.first}؟", data.third, data.second)
            }
            "word" -> {
                val words = listOf("كتاب", "قمر", "مدرسة", "تفاح", "بحر", "قلم", "شمس", "باب")
                val answer = words.random()
                val scrambled = answer.toList().shuffled().joinToString("")
                val distractors = (words.filter { it != answer }.shuffled().take(3) + answer).shuffled()
                Challenge("🔤 رتّب الحروف لتكوين كلمة:\n$scrambled", distractors, answer)
            }
            else -> {
                val a = Random.nextInt(2, 20); val b = Random.nextInt(2, 20)
                val answer = (a * b).toString()
                Challenge("⚡ بسرعة! $a × $b = ؟",
                    listOf(answer, (a*b+2).toString(), (a+b).toString(), (a*b-1).toString()).distinct().let {
                        (it + List(4-it.size) { (a*b+Random.nextInt(3,15)).toString() }).distinct().take(4)
                    }, answer)
            }
        }
        // ضمان أربعة خيارات مختلفة مع تضمين الإجابة الصحيحة.
        val opts = (c.options.filter { it != c.answer }.distinct().take(3) + c.answer).distinct().toMutableList()
        var filler = 1
        while (opts.size < 4) opts.add("خيار $filler".also { filler++ })
        return c.copy(options = opts.take(4))
    }
}
