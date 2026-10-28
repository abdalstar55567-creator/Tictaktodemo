# ١٠٠ لعبة وأكثر — دليل المشروع (للمطوّر ولأي ذكاء اصطناعي توليدي)

تطبيق أندرويد (Kotlin) يجمع ألعاباً بسيطة تحت قائمة رئيسية. كل الواجهات تُبنى **بالكود** (Views و Canvas) بدون Material ولا Compose ولا ملفات تصميم للألعاب، والنصوص عربية (RTL).
الحالة الحالية: **٤٧ لعبة** مسجّلة في `Games.kt` (والهدف: ١٠٠ وأكثر).

---

## ١) الحصول على APK بدون حاسوب

1. ارفع ملف ZIP للمشروع إلى **جذر** المستودع (Add file ← Upload files ← Commit).
   - يجب أن يكون `settings.gradle` داخل الأرشيف (في جذره أو داخل مجلد واحد).
   - **لا تضع مجلد `.github` داخل الـ ZIP** حتى لا يُستبدل سير العمل.
2. يعمل تلقائياً سير العمل **Unpack ZIP and build APK** (`.github/workflows/unpack-build.yml`): يختار أحدث ZIP، يحذف `app` القديم، ينسخ المشروع الجديد، يبني `assembleDebug`، ويحفظ النتيجة في المستودع.
3. في سجل خطوة Unpack تأكد من السطر `Games registered: N` (يجب أن يساوي عدد الألعاب).
4. من صفحة التشغيل ← **Artifacts** نزّل `apk-100-games` وفك ضغطه لتجد `app-debug.apk`.
5. **أزل التطبيق القديم من الهاتف قبل تثبيت الجديد**: كل بناء يُوقَّع بمفتاح debug جديد، فالتثبيت فوق القديم يفشل (وتُمسح الدرجات المحفوظة).
6. سير العمل الثاني **Build APK** (`android.yml`) يبني الكود الموجود في المستودع عند كل push على main، ويُنتج artifact باسم `tictactoe-apk`. تأكد أنك تنزّل الـ artifact من التشغيل الصحيح وتاريخه الأحدث.

**إن فشل البناء:** افتح job ← الخطوة الحمراء ← انسخ الأسطر التي تبدأ بـ `e: file:///...` وأرسلها لمن يصلح الكود. لا يمكن تجميع المشروع خارج GitHub Actions، فالتحقق الوحيد هو نتيجة البناء هناك.

---

## ٢) تعليمات لذكاء اصطناعي توليدي: إضافة ألعاب جديدة

> اقرأ هذا القسم كاملاً قبل كتابة أي كود، وافحص الملفات الموجودة قبل الإنشاء (أسماء الأصناف يجب ألا تتكرر).

### المسارات

- الحزمة: `com.example.tictactoe`
- كود Kotlin: `app/src/main/java/com/example/tictactoe/`
- المانيفست: `app/src/main/AndroidManifest.xml`
- قائمة الألعاب: `Games.kt` (في مجلد Kotlin نفسه)

### الخطوات الثلاث الإلزامية لكل لعبة

1. **ملف Kotlin** فيه صنف يرث `BaseGameActivity` (مباشرة أو عبر أحد المحركات المشتركة أدناه).
2. **سطر في المانيفست** داخل `<application>`:
   ```xml
   <activity android:name=".XxxActivity" android:exported="false" android:screenOrientation="portrait" />
   ```
3. **سطر في `Games.kt`** داخل `listOf(...)` (فاصلة بين العناصر):
   ```kotlin
   GameInfo("🎯", "اسم اللعبة", XxxActivity::class.java),
   ```

نسيان ٢ ⇒ ينهار التطبيق عند فتح اللعبة. نسيان ٣ ⇒ لا تظهر في القائمة. القائمة وعدّاد الألعاب يُبنيان تلقائياً من `Games.all`.

### أربعة أنماط جاهزة — اختر الأنسب (الأرخص والأقل خطراً أولاً)

