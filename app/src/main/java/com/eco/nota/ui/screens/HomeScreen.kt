package com.eco.nota.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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
    downloadState: com.eco.nota.utils.UpdateDownloadState = com.eco.nota.utils.UpdateDownloadState.Idle,
    onUpdateClick: () -> Unit = {},
    onDismissUpdate: () -> Unit = {}
) {
    var selectedDateFilter by remember { mutableStateOf("All Time") }
    var dateOffset by remember { mutableIntStateOf(0) }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var selectedPaymentFilter by remember { mutableStateOf("Semua") }

    val targetCalendar = remember(selectedDateFilter, dateOffset) {
        java.util.Calendar.getInstance().apply {
            when (selectedDateFilter) {
                "Harian" -> add(java.util.Calendar.DAY_OF_YEAR, dateOffset)
                "Bulanan" -> add(java.util.Calendar.MONTH, dateOffset)
                "Tahunan" -> add(java.util.Calendar.YEAR, dateOffset)
            }
        }
    }

    val periodLabel = remember(selectedDateFilter, dateOffset, targetCalendar) {
        val idLocale = java.util.Locale("id", "ID")
        when (selectedDateFilter) {
            "Harian" -> {
                val formatted = java.text.SimpleDateFormat("dd MMM yyyy", idLocale).format(targetCalendar.time)
                when (dateOffset) {
                    0 -> "Hari Ini ($formatted)"
                    -1 -> "Kemarin ($formatted)"
                    1 -> "Besok ($formatted)"
                    else -> java.text.SimpleDateFormat("EEE, dd MMM yyyy", idLocale).format(targetCalendar.time)
                }
            }
            "Bulanan" -> {
                val formatted = java.text.SimpleDateFormat("MMM yyyy", idLocale).format(targetCalendar.time)
                when (dateOffset) {
                    0 -> "Bulan Ini ($formatted)"
                    -1 -> "Bulan Kemarin ($formatted)"
                    1 -> "Bulan Depan ($formatted)"
                    else -> java.text.SimpleDateFormat("MMMM yyyy", idLocale).format(targetCalendar.time)
                }
            }
            "Tahunan" -> {
                val yearStr = targetCalendar.get(java.util.Calendar.YEAR).toString()
                when (dateOffset) {
                    0 -> "Tahun Ini ($yearStr)"
                    -1 -> "Tahun Kemarin ($yearStr)"
                    1 -> "Tahun Depan ($yearStr)"
                    else -> "Tahun $yearStr"
                }
            }
            else -> "Semua Waktu"
        }
    }

    val filteredNotas = remember(selectedDateFilter, dateOffset, selectedStatusFilter, selectedPaymentFilter, notas) {
        val list = notas.filter { nota ->
            val matchesStatus = when (selectedStatusFilter) {
                "Lunas" -> nota.status.equals("Lunas", ignoreCase = true)
                "Tagihan" -> nota.status.equals("Tagihan", ignoreCase = true) || nota.status.equals("Piutang", ignoreCase = true)
                else -> true
            }

            val matchesPayment = when (selectedPaymentFilter) {
                "Semua" -> true
                else -> getPaymentCategory(nota.paymentMethod).equals(selectedPaymentFilter, ignoreCase = true)
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
                            "Harian" -> cal.get(java.util.Calendar.DAY_OF_YEAR) == targetCalendar.get(java.util.Calendar.DAY_OF_YEAR) &&
                                cal.get(java.util.Calendar.YEAR) == targetCalendar.get(java.util.Calendar.YEAR)
                            "Bulanan" -> cal.get(java.util.Calendar.MONTH) == targetCalendar.get(java.util.Calendar.MONTH) &&
                                cal.get(java.util.Calendar.YEAR) == targetCalendar.get(java.util.Calendar.YEAR)
                            "Tahunan" -> cal.get(java.util.Calendar.YEAR) == targetCalendar.get(java.util.Calendar.YEAR)
                            else -> true
                        }
                    }
                } catch (e: Exception) { false }
            }
            matchesStatus && matchesPayment && matchesDate
        }

        list.sortedWith(
            compareByDescending<Nota> { nota ->
                try {
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).parse(nota.date)?.time
                        ?: java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).parse(nota.date)?.time
                        ?: 0L
                } catch (e: Exception) { 0L }
            }.thenByDescending { it.id }
        )
    }

    val totalOmzet = filteredNotas.filter { it.status.equals("Lunas", ignoreCase = true) }.sumOf { nota ->
        nota.calculateSubtotal()
    }

    val totalPiutang = filteredNotas.filter { it.status.equals("Tagihan", ignoreCase = true) || it.status.equals("Piutang", ignoreCase = true) }.sumOf { nota ->
        nota.calculateSubtotal()
    }

    Scaffold(
        containerColor = ProBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNotaClick,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("create_nota_fab")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fixed Top Header, Financial Summary & Filters (tetap terlihat saat daftar riwayat nota digeser)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ProBackground)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                HomeHeader()

                if (updateInfo?.hasUpdate == true) {
                    com.eco.nota.ui.components.UpdateBannerCard(
                        updateInfo = updateInfo,
                        downloadState = downloadState,
                        onClick = onUpdateClick,
                        onDismiss = onDismissUpdate
                    )
                }

                // Financial Summary Card (Fixed)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderGradient)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "RINGKASAN KEUANGAN",
                                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = TextMuted
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Subtotal Murni",
                                        style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Aktif",
                                    style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                                    color = AccentEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Omzet Lunas", style = NotaTheme.typography.label, color = TextGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Rp ${formatCurrency(totalOmzet)}",
                                    style = NotaTheme.typography.title.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                                    color = AccentEmerald
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Tagihan", style = NotaTheme.typography.label, color = TextGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Rp ${formatCurrency(totalPiutang)}",
                                    style = NotaTheme.typography.title.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                                    color = AccentAmber
                                )
                            }
                        }
                    }
                }

                // Filter Tabs (Fixed)
                val dateFilters = listOf("All Time", "Harian", "Bulanan", "Tahunan")
                val statusFilters = listOf("Semua", "Lunas", "Tagihan")
                val paymentFilters = listOf("Semua", "Tunai", "Transfer", "QRIS", "E-Wallet")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateFilters.forEach { tab ->
                            val isSelected = selectedDateFilter == tab
                            Surface(
                                modifier = Modifier
                                    .testTag("filter_date_${tab.lowercase().replace(" ", "_")}")
                                    .clickable {
                                        selectedDateFilter = tab
                                        dateOffset = 0
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(
                                    tab,
                                    style = NotaTheme.typography.body.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    ),
                                    color = if (isSelected) Color.White else TextGray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Period Stepper Bar (bisa digeser mundur/maju untuk Harian, Bulanan, Tahunan)
                    AnimatedVisibility(visible = selectedDateFilter != "All Time") {
                        var totalDragX by remember { mutableStateOf(0f) }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("period_stepper_bar")
                                .pointerInput(selectedDateFilter) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDragX = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDragX += dragAmount
                                        },
                                        onDragEnd = {
                                            if (totalDragX > 60f) {
                                                // Geser ke kanan -> periode sebelumnya (mundur)
                                                dateOffset -= 1
                                            } else if (totalDragX < -60f) {
                                                // Geser ke kiri -> periode berikutnya (maju)
                                                dateOffset += 1
                                            }
                                            totalDragX = 0f
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = ProCardSurface,
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { dateOffset -= 1 },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("period_prev_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Periode Sebelumnya",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = dateOffset != 0) { dateOffset = 0 }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = periodLabel,
                                        style = NotaTheme.typography.body.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (dateOffset != 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = AccentBlue.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Reset",
                                                style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = Color(0xFF93C5FD),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { dateOffset += 1 },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("period_next_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Periode Berikutnya",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Status Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusFilters.forEach { tab ->
                            val isSelected = selectedStatusFilter == tab
                            Surface(
                                modifier = Modifier
                                    .testTag("filter_status_${tab.lowercase()}")
                                    .clickable { selectedStatusFilter = tab },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(
                                    tab,
                                    style = NotaTheme.typography.body.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    ),
                                    color = if (isSelected) Color.White else TextGray,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Payment Method Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentFilters.forEach { tab ->
                            val isSelected = selectedPaymentFilter == tab
                            val chipColor = if (isSelected) AccentEmerald else ProCardSurfaceLight
                            val borderColor = if (isSelected) AccentEmerald else ProBorder
                            val contentColor = if (isSelected) Color.Black else TextGray
                            Surface(
                                modifier = Modifier
                                    .testTag("filter_payment_${tab.lowercase().replace("-", "_")}")
                                    .clickable { selectedPaymentFilter = tab },
                                shape = RoundedCornerShape(10.dp),
                                color = chipColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    if (tab != "Semua") {
                                        Icon(
                                            imageVector = getPaymentIcon(tab),
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = if (tab == "Semua") "Semua Bayar" else tab,
                                        style = NotaTheme.typography.body.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.5.sp
                                        ),
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "RIWAYAT NOTA (${filteredNotas.size})",
                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                HorizontalDivider(color = ProBorder.copy(alpha = 0.5f))
            }

            // Scrollable Riwayat Nota List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredNotas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .background(PrimaryGradient, CircleShape)
                                        .padding(3.dp)
                                        .background(ProBackground, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = AccentBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Tidak ada nota sesuai filter",
                                    style = NotaTheme.typography.heading.copy(fontSize = 16.sp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Ubah filter atau ketuk Buat Nota untuk mencatat transaksi",
                                    style = NotaTheme.typography.label,
                                    color = TextGray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredNotas, key = { it.id }) { nota ->
                        NotaItemCard(nota = nota, onClick = { onNotaClick(nota) })
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Selamat Datang, Eko 👋",
            style = NotaTheme.typography.title.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            "Kelola nota & transaksi bisnis dengan mudah",
            style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
            color = TextGray
        )
    }
}

@Composable
fun NotaItemCard(nota: Nota, onClick: () -> Unit) {
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)
    val statusColor = if (isLunas) AccentEmerald else AccentAmber
    val paymentCategory = getPaymentCategory(nota.paymentMethod)
    val paymentIcon = getPaymentIcon(paymentCategory)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nota_item_card_${nota.id}")
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
                        imageVector = paymentIcon,
                        contentDescription = "$paymentCategory - ${nota.status}",
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
                    Spacer(modifier = Modifier.width(8.dp))
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

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${nota.referenceCode} • ${nota.date}",
                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentBlue.copy(alpha = 0.18f)
                        ) {
                            Text(
                                paymentCategory,
                                style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
}

fun getPaymentCategory(paymentMethod: String): String {
    val normalized = paymentMethod.trim().lowercase()
    return when {
        normalized.contains("transfer") || normalized.contains("bank") -> "Transfer"
        normalized.contains("qris") -> "QRIS"
        normalized.contains("wallet") || normalized.contains("dana") || normalized.contains("ovo") || normalized.contains("gopay") || normalized.contains("shopeepay") -> "E-Wallet"
        else -> "Tunai"
    }
}

fun getPaymentIcon(paymentCategory: String): ImageVector {
    return when (paymentCategory) {
        "Transfer" -> Icons.Default.CreditCard
        "QRIS" -> Icons.Default.QrCode
        "E-Wallet" -> Icons.Default.Wallet
        else -> Icons.Default.Payments
    }
}

fun formatCurrency(amount: Long): String {
    return try {
        java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")).format(amount)
    } catch (e: Exception) {
        amount.toString()
    }
}

fun Nota.calculateSubtotal(): Long {
    if (subtotal > 0L) return subtotal
    if (items.isNotEmpty()) {
        val sum = items.sumOf { item ->
            val qty = item.quantity.trim().replace(',', '.').toDoubleOrNull() ?: 1.0
            val price = item.price.trim().replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
            (qty * price).toLong()
        }
        if (sum > 0L) return sum
    }
    val totalVal = try {
        total.replace(Regex("[^0-9]"), "").toLong()
    } catch (e: Exception) { 0L }
    return (totalVal - shippingCost + discount).coerceAtLeast(0L)
}
