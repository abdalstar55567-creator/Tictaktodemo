package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class RealisticDifferencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                RealisticDifferencesScreen()
            }
        }
    }
}

@Composable
fun RealisticDifferencesScreen() {
    // عدد الفروقات الكلي المطلوب اكتشافها
    var foundDifferences by remember { mutableStateOf(setOf<Int>()) }
    var message by remember { mutableStateOf("ابحث عن الفروقات في الصورة الواقعية واضغط عليها!") }

    // تعريف مناطق الفروقات بناءً على نسب موضعية تقريبية داخل الصورة (مثال: فرقين أو ثلاثة)
    // كل عنصر يمثل منطقة معينة (معرف الفارق، والموقع التقريبي كنسبة مئوية أو مربعات تفاعلية)
    val totalDifferencesCount = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "لعبة إيجاد الفروقات الواقعية", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "تم العثور على: ${foundDifferences.size} من $totalDifferencesCount", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(text = message, fontSize = 14.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(16.dp))

        // حاوية الصورة مع إمكانية وضع طبقات تفاعلية فوقها للفروقات
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(Color.LightGray)
        ) {
            // استبدل R.drawable.sample_image باسم صورتك الموجودة في مجلد drawable
            Image(
                painter = painterResource(id = R.drawable.sample_image ), // ضع صورتك هنا مثلاً R.drawable.game_photo
                contentDescription = "صورة اللعبة الواقعية",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // الفارق الأول (مثلاً في الزاوية العلوية اليسرى)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 40.dp, y = 30.dp)
                    .clickable(enabled = !foundDifferences.contains(1)) {
                        foundDifferences = foundDifferences + 1
                        message = "رائع! لقد وجدت الفارق الأول 🌟"
                    }
                    .background(if (foundDifferences.contains(1)) Color.Green.copy(alpha = 0.4f) else Color.Transparent)
            )

            // الفارق الثاني (في المنتصف)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clickable(enabled = !foundDifferences.contains(2)) {
                        foundDifferences = foundDifferences + 2
                        message = "ممتاز! وجدت الفارق الثاني 🔍"
                    }
                    .background(if (foundDifferences.contains(2)) Color.Green.copy(alpha = 0.4f) else Color.Transparent)
            )

            // الفارق الثالث (في الزاوية السفلى اليمنى)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .clickable(enabled = !foundDifferences.contains(3)) {
                        foundDifferences = foundDifferences + 3
                        message = "أحسنت! عثرت على جميع الفروقات 🎉"
                    }
                    .background(if (foundDifferences.contains(3)) Color.Green.copy(alpha = 0.4f) else Color.Transparent)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            foundDifferences = emptySet()
            message = "تم إعادة ضبط اللعبة، ابحث من جديد!"
        }) {
            Text(text = "إعادة اللعب")
        }
    }
}
