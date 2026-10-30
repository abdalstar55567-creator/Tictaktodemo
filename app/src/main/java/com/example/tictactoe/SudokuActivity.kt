package com.example.tictactoe

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.random.Random

class SudokuActivity : BaseGameActivity() {

    private val board = IntArray(81)
    private val solution = IntArray(81)
    private val fixed = BooleanArray(81)
    private val cells = ArrayList<TextView>()
    private var selected = -1
    private var mistakes = 0
    private var level = 0
    private var completed = false

    private lateinit var progressText: TextView
    private lateinit var statusText: TextView

    private val levelNames = arrayOf("سهل", "متوسط", "صعب")
    private val emptyCounts = intArrayOf(35, 45, 53)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🔢 السودوكو")

        content.addView(
            label("املأ الشبكة من 1 إلى 9 دون تكرار في الصف أو العمود أو المربع 3×3.", 14f, R.color.text_dim, false),
            lp(top = dp(2), bottom = dp(8))
        )

        val levelRow = row()
        levelNames.forEachIndexed { index, name ->
            levelRow.addView(
                button(name, index == level) {
                    level = index
                    newGame()
                },
                weightLp(dp(42))
            )
        }
        content.addView(levelRow, lp())

        progressText = label("", 15f, R.color.gold)
        content.addView(progressText, lp(top = dp(5)))

