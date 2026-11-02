package com.example.tictactoe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class AboutActivity : BaseGameActivity() {
    override val gameTitle = "عن التطبيق"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen("عن التطبيق")
        content.setPadding(dp(18), dp(12), dp(18), dp(24))

        content.addView(label("🎮 ١٠٠ لعبة وأكثر", 25f), lp(top = 8, bottom = 12))
        content.addView(label("تطبيق ترفيهي يضم مجموعة متنوعة من الألعاب البسيطة والتحديات.", 17f), lp(bottom = 18))
        content.addView(label("الخصوصية وسياسة الاستخدام", 20f), lp(top = 8, bottom = 8))
        val privacy = TextView(this).apply {
            text = "• التطبيق لا يجمع أي بيانات شخصية من خلال استخدام الألعاب.\n\n• لا نطلب إنشاء حساب لاستخدام الألعاب.\n\n• تُحفظ تفضيلاتك المحلية مثل الألعاب المفضلة وإحصاءات اللعب على جهازك فقط.\n\n• يُستخدم التطبيق للترفيه، ويُرجى استخدامه بطريقة قانونية ومسؤولة.\n\n• قد تتغير الألعاب أو تُحدّث لتحسين التجربة."
            textSize = 16f
            setTextColor(color(R.color.text_main))
            gravity = Gravity.RIGHT
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        content.addView(privacy, lp(bottom = 20))
        content.addView(label("معلومات الاتصال", 20f), lp(top = 8, bottom = 10))
        content.addView(button("واتساب: 00249997744755", false) {
            openLink("https://wa.me/24999774755")
        }, lp(bottom = 8))
        content.addView(button("البريد الإلكتروني: abdal.star55569@gmail.com", false) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:abdal.star55569@gmail.com"))
            startActivity(Intent.createChooser(intent, "إرسال بريد إلكتروني"))
        }, lp(bottom = 8))
       
    
        content.addView(label("الإصدار: 1.0", 13f, R.color.text_dim, false), lp(top = 16))
    }

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
