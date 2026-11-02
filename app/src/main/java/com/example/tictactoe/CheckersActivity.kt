package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import kotlin.math.min

/** لعبة الداما الكلاسيكية: لاعبان على الجهاز نفسه، مع أكل إجباري والترقية إلى ملك. */
class CheckersActivity : BaseGameActivity() {
    override val gameTitle = "الداما"
    private lateinit var board: CheckersBoardView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen(gameTitle)
        content.addView(label("لعبة كلاسيكية للاعبين • حرّك القطع قطرياً واقفز فوق الخصم لأكله", 14f, R.color.text_dim, false), lp(bottom = dp(8)))
        status = label("دور اللاعب الأحمر 🔴", 18f)
        content.addView(status, lp(bottom = dp(8)))
        board = CheckersBoardView(this) { message -> status.text = message }
        content.addView(board, lp(bottom = dp(8)))
        content.addView(button("إعادة اللعبة") { board.reset() }, lp(top = dp(4), bottom = dp(12)))
    }

    override fun onPause() {
        if (::board.isInitialized) board.clearSelection()
        super.onPause()
    }
}

private class CheckersBoardView(context: Context, private val onStatus: (String) -> Unit) : View(context) {
    private data class Piece(var side: Int, var king: Boolean = false)
    private data class Move(val row: Int, val col: Int, val captureRow: Int = -1, val captureCol: Int = -1) {
        val isCapture get() = captureRow >= 0
    }

    private val board = Array(8) { arrayOfNulls<Piece>(8) }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selected: Pair<Int, Int>? = null
    private var legal = emptyList<Move>()
    private var turn = 1 // الأحمر يبدأ من الأسفل
    private var finished = false
    private var size = 0f
    private var left = 0f
    private var top = 0f

