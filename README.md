# ١٠٠ لعبة وأكثر — دليل المشروع (للمطوّر ولأي ذكاء اصطناعي توليدي)

تطبيق أندرويد (Kotlin) يجمع ألعاباً بسيطة تحت قائمة رئيسية. الواجهات كلها تُبنى **بالكود** (Views) بدون Material وبدون ملفات تصميم للألعاب، والنصوص عربية (RTL).

---

## ١) كيف تحصل على ملف APK (بدون حاسوب)

1. ارفع ملف ZIP للمشروع إلى **جذر** المستودع على GitHub (Add file ← Upload files ← Commit).
2. يعمل تلقائياً workflow اسمه **Unpack ZIP and build APK**: يفك الضغط، يبني، ويحفظ النتيجة.
3. تأكد في سجل خطوة Unpack من السطر `Games registered: N` (N = عدد الألعاب).
4. من صفحة التشغيل ← Artifacts نزّل **apk-100-games** وفك ضغطه لتجد `app-debug.apk`.
5. **أزل التطبيق القديم من الهاتف قبل تثبيت الجديد** (كل بناء يُوقَّع بمفتاح debug مختلف، فالتحديث فوق القديم يفشل). هذا يمسح الدرجات المحفوظة.
6. احذف أي ZIP قديم من الجذر قبل رفع الجديد (الـ workflow يستخدم أحدث ملف ZIP ويحذف الباقي).

إذا فشل البناء: افتح job ← الخطوة الحمراء، وانسخ الأسطر التي تبدأ بـ `e: file:///...` وأرسلها لمن يصلح الكود.

---

## ٢) تعليمات لذكاء اصطناعي توليدي: إضافة ألعاب جديدة

> اقرأ هذا القسم كاملاً قبل كتابة أي كود. لا يمكن تجميع المشروع محلياً، فالدقة في اتباع القواعد هي ما يمنع فشل البناء.

### الحزمة والمسارات

- الحزمة: `com.example.tictactoe`
- كل ملفات Kotlin في: `app/src/main/java/com/example/tictactoe/`
- المانيفست: `app/src/main/AndroidManifest.xml`
- قائمة الألعاب: `Games.kt` في نفس مجلد Kotlin

### خطوات إضافة لعبة (٣ أشياء إلزامية)

1. **ملف جديد** `XxxActivity.kt` يحوي صنفاً يرث `BaseGameActivity`.
2. **تسجيل في المانيفست** (سطر واحد داخل `<application>`):
   ```xml
   <activity android:name=".XxxActivity" android:exported="false" android:screenOrientation="portrait" />
   ```
3. **سطر في `Games.kt`** داخل `listOf(...)` (لا تنسَ الفاصلة بين العناصر، وبلا فاصلة بعد الأخير):
   ```kotlin
   GameInfo("🎯", "اسم اللعبة", XxxActivity::class.java),
   ```

نسيان الخطوة ٢ ⇒ تتعطل اللعبة عند الفتح. نسيان الخطوة ٣ ⇒ لا تظهر في القائمة. القائمة تُبنى تلقائياً من `Games.all` وعدّاد الألعاب يتحدث وحده.

### قالب لعبة بسيطة (واجهة أزرار/نصوص)

```kotlin
package com.example.tictactoe

import android.os.Bundle
import android.widget.TextView

class XxxActivity : BaseGameActivity() {

    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎯 عنوان اللعبة")          // يضيف شريط الرجوع + العنوان + حاوية content

        info = label("النتيجة: 0", 24f)
        content.addView(info, lp(top = dp(16)))
        content.addView(button("ابدأ") { start() }, lp(height = dp(52), top = dp(12)))
        content.addView(button("لعبة جديدة", false) { start() }, lp(height = dp(46), top = dp(10)))
    }

    private fun start() { /* منطق اللعبة */ }
}
```

### قالب لعبة حركة (Canvas)

