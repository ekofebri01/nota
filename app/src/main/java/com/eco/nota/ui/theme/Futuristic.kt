package com.eco.nota.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cahaya ambient halus di sudut layar (violet kiri-atas, cyan kanan-bawah).
 * Hanya dekorasi: digambar di atas konten dengan alpha rendah, tidak menangkap sentuhan
 * dan tidak mengubah tata letak.
 */
fun Modifier.ambientGlow(): Modifier = this.drawWithContent {
    drawContent()
    val reach = size.maxDimension
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(AccentPurple.copy(alpha = 0.10f), Color.Transparent),
            center = Offset(0f, 0f),
            radius = reach * 0.75f
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(AccentCyan.copy(alpha = 0.07f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = reach * 0.70f
        )
    )
}

/**
 * Efek glow neon di sekeliling elemen (bayangan berwarna).
 * Berwarna di Android 9+; di versi lebih lama otomatis tampil sebagai bayangan biasa.
 */
fun Modifier.neonGlow(
    color: Color = AccentBlue,
    shape: Shape = RoundedCornerShape(16.dp),
    radius: Dp = 14.dp
): Modifier = this.shadow(
    elevation = radius,
    shape = shape,
    clip = false,
    ambientColor = color,
    spotColor = color
)

/** Garis gradien tipis di tepi bawah (mis. pemisah AppBar) — hanya dekorasi. */
fun Modifier.bottomGlowLine(
    brush: Brush = PrimaryGradient,
    thickness: Dp = 1.dp,
    alpha: Float = 0.65f
): Modifier = this.drawWithContent {
    drawContent()
    val h = thickness.toPx()
    drawRect(
        brush = brush,
        topLeft = Offset(0f, size.height - h),
        size = androidx.compose.ui.geometry.Size(size.width, h),
        alpha = alpha
    )
}
