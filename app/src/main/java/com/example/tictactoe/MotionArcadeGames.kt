package com.example.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random

/** Five distinct touch-first arcade games. No external assets or libraries required. */
enum class MotionGame(val title: String, val hint: String) {
    CAR("سباق السيارات", "اسحب يميناً ويساراً لتبديل المسار وتفادي السيارات"),
    PACMAN("متاهة آكل النقاط", "اسحب في اتجاه الحركة واجمع النقاط وابتعد عن الأشباح"),
    GHOST("مطاردة الأشباح", "اجمع النجوم داخل المتاهة وتجنب الأشباح"),
    SPACE("مدافع الفضاء", "حرّك سفينتك باللمس وأطلق النار تلقائياً"),
    BIKE("سباق الدراجات", "بدّل المسار بالسحب وتجنب الحواجز"),
    RIVER("مغامرة النهر", "حرّك القارب يميناً ويساراً لتفادي الصخور"),
    BALL("متاهة الكرة", "اسحب الكرة في الاتجاه المطلوب للوصول إلى الهدف"),
    JUNGLE("عدّاء الغابة", "المس الشاشة للقفز فوق جذوع الأشجار"),
    BUBBLES("تحدي الفقاعات السامة", "التقط الفقاعات الزرقاء وتجنب الحمراء؛ ثلاث فقاعات زرقاء فائتة أو لمس حمراء تنهي الجولة"),
    ESCAPE("الهروب من الأشباح", "اجمع النقاط ثم اعثر على المخرج وتجنب الأشباح")
}

