package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout

/** أدر قطع الأنابيب حتى يتصل المصدر أعلى اليسار بالمخرج أسفل اليمين. */
class PipeConnectActivity:BaseGameActivity(){
 override val gameTitle="وصل الأنابيب"
 private val pieces= arrayOf(intArrayOf(1,2,1),intArrayOf(0,1,2),intArrayOf(1,0,1)) // 0=─ 1=│ 2=└ 3=┌ 4=┐ 5=┘
 private val shapes=arrayOf("─","│","└","┌","┐","┘")
 private val tiles=Array(3){IntArray(3){0}};private lateinit var board:LinearLayout;private lateinit var info:android.widget.TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("اضغط القطعة لتدويرها. اجعل مسار الماء متصلاً من 💧 إلى 🚰.",15f);content.addView(info,lp(bottom=10));board=LinearLayout(this);board.orientation=LinearLayout.VERTICAL;content.addView(board,lp());content.addView(button("إعادة ترتيب",false){setup()},lp(top=10));setup()}
 private fun setup(){val a=arrayOf(intArrayOf(1,3,2),intArrayOf(4,0,3),intArrayOf(0,2,1));for(y in 0..2)for(x in 0..2)tiles[y][x]=a[y][x];draw()}
 private fun rotate(x:Int,y:Int){tiles[y][x]=(tiles[y][x]+1)%6;draw()}
 private fun draw(){board.removeAllViews();for(y in 0..2){val r=row();for(x in 0..2){val icon=if(x==0&&y==0)"💧"+shapes[tiles[y][x]] else if(x==2&&y==2)shapes[tiles[y][x]]+"🚰" else shapes[tiles[y][x]];r.addView(button(icon,false){rotate(x,y)},LinearLayout.LayoutParams(0,dp(70),1f))};board.addView(r,lp())};if(tiles[0][0]==2&&tiles[0][1]==0&&tiles[0][2]==4&&tiles[1][0]==1&&tiles[1][1]==1&&tiles[1][2]==1&&tiles[2][0]==3&&tiles[2][1]==0&&tiles[2][2]==5)info.text="أحسنت! المسار مضبوط — تحقق من اتصال القطع المجاورة." else info.text="أدر القطع لتكوين مسار متصل. (─ أفقي، │ عمودي، والقطع المنحنية للزوايا)"}
}
