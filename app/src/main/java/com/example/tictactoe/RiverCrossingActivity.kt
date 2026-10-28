package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout

/** انقل الذئب والماعز والملفوف؛ لا تترك الذئب مع الماعز أو الماعز مع الملفوف دون المزارع. */
class RiverCrossingActivity:BaseGameActivity(){
 override val gameTitle="عبور النهر"
 private var farmer=0;private var wolf=0;private var goat=0;private var cabbage=0;private var trips=0;private lateinit var info:android.widget.TextView;private lateinit var sides:android.widget.TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("انقل الجميع للضفة اليمنى. القارب يحمل المزارع وشيئاً واحداً فقط.",15f);content.addView(info,lp(bottom=10));sides=label("",20f);content.addView(sides,lp(bottom=12));listOf("اعبر وحدك" to -1,"خذ الذئب 🐺" to 1,"خذ الماعز 🐐" to 2,"خذ الملفوف 🥬" to 3).forEach{(s,v)->content.addView(button(s){cross(v)},lp(top=5))};content.addView(button("إعادة اللغز",false){reset()},lp(top=10));draw()}
 private fun cross(item:Int){val side=farmer;if(item==1&&wolf!=side||item==2&&goat!=side||item==3&&cabbage!=side){info.text="هذا العنصر ليس على ضفتك!";return};farmer=1-farmer;if(item==1)wolf=1-wolf;if(item==2)goat=1-goat;if(item==3)cabbage=1-cabbage;trips++;if((wolf==goat&&farmer!=wolf)||(goat==cabbage&&farmer!=goat)){reset(false);info.text="حدثت مشكلة: لا تترك الذئب مع الماعز أو الماعز مع الملفوف. حاول مجدداً.";return};draw();if(farmer==1&&wolf==1&&goat==1&&cabbage==1)info.text="نجحت في عبور الجميع خلال $trips رحلة! 🎉"}
 private fun reset(show:Boolean=true){farmer=0;wolf=0;goat=0;cabbage=0;trips=0;if(show)info.text="ابدأ بنقل الماعز غالباً!";draw()}
 private fun draw(){sides.text="الضفة اليسرى                    الضفة اليمنى\n"+listOf("🧑" to farmer,"🐺" to wolf,"🐐" to goat,"🥬" to cabbage).joinToString("     "){(e,s)->if(s==0)e else "　"}+"\n"+listOf("🧑" to farmer,"🐺" to wolf,"🐐" to goat,"🥬" to cabbage).joinToString("     "){(e,s)->if(s==1)e else "　"};info.text="عدد الرحلات: $trips"}
}
