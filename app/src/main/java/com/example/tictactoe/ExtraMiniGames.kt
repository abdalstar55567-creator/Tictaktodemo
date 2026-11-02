package com.example.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import kotlin.random.Random

/** Ten lightweight Arabic mini-games. They use the app's shared visual components and need no assets. */
abstract class ExtraMiniGameActivity : BaseGameActivity() {
    protected var points = 0
    protected var rounds = 0
    protected var ended = false
    protected lateinit var status: android.widget.TextView
    protected fun prepare(title: String, hint: String) {
        initScreen(title)
        content.addView(label(hint, 14f, R.color.text_dim, false), lp(bottom = dp(8)))
        status = label("النقاط: 0", 20f)
        content.addView(status, lp(bottom = dp(8)))
    }
    protected fun updateStatus(message: String = "") {
        status.text = if (message.isEmpty()) "النقاط: $points" else "النقاط: $points\n$message"
    }
    protected fun resetScore() { points = 0; rounds = 0; ended = false; updateStatus() }
}

class TargetTapActivity : ExtraMiniGameActivity() {
    private lateinit var grid: LinearLayout
    private var target = 0
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); prepare("🎯 صائد الهدف", "اضغط على الدائرة الذهبية واجمع 10 إصابات قبل انتهاء المحاولات.")
        grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(grid, lp())
        content.addView(button("ابدأ من جديد") { resetScore(); buildGrid() }, lp(top = dp(8)))
        buildGrid()
    }
    private fun buildGrid() {
        grid.removeAllViews(); target = Random.nextInt(9)
        for (r in 0..2) { val line = row(4); for (c in 0..2) { val i = r*3+c
            line.addView(button(if(i==target) "🎯" else listOf("🔵","🟣","🟢","⚪").random(), false) { if(!ended) { rounds++; if(i==target) points++ else points=(points-1).coerceAtLeast(0); if(points>=10){ended=true;updateStatus("فزت! أحسنت 👏")} else if(rounds>=25){ended=true;updateStatus("انتهت المحاولات. حاول مجدداً")} else { updateStatus(); buildGrid() } } }, weightLp(dp(66)))
        }; grid.addView(line) }
    }
}

class PongSoloActivity : ExtraMiniGameActivity() {
    private var paddle = 1
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); prepare("🏓 تنس الطاولة", "حرّك المضرب إلى جهة الكرة قبل كل إرسال.")
        content.addView(label("🏓\n\n       ●\n━━━━━━━━━━", 30f), lp(top=dp(12), bottom=dp(12)))
        val line = row(); line.addView(button("⬅ يسار", false) { paddle=0; updateStatus("المضرب يسار") }, weightLp(dp(56))); line.addView(button("وسط", false) { paddle=1; updateStatus("المضرب في الوسط") }, weightLp(dp(56))); line.addView(button("يمين ➡", false) { paddle=2; updateStatus("المضرب يمين") }, weightLp(dp(56))); content.addView(line)
        content.addView(button("إرسال الكرة") { if(!ended){ val ball=Random.nextInt(3); rounds++; if(ball==paddle){points++; updateStatus("صدّ ناجح! 🟢")}else{points=(points-1).coerceAtLeast(0);updateStatus("فاتتك الكرة! الكرة في ${listOf("اليسار","الوسط","اليمين")[ball]}")}; if(rounds>=12){ended=true;updateStatus(if(points>=8) "فزت بالمباراة!" else "انتهت المباراة — جرّب مرة أخرى")} } }, lp(top=dp(12)))
        content.addView(button("إعادة المباراة") { resetScore(); paddle=1 }, lp(top=dp(6)))
    }
}

class BasketShotActivity : ExtraMiniGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); prepare("🏀 تصويب السلة", "اختر قوة الرمية ثم حاول إدخال الكرة في السلة.")
        content.addView(label("      🏀\n\n   🟠────⭕\n      🏀", 38f), lp(top=dp(10),bottom=dp(10)))
        val power = intArrayOf(2)
        content.addView(label("قوة الرمية",16f),lp())
        val line=row(); for(i in 1..3) line.addView(button("${i*33}%",false){power[0]=i;updateStatus("القوة المختارة: ${i*33}%")},weightLp(dp(55)));content.addView(line)
        content.addView(button("ارمِ الكرة") { if(!ended){ rounds++; val hit = Random.nextInt(100) < (if(power[0]==2) 68 else if(power[0]==1) 48 else 43); if(hit)points+=2 else points=(points-1).coerceAtLeast(0); updateStatus(if(hit) "سلة رائعة! +2" else "لم تدخل الكرة"); if(rounds>=10){ended=true;updateStatus(if(points>=12) "فوز ممتاز!" else "انتهت الجولة")} } },lp(top=dp(10)))
        content.addView(button("إعادة اللعب") { resetScore() },lp(top=dp(6)))
    }
}

