package com.example.tictactoe

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.*
import kotlin.random.Random

/** لعبة تلوين الحروف: اختر لوناً ومرّر إصبعك داخل الحرف لتلوينه. */
class LetterColoringActivity : BaseGameActivity() {
    private lateinit var board: LetterPaintView
    private lateinit var status: TextView
    private val palette = listOf(
        0xFFE74C3C.toInt(), 0xFFFF9F1C.toInt(), 0xFFFFD23F.toInt(), 0xFF2ECC71.toInt(),
        0xFF16A6A1.toInt(), 0xFF3498DB.toInt(), 0xFF6655CC.toInt(), 0xFFE84393.toInt(),
        0xFF795548.toInt(), 0xFF263238.toInt()
    )
    private var letterIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎨 تلوين الحروف")
        content.addView(label("اختر لوناً ثم مرّر إصبعك فوق الحرف لتلوينه", 16f, R.color.text_dim, false), lp(top = dp(8), bottom = dp(8)))
        status = label("لوّن الحرف ثم انتقل إلى التالي", 17f, R.color.x_color)
        content.addView(status, lp(bottom = dp(6)))
        board = LetterPaintView(this)
        content.addView(board, lp(height = dp(310), top = dp(4), bottom = dp(8)))
        val colors = row()
        palette.forEachIndexed { i, c ->
            val swatch = View(this)
            swatch.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(c); setStroke(dp(2), Color.WHITE)
            }
            swatch.setOnClickListener { board.setBrush(c); status.text = "اللون المختار ${i + 1}" }
            colors.addView(swatch, weightLp(dp(38)))
        }
        content.addView(colors, lp(height = dp(48), bottom = dp(12)))
        content.addView(button("حرف جديد  ↻") {
            letterIndex = (letterIndex + 1) % arabicLetters.size
            board.setLetter(arabicLetters[letterIndex]); status.text = "جرّب تلوين الحرف الجديد"
        }, lp(height = dp(52)))
        board.setLetter(arabicLetters[letterIndex])
        board.setBrush(palette.first())
    }

    companion object { private val arabicLetters = listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ر", "س", "ش", "ص", "ض", "ط", "ع", "ف", "ق", "ك", "ل", "م", "ن", "هـ", "و", "ي") }
}

private class LetterPaintView(context: Context) : View(context) {
    private val strokes = ArrayList<Pair<Path, Int>>()
    private var active: Path? = null
    private var brush = Color.RED
    private var glyph = "أ"
    private val glyphPath = Path()
    private val glyphRegion = Region()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.BOLD); textAlign = Paint.Align.CENTER }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF4F6FA.toInt(); style = Paint.Style.FILL }
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF9AA8B8.toInt(); style = Paint.Style.STROKE; strokeWidth = 5f }
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    fun setBrush(c: Int) { brush = c }
    fun setLetter(s: String) { glyph = s; strokes.clear(); active = null; rebuildPath(); invalidate() }
    private fun rebuildPath() {
        glyphPath.reset(); textPaint.textSize = height.coerceAtLeast(200).toFloat() * .72f
        val p = Path(); textPaint.getTextPath(glyph, 0, glyph.length, 0f, 0f, p)
        val bounds = RectF(); p.computeBounds(bounds, true)
        val scale = min((width * .58f) / bounds.width().coerceAtLeast(1f), (height * .70f) / bounds.height().coerceAtLeast(1f))
        val matrix = Matrix().apply { setScale(scale, scale); postTranslate(width / 2f - (bounds.left + bounds.width()/2f)*scale, height / 2f - (bounds.top + bounds.height()/2f)*scale) }
        p.transform(matrix); glyphPath.set(p)
        glyphRegion.setPath(glyphPath, Region(0, 0, width.coerceAtLeast(1), height.coerceAtLeast(1)))
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = rebuildPath()
    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(8f, 8f, width-8f, height-8f, 28f, 28f, bg)
        canvas.drawPath(glyphPath, fill)
        val save = canvas.save(); canvas.clipPath(glyphPath)
        strokes.forEach { (p,c) -> val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color=c; style=Paint.Style.STROKE; strokeWidth=28f; strokeCap=Paint.Cap.ROUND; strokeJoin=Paint.Join.ROUND }; canvas.drawPath(p,paint) }
        active?.let { p -> val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=brush; style=Paint.Style.STROKE; strokeWidth=28f; strokeCap=Paint.Cap.ROUND; strokeJoin=Paint.Join.ROUND }; canvas.drawPath(p,paint) }
        canvas.restoreToCount(save)
        canvas.drawPath(glyphPath, outline)
    }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x=e.x; val y=e.y
        when(e.action) {
            MotionEvent.ACTION_DOWN -> { if (!glyphRegion.contains(x.toInt(),y.toInt())) return false; parent?.requestDisallowInterceptTouchEvent(true); active=Path().apply { moveTo(x,y) }; invalidate(); return true }
            MotionEvent.ACTION_MOVE -> { active?.lineTo(x,y); invalidate(); return true }
            MotionEvent.ACTION_UP -> { active?.let { strokes.add(it to brush) }; active=null; invalidate(); performClick(); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}

/** لعبة قوة النظر: ابحث عن الدائرة ذات درجة اللون المختلفة قليلاً. */
class EyesightChallengeActivity : BaseGameActivity() {
    private lateinit var board: OddShadeView
    private lateinit var info: TextView
    private lateinit var restart: android.widget.Button
    private var level = 1
    private var score = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("👀 قوة النظر")
        content.addView(label("اختر الدائرة التي يختلف لونها قليلاً عن البقية", 16f, R.color.text_dim, false), lp(top=dp(10)))
        info=label("المستوى 1  •  النقاط 0",20f,R.color.x_color); content.addView(info,lp(top=dp(12),bottom=dp(10)))
        board=OddShadeView(this) { correct ->
            if(correct) { score++; level++; Feedback.win(this); info.text="أحسنت! المستوى $level  •  النقاط $score" }
            else { score=max(0,score-1); Feedback.lose(this); info.text="حاول مرة أخرى  •  المستوى $level  •  النقاط $score" }
            board.nextRound(level)
        }
        content.addView(board,lp(height=dp(340),top=dp(6),bottom=dp(14)))
        restart=button("إعادة من البداية") { level=1; score=0; info.text="المستوى 1  •  النقاط 0"; board.nextRound(level) }
        content.addView(restart,lp(height=dp(52)))
        board.nextRound(level)
    }
}