    init { reset() }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec).takeIf { it > 0 } ?: availableWidth
        val boardSide = min(availableWidth, availableHeight).coerceAtLeast(1)
        setMeasuredDimension(availableWidth, boardSide)
    }

    fun reset() {
        for (r in 0..7) for (c in 0..7) board[r][c] = null
        for (r in 0..2) for (c in 0..7) if ((r + c) % 2 == 1) board[r][c] = Piece(-1)
        for (r in 5..7) for (c in 0..7) if ((r + c) % 2 == 1) board[r][c] = Piece(1)
        turn = 1; finished = false; selected = null; legal = emptyList()
        onStatus("دور اللاعب الأحمر 🔴")
        invalidate()
    }

    fun clearSelection() { selected = null; legal = emptyList(); invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val boardSize = min(width.toFloat(), height.toFloat()).coerceAtMost(width.toFloat())
        size = boardSize / 8f
        left = (width - boardSize) / 2f
        top = (height - boardSize) / 2f
        paint.style = Paint.Style.FILL
        for (r in 0..7) for (c in 0..7) {
            val x = left + c * size; val y = top + r * size
            paint.color = if ((r + c) % 2 == 0) Color.rgb(239, 220, 184) else Color.rgb(105, 69, 48)
            canvas.drawRect(x, y, x + size, y + size, paint)
        }
        selected?.let { (r, c) ->
            paint.color = Color.argb(145, 255, 220, 40)
            canvas.drawRect(left + c * size, top + r * size, left + (c + 1) * size, top + (r + 1) * size, paint)
        }
        legal.forEach { m ->
            paint.color = if (m.isCapture) Color.rgb(245, 95, 65) else Color.rgb(95, 210, 130)
            canvas.drawCircle(left + (m.col + .5f) * size, top + (m.row + .5f) * size, size * .14f, paint)
        }
        for (r in 0..7) for (c in 0..7) board[r][c]?.let { piece -> drawPiece(canvas, r, c, piece) }
        paint.color = Color.argb(100, 255, 255, 255)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 2f
        canvas.drawRect(left, top, left + boardSize, top + boardSize, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawPiece(canvas: Canvas, row: Int, col: Int, piece: Piece) {
        val cx = left + (col + .5f) * size; val cy = top + (row + .5f) * size
        val radius = size * .37f
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawCircle(cx + size * .035f, cy + size * .045f, radius, paint)
        paint.color = if (piece.side == 1) Color.rgb(203, 48, 55) else Color.rgb(35, 42, 55)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.color = if (piece.side == 1) Color.rgb(255, 116, 105) else Color.rgb(105, 117, 135)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = size * .055f
        canvas.drawCircle(cx, cy, radius * .78f, paint); paint.style = Paint.Style.FILL
        if (piece.king) {
            paint.color = Color.rgb(255, 213, 76)
            paint.textAlign = Paint.Align.CENTER; paint.textSize = size * .55f; paint.isFakeBoldText = true
            canvas.drawText("♛", cx, cy + size * .19f, paint)
            paint.isFakeBoldText = false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP || finished || size <= 0f) return true
        val col = ((event.x - left) / size).toInt()
        val row = ((event.y - top) / size).toInt()
        if (row !in 0..7 || col !in 0..7) return true
        val current = selected
        if (current != null) {
            val move = legal.firstOrNull { it.row == row && it.col == col }
            if (move != null) { applyMove(current.first, current.second, move); return true }
        }
        val piece = board[row][col]
        if (piece != null && piece.side == turn) {
            val all = allMoves(turn)
            val hasCapture = all.any { it.second.isCapture }
            val options = all.filter { it.first == (row to col) }.map { it.second }
            val allowed = if (hasCapture) options.filter { it.isCapture } else options
            if (allowed.isNotEmpty()) {
                selected = row to col
                legal = allowed
                invalidate()
            } else if (hasCapture) {
                selected = null
                legal = emptyList()
                onStatus("يوجد أكل إجباري! اختر قطعة تستطيع القفز فوق الخصم")
                invalidate()
            }
        } else if (current != null) {
            selected = null; legal = emptyList(); invalidate()
        }
        return true
    }

    private fun applyMove(r: Int, c: Int, move: Move) {
        val piece = board[r][c] ?: return
        board[move.row][move.col] = piece; board[r][c] = null
        if (move.isCapture) board[move.captureRow][move.captureCol] = null
        if ((piece.side == 1 && move.row == 0) || (piece.side == -1 && move.row == 7)) piece.king = true
        if (move.isCapture) {
            val follow = movesFor(move.row, move.col, piece).filter { it.isCapture }
            if (follow.isNotEmpty()) {
                selected = move.row to move.col; legal = follow
                onStatus("قفزة أخرى متاحة! أكمل الأكل ${sideName(turn)}")
                invalidate(); return
            }
        }
        selected = null; legal = emptyList()
        turn = -turn
        val remaining = board.sumOf { row -> row.count { it?.side == turn } }
        if (remaining == 0 || allMoves(turn).isEmpty()) {
            finished = true
            onStatus("🏆 فاز ${sideName(-turn)}! اضغط إعادة اللعبة للعب مجدداً")
        } else onStatus("دور ${sideName(turn)} ${if (turn == 1) "🔴" else "⚫"}")
        invalidate()
    }

    private fun sideName(side: Int) = if (side == 1) "اللاعب الأحمر" else "اللاعب الأسود"

    private fun allMoves(side: Int): List<Pair<Pair<Int, Int>, Move>> {
        val result = mutableListOf<Pair<Pair<Int, Int>, Move>>()
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c]
            if (p != null && p.side == side) movesFor(r, c, p).forEach { result.add((r to c) to it) }
        }
        return result
    }

    private fun movesFor(r: Int, c: Int, piece: Piece): List<Move> {
        val result = mutableListOf<Move>()
        val dirs = if (piece.king) listOf(-1, 1) else listOf(if (piece.side == 1) -1 else 1)
        for (dr in dirs) for (dc in listOf(-1, 1)) {
            val nr = r + dr; val nc = c + dc
            if (nr !in 0..7 || nc !in 0..7) continue
            val target = board[nr][nc]
            if (target == null) result.add(Move(nr, nc))
            else if (target.side != piece.side) {
                val jr = nr + dr; val jc = nc + dc
                if (jr in 0..7 && jc in 0..7 && board[jr][jc] == null) result.add(Move(jr, jc, nr, nc))
            }
        }
        return result
    }
}