ضع `View` ترث `GameLoopView` + `Activity` تحتضنها (انظر `FlappyActivity.kt` و`TowerActivity.kt` كمثال حي):

```kotlin
class XxxView(context: Context, private val onOver: (Int) -> Unit) : GameLoopView(context) {
    override val aspect: Float get() = 1.2f          // الارتفاع = العرض × aspect
    private val p = paint(Color.parseColor("#22C55E"))

    fun start() { /* صفّر الحالة */ startLoop() }

    override fun update(dt: Float) { /* dt بالثواني؛ حرّك الأشياء */ }

    override fun onDraw(canvas: Canvas) { canvas.drawRect(0f, 0f, 50f, 50f, p) }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        grab()                                        // يمنع ScrollView من سرقة اللمس
        if (e.action == MotionEvent.ACTION_DOWN) { /* ... */ }
        return true
    }
}
```

وفي الـ Activity: أنشئ الـ View، وأضفها بـ `content.addView(view, lp())`، واستدعِ `override fun onPause() { view.stopLoop(); super.onPause() }`.

### الأدوات المتاحة (استخدمها ولا تعد اختراعها)

**داخل أي `BaseGameActivity`** (دوال protected):
| الدالة | الوظيفة |
|---|---|
| `initScreen(title)` | يبني الشاشة؛ يعرّف `content` (LinearLayout عمودي داخل ScrollView) |
| `label(text, sp=18f, colorRes=R.color.text_main, bold=true)` | TextView متمركز |
| `button(text, primary=true) { ... }` | زر؛ `primary=false` للزر الباهت |
| `lp(width, height, top, bottom)` | LayoutParams (الهوامش بالبكسل → مرّرها بـ `dp(..)`) |
| `weightLp(height)` | عنصر بحصة متساوية داخل `row()` |
| `row()` | LinearLayout أفقي متمركز |
| `dp(n)`, `color(R.color.x)` | تحويل وحدات / لون |
| `confirmExit()` | رسالة تأكيد المغادرة (تُستدعى تلقائياً من زر الرجوع) |

**كائنات مشتركة:**
- `Scores.get(ctx, key): Int`، `Scores.put(ctx, key, value)`، `Scores.saveHigh(ctx, key, v)` (الأكبر أفضل، يرجع true عند رقم قياسي)، `Scores.saveLow(ctx, key, v)` (الأقل أفضل). **اجعل مفتاح الدرجة فريداً** مثل `"xxx_best"`.
- `Feedback.tap(this)`, `.match(this)`, `.win(this)`, `.lose(this)`, `.draw(this)` — صوت واهتزاز (يحترم زر الصوت).
- `Ui.*` — بدائل ثابتة للأدوات (`Ui.text`, `Ui.button`, `Ui.row`, `Ui.panelBg`…) تأخذ `ctx`.

**ألوان** (`R.color.*`): `bg`, `panel`, `cell`, `x_color` (أزرق), `o_color` (وردي), `gold`, `text_main`, `text_dim`.
**خلفيات** (`R.drawable.*`): `bg_button`, `bg_button_dim`, `bg_panel`, `bg_cell`, `bg_cell_match`, `bg_cell_win`.

### قواعد صارمة (كل مخالفة = فشل البناء أو تعطل اللعبة)

1. **اسم الصنف فريد** ولا يتكرر مع أي ملف موجود. افحص مجلد Kotlin أولاً. ضع الأصناف الإضافية للعبة (Views، enums) في نفس ملفها بأسماء مسبوقة باسم اللعبة (`XxxView`, `XxxCell`).
2. **لا تُعرّف دوالاً أو خصائص تتعارض مع أعضاء `Activity`/`AppCompatActivity`**. أسماء ممنوعة كاسم دالة: `onStop`, `onStart`, `onResume`, `onPause` (إلا بـ `override` و`super`), `finish`, `recreate`, `setTitle`, `getTitle`, `onBackPressed`, `start`/`stop` فقط آمنة إن لم تكن override. استخدم أسماء مثل `pressStop`, `endGame`, `startRound`.
   (حدث فعلاً: دالة private باسم `onStop` أسقطت البناء.)