**أ) أسئلة سريعة بأربعة خيارات — `QuickChallengeActivity`** (الأسهل، ١٠ ألعاب حالياً: العواصم، الحيوانات، صح/خطأ، المتتالية، الألوان، خلط الكلمات، الحساب السريع، العملة، النرد، زوجي/فردي)
لعبة جديدة من هذا النوع = ملف من ٦ أسطر + إضافة فرع `"mode"` جديد في `when (mode)` داخل `QuickChallengeActivity.kt` يولّد `Challenge(prompt, options, answer)`:
```kotlin
class XxxActivity : QuickChallengeActivity() {
    override val mode: String get() = "xxx"
    override val titleArabic: String get() = "اسم اللعبة"
    override val emoji: String get() = "🎯"
}
```
> عدّل `QuickChallengeActivity.kt` بإضافة فرع فقط، ولا تحذف أو تغيّر الفروع الموجودة.

**ب) ألعاب حركة بلمس/سحب — `MotionGameActivity`** (في `MotionArcadeGames.kt`)
عشرة أوضاع معرّفة في `enum class MotionGame`، **خمسة منها مسجّلة** (سباق السيارات، آكل النقاط، مدافع الفضاء، عدّاء الغابة، الفقاعات) و**خمسة جاهزة غير مسجّلة**: `GhostMazeActivity`, `BikeRaceActivity`, `RiverRunActivity`, `BallMazeActivity`, `MazeEscapeActivity`. تفعيل أي منها = سطر في المانيفست + سطر في `Games.kt` فقط (بعد التأكد أن منطقها مرسوم داخل `MotionArcadeView`).

**ج) ألعاب سقوط/التقاط — `FallingGameActivity`** (في `FallingActivity.kt`): تحتاج `mode`, `titleText`, `hint`, `bestKey`. انظر `CatchActivity` و`DodgeActivity`.

**د) لعبة مستقلة بالكامل** — الأكثر مرونة والأكثر خطراً:
```kotlin
class XxxActivity : BaseGameActivity() {
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("🎯 عنوان اللعبة")      // شريط الرجوع + العنوان + الحاوية content
        info = label("النتيجة: 0", 24f)
        content.addView(info, lp(top = dp(16)))
        content.addView(button("ابدأ") { startRound() }, lp(height = dp(52), top = dp(12)))
    }
    private fun startRound() { /* منطق اللعبة */ }
}
```
ولألعاب Canvas: اكتب `View` ترث `GameLoopView` (تنفّذ `update(dt)` و`onDraw(canvas)`، وتستدعي `grab()` في اللمس و`startLoop()` للبدء)، وأضفها بـ `content.addView(view, lp())`، وأوقفها في `onPause` عبر `view.stopLoop()`. أمثلة: `FlappyActivity.kt`, `TowerActivity.kt`, `BrickActivity.kt`.

### الأدوات المتاحة (استخدمها ولا تعد اختراعها)

**داخل أي `BaseGameActivity`:**
| الأداة | الوظيفة |
|---|---|
| `initScreen(title)` | يبني الشاشة ويعرّف `content` (LinearLayout عمودي داخل ScrollView) |
| `label(text, sp=18f, colorRes=R.color.text_main, bold=true)` | TextView متمركز |
| `button(text, primary=true) { }` | زر؛ `primary=false` للزر الباهت |
| `lp(width, height, top, bottom)` | LayoutParams (مرّر الهوامش بـ `dp(..)`) |
| `weightLp(height)` / `row()` | عناصر متساوية الحصة في صف أفقي |
| `dp(n)` / `color(R.color.x)` | وحدات / ألوان |
| `confirmExit()` | رسالة «هل أنت متأكد أنك تريد المغادرة؟» (تلقائية من زر الرجوع وزر الهاتف) |

**كائنات مشتركة:**
- `Scores.get / put / saveHigh / saveLow` (الأكبر أفضل / الأقل أفضل). استخدم **مفتاحاً فريداً** مثل `"xxx_best"`.
- `Feedback.tap / match / win / lose / draw (this)` — صوت واهتزاز يحترمان زر الصوت.
- `Ui.*` — نسخ ثابتة من الأدوات تأخذ `ctx`.

**الألوان** `R.color.*`: `bg`, `panel`, `cell`, `x_color`, `o_color`, `gold`, `text_main`, `text_dim` (راجع `colors.xml` فقد تُضاف ألوان).
**الخلفيات** `R.drawable.*`: `bg_button`, `bg_button_dim`, `bg_panel`, `bg_cell`, `bg_cell_match`, `bg_cell_win`.

### قواعد صارمة (كل مخالفة = فشل البناء أو انهيار)

