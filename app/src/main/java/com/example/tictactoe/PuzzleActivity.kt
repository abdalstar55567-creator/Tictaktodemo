@file:androidx.compose.runtime.NoLiveLiterals

package com.example.tictactoe

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.min
import kotlin.random.Random

/** لعبة الانزلاق: أرقام أو صورة، مع خلط مضمون الحل. */
class PuzzleActivity : BaseGameActivity() {
    override val gameTitle: String get() = "تحدي الأرقام والصور"

    private enum class Mode { NUMBERS, PICTURE }
    private var mode = Mode.NUMBERS
    private var boardSize = 3
    private var imageIndex = 0
    private var sourceBitmap: Bitmap? = null
    private lateinit var board: SlidingPuzzleBoard
    private lateinit var movesText: TextView
    private lateinit var timeText: TextView
    private lateinit var bestText: TextView
    private lateinit var statusText: TextView
    private lateinit var pauseButton: android.widget.Button

    private var moves = 0
    private var finished = false
    private var paused = false
    private var startedAt = 0L
    private var pausedElapsed = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerTask = object : Runnable {
        override fun run() {
            if (!paused && !finished) {
                timeText.text = "الوقت: ${formatTime(elapsedSeconds())}"
                timerHandler.postDelayed(this, 1000L)
            }
        }
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val loaded = decodeScaled(uri)
                if (loaded != null) {
                    sourceBitmap?.recycle()
                    sourceBitmap = loaded
                    mode = Mode.PICTURE
                    updateModeButtons()
                    newGame()
                } else {
                    statusText.text = "تعذر فتح الصورة. جرّب صورة أخرى."
                }
            } catch (_: Exception) {
                statusText.text = "تعذر قراءة الصورة من المعرض."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🧩 تحدي الأرقام والصور")

        content.addView(label("حرّك القطع المجاورة للفراغ حتى تعيد ترتيب اللوحة", 15f, R.color.text_dim, false), lp(top = dp(4), bottom = dp(8)))

        val modeRow = row()
        val numbersButton = button("🔢 الأرقام") { mode = Mode.NUMBERS; updateModeButtons(); newGame() }
        val pictureButton = button("🖼️ الصور", false) { mode = Mode.PICTURE; updateModeButtons(); newGame() }
        modeRow.addView(numbersButton, weightLp(dp(48)))
        modeRow.addView(pictureButton, weightLp(dp(48)))
        content.addView(modeRow, lp(top = dp(2), bottom = dp(8)))
        numberModeButton = numbersButton
        imageModeButton = pictureButton

        val levelRow = row()
        listOf(3, 4, 5).forEach { size ->
            val text = when (size) { 3 -> "سهل 3×3"; 4 -> "متوسط 4×4"; else -> "صعب 5×5" }
            val b = button(text, size == boardSize) { boardSize = size; updateLevelButtons(); newGame() }
            levelButtons[size] = b
            levelRow.addView(b, weightLp(dp(48)))
        }
        content.addView(levelRow, lp(bottom = dp(8)))

        movesText = label("الحركات: 0", 15f, R.color.text_main, true)
        timeText = label("الوقت: 00:00", 15f, R.color.text_main, true)
        bestText = label("أفضل نتيجة: —", 14f, R.color.text_dim, false)
        val stats = row()
        stats.addView(movesText, weightLp(dp(32)))
        stats.addView(timeText, weightLp(dp(32)))
        content.addView(stats)
        content.addView(bestText, lp(top = dp(2), bottom = dp(4)))

        statusText = label("اضغط على قطعة ملاصقة للفراغ لتحريكها", 15f, R.color.x_color, true)
        content.addView(statusText, lp(top = dp(4), bottom = dp(4)))

        board = SlidingPuzzleBoard(this) { index -> onTileTapped(index) }
        content.addView(board, lp(height = dp(340), top = dp(4), bottom = dp(8)))

        val actionRow = row()
        actionRow.addView(button("↻ لغز جديد") { newGame() }, weightLp(dp(48)))
        pauseButton = button("⏸ إيقاف") { togglePause() }
        actionRow.addView(pauseButton, weightLp(dp(48)))
        content.addView(actionRow, lp(bottom = dp(4)))

        val imageRow = row()
        imageRow.addView(button("🖼️ صورة جاهزة", false) { cycleBuiltInImage() }, weightLp(dp(48)))
        imageRow.addView(button("📱 من المعرض", false) { imagePicker.launch("image/*") }, weightLp(dp(48)))
        content.addView(imageRow, lp(bottom = dp(4)))
        content.addView(button("👁️ عرض الصورة الأصلية", false) { showReferenceImage() }, lp(top = dp(2), bottom = dp(8)))

        sourceBitmap = createBuiltInImage(0)
        updateModeButtons()
        updateLevelButtons()
        newGame()
    }