        val grid = GridLayout(this).apply {
            columnCount = 9
            rowCount = 9
            useDefaultMargins = false
            alignmentMode = GridLayout.ALIGN_BOUNDS
        }
        val cellSize = minOf((resources.displayMetrics.widthPixels - dp(56)) / 9, dp(41))
        for (i in 0 until 81) {
            val cell = TextView(this).apply {
                gravity = Gravity.CENTER
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.WHITE)
                setOnClickListener { selectCell(i) }
            }
            val params = GridLayout.LayoutParams()
            params.width = cellSize
            params.height = cellSize
            params.setMargins(dp(1), dp(1), dp(1), dp(1))
            cell.layoutParams = params
            cells.add(cell)
            grid.addView(cell)
        }
        content.addView(grid, lp(width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT, top = dp(7)))

        statusText = label("اختر خلية فارغة ثم اضغط الرقم المطلوب.", 14f, R.color.text_dim, false)
        content.addView(statusText, lp(top = dp(7), bottom = dp(3)))

        for (r in 0..2) {
            val numberRow = row()
            for (c in 1..3) {
                val number = r * 3 + c
                numberRow.addView(
                    button(number.toString()) { enterNumber(number) },
                    weightLp(dp(42))
                )
            }
            content.addView(numberRow, lp(top = dp(2)))
        }

        val actions = row()
        actions.addView(button("مسح", false) { eraseSelected() }, weightLp(dp(44)))
        actions.addView(button("تلميح 💡") { giveHint() }, weightLp(dp(44)))
        actions.addView(button("تحقق ✓", false) { checkBoard() }, weightLp(dp(44)))
        content.addView(actions, lp(top = dp(5)))
        content.addView(button("لعبة جديدة ↻") { newGame() }, lp(height = dp(46), top = dp(5), bottom = dp(8)))

        newGame()
    }

    // إنشاء شبكة مكتملة صحيحة، ثم تبديل الأرقام والصفوف والأعمدة عشوائياً.
    private fun makeSolution() {
        val base = IntArray(81)
        fun pattern(r: Int, c: Int): Int = (r * 3 + r / 3 + c) % 9

        val digits = (1..9).shuffled()
        val rows = (0..2).shuffled().flatMap { band ->
            (0..2).shuffled().map { band * 3 + it }
        }
        val cols = (0..2).shuffled().flatMap { stack ->
            (0..2).shuffled().map { stack * 3 + it }
        }
        for (r in 0..8) {
            for (c in 0..8) {
                base[r * 9 + c] = digits[pattern(rows[r], cols[c])]
            }
        }
        for (i in 0 until 81) solution[i] = base[i]
    }

    private fun newGame() {
        makeSolution()
        for (i in 0 until 81) {
            board[i] = solution[i]
            fixed[i] = true
        }
        (0 until 81).shuffled().take(emptyCounts[level]).forEach { index ->
            board[index] = 0
            fixed[index] = false
        }
        selected = -1
        mistakes = 0
        completed = false
        statusText.text = "اختر خلية فارغة ثم اضغط الرقم المطلوب."
        render()
    }

    private fun selectCell(index: Int) {
        if (completed) return
        selected = index
        statusText.text = if (fixed[index]) {
            "هذا رقم ثابت من اللغز. اختر خلية أخرى."
        } else {
            "تم تحديد الخلية. اختر رقماً من الأسفل."
        }
        render()
    }

    private fun enterNumber(number: Int) {
        if (completed) return
        if (selected < 0) {
            statusText.text = "حدد خلية فارغة أولاً."
            return
        }
        if (fixed[selected]) {
            statusText.text = "لا يمكن تغيير الأرقام الثابتة."
            return
        }

        val row = selected / 9
        val col = selected % 9
        val conflict = (0..8).any { c ->
            c != col && board[row * 9 + c] == number
        } || (0..8).any { r ->
            r != row && board[r * 9 + col] == number
        } || (row / 3 * 3 until row / 3 * 3 + 3).any { r ->
            (col / 3 * 3 until col / 3 * 3 + 3).any { c ->
                (r != row || c != col) && board[r * 9 + c] == number
            }
        }

        if (conflict) {
            mistakes++
            statusText.text = "❌ هذا الرقم موجود بالفعل في الصف أو العمود أو المربع."
            Feedback.lose(this)
            render()
            return
        }

        board[selected] = number
        Feedback.tap(this)
        if (board.contentEquals(solution)) {
            completed = true
            statusText.text = "🏆 رائع! أكملت السودوكو بنجاح."
            Feedback.win(this)
        } else if (board[selected] != solution[selected]) {
            statusText.text = "تم إدخال الرقم. راجع الحل عند الانتهاء."
        } else {
            statusText.text = "إجابة صحيحة، واصل الحل!"
        }
        render()
    }

    private fun eraseSelected() {
        if (completed) return
        if (selected < 0 || fixed[selected]) {
            statusText.text = "اختر خلية أدخلت فيها رقماً لمسحه."
            return
        }
        board[selected] = 0
        statusText.text = "تم مسح الرقم."
        Feedback.tap(this)
        render()
    }

    private fun giveHint() {
        if (completed) return
        val target = if (selected >= 0 && !fixed[selected] && board[selected] != solution[selected]) {
            selected
        } else {
            (0 until 81).filter { !fixed[it] && board[it] != solution[it] }.randomOrNull()
        }

        if (target == null) {
            statusText.text = "لا توجد خلية تحتاج إلى تلميح."
            return
        }

        selected = target
        board[target] = solution[target]
        statusText.text = "💡 وضعنا الرقم الصحيح في الخلية المحددة."
        if (board.contentEquals(solution)) {
            completed = true
            statusText.text = "🏆 اكتمل اللغز!"
            Feedback.win(this)
        }
        render()
    }

    private fun checkBoard() {
        if (completed) return
        val empty = board.count { it == 0 }
        val wrong = (0 until 81).count { board[it] != 0 && board[it] != solution[it] }
        statusText.text = when {
            empty > 0 -> "بقي $empty خلية فارغة. أرقام تحتاج مراجعة: $wrong"
            wrong > 0 -> "هناك $wrong أرقام غير صحيحة. راجعها وحاول مجدداً."
            else -> "🏆 الحل صحيح! أحسنت."
        }
        if (empty == 0 && wrong == 0) {
            completed = true
            Feedback.win(this)
        }
        render()
    }

    private fun render() {
        val selectedRow = if (selected >= 0) selected / 9 else -1
        val selectedCol = if (selected >= 0) selected % 9 else -1
        val selectedValue = if (selected >= 0) board[selected] else 0

        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            val sameBox = selected >= 0 && r / 3 == selectedRow / 3 && c / 3 == selectedCol / 3
            val related = selected >= 0 && (r == selectedRow || c == selectedCol || sameBox)
            val sameNumber = selectedValue != 0 && board[i] == selectedValue

            val bgColor = when {
                i == selected -> Color.rgb(14, 165, 233)
                sameNumber -> Color.rgb(51, 91, 125)
                related -> Color.rgb(45, 61, 82)
                fixed[i] -> Color.rgb(51, 65, 85)
                else -> Color.rgb(30, 41, 59)
            }
            val strokeColor = if (r % 3 == 0 || c % 3 == 0) {
                Color.rgb(148, 163, 184)
            } else {
                Color.rgb(71, 85, 105)
            }
            val drawable = GradientDrawable().apply {
                cornerRadius = dp(3).toFloat()
                setColor(bgColor)
                setStroke(dp(if (r % 3 == 0 || c % 3 == 0) 2 else 1), strokeColor)
            }

            cells[i].background = drawable
            cells[i].text = if (board[i] == 0) "" else board[i].toString()
            cells[i].setTextColor(
                if (fixed[i]) Color.rgb(248, 250, 252) else Color.rgb(103, 232, 249)
            )
        }

        val filled = board.count { it != 0 }
        progressText.text = "المكتمل: $filled / 81     الأخطاء: $mistakes"
    }
}