1. **اسم الصنف فريد** في كل المشروع. الأصناف المساعدة تُسمّى بادئة اسم اللعبة (`XxxView`, `XxxCell`).
2. **لا تعرّف دوالاً أو خصائص تتعارض مع `Activity`/`AppCompatActivity`**: ممنوع كاسم دالة `onStop`, `onStart`, `onResume`, `finish`, `recreate`, `setTitle`, `getTitle`, `onBackPressed` (إلا override حقيقي مع `super`). استخدم `pressStop`, `endGame`, `startRound`… (خطأ حدث فعلاً: دالة `private fun onStop()` أسقطت البناء).
3. لا تضف `handler` إلى `BaseGameActivity`. إن احتجت مؤقتاً فأعلن في لعبتك `private val handler = Handler(Looper.getMainLooper())` وألغِ المهام في `onDestroy`.
4. استدعِ `super.onCreate(savedInstanceState)` ثم `initScreen(...)` **قبل** استخدام `content`.
5. لا مكتبات خارجية، لا Material/Compose/ViewBinding، لا ملفات صوت أو صور. الصوت عبر `Feedback` فقط.
6. كل النصوص الظاهرة عربية، والعنوان يبدأ بإيموجي.
7. أي View ترث `GameLoopView` تنفّذ `update(dt)` و`onDraw` وتوقف الحلقة في `onPause`.
8. لا تعدّل `BaseGameActivity` أو `Scores` أو `Feedback` أو `Ui` أو `GameLoopView` إلا لضرورة، وإن عدّلتها فلا تحذف أي دالة موجودة. في `Games.kt` أضف سطراً فقط.
9. فكّر كالمترجم: كل `import` مكتوب، `Int` مقابل `Float` متطابقة، لا `private` يُستدعى من خارج صنفه، لا فاصلة ناقصة أو زائدة في `Games.kt`، وكل وسم `<activity ... />` مغلق.
10. لا تضف أكثر من ٨–١٠ ألعاب في الدفعة الواحدة، وابدأ بالأنماط (أ) و(ب) و(ج) لأنها الأقل خطراً.

### قائمة تحقق قبل التسليم

- [ ] لكل لعبة: ملف Kotlin + سطر مانيفست + سطر في `Games.kt`
- [ ] أسماء الأصناف فريدة، ولا دالة تتعارض مع `Activity`
- [ ] عدد `::class.java` في `Games.kt` = عدد وسوم `<activity>` في المانيفست ناقص واحد (`MenuActivity` المشغِّل)
- [ ] مفاتيح الدرجات فريدة
- [ ] ZIP جديد باسم يحمل رقم نسخة (`Games100-v6.zip`…) و`settings.gradle` داخله، بلا مجلد `.github`

---

## ٣) هيكل المستودع

```
.github/workflows/
  unpack-build.yml        ← يفك ZIP ويبني (الأساسي)
  android.yml             ← يبني الكود الحالي عند كل push
app/src/main/
  AndroidManifest.xml     ← سجّل كل Activity هنا
  java/com/example/tictactoe/
    MenuActivity.kt       ← القائمة الرئيسية (تقرأ Games.all)
    Games.kt              ← قائمة الألعاب GameInfo(emoji, title, cls)
    BaseGameActivity.kt   ← أساس كل لعبة: شريط رجوع + تأكيد مغادرة + أدوات
    GameLoopView.kt       ← أساس ألعاب Canvas
    QuickChallengeActivity.kt  ← محرك الأسئلة السريعة
    MotionArcadeGames.kt  ← ألعاب الحركة (١٠ أوضاع، ٥ مسجّلة)
    FallingActivity.kt    ← محرك ألعاب السقوط
    Ui.kt / Scores.kt / Feedback.kt
    *Activity.kt          ← بقية الألعاب
  res/layout/view_topbar.xml, res/values/colors.xml, strings.xml
```

## ٤) ملاحظات تنظيف للمستودع (غير ضرورية للبناء)

- `README-100PLUS.txt` قديم (يخص نسخة سابقة) ويمكن حذفه.
- `.android.yml` و`unpack-build.yml` في **الجذر** نسخ زائدة؛ المعتمد هو ما داخل `.github/workflows/`.
- `TicTacToe` مسجَّل كـ submodule فارغ (gitlink) ولا فائدة منه؛ يمكن إزالته.