    private lateinit var numberModeButton: android.widget.Button
    private lateinit var imageModeButton: android.widget.Button
    private val levelButtons = mutableMapOf<Int, android.widget.Button>()

    private fun updateModeButtons() {
        numberModeButton.setBackgroundResource(if (mode == Mode.NUMBERS) R.drawable.bg_button else R.drawable.bg_button_dim)
        imageModeButton.setBackgroundResource(if (mode == Mode.PICTURE) R.drawable.bg_button else R.drawable.bg_button_dim)
    }

    private fun updateLevelButtons() {
        levelButtons.forEach { (size, button) ->
            button.setBackgroundResource(if (size == boardSize) R.drawable.bg_button else R.drawable.bg_button_dim)
        }
    }

    private fun newGame() {
        if (!::board.isInitialized) return
        finished = false
        paused = false
        pausedElapsed = 0L
        moves = 0
        pauseButton.text = "⏸ إيقاف"
        board.setInteractionEnabled(true)
        board.setPuzzle(boardSize, mode == Mode.NUMBERS, sourceBitmap)
        startedAt = SystemClock.elapsedRealtime()
        statusText.text = if (mode == Mode.NUMBERS) "رتّب الأرقام تصاعدياً من 1 إلى ${boardSize * boardSize - 1}" else "أعد تكوين الصورة بتحريك القطع إلى الفراغ"
        updateStats()
        timerHandler.removeCallbacks(timerTask)
        timerHandler.postDelayed(timerTask, 1000L)
    }

    private fun onTileTapped(index: Int) {
        if (paused || finished) return
        if (board.moveTile(index)) {
            moves++
            Feedback.tap(this)
            updateStats()
            if (board.isSolved()) {
                finished = true
                timerHandler.removeCallbacks(timerTask)
                board.setInteractionEnabled(false)
                val seconds = elapsedSeconds()
                val key = "sliding_${if (mode == Mode.NUMBERS) "numbers" else "picture"}_${boardSize}"
                val newRecord = Scores.saveLow(this, key, moves)
                statusText.text = if (newRecord) "🏆 رقم قياسي جديد! ${moves} حركة في ${formatTime(seconds)}" else "🎉 أحسنت! أكملت اللغز في ${moves} حركة"
                Feedback.win(this)
                updateStats()
            }
        }
    }

    private fun updateStats() {
        movesText.text = "الحركات: $moves"
        timeText.text = "الوقت: ${formatTime(elapsedSeconds())}"
        val key = "sliding_${if (mode == Mode.NUMBERS) "numbers" else "picture"}_${boardSize}"
        val best = Scores.get(this, key)
        bestText.text = if (best > 0) "أفضل عدد حركات لهذا المستوى: $best" else "لم تسجل نتيجة لهذا المستوى بعد"
    }

    private fun elapsedSeconds(): Long {
        val elapsed = if (paused) pausedElapsed else pausedElapsed + (SystemClock.elapsedRealtime() - startedAt)
        return (elapsed / 1000L).coerceAtLeast(0L)
    }

