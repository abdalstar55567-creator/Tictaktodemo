package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout

/** لعبة أعواد استراتيجية ضد الحاسوب: خذ 1 أو 2 أو 3، ومن يأخذ آخر عود يخسر. */
class NimStrategyActivity:BaseGameActivity(){
 override val gameTitle="تحدي الأعواد"
 private var sticks=17;private var busy=false;private lateinit var info:android.widget.TextView;private lateinit var pile:android.widget.TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("خذ من 1 إلى 3 أعواد. من يأخذ العود الأخير يخسر!",15f);content.addView(info,lp(bottom=12));pile=label("",23f);content.addView(pile,lp(bottom=12));val r=row();for(n in 1..3)r.addView(button("خذ $n",true){take(n)},weightLp(dp(58)));content.addView(r,lp());content.addView(button("جولة جديدة",false){sticks=17;busy=false;draw()},lp(top=12));draw()}
 private fun take(n:Int){if(busy||sticks<=0)return;if(n>sticks)return;sticks-=n;if(sticks==0){info.text="أخذت آخر عود؛ خسرت الجولة!";draw();return};busy=true;draw();pile.postDelayed({val take=when(sticks%4){1->1;2->1;3->2;else->3}.coerceAtMost(sticks);sticks-=take;busy=false;if(sticks==0)info.text="الحاسوب أخذ آخر عود وخسر! أنت الفائز 🎉" else info.text="الحاسوب أخذ $take عوداً. دورك.";draw()},650)}
 private fun draw(){pile.text=(1..sticks).joinToString(" "){"🪵"}+"\nالمتبقي: $sticks"}
}