class MotionArcadeView(context: Context, private val mode: MotionGame, private val onScore: (Int) -> Unit, private val onOver: (Int) -> Unit) : GameLoopView(context) {
    override val aspect: Float get() = 0.90f
    private val bg = paint(Color.rgb(12, 20, 42)); private val road = paint(Color.rgb(45, 55, 75))
    private val white = paint(Color.WHITE); private val cyan = paint(Color.rgb(58, 220, 240))
    private val yellow = paint(Color.rgb(255, 218, 70)); private val red = paint(Color.rgb(255, 85, 105))
    private val green = paint(Color.rgb(80, 230, 145)); private val purple = paint(Color.rgb(190, 120, 255))
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var score = 0; private var over = false; private var started = false; private var elapsed = 0f; private var missedBubbles = 0
    private var playerX = .5f; private var playerY = .82f; private var lane = 1; private var jump = 0f
    private var moveX = 0; private var moveY = 0; private var tick = 0f; private var shotTick = 0f
    private data class Obj(var x: Float, var y: Float, var type: Int = 0, var alive: Boolean = true)
    private val objects = mutableListOf<Obj>(); private val bullets = mutableListOf<Obj>()
    private val dots = mutableSetOf<Pair<Int,Int>>(); private val walls = mutableSetOf<Pair<Int,Int>>()
    private val ghosts = mutableListOf<Pair<Int,Int>>(); private val gridW = 9; private val gridH = 12
    private var gx = 1; private var gy = 1; private var keys = 0
    private val mapWalls = setOf("1,3","2,3","3,3","5,2","6,2","7,2","2,5","3,5","5,5","6,5","1,7","2,7","4,7","5,7","7,8","6,8","3,9","4,9","5,9")
    init { isFocusable = true; reset() }
    private fun reset() {
        stopLoop(); score=0; over=false; started=false; elapsed=0f; missedBubbles=0; playerX=.5f; playerY=.82f; lane=1; jump=0f; moveX=0; moveY=0; tick=0f; shotTick=0f; objects.clear(); bullets.clear(); dots.clear(); walls.clear(); ghosts.clear(); gx=1; gy=1; keys=0
        if (mode==MotionGame.PACMAN || mode==MotionGame.GHOST || mode==MotionGame.BALL || mode==MotionGame.ESCAPE) {
            for (y in 0 until gridH) for (x in 0 until gridW) {
                if (x==0 || y==0 || x==gridW-1 || y==gridH-1 || "${x},${y}" in mapWalls) walls.add(x to y)
                else if (!(x==1 && y==1)) dots.add(x to y)
            }
            ghosts.add(7 to 1); ghosts.add(7 to 9)
        }
        onScore(0); invalidate()
    }
    fun startGame() { if (over) reset(); started=true; over=false; startLoop(); invalidate() }
    fun moveDirection(dx: Int, dy: Int) {
        if (mode != MotionGame.PACMAN && mode != MotionGame.GHOST && mode != MotionGame.BALL && mode != MotionGame.ESCAPE) return
        if (!started || over) startGame()
        moveX = dx.coerceIn(-1, 1)
        moveY = dy.coerceIn(-1, 1)
        invalidate()
    }
    private fun endGame() { if (!over) { over=true; stopLoop(); onOver(score); invalidate() } }
    private fun addScore(n:Int=1) { score+=n; onScore(score) }
    override fun update(dt: Float) {
        if (!started || over) return
        elapsed += dt; tick += dt; shotTick += dt
        when(mode) {
            MotionGame.CAR, MotionGame.BIKE, MotionGame.RIVER -> {
                val speed = .38f + minOf(.35f, score*.003f)
                if (tick > .62f) { tick=0f; objects.add(Obj((Random.nextInt(3)+.5f)/3f, -.08f, if(Random.nextFloat()<.2f)1 else 0)) }
                objects.forEach { it.y += speed*dt }
                val px=(lane+.5f)/3f
                objects.filter { it.alive && it.y>.72f && it.y<.91f && abs(it.x-px)<.19f }.forEach { endGame() }
                objects.removeAll { if(it.y>1.08f) { if(it.alive) addScore(); true } else false }
            }
            MotionGame.SPACE -> {
                if (shotTick>.25f) { shotTick=0f; bullets.add(Obj(playerX,.73f)) }
                if (tick>.55f) { tick=0f; objects.add(Obj(Random.nextFloat()*.84f+.08f,-.06f)) }
                bullets.forEach { it.y-=.85f*dt }; objects.forEach { it.y+=.28f*dt }
                for (b in bullets) for (e in objects) if(b.alive&&e.alive&&abs(b.x-e.x)<.06f&&abs(b.y-e.y)<.07f){b.alive=false;e.alive=false;addScore(2)}
                if(objects.any{it.alive&&it.y>.78f&&abs(it.x-playerX)<.09f}) endGame()
                bullets.removeAll{!it.alive||it.y<-.1f}; objects.removeAll{!it.alive||it.y>1.1f}
            }
            MotionGame.PACMAN, MotionGame.GHOST, MotionGame.BALL, MotionGame.ESCAPE -> {
                if(tick>.22f){ tick=0f
                    var nx=(gx+moveX).coerceIn(1,gridW-2); var ny=(gy+moveY).coerceIn(1,gridH-2)
                    if ((nx to ny) !in walls){gx=nx;gy=ny; if(dots.remove(gx to gy)) addScore(if(mode==MotionGame.ESCAPE)2 else 1); if(mode==MotionGame.ESCAPE && gx==7&&gy==10&&score>=5){endGame()} }
                    ghosts.indices.forEach { i -> val (x,y)=ghosts[i]; val opts=listOf(x+1 to y,x-1 to y,x to y+1,x to y-1).filter{it !in walls}; if(opts.isNotEmpty()){ val target=opts.minByOrNull{abs(it.first-gx)+abs(it.second-gy)}!!; ghosts[i]=if(Random.nextFloat()<.65f) target else opts.random() } }
                    if(ghosts.any{it.first==gx&&it.second==gy}) endGame()
                    if(dots.isEmpty() && mode==MotionGame.PACMAN) { addScore(10); endGame() }
                }
            }
            MotionGame.JUNGLE -> {
                // A jump is a fixed-duration arc. Touching and holding the screen
                // cannot refresh it; the player must land before jumping again.
                if (jump > 0f) jump = (jump - dt * 1.6f).coerceAtLeast(0f)
                if (tick > 1.1f) {
                    tick = 0f
                    objects.add(Obj(1.08f, .78f, 1))
                }
                objects.forEach { it.x -= .65f * dt }
                // The trunk overlaps the runner between x=.08 and x=.26.
                // The jump must still be high when that overlap occurs.
                if (objects.any { it.x < .26f && it.x + .14f > .16f && jump < .35f }) endGame()
                objects.removeAll { if (it.x < -.15f) { addScore(); true } else false }
            }
            MotionGame.BUBBLES -> {
                // Blue bubbles are targets; red bubbles are toxic. Missing three targets or
                // catching a toxic bubble ends the round, so standing still cannot win.
                if(tick>.62f){
                    tick=0f
                    objects.add(Obj(Random.nextFloat()*.84f+.08f,-.04f,if(Random.nextFloat()<.24f)1 else 0))
                }
                val speed=.28f+minOf(.28f,score*.008f)
                objects.forEach{it.y+=speed*dt}
                val iterator=objects.iterator()
                while(iterator.hasNext()){
                    val bubble=iterator.next()
                    if(bubble.y>=.83f && abs(bubble.x-playerX)<.13f){
                        if(bubble.type==1){endGame()} else {addScore(); if(score>=15) endGame()}
                        iterator.remove()
                    } else if(bubble.y>1.02f){
                        if(bubble.type==0){missedBubbles++; if(missedBubbles>=3) endGame()}
                        iterator.remove()
                    }
                }
            }
        }
        if (elapsed>120f) endGame()
    }
    override fun onDraw(c: Canvas) {
        val w=width.toFloat(); val h=height.toFloat(); c.drawColor(bg.color)
        when(mode) {
            MotionGame.CAR, MotionGame.BIKE, MotionGame.RIVER -> {
                c.drawRect(w*.12f,0f,w*.88f,h,road)
                for(i in 1..2) for(y in 0..8) c.drawRect(w*(.12f+i*.253f)-3f, h*(y/9f)+(elapsed*120%h), w*(.12f+i*.253f)+3f,h*(y/9f)+(elapsed*120%h)+h*.06f,white)
                objects.forEach{ drawCar(c,it.x*w,it.y*h,w*.12f,if(it.type==1)purple else red) }
                drawCar(c, w*(lane+.5f)/3f,h*.82f,w*.13f,if(mode==MotionGame.RIVER)cyan else green)
            }
            MotionGame.SPACE -> {
                for(i in 0..28) c.drawCircle(w*((i*37%97)/100f),h*((i*53%101)/100f),2f,white)
                objects.forEach{c.drawCircle(it.x*w,it.y*h,w*.035f,red)}; bullets.forEach{c.drawRoundRect(it.x*w-w*.012f,it.y*h,it.x*w+w*.012f,it.y*h+h*.045f,5f,5f,yellow)}
                val x=playerX*w; val y=.83f*h; val path=android.graphics.Path().apply{moveTo(x,y-h*.07f);lineTo(x-w*.06f,y+h*.04f);lineTo(x,y+h*.01f);lineTo(x+w*.06f,y+h*.04f);close()}; c.drawPath(path,cyan)
            }
            MotionGame.PACMAN, MotionGame.GHOST, MotionGame.BALL, MotionGame.ESCAPE -> {
                val cell=minOf(w/gridW,h/gridH); val ox=(w-cell*gridW)/2; val oy=(h-cell*gridH)/2
                // Recessed maze cells with subtle inner highlights.
                walls.forEach{(x,y)->
                    c.drawRoundRect(ox+x*cell+2,oy+y*cell+2,ox+(x+1)*cell-2,oy+(y+1)*cell-2,cell*.12f,cell*.12f,cyan)
                    p.color=Color.argb(95,255,255,255);p.style=Paint.Style.STROKE;p.strokeWidth=cell*.025f
                    c.drawRoundRect(ox+x*cell+cell*.09f,oy+y*cell+cell*.09f,ox+(x+1)*cell-cell*.09f,oy+(y+1)*cell-cell*.09f,cell*.10f,cell*.10f,p);p.style=Paint.Style.FILL
                }
                dots.forEach{(x,y)->
                    val dx=ox+(x+.5f)*cell;val dy=oy+(y+.5f)*cell
                    p.color=Color.argb(55,255,218,70);c.drawCircle(dx,dy,cell*.15f,p)
                    c.drawCircle(dx,dy,cell*.065f,yellow);c.drawCircle(dx-cell*.02f,dy-cell*.025f,cell*.022f,white)
                }
                ghosts.forEach{(x,y)-> GameGraphics.drawGhost(c,ox+(x+.5f)*cell,oy+(y+.5f)*cell,cell*.30f,elapsed,if(mode==MotionGame.ESCAPE)Color.rgb(188,105,255) else red.color) }
                if(mode==MotionGame.ESCAPE){
                    p.color=green.color
                    c.drawRoundRect(ox+7.42f*cell,oy+10.42f*cell,ox+8f*cell,oy+11f*cell,cell*.10f,cell*.10f,p)
                    p.color=Color.WHITE;p.style=Paint.Style.STROKE;p.strokeWidth=cell*.04f
                    c.drawRoundRect(ox+7.48f*cell,oy+10.48f*cell,ox+7.94f*cell,oy+10.94f*cell,cell*.08f,cell*.08f,p);p.style=Paint.Style.FILL
                }
                val px=ox+(gx+.5f)*cell;val py=oy+(gy+.5f)*cell
                if(mode==MotionGame.BALL){
                    c.drawCircle(px,py+cell*.035f,cell*.29f,p.apply{color=Color.argb(80,0,0,0)})
                    c.drawCircle(px,py,cell*.28f,purple)
                    c.drawCircle(px-cell*.08f,py-cell*.09f,cell*.075f,white)
                    p.color=Color.argb(80,255,255,255);p.style=Paint.Style.STROKE;p.strokeWidth=cell*.025f;c.drawCircle(px,py,cell*.24f,p);p.style=Paint.Style.FILL
                } else if(mode==MotionGame.PACMAN){
                    val mouth=(.10f+.36f*((kotlin.math.sin(elapsed*12f)+1f)*.5f))*180f
                    p.color=yellow.color
                    c.drawArc(RectF(px-cell*.34f,py-cell*.34f,px+cell*.34f,py+cell*.34f),mouth/2f,360f-mouth,true,p)
                    p.color=Color.rgb(34,39,55);c.drawCircle(px+cell*.09f,py-cell*.16f,cell*.045f,p)
                } else {
                    c.drawCircle(px,py,cell*.34f,yellow)
                    c.drawCircle(px+cell*.10f,py-cell*.10f,cell*.10f,white)
                    p.color=Color.BLACK;c.drawCircle(px+cell*.12f,py-cell*.08f,cell*.045f,p)
                }
            }
            MotionGame.JUNGLE -> {
                c.drawRect(0f,h*.83f,w,h,road);c.drawRect(0f,h*.83f,w,h*.85f,paint(Color.rgb(100,116,139)))
                objects.forEach{obj->
                    c.drawOval(obj.x*w,h*.81f,obj.x*w+w*.16f,h*.86f,paint(Color.argb(75,0,0,0)))
                    c.drawRoundRect(obj.x*w,h*.74f,obj.x*w+w*.14f,h*.84f,8f,8f,red)
                    c.drawLine(obj.x*w+w*.04f,h*.76f,obj.x*w+w*.04f,h*.82f,paint(Color.argb(90,255,255,255)).apply{strokeWidth=w*.008f})
                }
                GameGraphics.drawRunner(c,w*.22f,h*.83f,w*.10f,elapsed,jump*h*.38f)
            }
            MotionGame.BUBBLES -> {
                c.drawRoundRect(playerX*w-w*.12f,h*.84f,playerX*w+w*.12f,h*.89f,12f,12f,cyan)
                objects.forEach{c.drawCircle(it.x*w,it.y*h,w*.035f,if(it.type==1)red else cyan)}
                p.color=white.color;p.textSize=w*.038f;p.textAlign=Paint.Align.RIGHT
                c.drawText("الفائت: $missedBubbles/3",w*.96f,h*.07f,p)
                p.textAlign=Paint.Align.LEFT
            }
        }
        p.color=white.color;p.textSize=w*.045f;p.textAlign=Paint.Align.LEFT;c.drawText("النقاط: $score",w*.04f,h*.07f,p)
        if(!started||over){p.color=Color.argb(210,0,0,0);c.drawRoundRect(w*.08f,h*.37f,w*.92f,h*.61f,18f,18f,p);p.color=white.color;p.textAlign=Paint.Align.CENTER;p.textSize=w*.052f;c.drawText(if(over)"انتهت اللعبة — $score" else "المس الشاشة لبدء اللعب",w*.5f,h*.48f,p);p.textSize=w*.032f;c.drawText("${mode.hint}",w*.5f,h*.54f,p)}
    }
    private fun drawCar(c:Canvas,x:Float,y:Float,size:Float,body:Paint){
        // Tires, chassis, windshield, reflections and lights give the cars more depth.
        val tire=paint(Color.rgb(17,24,39));val rim=paint(Color.rgb(148,163,184))
        c.drawRoundRect(x-size*.43f,y-size*.40f,x-size*.28f,y-size*.12f,4f,4f,tire)
        c.drawRoundRect(x+size*.28f,y-size*.40f,x+size*.43f,y-size*.12f,4f,4f,tire)
        c.drawRoundRect(x-size*.43f,y+size*.18f,x-size*.28f,y+size*.43f,4f,4f,tire)
        c.drawRoundRect(x+size*.28f,y+size*.18f,x+size*.43f,y+size*.43f,4f,4f,tire)
        c.drawRoundRect(x-size*.36f,y-size*.60f,x+size*.36f,y+size*.60f,size*.13f,size*.13f,body)
        c.drawRoundRect(x-size*.24f,y-size*.36f,x+size*.24f,y-size*.04f,size*.07f,size*.07f,paint(Color.rgb(147,220,245)))
        c.drawRoundRect(x-size*.22f,y+size*.06f,x+size*.22f,y+size*.32f,size*.05f,size*.05f,paint(Color.rgb(30,41,59)))
        c.drawRoundRect(x-size*.29f,y-size*.52f,x-size*.20f,y-size*.30f,size*.03f,size*.03f,white)
        c.drawRoundRect(x+size*.20f,y-size*.52f,x+size*.29f,y-size*.30f,size*.03f,size*.03f,white)
        c.drawRoundRect(x-size*.28f,y+size*.43f,x-size*.12f,y+size*.51f,size*.03f,size*.03f,paint(Color.rgb(255,241,170)))
        c.drawRoundRect(x+size*.12f,y+size*.43f,x+size*.28f,y+size*.51f,size*.03f,size*.03f,paint(Color.rgb(255,241,170)))
        c.drawLine(x-size*.20f,y-size*.47f,x-size*.20f,y-size*.34f,paint(Color.argb(90,255,255,255)).apply{strokeWidth=size*.025f})
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{
        when(e.action){MotionEvent.ACTION_DOWN->{grab();touchX=e.x;touchY=e.y;if(!started||over){startGame();return true}; if(mode==MotionGame.JUNGLE && jump <= 0f){jump=1f}; if(mode==MotionGame.PACMAN||mode==MotionGame.GHOST||mode==MotionGame.BALL||mode==MotionGame.ESCAPE){moveX=0;moveY=0}; return true}
            MotionEvent.ACTION_UP->{val dx=e.x-touchX;val dy=e.y-touchY
                when(mode){MotionGame.CAR,MotionGame.BIKE,MotionGame.RIVER->{if(abs(dx)>dp(18)) lane=(lane+if(dx>0)1 else -1).coerceIn(0,2) else lane=when{e.x<width/3f->(lane-1).coerceAtLeast(0);e.x>width*2/3f->(lane+1).coerceAtMost(2);else->lane}}
                    MotionGame.PACMAN,MotionGame.GHOST,MotionGame.BALL,MotionGame.ESCAPE->{if(abs(dx)>abs(dy)){moveX=if(dx>0)1 else -1;moveY=0}else{moveY=if(dy>0)1 else -1;moveX=0}}
                    MotionGame.SPACE,MotionGame.BUBBLES->{playerX=(e.x/width).coerceIn(.08f,.92f)}
                    MotionGame.JUNGLE -> { /* القفز يبدأ عند لمس الشاشة، ولا يتغير عند رفع الإصبع */ }
                };return true}
            MotionEvent.ACTION_MOVE->{if(mode==MotionGame.SPACE||mode==MotionGame.BUBBLES)playerX=(e.x/width).coerceIn(.08f,.92f);return true}}
        return true
    }
    private var touchX=0f; private var touchY=0f
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toFloat()
}

abstract class MotionGameActivity : BaseGameActivity() {
    protected abstract val mode: MotionGame
    private lateinit var game: MotionArcadeView
    private lateinit var scoreLabel: TextView
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); initScreen("${mode.title}")
        content.addView(label(mode.hint,14f,R.color.text_dim,false),lp(top=dp(4),bottom=dp(5)))
        scoreLabel=label("النقاط: 0",20f);content.addView(scoreLabel,lp(bottom=dp(4)))
        game=MotionArcadeView(this,mode,{s->scoreLabel.text="النقاط: $s"},{s->scoreLabel.text="انتهت اللعبة — النقاط: $s"})
        content.addView(game,lp())
        if (mode == MotionGame.PACMAN || mode == MotionGame.GHOST || mode == MotionGame.BALL || mode == MotionGame.ESCAPE) {
            content.addView(label("أزرار الحركة", 13f, R.color.text_dim, false), lp(top = dp(2), bottom = dp(1)))
            content.addView(Ui.directionPad(this) { dx, dy -> game.moveDirection(dx, dy) }, lp())
        }
        content.addView(button("ابدأ / أعد اللعب"){game.startGame()},lp(top=dp(4),bottom=dp(8)))
    }
    override fun onPause(){if(::game.isInitialized)game.stopLoop();super.onPause()}
}
class CarRaceActivity:MotionGameActivity(){override val mode=MotionGame.CAR}
class PacManActivity:MotionGameActivity(){override val mode=MotionGame.PACMAN}
class GhostMazeActivity:MotionGameActivity(){override val mode=MotionGame.GHOST}
class SpaceShooterActivity:MotionGameActivity(){override val mode=MotionGame.SPACE}
class BikeRaceActivity:MotionGameActivity(){override val mode=MotionGame.BIKE}
class RiverRunActivity:MotionGameActivity(){override val mode=MotionGame.RIVER}
class BallMazeActivity:MotionGameActivity(){override val mode=MotionGame.BALL}
class JungleRunnerActivity:MotionGameActivity(){override val mode=MotionGame.JUNGLE}
class BubbleCatchActivity:MotionGameActivity(){override val mode=MotionGame.BUBBLES}
class MazeEscapeActivity:MotionGameActivity(){override val mode=MotionGame.ESCAPE}
