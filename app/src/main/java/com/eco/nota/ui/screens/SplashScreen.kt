package com.eco.nota.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.PrimaryGradient
import com.eco.nota.ui.theme.ProBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 800),
        label = "splashScale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scaleAnim)
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(100.dp),
                color = AccentBlue.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(2.dp, AccentBlue)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "NOTA KASIR PRO",
                style = NotaTheme.typography.title.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Aplikasi Pembuat Nota & Struk Profesional",
                style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                color = Color(0xFF94A3B8)
            )
        }
    }
}