3. **لا تعرّف `handler` في `BaseGameActivity`**. إن احتجت مؤقتاً أعلن في لعبتك: `private val handler = Handler(Looper.getMainLooper())` وألغِ المهام في `onDestroy` عبر `handler.removeCallbacksAndMessages(null)`.
4. **استدعِ `super.onCreate(savedInstanceState)` ثم `initScreen("...")` أولاً** قبل استخدام `content` (وإلا `lateinit` يسبب انهياراً).
5. **لا تستخدم** Material أو Compose أو ViewBinding أو أي مكتبة خارجية أو ملفات صوت/صور/تصميم XML. فقط Views بالكود، و`Canvas`، و`Feedback` للصوت.
6. **كل النصوص الظاهرة بالعربية**، وعنوان اللعبة يبدأ بإيموجي مناسب.
7. **أي View ترث `GameLoopView` يجب أن تنفّذ `update(dt)` و`onDraw(canvas)`** وتوقف الحلقة في `onPause`.
8. لا تلمس `BaseGameActivity` أو `Games.kt` (عدا إضافة السطر) أو `Scores/Feedback/Ui` إلا لضرورة قصوى، وإن غيّرتها حافظ على كل الدوال الموجودة.
9. تحقق بعين المترجم: كل `import` مطلوب مكتوب، الأنواع (`Int` مقابل `Float`) متطابقة، لا متغيرات غير مستخدمة تُسبب خطأ، لا `private` يُستدعى من خارج صنفه.
10. الأقواس والفواصل في `Games.kt` والمانيفست سليمة (XML مغلق بـ `/>`).

### أفكار أحجام الدفعات

الدفعة المناسبة ٨–١٠ ألعاب في المرة. ابدأ بالألعاب النصية/الأزرار (أقل مخاطرة)، ثم ألعاب Canvas. بعد كل دفعة: أعد ضغط المشروع كاملاً في ZIP جديد باسم يحمل رقم نسخة (`Games100-v5.zip`…) بحيث يكون `settings.gradle` داخل مجلد المشروع، ولا تضع فيه مجلد `.github` (الـ workflow موجود في المستودع).

### قائمة تحقق قبل التسليم

- [ ] لكل لعبة جديدة: ملف Kotlin + سطر في المانيفست + سطر في `Games.kt`
- [ ] أسماء الأصناف فريدة، ولا دالة تتعارض مع `Activity`
- [ ] `initScreen` أو `setContentView` يُستدعى (بحيث يظهر شريط الرجوع تلقائياً)
- [ ] مفاتيح الدرجات فريدة
- [ ] عدد `::class.java` في `Games.kt` = عدد الأنشطة (غير المشغِّل) في المانيفست
- [ ] ZIP جديد برقم نسخة جديد

---

## ٣) هيكل المشروع

```
app/src/main/
  AndroidManifest.xml            ← سجّل كل Activity هنا
  java/com/example/tictactoe/
    MenuActivity.kt              ← القائمة الرئيسية (تقرأ Games.all)
    Games.kt                     ← قائمة الألعاب GameInfo(emoji, title, cls)
    BaseGameActivity.kt          ← أساس كل لعبة (شريط رجوع + تأكيد مغادرة + أدوات)
    GameLoopView.kt              ← أساس ألعاب الحركة (Canvas)
    Ui.kt / Scores.kt / Feedback.kt
    *Activity.kt                 ← الألعاب
  res/layout/view_topbar.xml     ← الشريط العلوي
  res/values/colors.xml, strings.xml
.github/workflows/               ← البناء التلقائي (لا تعدّله إلا لإصلاح البناء)
```

## ٤) الألعاب الحالية

انظر `Games.kt` للقائمة الكاملة (٣٢ لعبة حالياً). الهدف: «١٠٠ لعبة وأكثر».
