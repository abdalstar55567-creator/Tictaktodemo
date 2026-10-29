package com.example.tictactoe

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin

/** Shared, asset-free Canvas artwork used by the arcade games. */
object GameGraphics {
    private fun p(color: Int, style: Paint.Style = Paint.Style.FILL): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = style
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun drawBird(c: Canvas, x: Float, y: Float, r: Float, time: Float, tilt: Float = 0f) {
        c.save()
        c.rotate(tilt.coerceIn(-28f, 35f), x, y)
        // Tail feathers
        val tail = Path().apply { moveTo(x-r*.72f,y); lineTo(x-r*1.35f,y-r*.22f); lineTo(x-r*1.27f,y+r*.22f); close() }
        c.drawPath(tail, p(Color.rgb(224,145,12)))
        // soft body and belly
        c.drawOval(x-r, y-r*.76f, x+r*.92f, y+r*.76f, p(Color.rgb(255,190,35)))
        c.drawOval(x-r*.35f, y-r*.02f, x+r*.62f, y+r*.58f, p(Color.rgb(255,224,116)))
        // animated wing
        val flap = sin(time*15f)*.20f
        c.save(); c.rotate(flap*50f, x-r*.25f, y+r*.05f)
        c.drawOval(x-r*.72f,y-r*.20f,x+r*.10f,y+r*.43f,p(Color.rgb(236,155,18)))
        c.drawArc(RectF(x-r*.57f,y-r*.12f,x-r*.02f,y+r*.30f),195f,105f,false,p(Color.rgb(255,216,88),Paint.Style.STROKE).apply{strokeWidth=r*.06f})
        c.restore()
        // beak, nostril, eye
        val beak = Path().apply { moveTo(x+r*.70f,y-r*.15f); lineTo(x+r*1.52f,y+r*.02f); lineTo(x+r*.70f,y+r*.25f); close() }
        c.drawPath(beak,p(Color.rgb(245,94,35)))
        c.drawLine(x+r*.82f,y+r*.08f,x+r*1.35f,y+r*.08f,p(Color.rgb(198,61,20)).apply{strokeWidth=r*.035f})
        c.drawCircle(x+r*.38f,y-r*.34f,r*.25f,p(Color.WHITE))
        c.drawCircle(x+r*.47f,y-r*.34f,r*.13f,p(Color.rgb(24,34,49)))
        c.drawCircle(x+r*.50f,y-r*.39f,r*.045f,p(Color.WHITE))
        c.restore()
    }

    fun drawGhost(c: Canvas, cx: Float, cy: Float, r: Float, time: Float, tint: Int = Color.rgb(255,90,125)) {
        val bob = sin(time*4.2f + cx*.01f)*r*.045f
        val y = cy+bob
        val body = Path().apply {
            moveTo(cx-r, y+r*.72f); lineTo(cx-r,y-r*.18f)
            cubicTo(cx-r,y-r*.82f,cx-r*.45f,y-r,cx,y-r)
            cubicTo(cx+r*.55f,y-r,cx+r,y-r*.70f,cx+r,y-r*.12f)
            lineTo(cx+r,y+r*.68f)
            cubicTo(cx+r*.68f,y+r*.43f,cx+r*.53f,y+r*.90f,cx+r*.28f,y+r*.70f)
            cubicTo(cx+r*.05f,y+r*.48f,cx-r*.12f,y+r*.94f,cx-r*.35f,y+r*.70f)
            cubicTo(cx-r*.62f,y+r*.48f,cx-r*.78f,y+r*.87f,cx-r,y+r*.72f)
            close()
        }
        c.drawPath(body,p(Color.argb(65,0,0,0)))
        c.save(); c.translate(0f,-r*.04f)
        c.drawPath(body,p(tint))
        c.drawArc(RectF(cx-r*.82f,y-r*.78f,cx+r*.82f,y+r*.62f),205f,130f,false,p(Color.WHITE).apply{alpha=85;style=Paint.Style.STROKE;strokeWidth=r*.07f})
        // frightened eyes and animated opening/closing mouth
        c.drawOval(cx-r*.52f,y-r*.42f,cx-r*.13f,y+r*.02f,p(Color.WHITE))
        c.drawOval(cx+r*.13f,y-r*.42f,cx+r*.52f,y+r*.02f,p(Color.WHITE))
        c.drawCircle(cx-r*.31f,y-r*.19f,r*.105f,p(Color.rgb(31,40,67)))
        c.drawCircle(cx+r*.31f,y-r*.19f,r*.105f,p(Color.rgb(31,40,67)))
        val open = .18f + .82f*((sin(time*10f)+1f)*.5f)
        c.drawOval(cx-r*.20f,y+r*.12f,cx+r*.20f,y+r*(.12f+.43f*open),p(Color.rgb(49,25,48)))
        if(open>.48f) c.drawRoundRect(cx-r*.11f,y+r*.13f,cx+r*.11f,y+r*.23f,r*.04f,r*.04f,p(Color.WHITE))
        c.restore()
    }