private class OddShadeView(context: Context, val answer: (Boolean)->Unit) : View(context) {
    private var level=1; private var odd=0; private var columns=5; private var rows=5
    private var base=Color.rgb(45,150,220); private var oddColor=Color.rgb(65,170,235)
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    fun nextRound(l:Int) { level=l; columns=if(l<5) 5 else 6; rows=columns; odd=Random.nextInt(columns*rows); val hue=Random.nextInt(0,360).toFloat(); val hsv=floatArrayOf(hue,.68f,.88f); base=Color.HSVToColor(hsv); hsv[1]=.68f; hsv[2]=(.88f-(.025f+(l.coerceAtMost(18)*.002f))).coerceAtLeast(.56f); oddColor=Color.HSVToColor(hsv); invalidate() }
    override fun onDraw(c:Canvas) { val cell=min(width.toFloat()/columns,height.toFloat()/rows); val size=cell*.68f; val left=(width-columns*cell)/2f; val top=(height-rows*cell)/2f
        for(i in 0 until columns*rows) { val x=left+(i%columns)*cell+cell/2; val y=top+(i/columns)*cell+cell/2; p.color=if(i==odd) oddColor else base; c.drawCircle(x,y,size/2,p); p.color=0x22FFFFFF; c.drawCircle(x-size*.14f,y-size*.16f,size*.10f,p) }
    }
    override fun onTouchEvent(e:MotionEvent):Boolean { if(e.action!=MotionEvent.ACTION_UP)return true; val cell=min(width.toFloat()/columns,height.toFloat()/rows); val left=(width-columns*cell)/2f; val top=(height-rows*cell)/2f; val col=((e.x-left)/cell).toInt(); val row=((e.y-top)/cell).toInt(); if(col in 0 until columns && row in 0 until rows) answer(row*columns+col==odd); performClick(); return true }
    override fun performClick():Boolean { super.performClick(); return true }
}

/** لعبة تركيب الصورة: بدّل قطعتين حتى تعود الصورة إلى شكلها الصحيح. */
class PicturePuzzleActivity : BaseGameActivity() {
    private lateinit var puzzle: PicturePuzzleView
    private lateinit var info: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🧩 تجميع الصور")
        content.addView(label("بدّل مكان قطعتين بالضغط عليهما حتى تكتمل الصورة",16f,R.color.text_dim,false),lp(top=dp(8),bottom=dp(8)))
        info=label("اختر القطعة الأولى ثم الثانية",17f,R.color.x_color); content.addView(info,lp(bottom=dp(8)))
        puzzle=PicturePuzzleView(this) { moves,done -> info.text=if(done) "🎉 اكتملت الصورة في $moves حركة!" else "الحركات: $moves  •  اختر القطعة التالية"; if(done) Feedback.win(this) }
        content.addView(puzzle,lp(height=dp(340),top=dp(4),bottom=dp(12)))
        content.addView(button("صورة جديدة ↻") { puzzle.newPuzzle() },lp(height=dp(52)))
    }
}