class GemCollectorActivity : ExtraMiniGameActivity() {
    private lateinit var grid: LinearLayout
    private var gems=0; private var lives=3
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); prepare("💎 جامع الجواهر", "اجمع الجواهر 💎 وتجنب القنابل 💣. لديك 3 فرص.")
        grid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};content.addView(grid,lp());content.addView(button("إعادة اللعب"){gems=0;lives=3;resetScore();fill()},lp(top=dp(8)));fill()
    }
    private fun fill(){grid.removeAllViews();for(r in 0..2){val line=row(4);for(c in 0..2){val item=if(Random.nextInt(5)==0)"💣" else "💎";val tile=button("❔",false){if(!ended){if(item=="💎"){gems++;points+=2}else lives--;updateStatus("الجواهر: $gems | الفرص: $lives");if(gems>=10||lives<=0){ended=true;updateStatus(if(gems>=10)"جمعت ما يكفي من الجواهر!" else "انتهت الفرص")};fill()}};line.addView(tile,weightLp(dp(66)))};grid.addView(line)}}
}

class BubblePopActivity : ExtraMiniGameActivity() {
    private lateinit var grid: LinearLayout
    private var misses=0
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("🫧 فرقع الفقاعات","اضغط الفقاعات الزرقاء للحصول على النقاط وتجنب الحمراء.");grid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};content.addView(grid,lp());content.addView(button("إعادة اللعب"){resetScore();misses=0;fill()},lp(top=dp(8)));fill() }
    private fun fill(){grid.removeAllViews();for(r in 0..3){val line=row(3);for(c in 0..3){val good=Random.nextInt(4)!=0;line.addView(button("🫧",false){if(!ended){if(good)points++ else {points=(points-1).coerceAtLeast(0);misses++};rounds++;updateStatus(if(good)"فقاعة ناجحة!" else "فقاعة حمراء!");if(points>=15||misses>=5||rounds>=25){ended=true;updateStatus(if(points>=15)"فزت!" else "انتهت الجولة")}else fill()}},weightLp(dp(54)))};grid.addView(line)}}
}

class MeteorDodgeActivity : ExtraMiniGameActivity() {
    private var lane=1
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("☄️ تفادي النيازك","اختر مساراً آمناً وتفادى النيزك في كل جولة.");content.addView(label("☄️        ☄️\n\n       🚀\n\n━━━━━━━━━━━━",34f),lp(top=dp(12),bottom=dp(12)))
        val line=row();for(i in 0..2)line.addView(button(listOf("المسار الأيسر","المسار الأوسط","المسار الأيمن")[i],false){lane=i;updateStatus("اخترت ${listOf("الأيسر","الأوسط","الأيمن")[i]}")},weightLp(dp(72)));content.addView(line)
        content.addView(button("الجولة التالية") { if(!ended){val meteor=Random.nextInt(3);rounds++;if(lane!=meteor)points++ else points=(points-2).coerceAtLeast(0);updateStatus(if(lane!=meteor)"نجوت من النيزك!" else "اصطدم بك النيزك!");if(rounds>=15){ended=true;updateStatus(if(points>=10)"نجحت في الرحلة الفضائية!" else "انتهت الرحلة")}} },lp(top=dp(10)))
        content.addView(button("إعادة اللعب"){resetScore();lane=1},lp(top=dp(6)))
    }
}

class JumpRunnerActivity : ExtraMiniGameActivity() {
    private var action="قفز"
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("🏃 عدّاء الحواجز","اختر القفز أو الانحناء حسب العائق القادم.");val scene=label("🏃‍♂️  🪵  🐦\n━━━━━━━━━━━━",32f);content.addView(scene,lp(top=dp(14),bottom=dp(12)));val line=row();line.addView(button("⬆ قفز",false){action="قفز";updateStatus("ستقفز فوق الجذع")},weightLp(dp(60)));line.addView(button("⬇ انحناء",false){action="انحناء";updateStatus("ستنحني أسفل العائق")},weightLp(dp(60)));content.addView(line)
        content.addView(button("اجتز العائق") { if(!ended){val obstacle=if(Random.nextBoolean())"قفز" else "انحناء";rounds++;if(action==obstacle)points++ else points=(points-1).coerceAtLeast(0);updateStatus(if(action==obstacle)"اجتزت العائق بنجاح!" else "اصطدمت بالعائق: كان عليك $obstacle");if(rounds>=15){ended=true;updateStatus(if(points>=11)"وصلت إلى خط النهاية!" else "انتهى السباق")}} },lp(top=dp(10)));content.addView(button("إعادة السباق"){resetScore();action="قفز"},lp(top=dp(6)))
    }
}

class MiniGolfActivity : ExtraMiniGameActivity() {
    private var power=2
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("⛳ الغولف المصغّر","اختر قوة الضربة وأدخل الكرة في الحفرة بأقل عدد من الضربات.");content.addView(label("⚪ ──────────────── 🕳️",30f),lp(top=dp(24),bottom=dp(24)));val line=row();for(i in 1..3)line.addView(button("قوة $i",false){power=i;updateStatus("قوة الضربة: $power")},weightLp(dp(58)));content.addView(line)
        content.addView(button("اضرب الكرة") { if(!ended){rounds++;val success=Random.nextInt(4)==power-1||Random.nextInt(5)==0;if(success){points+=maxOf(1,5-rounds);ended=true;updateStatus("أدخلت الكرة في الحفرة خلال $rounds ضربات!")}else updateStatus("لم تصل الكرة للحفرة — حاول مرة أخرى") ;if(rounds>=8&&!ended){ended=true;updateStatus("انتهت المحاولات. حاول مجدداً")}} },lp(top=dp(10)));content.addView(button("ملعب جديد"){resetScore();power=2},lp(top=dp(6)))
    }
}