    fun drawRunner(c: Canvas, cx: Float, groundY: Float, size: Float, time: Float, jumpOffset: Float = 0f) {
        val y = groundY-jumpOffset
        val swing = sin(time*14f)*size*.22f
        val shadow = p(Color.argb((90*(1f-jumpOffset/(size*2f)).coerceIn(.15f,1f)).toInt(),0,0,0))
        c.drawOval(cx-size*.48f,groundY-size*.07f,cx+size*.48f,groundY+size*.08f,shadow)
        // head, neck and torso
        c.drawCircle(cx,y-size*.88f,size*.22f,p(Color.rgb(244,190,143)))
        c.drawOval(cx-size*.22f,y-size*.70f,cx+size*.22f,y-size*.20f,p(Color.rgb(37,156,207)))
        c.drawLine(cx-size*.08f,y-size*.55f,cx-size*.40f+ swing,y-size*.34f,p(Color.rgb(244,190,143)).apply{strokeWidth=size*.12f})
        c.drawLine(cx+size*.08f,y-size*.55f,cx+size*.37f-swing,y-size*.34f,p(Color.rgb(244,190,143)).apply{strokeWidth=size*.12f})
        c.drawLine(cx-size*.08f,y-size*.22f,cx-size*.27f+swing,y+size*.02f,p(Color.rgb(35,73,117)).apply{strokeWidth=size*.14f})
        c.drawLine(cx+size*.08f,y-size*.22f,cx+size*.30f-swing,y+size*.02f,p(Color.rgb(35,73,117)).apply{strokeWidth=size*.14f})
        c.drawLine(cx-size*.27f+swing,y+size*.02f,cx-size*.42f+swing,y+size*.02f,p(Color.rgb(238,242,248)).apply{strokeWidth=size*.10f})
        c.drawLine(cx+size*.30f-swing,y+size*.02f,cx+size*.45f-swing,y+size*.02f,p(Color.rgb(238,242,248)).apply{strokeWidth=size*.10f})
        // hair and face detail
        c.drawArc(RectF(cx-size*.23f,y-size*1.10f,cx+size*.23f,y-size*.72f),185f,170f,false,p(Color.rgb(64,43,34)).apply{style=Paint.Style.STROKE;strokeWidth=size*.10f})
        c.drawCircle(cx+size*.08f,y-size*.91f,size*.025f,p(Color.rgb(40,35,33)))
    }

    fun drawHurdle(c: Canvas, left: Float, top: Float, right: Float, ground: Float, time: Float = 0f) {
        val width=right-left
        c.drawOval(left-width*.18f,ground-width*.06f,right+width*.18f,ground+width*.08f,p(Color.argb(70,0,0,0)))
        c.drawRoundRect(left,top,right,ground,width*.10f,width*.10f,p(Color.rgb(173,43,59)))
        c.drawRoundRect(left+width*.10f,top+width*.08f,right-width*.10f,top+width*.24f,width*.04f,width*.04f,p(Color.rgb(255,210,62)))
        c.drawRoundRect(left+width*.10f,top+width*.39f,right-width*.10f,top+width*.53f,width*.04f,width*.04f,p(Color.rgb(255,210,62)))
        c.drawLine(left+width*.16f,top+width*.10f,left+width*.16f,ground-width*.08f,p(Color.WHITE).apply{alpha=55;strokeWidth=width*.035f})
        c.drawRoundRect(left-width*.07f,ground-width*.10f,right+width*.07f,ground,width*.04f,width*.04f,p(Color.rgb(105,32,46)))
    }
}
