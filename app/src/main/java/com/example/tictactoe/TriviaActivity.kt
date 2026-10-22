package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView

class TriviaActivity : BaseGameActivity() {

    private class Q(val question: String, val answer: String, val wrong: List<String>)

    private val handler = Handler(Looper.getMainLooper())

    private val bank = listOf(
        Q("ما عاصمة السودان؟", "الخرطوم", listOf("القاهرة", "الرياض", "عمّان")),
        Q("ما عاصمة مصر؟", "القاهرة", listOf("الخرطوم", "تونس", "بغداد")),
        Q("ما عاصمة السعودية؟", "الرياض", listOf("جدة", "دبي", "مسقط")),
        Q("ما عاصمة المغرب؟", "الرباط", listOf("تونس", "الجزائر", "طرابلس")),
        Q("ما عاصمة فرنسا؟", "باريس", listOf("روما", "مدريد", "برلين")),
        Q("ما أكبر كوكب في المجموعة الشمسية؟", "المشتري", listOf("الأرض", "المريخ", "زحل")),
        Q("ما الكوكب الملقّب بالأحمر؟", "المريخ", listOf("الزهرة", "عطارد", "نبتون")),
        Q("ما أطول نهر في إفريقيا؟", "النيل", listOf("الكونغو", "النيجر", "الزامبيزي")),
        Q("كم عدد قارات العالم؟", "7", listOf("5", "6", "8")),
        Q("كم عدد أيام الأسبوع؟", "7", listOf("5", "6", "10")),
        Q("كم عدد ألوان قوس قزح؟", "7", listOf("5", "6", "8")),
        Q("ما أكبر محيط في العالم؟", "الهادئ", listOf("الأطلسي", "الهندي", "المتجمد")),
        Q("من ملك الغابة؟", "الأسد", listOf("النمر", "الفيل", "الذئب")),
        Q("ما أسرع حيوان بري؟", "الفهد", listOf("الحصان", "الأسد", "الغزال")),
        Q("ما عملة اليابان؟", "الين", listOf("اليوان", "الوون", "الدولار")),
        Q("ما أكبر دولة في العالم مساحةً؟", "روسيا", listOf("الصين", "كندا", "البرازيل")),
        Q("كم عدد أضلاع المثلث؟", "3", listOf("4", "5", "6")),
        Q("ما الغاز الذي نتنفسه ليبقى الجسم حياً؟", "الأكسجين", listOf("النيتروجين", "الهيدروجين", "الهيليوم")),
        Q("كم يساوي 12 × 12؟", "144", listOf("124", "132", "154")),
        Q("ما لون السماء الصافية نهاراً؟", "أزرق", listOf("أخضر", "أحمر", "أصفر"))
    )

    private val total = 10
    private var queue = listOf<Q>()
    private var index = 0
    private var score = 0
    private var answered = false

    private lateinit var progress: TextView
    private lateinit var question: TextView
    private lateinit var feedback: TextView
    private lateinit var restartButton: Button
    private val options = ArrayList<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("❓ أسئلة عامة")

        progress = label("", 16f, R.color.text_dim)
        content.addView(progress, lp(top = dp(6)))

        question = label("", 24f)
        content.addView(question, lp(top = dp(20), bottom = dp(8)))

        feedback = label("", 17f, R.color.gold)
        content.addView(feedback, lp(bottom = dp(10)))

        for (i in 0 until 4) {
            val b = button("", false) { }
            b.textSize = 20f
            options.add(b)
            content.addView(b, lp(height = dp(56), top = dp(8)))
        }

        restartButton = button("العب من جديد") { newGame() }
        restartButton.visibility = View.GONE
        content.addView(restartButton, lp(height = dp(52), top = dp(20)))

        newGame()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun newGame() {
        queue = bank.shuffled().take(total)
        index = 0
        score = 0
        restartButton.visibility = View.GONE
        options.forEach { it.visibility = View.VISIBLE }
        showQuestion()
    }

    private fun showQuestion() {
        answered = false
        val q = queue[index]
        progress.text = "السؤال ${index + 1} من $total   |   النقاط: $score"
        question.text = q.question
        feedback.text = ""

        val choices = (q.wrong + q.answer).shuffled()
        for (i in 0 until 4) {
            val b = options[i]
            val text = choices[i]
            b.text = text
            b.setOnClickListener { choose(text) }
        }
    }

    private fun choose(text: String) {
        if (answered) return
        answered = true
        val q = queue[index]
        if (text == q.answer) {
            score++
            feedback.text = "✅ إجابة صحيحة!"
            Feedback.match(this)
        } else {
            feedback.text = "❌ الصحيح: ${q.answer}"
            Feedback.lose(this)
        }
        handler.postDelayed({
            index++
            if (index >= total) finishGame() else showQuestion()
        }, 900)
    }

    private fun finishGame() {
        options.forEach { it.visibility = View.GONE }
        restartButton.visibility = View.VISIBLE
        progress.text = ""
        question.text = "نتيجتك: $score / $total"

        val best = Scores.get(this, "trivia_best")
        if (score > best) {
            Scores.put(this, "trivia_best", score)
            feedback.text = "رقم قياسي جديد! 🏆"
        } else {
            feedback.text = "أفضل نتيجة: $best / $total"
        }
        Feedback.win(this)
    }
}
