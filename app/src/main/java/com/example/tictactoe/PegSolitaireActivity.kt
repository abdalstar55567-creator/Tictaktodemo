package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout

/** اقفز بقطعة فوق أخرى لإزالتها، وحاول إبقاء قطعة واحدة. */
class PegSolitaireActivity:BaseGameActivity(){
 override val gameTitle="قفز القطع"
 private val cells=mutableSetOf<Int>();private var selected=-1;private var moves=0;private lateinit var board:LinearLayout;private lateinit var info:android.widget.TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("اختر قطعة ثم اختر خانة فارغة تبعد خانتين أفقياً أو عمودياً للقفز فوق قطعة وإزالتها.",14f);content.addView(info,lp(bottom=8));board=LinearLayout(this);board.orientation=LinearLayout.VERTICAL;content.addView(board,lp());content.addView(button("إعادة اللعب",false){reset()},lp(top=10));reset()}
 private fun reset(){cells.clear();for(i in 0..24)if(i!=12)cells.add(i);selected=-1;moves=0;draw()}
 private fun tap(i:Int){if(i !in 0..24)return;if(selected<0){if(i in cells)selected=i}else{val sx=selected%5;val sy=selected/5;val tx=i%5;val ty=i/5;val mx=(sx+tx)/2;val my=(sy+ty)/2;if(i !in cells&&((sx==tx&&kotlin.math.abs(sy-ty)==2)||(sy==ty&&kotlin.math.abs(sx-tx)==2))&&(mx+my*5) in cells){cells.remove(selected);cells.remove(mx+my*5);cells.add(i);moves++};selected=-1};draw()}
 private fun draw(){board.removeAllViews();info.text="القطع المتبقية: ${cells.size} — الحركات: $moves";for(y in 0..4){val r=row();for(x in 0..4){val i=x+y*5;r.addView(button(if(i in cells)if(i==selected)"🔵" else "🟣" else "⚪",i==selected){tap(i)},LinearLayout.LayoutParams(0,dp(48),1f))};board.addView(r,lp())};if(cells.size==1)info.text="حل مثالي! بقيت قطعة واحدة 🎉"}
}