    private fun togglePause() {
        if (finished) return
        if (!paused) {
            pausedElapsed += SystemClock.elapsedRealtime() - startedAt
            paused = true
            timerHandler.removeCallbacks(timerTask)
            board.setInteractionEnabled(false)
            pauseButton.text = "▶ متابعة"
            statusText.text = "اللعبة متوقفة مؤقتاً"
        } else {
            paused = false
            startedAt = SystemClock.elapsedRealtime()
            board.setInteractionEnabled(true)
            pauseButton.text = "⏸ إيقاف"
            statusText.text = "تابع ترتيب القطع"
            timerHandler.postDelayed(timerTask, 1000L)
        }
        updateStats()
    }

    private fun cycleBuiltInImage() {
        imageIndex = (imageIndex + 1) % 2
        val next = createBuiltInImage(imageIndex)
        sourceBitmap?.recycle()
        sourceBitmap = next
        mode = Mode.PICTURE
        updateModeButtons()
        newGame()
    }

    private fun showReferenceImage() {
        if (mode != Mode.PICTURE) {
            statusText.text = "اختر نمط الصور أولاً لعرض الصورة الأصلية"
            return
        }
        val bitmap = sourceBitmap ?: return
        val image = ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            setBackgroundColor(Color.BLACK)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        AlertDialog.Builder(this)
            .setTitle("الصورة المطلوب تركيبها")
            .setView(image)
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun decodeScaled(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val original = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        val side = min(original.width, original.height)
        val x = (original.width - side) / 2
        val y = (original.height - side) / 2
        if (x == 0 && y == 0 && original.width == original.height) return original
        val square = Bitmap.createBitmap(original, x, y, side, side)
        original.recycle()
        return square
    }

    /** رسم صورتين أصليتين داخل التطبيق حتى تكون تجربة الصور جاهزة دون إنترنت. */
    private fun createBuiltInImage(scene: Int): Bitmap {
        val w = 800
        val h = 800
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (scene == 0) {
            paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 0xFF55C8F2.toInt(), 0xFFFFD18A.toInt(), Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
            paint.color = 0xFFFFE27A.toInt(); canvas.drawCircle(620f, 175f, 72f, paint)
            paint.color = Color.WHITE
            canvas.drawOval(95f, 125f, 275f, 185f, paint); canvas.drawOval(155f, 95f, 335f, 180f, paint)
            val back = android.graphics.Path().apply { moveTo(0f, 500f); lineTo(180f, 260f); lineTo(360f, 500f); lineTo(555f, 285f); lineTo(800f, 510f); lineTo(800f, 800f); lineTo(0f, 800f); close() }
            paint.color = 0xFF657F9C.toInt(); canvas.drawPath(back, paint)
            val front = android.graphics.Path().apply { moveTo(0f, 550f); quadTo(220f, 410f, 410f, 555f); quadTo(625f, 420f, 800f, 555f); lineTo(800f, 800f); lineTo(0f, 800f); close() }
            paint.color = 0xFF3B9B68.toInt(); canvas.drawPath(front, paint)
            paint.color = 0xFF278CB7.toInt(); canvas.drawOval(265f, 590f, 560f, 870f, paint)
            // شجرة على اليسار
            paint.color = 0xFF80502F.toInt(); canvas.drawRoundRect(105f, 475f, 145f, 650f, 12f, 12f, paint)
            paint.color = 0xFF176B45.toInt(); canvas.drawCircle(125f, 440f, 82f, paint); canvas.drawCircle(75f, 475f, 55f, paint); canvas.drawCircle(175f, 480f, 55f, paint)
            // بيت صغير ملون
            paint.color = 0xFFFFF1D2.toInt(); canvas.drawRoundRect(565f, 475f, 735f, 640f, 12f, 12f, paint)
            val roof = android.graphics.Path().apply { moveTo(540f, 490f); lineTo(650f, 390f); lineTo(760f, 490f); close() }
            paint.color = 0xFFCA5948.toInt(); canvas.drawPath(roof, paint)
            paint.color = 0xFF6C9EC5.toInt(); canvas.drawRoundRect(625f, 535f, 675f, 590f, 6f, 6f, paint)
            paint.color = 0xFF80502F.toInt(); canvas.drawRect(690f, 555f, 720f, 640f, paint)
        } else {
            paint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 0xFF101B4E.toInt(), 0xFF4D267B.toInt(), Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint); paint.shader = null
            paint.color = Color.WHITE
            val stars = arrayOf(70f to 90f, 180f to 190f, 690f to 110f, 570f to 245f, 110f to 390f, 730f to 480f, 460f to 90f, 350f to 270f, 650f to 680f, 230f to 650f)
            stars.forEachIndexed { i, point -> canvas.drawCircle(point.first, point.second, if (i % 3 == 0) 7f else 4f, paint) }
            paint.color = 0xFFFFC857.toInt(); canvas.drawCircle(625f, 250f, 105f, paint)
            paint.color = 0xFFEF8B54.toInt(); canvas.drawCircle(590f, 225f, 24f, paint); canvas.drawCircle(665f, 285f, 17f, paint)
            // صاروخ مرسوم في منتصف المشهد
            canvas.save(); canvas.rotate(-22f, 350f, 480f)
            paint.color = 0xFFEAF3FF.toInt(); canvas.drawRoundRect(295f, 330f, 405f, 610f, 52f, 52f, paint)
            val nose = android.graphics.Path().apply { moveTo(295f, 390f); quadTo(350f, 205f, 405f, 390f); close() }
            paint.color = 0xFFE65B68.toInt(); canvas.drawPath(nose, paint)
            paint.color = 0xFF438ED1.toInt(); canvas.drawCircle(350f, 435f, 35f, paint)
            paint.color = 0xFFE65B68.toInt()
            val finL = android.graphics.Path().apply { moveTo(300f, 520f); lineTo(235f, 625f); lineTo(305f, 590f); close() }
            val finR = android.graphics.Path().apply { moveTo(400f, 520f); lineTo(465f, 625f); lineTo(395f, 590f); close() }
            canvas.drawPath(finL, paint); canvas.drawPath(finR, paint)
            paint.color = 0xFFFFB347.toInt(); val flame = android.graphics.Path().apply { moveTo(315f, 600f); quadTo(350f, 760f, 385f, 600f); close() }; canvas.drawPath(flame, paint)
            paint.color = 0xFFFFE08A.toInt(); canvas.drawOval(330f, 600f, 370f, 700f, paint)
            canvas.restore()
            paint.color = 0xFF5B5BC7.toInt(); canvas.drawCircle(125f, 700f, 70f, paint)
        }
        return bitmap
    }

