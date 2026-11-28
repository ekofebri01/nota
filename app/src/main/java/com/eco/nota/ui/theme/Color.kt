package com.eco.nota.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Permukaan: biru-hitam dalam (deep space) dengan lapisan kartu kebiruan ──
val ProBackground = Color(0xFF05070F)
val ProCardSurface = Color(0xFF0C1222)
val ProCardSurfaceLight = Color(0xFF141C33)
val ProBorder = Color(0xFF212C4F)

// ── Aksen neon: electric blue, violet, cyan ──
val AccentBlue = Color(0xFF4C6FFF)      // aksi utama (putih di atasnya tetap terbaca)
val AccentPurple = Color(0xFF9B5CFF)
val AccentCyan = Color(0xFF22D3EE)      // sorot / glow
val AccentCyanSoft = Color(0xFF7DE3FF)  // teks bernuansa aksen di atas latar gelap

// ── Warna status (peran tetap: sukses / peringatan / bahaya) ──
val AccentEmerald = Color(0xFF10C48C)
val AccentAmber = Color(0xFFFFB020)
val AccentRose = Color(0xFFFF4D6D)

// ── Teks ──
val TextPrimary = Color(0xFFF2F6FF)
val TextGray = Color(0xFF9AA9CF)
val TextMuted = Color(0xFF6F7FA8)

// ── Gradient ──
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF22D3EE), Color(0xFF4C6FFF), Color(0xFF9B5CFF))
)

/** Border kartu bercahaya tipis: cyan → biru → violet (transparan agar tetap halus di atas gelap). */
val CardBorderGradient = Brush.linearGradient(
    colors = listOf(Color(0x7022D3EE), Color(0x504C6FFF), Color(0x709B5CFF))
)
