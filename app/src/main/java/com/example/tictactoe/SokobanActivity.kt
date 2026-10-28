package com.example.tictactoe

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout

/** ادفع الصناديق إلى الأهداف؛ لا يمكن سحب الصندوق للخلف. */
class SokobanActivity : BaseGameActivity() {
    override val gameTitle = "دافع الصناديق"
    private val original = listOf("########", "#......#", "#.B....#", "#..##..#", "#..G...#", "#......#", "########")
    private var walls = mutableSetOf<Pair<Int,Int>>()
    private var goals = mutableSetOf(4 to 3)
    private var box = 2 to 2
    private var player = 1 to 1
    private lateinit var board: LinearLayout
    private lateinit var status: android.widget.TextView
    private var moves = 0
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); initScreen(gameTitle)
        for (y in original.indices) for (x in original[y].indices) if (original[y][x]=='#') walls.add(x to y)
        status=label("ادفع الصندوق 🟫 فوق الهدف 🟢 دون أن تحشره في زاوية.",15f); content.addView(status,lp(top=4,bottom=10))
        board=LinearLayout(this); board.orientation=LinearLayout.VERTICAL; content.addView(board,lp())
        content.addView(Ui.directionPad(this) { dx, dy -> move(dx, dy) }, lp(top = dp(4)))
        content.addView(button("إعادة المرحلة",false){ reset() },lp(top=8)); drawBoard()
    }
    private fun move(dx:Int,dy:Int){ val nx=player.first+dx; val ny=player.second+dy; if(nx to ny in walls)return
        if(nx==box.first && ny==box.second){ val bx=nx+dx; val by=ny+dy; if(bx to by in walls || (bx==player.first && by==player.second))return; box=bx to by }
        player=nx to ny; moves++; drawBoard(); if(box in goals){ status.text="أحسنت! أنهيت المرحلة في $moves حركة 🎉" }
    }
    private fun reset(){player=1 to 1;box=2 to 2;moves=0;drawBoard()}
    private fun drawBoard(){board.removeAllViews(); for(y in original.indices){val r=row();for(x in original[y].indices){val pos=x to y; val tile=when{pos in walls->"⬛";pos==player->"🧍";pos==box && pos in goals->"🟩";pos==box->"📦";pos in goals->"🟢";else->"·"};r.addView(button(tile,false){}, LinearLayout.LayoutParams(0, dp(40), 1f)) ;};board.addView(r,lp())}; status.text="الحركات: $moves — ادفع الصندوق إلى الهدف."}
}