private class PicturePuzzleView(context:Context,val changed:(Int,Boolean)->Unit):View(context) {
    private val n=3; private val order=IntArray(n*n){it}; private var selected=-1; private var moves=0
    private var bitmap:Bitmap?=null; private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG); private val border=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.WHITE; style=Paint.Style.STROKE; strokeWidth=5f }
    init { newPuzzle() }
    fun newPuzzle() { for(i in order.indices) order[i]=i; order.shuffle(); if(order.indices.all{order[it]==it}) { val t=order[0]; order[0]=order[1]; order[1]=t }; selected=-1; moves=0; invalidate(); changed(moves,false) }
    private fun makeImage(w:Int,h:Int):Bitmap { val b=Bitmap.createBitmap(w.coerceAtLeast(1),h.coerceAtLeast(1),Bitmap.Config.ARGB_8888); val c=Canvas(b); val sky=Paint().apply { shader=LinearGradient(0f,0f,0f,h.toFloat(),0xFF5BBDEB.toInt(),0xFFFFB36B.toInt(),Shader.TileMode.CLAMP) }; c.drawRect(0f,0f,w.toFloat(),h.toFloat(),sky)
        val sun=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xFFFFE28A.toInt() }; c.drawCircle(w*.76f,h*.28f,min(w,h)*.095f,sun)
        val mountain=Path().apply { moveTo(0f,h*.72f); lineTo(w*.28f,h*.30f); lineTo(w*.48f,h*.68f); lineTo(w*.70f,h*.38f); lineTo(w.toFloat(),h*.75f); lineTo(w.toFloat(),h.toFloat()); lineTo(0f,h.toFloat()); close() }; c.drawPath(mountain,Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xFF536B78.toInt() })
        val hill=Path().apply { moveTo(0f,h*.78f); quadTo(w*.5f,h*.55f,w.toFloat(),h*.80f); lineTo(w.toFloat(),h.toFloat()); lineTo(0f,h.toFloat()); close() }; c.drawPath(hill,Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xFF277D68.toInt() })
        val water=Paint().apply { color=0xFF3A9FB0.toInt() }; c.drawRect(0f,h*.82f,w.toFloat(),h.toFloat(),water)
        val line=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0x88FFFFFF.toInt(); strokeWidth=4f }; for(i in 0..6)c.drawLine(w*.12f+i*w*.1f,h*.88f,w*.30f+i*w*.1f,h*.88f,line)
        return b }
    override fun onSizeChanged(w:Int,h:Int,ow:Int,oh:Int) { bitmap?.recycle(); bitmap=makeImage(w,h) }
    override fun onDraw(c:Canvas) { val side=min(width,height).toFloat(); val left=(width-side)/2; val top=(height-side)/2; val tile=side/n; val b=bitmap ?: return
        for(pos in order.indices) { val source=order[pos]; val src=Rect((source%n)*b.width/n,(source/n)*b.height/n,(source%n+1)*b.width/n,(source/n+1)*b.height/n); val dst=RectF(left+(pos%n)*tile,top+(pos/n)*tile,left+(pos%n+1)*tile,top+(pos/n+1)*tile); c.drawBitmap(b,src,dst,paint); c.drawRect(dst,border); if(pos==selected) { val hi=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xAAFFD54F.toInt(); style=Paint.Style.STROKE; strokeWidth=10f }; c.drawRect(dst,hi) } }
    }
    override fun onTouchEvent(e:MotionEvent):Boolean { if(e.action!=MotionEvent.ACTION_UP)return true; val side=min(width,height).toFloat(); val left=(width-side)/2; val top=(height-side)/2; val tile=side/n; val col=((e.x-left)/tile).toInt(); val row=((e.y-top)/tile).toInt(); if(col !in 0 until n || row !in 0 until n)return true; val pos=row*n+col
        if(selected<0) selected=pos else { if(selected!=pos) { val t=order[selected]; order[selected]=order[pos]; order[pos]=t; moves++ }; selected=-1; val done=order.indices.all{order[it]==it}; invalidate(); changed(moves,done); if(done) { /* keep solved board visible */ } }; invalidate(); performClick(); return true }
    override fun performClick():Boolean { super.performClick(); return true }
    override fun onDetachedFromWindow() { bitmap?.recycle(); bitmap=null; super.onDetachedFromWindow() }
}