    private fun formatTime(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)

    override fun onPause() {
        super.onPause()
        if (!finished && !paused && ::board.isInitialized) {
            pausedElapsed += SystemClock.elapsedRealtime() - startedAt
            paused = true
            board.setInteractionEnabled(false)
            pauseButton.text = "▶ متابعة"
            timerHandler.removeCallbacks(timerTask)
        }
    }

    override fun onDestroy() {
        timerHandler.removeCallbacks(timerTask)
        sourceBitmap?.recycle()
        sourceBitmap = null
        super.onDestroy()
    }
}

/** لوحة رسم مستقلة؛ تعمل بالطريقة نفسها في نمط الأرقام ونمط الصور. */
private class SlidingPuzzleBoard(
    context: android.content.Context,
    private val onTileTap: (Int) -> Unit
) : View(context) {
    private var size = 3
    private var numbersMode = true
    private var tiles = IntArray(9) { if (it == 8) 0 else it + 1 }
    private var image: Bitmap? = null
    private var enabled = true
    private val density = resources.displayMetrics.density
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.create("sans-serif-rounded", android.graphics.Typeface.BOLD) }
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xAAFFFFFF.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.5f * density }
    private val blankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33202B3A }
    private val tileRect = Rect()
    private val destRect = RectF()

    fun setInteractionEnabled(value: Boolean) { enabled = value; alpha = if (value) 1f else 0.72f }

    fun setPuzzle(boardSize: Int, showNumbers: Boolean, bitmap: Bitmap?) {
        size = boardSize
        numbersMode = showNumbers
        image = bitmap
        tiles = IntArray(size * size) { if (it == size * size - 1) 0 else it + 1 }
        shuffleSolvable()
        invalidate()
    }

    private fun adjacent(index: Int): List<Int> {
        val r = index / size
        val c = index % size
        val result = ArrayList<Int>(4)
        if (r > 0) result.add(index - size)
        if (r < size - 1) result.add(index + size)
        if (c > 0) result.add(index - 1)
        if (c < size - 1) result.add(index + 1)
        return result
    }

    private fun shuffleSolvable() {
        var blank = tiles.indexOf(0)
        var previousBlank = -1
        repeat(size * size * 24) {
            val options = adjacent(blank).filter { it != previousBlank }.ifEmpty { adjacent(blank) }
            val next = options[Random.nextInt(options.size)]
            tiles[blank] = tiles[next]
            tiles[next] = 0
            previousBlank = blank
            blank = next
        }
        if (isSolved()) {
            val next = adjacent(blank).first()
            tiles[blank] = tiles[next]
            tiles[next] = 0
        }
    }

    fun moveTile(index: Int): Boolean {
        if (index !in tiles.indices) return false
        val blank = tiles.indexOf(0)
        if (index !in adjacent(blank)) return false
        tiles[blank] = tiles[index]
        tiles[index] = 0
        invalidate()
        return true
    }

    fun isSolved(): Boolean = tiles.indices.all { index ->
        tiles[index] == (if (index == tiles.lastIndex) 0 else index + 1)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val side = min(width, height).toFloat() - 8f * density
        if (side <= 0f) return
        val left = (width - side) / 2f
        val top = (height - side) / 2f
        val gap = 4f * density
        val cell = (side - gap * (size - 1)) / size
        val bitmap = image
        for (position in tiles.indices) {
            val row = position / size
            val col = position % size
            val x = left + col * (cell + gap)
            val y = top + row * (cell + gap)
            destRect.set(x, y, x + cell, y + cell)
            val value = tiles[position]
            if (value == 0) {
                canvas.drawRoundRect(destRect, 10f * density, 10f * density, blankPaint)
                continue
            }
            if (numbersMode || bitmap == null) {
                val colors = intArrayOf(0xFF2878C8.toInt(), 0xFF174B87.toInt())
                tilePaint.shader = LinearGradient(x, y, x, y + cell, colors[0], colors[1], Shader.TileMode.CLAMP)
                canvas.drawRoundRect(destRect, 12f * density, 12f * density, tilePaint)
                tilePaint.shader = null
                textPaint.textSize = cell * if (size == 5) 0.34f else 0.40f
                val baseline = y + cell / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
                canvas.drawText(value.toString(), x + cell / 2f, baseline, textPaint)
            } else {
                val sourceIndex = value - 1
                val srcLeft = sourceIndex % size * bitmap.width / size
                val srcTop = sourceIndex / size * bitmap.height / size
                val srcRight = (sourceIndex % size + 1) * bitmap.width / size
                val srcBottom = (sourceIndex / size + 1) * bitmap.height / size
                tileRect.set(srcLeft, srcTop, srcRight, srcBottom)
                canvas.drawBitmap(bitmap, tileRect, destRect, imagePaint)
            }
            canvas.drawRoundRect(destRect, 9f * density, 9f * density, borderPaint)
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (!enabled) return false
        if (event.action == android.view.MotionEvent.ACTION_UP) {
            val side = min(width, height).toFloat() - 8f * density
            val left = (width - side) / 2f
            val top = (height - side) / 2f
            val gap = 4f * density
            val cell = (side - gap * (size - 1)) / size
            val col = ((event.x - left) / (cell + gap)).toInt()
            val row = ((event.y - top) / (cell + gap)).toInt()
            if (col in 0 until size && row in 0 until size) {
                val localX = (event.x - left) - col * (cell + gap)
                val localY = (event.y - top) - row * (cell + gap)
                if (localX <= cell && localY <= cell) onTileTap(row * size + col)
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean { super.performClick(); return true }
}