class MemoryPairsPlusActivity : ExtraMiniGameActivity() {
    private lateinit var grid: LinearLayout
    private val icons=listOf("🍎","🚀","🐱","⚽","🌟","🎲","🐸","🎈")
    private var deck= mutableListOf<String>()
    private var open= mutableSetOf<Int>()
    private var matched= mutableSetOf<Int>()
    private var first=-1
    private var waiting=false
    private val handler=Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("🧠 ذاكرة الأزواج","اكشف بطاقتين متطابقتين. اعثر على جميع الأزواج.");grid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};content.addView(grid,lp());content.addView(button("خلط البطاقات"){newGame()},lp(top=dp(8)));newGame() }
    private fun newGame(){handler.removeCallbacksAndMessages(null);resetScore();deck=(icons+icons).shuffled().toMutableList();open.clear();matched.clear();first=-1;waiting=false;draw()}
    private fun draw(){grid.removeAllViews();for(r in 0..3){val line=row(3);for(c in 0..3){val i=r*4+c;line.addView(button(if(i in matched||i in open)deck[i] else "❔",false){if(!ended&&!waiting&&i !in matched&&i !in open){open.add(i);if(first<0){first=i;draw()}else{val second=i;rounds++;if(deck[first]==deck[second]){matched.add(first);matched.add(second);points+=2;first=-1;if(matched.size==16){ended=true;updateStatus("أكملت جميع الأزواج في $rounds محاولة!")};updateStatus("الأزواج: ${matched.size/2} من 8");draw()}else{waiting=true;val previous=first;first=-1;updateStatus("حاول تذكر موضعي البطاقتين!");draw();handler.postDelayed({open.remove(previous);open.remove(second);waiting=false;draw()},650)}}}},weightLp(dp(58)))};grid.addView(line)}}
}

class WordSprintActivity : ExtraMiniGameActivity() {
    private data class Q(val word:String,val options:List<String>,val answer:String)
    private val qs=listOf(Q("سودان",listOf("سودان","سندان","دوسان"),"سودان"),Q("كتاب",listOf("كاتب","كتاب","كباب"),"كتاب"),Q("قمر",listOf("قمر","قلم","عمر"),"قمر"),Q("نهر",listOf("نهر","نار","نحر"),"نهر"),Q("شمس",listOf("شمس","شمع","شمسية"),"شمس"))
    private var q=0
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("🔤 سباق الكلمات","اختر الكلمة المكتوبة بشكل صحيح. أمامك خمس جولات.");content.addView(button("ابدأ التحدي"){resetScore();q=0;showQ()},lp(top=dp(8)));showQ() }
    private fun showQ(){content.removeAllViews();content.addView(label("اختر الكلمة الصحيحة",17f),lp(bottom=dp(8)));status=label("الجولة ${q+1} من ${qs.size} | النقاط: $points",20f);content.addView(status,lp(bottom=dp(10)));if(q>=qs.size){ended=true;content.addView(label("النتيجة النهائية: $points من ${qs.size}",24f),lp(top=dp(14)));content.addView(button("إعادة التحدي"){resetScore();q=0;showQ()},lp(top=dp(10)));return};content.addView(label("${qs[q].word}",30f),lp(bottom=dp(10)));qs[q].options.shuffled().forEach{answer->content.addView(button(answer,false){if(!ended){if(answer==qs[q].answer)points++;q++;showQ()}},lp(top=dp(5)))} }
}

class BallBounceActivity : ExtraMiniGameActivity() {
    private var direction=1
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);prepare("⚽ ارتداد الكرة","حرّك الكرة في الاتجاه الصحيح لتصل إلى المرمى.");content.addView(label("🥅\n\n        ⚽\n\n━━━━━━━━━━━━",38f),lp(top=dp(14),bottom=dp(14)));val line=row();line.addView(button("⬅ يسار",false){direction=0;updateStatus("الاتجاه: يسار")},weightLp(dp(58)));line.addView(button("⬆ أمام",false){direction=1;updateStatus("الاتجاه: أمام")},weightLp(dp(58)));line.addView(button("➡ يمين",false){direction=2;updateStatus("الاتجاه: يمين")},weightLp(dp(58)));content.addView(line)
        content.addView(button("ركل الكرة") { if(!ended){rounds++;val goal=Random.nextInt(3);if(goal==direction){points+=2;updateStatus("هدف! ⚽ +2")}else{points=(points-1).coerceAtLeast(0);updateStatus("ذهبت الكرة إلى جهة أخرى")};if(rounds>=10){ended=true;updateStatus(if(points>=10)"فوز كبير!" else "انتهت المباراة")}} },lp(top=dp(10)));content.addView(button("مباراة جديدة"){resetScore();direction=1},lp(top=dp(6)))
    }
}
