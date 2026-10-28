package com.example.tictactoe

import android.os.Bundle
import android.widget.LinearLayout
import kotlin.random.Random

/** فك شفرة من أربعة ألوان؛ التلميح يوضح الصحيح في المكان الصحيح والصحيح في مكان آخر. */
class MastermindActivity : BaseGameActivity() {
 override val gameTitle="فك الشفرة"
 private val colors=listOf("🔴","🔵","🟢","🟡","🟣","🟠")
 private var secret=List(4){Random.nextInt(colors.size)};private var guess= mutableListOf<Int>();private var tries=0;private lateinit var info:android.widget.TextView;private lateinit var history:LinearLayout;private lateinit var current:android.widget.TextView
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);initScreen(gameTitle);info=label("اختر أربعة ألوان بالترتيب. لديك 8 محاولات.",15f);content.addView(info,lp(bottom=8));current=label("الرمز: — — — —",24f);content.addView(current,lp(bottom=8));val r=row();colors.forEachIndexed{i,c->r.addView(button(c){if(guess.size<4){guess.add(i);refresh()}},weightLp(dp(54)))};content.addView(r,lp());content.addView(button("فحص التخمين",true){checkGuess()},lp(top=8));history=LinearLayout(this);history.orientation=LinearLayout.VERTICAL;content.addView(history,lp(top=12));content.addView(button("شفرة جديدة",false){reset()},lp(top=8))}
 private fun refresh(){current.text="الرمز: "+(guess.map{colors[it]}+List(4-guess.size){"▫️"}).joinToString(" ")}
 private fun checkGuess(){if(guess.size!=4){info.text="اختر أربعة ألوان أولاً.";return};tries++;val exact=(0..3).count{guess[it]==secret[it]};val a=guess.toMutableList();val b=secret.toMutableList();for(i in 3 downTo 0)if(a[i]==b[i]){a.removeAt(i);b.removeAt(i)};val misplaced=a.count{v->val ix=b.indexOf(v);if(ix>=0){b.removeAt(ix);true}else false};history.addView(label("$tries. ${guess.joinToString(" "){colors[it]}}   ✓ $exact   ↔ $misplaced",16f),lp(bottom=4));if(exact==4){info.text="فككت الشفرة! 🎉";lock(true)}else if(tries>=8){info.text="انتهت المحاولات. الشفرة: ${secret.joinToString(" "){colors[it]}}";lock(true)};guess.clear();refresh()}
 private fun lock(done:Boolean){/* اللعبة تبقى قابلة لإعادة التشغيل؛ يمنع الإدخال عبر رسالة الحالة */}
 private fun reset(){secret=List(4){Random.nextInt(colors.size)};guess.clear();tries=0;history.removeAllViews();info.text="اختر أربعة ألوان بالترتيب. لديك 8 محاولات.";refresh()}
}
