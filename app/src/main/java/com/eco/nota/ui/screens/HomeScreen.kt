package com.eco.nota.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eco.nota.models.Nota
import com.eco.nota.ui.theme.AccentAmber
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.CardBorderGradient
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.PrimaryGradient
import com.eco.nota.ui.theme.ProBackground
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted

@Composable
fun HomeScreen(
    notas: List<Nota>,
    onNotaClick: (Nota) -> Unit,
    onCreateNotaClick: () -> Unit,
    updateInfo: com.eco.nota.utils.AppUpdateInfo? = null,
    onUpdateClick: () -> Unit = {},
    onDismissUpdate: () -> Unit = {}
) {
    var selectedDateFilter by remember { mutableStateOf("All Time") }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }

    val filteredNotas = remember(selectedDateFilter, selectedStatusFilter, notas) {
        val now = java.util.Calendar.getInstance()
        val list = notas.filter { nota ->
            val matchesStatus = when (selectedStatusFilter) {
                "Lunas" -> nota.status.equals("Lunas", ignoreCase = true)
                "Tagihan" -> nota.status.equals("Tagihan", ignoreCase = true) || nota.status.equals("Piutang", ignoreCase = true)
                else -> true
            }
            
            val matchesDate = if (selectedDateFilter == "All Time") true else {
                try {
                    val date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).parse(nota.date)
                        ?: java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).parse(nota.date)
                    if (date == null) false
                    else {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = date
                        when (selectedDateFilter) {
                            "Harian" -> cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) && cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
                            "Bulanan" -> cal.get(java.util.Calendar.MONTH) == now.get(java.util.Calendar.MONTH) && cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
                            "Tahunan" -> cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
                            else -> true
                        }
                    }
                } catch (e: Exception) { false }
            }
            matchesStatus && matchesDate
        }
        
        list.sortedWith(
            compareByDescending<Nota> { nota ->
                // ... sorting logic ...
                try {
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).parse(nota.date)?.time
                        ?: java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).parse(nota.date)?.time
                        ?: 0L
                } catch (e: Exception) { 0L }
            }.thenByDescending { it.id }
        )
    }

    val totalOmzet = filteredNotas.filter { it.status.equals("Lunas", ignoreCase = true) }.sumOf {
        try {
            it.total.replace(Regex("[^0-9]"), "").toLong()
        } catch (e: Exception) { 0L }
    }

    val totalPiutang = filteredNotas.filter { it.status.equals("Tagihan", ignoreCase = true) || it.status.equals("Piutang", ignoreCase = true) }.sumOf {
        try {
            it.total.replace(Regex("[^0-9]"), "").toLong()
        } catch (e: Exception) { 0L }
    }

    Scaffold(
        containerColor = ProBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNotaClick,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Buat Nota", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HomeHeader()
            }

            if (updateInfo?.hasUpdate == true) {
                item {
                    com.eco.nota.ui.components.UpdateBannerCard(
                        updateInfo = updateInfo,
                        onClick = onUpdateClick,
                        onDismiss = onDismissUpdate
                    )
                }
            }

            if (updateInfo?.hasUpdate == true) {
                item {
                    com.eco.nota.ui.components.UpdateBannerCard(
                        updateInfo = updateInfo,
                        onClick = onUpdateClick,
                        onDismiss = onDismissUpdate
                    )
                }
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGradient)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RINGKASAN KEUANGAN", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentEmerald.copy(alpha = 0.2f)
                            ) {
                                Text("Aktif", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = AccentEmerald, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Omzet Lunas", style = NotaTheme.typography.label, color = TextGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Rp ${formatCurrency(totalOmzet)}", style = NotaTheme.typography.title.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = AccentEmerald)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Tagihan", style = NotaTheme.typography.label, color = TextGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Rp ${formatCurrency(totalPiutang)}", style = NotaTheme.typography.title.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = AccentAmber)
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                val filters = listOf("All Time", "Harian", "Bulanan", "Tahunan")
                val statusFilters = listOf("Semua", "Lunas", "Tagihan")
                
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Date Filter
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        filters.forEach { tab ->
                            val isSelected = selectedDateFilter == tab
                            Surface(
                                modifier = Modifier.clickable { selectedDateFilter = tab },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(tab, style = NotaTheme.typography.body.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp),
                                    color = if (isSelected) Color.White else TextGray, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                            }
                        }
                    }
                    // Status Filter
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        statusFilters.forEach { tab ->
                            val isSelected = selectedStatusFilter == tab
                            Surface(
                                modifier = Modifier.clickable { selectedStatusFilter = tab },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(tab, style = NotaTheme.typography.body.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp),
                                    color = if (isSelected) Color.White else TextGray, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "RIWAYAT NOTA (${filteredNotas.size})",
                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (filteredNotas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(PrimaryGradient, CircleShape)
                                    .padding(3.dp)
                                    .background(ProBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    tint = AccentBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Belum ada nota tersimpan", style = NotaTheme.typography.heading.copy(fontSize = 16.sp), color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ketuk tombol Buat Nota di bawah untuk mulai mencatat", style = NotaTheme.typography.label, color = TextGray)
                        }
                    }
                }
            } else {
                items(filteredNotas, key = { it.id }) { nota ->
                    NotaItemCard(nota = nota, onClick = { onNotaClick(nota) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Selamat Datang, Eko 👋", style = NotaTheme.typography.title.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text("Kelola nota & transaksi bisnis dengan mudah", style = NotaTheme.typography.label.copy(fontSize = 11.5.sp), color = TextGray)
    }
}

@Composable
fun NotaItemCard(nota: Nota, onClick: () -> Unit) {
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)
    val statusColor = if (isLunas) AccentEmerald else AccentAmber

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isLunas) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        nota.customerName,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        nota.total,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = Color.White
                    )
                }
                
                if (nota.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = nota.items.joinToString(", ") { it.name },
                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${nota.referenceCode} • ${nota.date}",
                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            nota.status,
                            style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatCurrency(amount: Long): String {
    return try {
        java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")).format(amount)
    } catch (e: Exception) {
        amount.toString()
    }
}
