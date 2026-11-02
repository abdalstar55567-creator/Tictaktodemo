package com.example.tictactoe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import kotlinx.coroutines.delay

class RealisticDifferencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DifferencesGameScreen()
            }
        }
    }
}

private data class DifferenceSpot(
    val id: Int,
    val label: String,
    // Position and dimensions are fractions of the displayed image.
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

private val differenceSpots = listOf(
    // مجموعة الزجاجات الصغيرة أسفل يسار الصورة
    DifferenceSpot(0, "الزجاجات", 0.02f, 0.84f, 0.17f, 0.12f),
    // الترمومتر الرقمي بجوار الميزان
    DifferenceSpot(1, "الترمومتر", 0.59f, 0.60f, 0.18f, 0.07f),
    // بكرة الخيط المعلقة قرب المريلة
    DifferenceSpot(2, "بكرة الخيط", 0.40f, 0.16f, 0.12f, 0.13f)
)

@Composable
private fun DifferencesGameScreen() {
    var showingSecondImage by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(5) }
    var foundIds by remember { mutableStateOf(setOf<Int>()) }
    var round by remember { mutableIntStateOf(0) }

    LaunchedEffect(round) {
        showingSecondImage = false
        secondsLeft = 5
        foundIds = emptySet()
        for (second in 5 downTo 1) {
            secondsLeft = second
            delay(1000)
        }
        showingSecondImage = true
    }

    val allFound = foundIds.size == differenceSpots.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101820)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "اكتشف الفروقات",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))

            Text(
                text = when {
                    allFound -> "أحسنت! اكتشفت الفروقات الثلاثة 🎉"
                    !showingSecondImage -> "احفظ تفاصيل الصورة جيداً"
                    else -> "اضغط على أماكن الاختلاف في الصورة"
                },
                color = Color(0xFFFFD166),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF243746))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showingSecondImage) "الفروقات: ${foundIds.size} / 3" else "الصورة الأولى",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (!showingSecondImage) {
                    Text(
                        text = "$secondsLeft ث",
                        color = Color(0xFFFFD166),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "الجولة $round",
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Both source images have nearly the same 857:1536 aspect ratio.
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(857f / 1536f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF526777), RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(
                            id = if (showingSecondImage) {
                                R.drawable.sample_image_2
                            } else {
                                R.drawable.sample_image
                            }
                        ),
                        contentDescription = if (showingSecondImage) {
                            "الصورة الثانية للبحث عن الفروقات"
                        } else {
                            "الصورة الأولى للحفظ والملاحظة"
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    if (showingSecondImage) {
                        differenceSpots.forEach { spot ->
                            val isFound = spot.id in foundIds
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = maxWidth * spot.left,
                                        y = maxHeight * spot.top
                                    )
                                    .size(
                                        width = maxWidth * spot.width,
                                        height = maxHeight * spot.height
                                    )
                                    .then(
                                        if (isFound) {
                                            Modifier
                                                .border(
                                                    width = 3.dp,
                                                    color = Color(0xFF39D98A),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .background(
                                                    Color(0x5539D98A),
                                                    RoundedCornerShape(8.dp)
                                                )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable(enabled = !isFound && !allFound) {
                                        foundIds = foundIds + spot.id
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (showingSecondImage) {
                    "المناطق التي عثرت عليها ستظهر بإطار أخضر."
                } else {
                    "ستظهر الصورة الثانية تلقائياً بعد انتهاء الثواني الخمس."
                },
                color = Color(0xFFCFD8DC),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { round += 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A884)
                )
            ) {
                Text(
                    text = "إعادة اللعب",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
