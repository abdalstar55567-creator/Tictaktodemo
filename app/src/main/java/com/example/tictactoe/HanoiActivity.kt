package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout

/** برج هانوي: انقل الأقراص إلى العمود الأخير ولا تضع قرصاً كبيراً فوق صغير. */
class HanoiActivity : BaseGameActivity() {
    override val gameTitle="أبراج هانوي"
    private val towers= mutableListOf(mutableListOf(4,3,2,1), mutableListOf<Int>(), mutableListOf<Int>())
    private var selected=-1; private var moves=0; private lateinit var info:android.widget.TextView; private lateinit var area:LinearLayout
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("انقل الأقراص إلى العمود الثالث. القرص الأصغر فقط يوضع فوق الأكبر.",15f);content.addView(info,lp(bottom=12));area=LinearLayout(this);area.orientation=LinearLayout.VERTICAL;content.addView(area,lp());draw();content.addView(button("إعادة من البداية",false){reset()},lp(top=14))}
    private fun tap(col:Int){if(selected<0){if(towers[col].isNotEmpty())selected=col}else if(selected==col){selected=-1}else{val from=towers[selected];val to=towers[col];if(from.isNotEmpty()&&(to.isEmpty()||to.last()>from.last())){to.add(from.removeAt(from.lastIndex));moves++};selected=-1};draw();if(towers[2].size==4)info.text="رائع! أنهيت الأبراج في $moves حركة."}
    private fun reset(){towers[0].clear();towers[0].addAll(listOf(4,3,2,1));towers[1].clear();towers[2].clear();moves=0;selected=-1;draw()}
    private fun draw(){area.removeAllViews();info.text="الحركات: $moves — ${if(selected>=0) "اختر عموداً لنقل القرص إليه" else "اختر العمود الذي تريد أخذ قرصه"}";val r=row();for(i in 0..2){val col=LinearLayout(this);col.orientation=LinearLayout.VERTICAL;col.setPadding(dp(3),dp(4),dp(3),dp(4));col.background=Ui.panelBg(this,if(i==selected)0xFFB7D9F8.toInt() else 0xFFE8EDF3.toInt());col.addView(label("العمود ${i+1}",14f));val disks=towers[i];for(d in 4 downTo 1){val v=if(disks.contains(d)) "━".repeat(d*2+1) else "　";col.addView(label(v,18f,if(disks.contains(d)) R.color.gold else R.color.text_main))};col.addView(button("اختيار",i==selected){tap(i)});r.addView(col,LinearLayout.LayoutParams(0,dp(250),1f))};area.addView(r,lp())}
}
