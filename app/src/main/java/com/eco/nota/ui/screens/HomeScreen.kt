package com.eco.nota.ui.screens

import com.eco.nota.ui.theme.neonGlow
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
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
    defaultDateFilter: String = "All Time",
    updateInfo: com.eco.nota.utils.AppUpdateInfo? = null,
    downloadState: com.eco.nota.utils.UpdateDownloadState = com.eco.nota.utils.UpdateDownloadState.Idle,
    onUpdateClick: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
    onToggleItemDone: (notaId: String, itemIndex: Int) -> Unit = { _, _ -> },
    onSetAllItemsDone: (notaId: String, allDone: Boolean) -> Unit = { _, _ -> },
    onTogglePickedUp: (notaId: String, isPickedUp: Boolean) -> Unit = { _, _ -> },
    onTogglePaymentStatus: (notaId: String, newStatus: String) -> Unit = { _, _ -> }
) {
    var selectedDateFilter by remember { mutableStateOf(defaultDateFilter) }
    var dateOffset by remember { mutableIntStateOf(0) }
    var selectedStatusFilter by remember { mutableStateOf("Semua") }
    var selectedPaymentFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedChecklistNotaIds by remember { mutableStateOf(setOf<String>()) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(defaultDateFilter) {
        selectedDateFilter = defaultDateFilter
        dateOffset = 0
    }

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

    val queryTokens = remember(searchQuery) {
        searchQuery.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    }

    val filteredNotas = remember(selectedDateFilter, dateOffset, selectedStatusFilter, selectedPaymentFilter, queryTokens, notas) {
        val list = notas.filter { nota ->
            val matchesStatus = when (selectedStatusFilter) {
                "Lunas" -> nota.status.equals("Lunas", ignoreCase = true)
                "Tagihan" -> nota.status.equals("Tagihan", ignoreCase = true) || nota.status.equals("Piutang", ignoreCase = true)
                "Belum Diambil" -> !nota.isPickedUp
                "Sudah Diambil" -> nota.isPickedUp
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

            val matchesSearch = if (queryTokens.isEmpty()) true else {
                val itemKeywords = nota.items.joinToString(" ") { "${it.name} ${it.unit}" }
                val searchableText = buildString {
                    append(nota.customerName).append(' ')
                    append(itemKeywords).append(' ')
                    append(nota.referenceCode).append(' ')
                    append(nota.phoneNumber).append(' ')
                    append(nota.address).append(' ')
                    append(nota.customerLabel).append(' ')
                    append(nota.notes).append(' ')
                    append(nota.date).append(' ')
                    append(nota.paymentMethod).append(' ')
                    append(nota.status).append(' ')
                    append(if (nota.isPickedUp) "sudah diambil" else "belum diambil")
                }.lowercase()
                queryTokens.all { token -> searchableText.contains(token) }
            }

            matchesStatus && matchesPayment && matchesDate && matchesSearch
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNotaClick,
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .neonGlow(AccentBlue, RoundedCornerShape(16.dp))
                    .testTag("create_nota_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Buat Nota", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Compact Fixed Top Header, Financial Summary, Search & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ProBackground)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HomeHeader(totalNotas = notas.size)

                if (updateInfo?.hasUpdate == true) {
                    com.eco.nota.ui.components.UpdateBannerCard(
                        updateInfo = updateInfo,
                        downloadState = downloadState,
                        onClick = onUpdateClick,
                        onDismiss = onDismissUpdate
                    )
                }

                // Compact Financial Summary Card (Fixed)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, CardBorderGradient)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
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
                                    style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                    color = TextMuted
                                )
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = AccentBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Subtotal Murni",
                                        style = NotaTheme.typography.label.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Medium),
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    if (queryTokens.isNotEmpty()) "Filter Cari" else "Aktif",
                                    style = NotaTheme.typography.label.copy(fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold),
                                    color = AccentEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Omzet Lunas", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp), color = TextGray)
                                Text(
                                    "Rp ${formatCurrency(totalOmzet)}",
                                    style = NotaTheme.typography.title.copy(fontSize = 16.5.sp, fontWeight = FontWeight.Bold),
                                    color = AccentEmerald
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Tagihan", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp), color = TextGray)
                                Text(
                                    "Rp ${formatCurrency(totalPiutang)}",
                                    style = NotaTheme.typography.title.copy(fontSize = 16.5.sp, fontWeight = FontWeight.Bold),
                                    color = AccentAmber
                                )
                            }
                        }
                    }
                }

                // Compact Search Bar
                HomeSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = {
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    onSearchAction = { focusManager.clearFocus() }
                )

                // Compact Filter Tabs (Fixed)
                val dateFilters = listOf("All Time", "Harian", "Bulanan", "Tahunan")
                val statusFilters = listOf("Semua", "Lunas", "Tagihan", "Belum Diambil", "Sudah Diambil")
                val paymentFilters = listOf("Semua", "Tunai", "Transfer", "QRIS", "E-Wallet")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Date Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(
                                    tab,
                                    style = NotaTheme.typography.body.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (isSelected) Color.White else TextGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
                                                dateOffset -= 1
                                            } else if (totalDragX < -60f) {
                                                dateOffset += 1
                                            }
                                            totalDragX = 0f
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = ProCardSurface,
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { dateOffset -= 1 },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("period_prev_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Periode Sebelumnya",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = dateOffset != 0) { dateOffset = 0 }
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = periodLabel,
                                        style = NotaTheme.typography.body.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (dateOffset != 0) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentBlue.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Reset",
                                                style = NotaTheme.typography.label.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                                color = Color(0xFF93C5FD),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { dateOffset += 1 },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("period_next_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Periode Berikutnya",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusFilters.forEach { tab ->
                            val isSelected = selectedStatusFilter == tab
                            Surface(
                                modifier = Modifier
                                    .testTag("filter_status_${tab.lowercase().replace(" ", "_")}")
                                    .clickable { selectedStatusFilter = tab },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(
                                    tab,
                                    style = NotaTheme.typography.body.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    ),
                                    color = if (isSelected) Color.White else TextGray,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Payment Method Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                shape = RoundedCornerShape(8.dp),
                                color = chipColor,
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (tab != "Semua") {
                                        Icon(
                                            imageVector = getPaymentIcon(tab),
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = if (tab == "Semua") "Semua Bayar" else tab,
                                        style = NotaTheme.typography.body.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.5.sp
                                        ),
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RIWAYAT NOTA (${filteredNotas.size})",
                        style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                        color = TextMuted
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Pencarian: \"${searchQuery.trim()}\"",
                            style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Medium),
                            color = Color(0xFF93C5FD),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                HorizontalDivider(color = ProBorder.copy(alpha = 0.5f))
            }

            // Scrollable Riwayat Nota List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredNotas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(PrimaryGradient, CircleShape)
                                        .padding(3.dp)
                                        .background(ProBackground, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(34.dp),
                                        tint = AccentBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Tidak ada nota untuk \"${searchQuery.trim()}\"" else "Tidak ada nota sesuai filter",
                                    style = NotaTheme.typography.heading.copy(fontSize = 15.sp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Coba kata kunci pelanggan, barang, atau hapus filter lain" else "Ubah filter atau ketuk Buat Nota untuk mencatat transaksi",
                                    style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                    color = TextGray
                                )
                                if (searchQuery.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TextButton(
                                        onClick = { searchQuery = "" },
                                        colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)
                                    ) {
                                        Text("Reset Pencarian", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredNotas, key = { it.id }) { nota ->
                        val isChecklistExpanded = expandedChecklistNotaIds.contains(nota.id)
                        NotaItemCard(
                            nota = nota,
                            showDetailedItems = queryTokens.isNotEmpty(),
                            isChecklistExpanded = isChecklistExpanded,
                            onToggleChecklistExpand = {
                                expandedChecklistNotaIds = if (isChecklistExpanded) {
                                    expandedChecklistNotaIds - nota.id
                                } else {
                                    expandedChecklistNotaIds + nota.id
                                }
                            },
                            onToggleItemDone = { itemIndex -> onToggleItemDone(nota.id, itemIndex) },
                            onSetAllItemsDone = { allDone -> onSetAllItemsDone(nota.id, allDone) },
                            onTogglePickedUp = { onTogglePickedUp(nota.id, !nota.isPickedUp) },
                            onTogglePaymentStatus = {
                                val nextStatus = if (nota.status.equals("Lunas", ignoreCase = true)) "Tagihan" else "Lunas"
                                onTogglePaymentStatus(nota.id, nextStatus)
                            },
                            onClick = { onNotaClick(nota) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(totalNotas: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Halo, Eko 👋",
            style = NotaTheme.typography.title.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            "$totalNotas Transaksi",
            style = NotaTheme.typography.label.copy(fontSize = 11.sp),
            color = TextMuted
        )
    }
}

@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearchAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp),
        shape = RoundedCornerShape(10.dp),
        color = ProCardSurface,
        border = BorderStroke(1.dp, if (query.isNotEmpty()) AccentBlue.copy(alpha = 0.8f) else ProBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari nota",
                tint = if (query.isNotEmpty()) AccentBlue else TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Cari pelanggan, barang dibeli, atau no. nota...",
                        style = NotaTheme.typography.body.copy(fontSize = 12.5.sp),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = NotaTheme.typography.body.copy(
                        fontSize = 13.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("home_search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus pencarian",
                        tint = TextGray,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotaItemCard(
    nota: Nota,
    showDetailedItems: Boolean = false,
    isChecklistExpanded: Boolean = false,
    onToggleChecklistExpand: () -> Unit = {},
    onToggleItemDone: (Int) -> Unit = {},
    onSetAllItemsDone: (Boolean) -> Unit = {},
    onTogglePickedUp: () -> Unit = {},
    onTogglePaymentStatus: () -> Unit = {},
    onClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)
    val statusColor = if (isLunas) AccentEmerald else AccentAmber
    val paymentCategory = getPaymentCategory(nota.paymentMethod)
    val paymentIcon = getPaymentIcon(paymentCategory)
    val doneCount = nota.items.count { it.isDone }
    val totalCount = nota.items.size
    val allItemsDone = totalCount > 0 && doneCount == totalCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nota_item_card_${nota.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isChecklistExpanded) AccentBlue.copy(alpha = 0.6f) else ProBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = paymentIcon,
                            contentDescription = "$paymentCategory - ${nota.status}",
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            nota.customerName,
                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            nota.total,
                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                            color = Color.White
                        )
                    }

                    if (nota.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val itemsSummary = if (showDetailedItems) {
                                nota.items.joinToString(", ") { item ->
                                    val qtyStr = item.quantity.trim()
                                    val unitStr = item.unit.trim()
                                    val qtyPrefix = if (qtyStr.isNotEmpty() && qtyStr != "1") {
                                        if (unitStr.isNotEmpty()) "${qtyStr} ${unitStr} " else "${qtyStr}x "
                                    } else {
                                        ""
                                    }
                                    "$qtyPrefix${item.name}"
                                }
                            } else {
                                nota.items.joinToString(", ") { it.name }
                            }
                            Text(
                                text = itemsSummary,
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                color = if (showDetailedItems) Color(0xFFCBD5E1) else TextGray,
                                maxLines = if (showDetailedItems) 3 else 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            val progressBg = when {
                                allItemsDone -> AccentEmerald.copy(alpha = 0.2f)
                                doneCount > 0 -> AccentAmber.copy(alpha = 0.2f)
                                else -> ProCardSurfaceLight
                            }
                            val progressTint = when {
                                allItemsDone -> AccentEmerald
                                doneCount > 0 -> AccentAmber
                                else -> TextGray
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .clickable { onToggleChecklistExpand() }
                                    .testTag("nota_checklist_toggle_${nota.id}"),
                                shape = RoundedCornerShape(7.dp),
                                color = progressBg,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isChecklistExpanded) AccentBlue else progressTint.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (allItemsDone) "Dibuat $doneCount/$totalCount" else "Pesanan $doneCount/$totalCount",
                                        style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = progressTint
                                    )
                                    Icon(
                                        imageVector = if (isChecklistExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isChecklistExpanded) "Tutup checklist pesanan" else "Buka checklist pesanan",
                                        tint = progressTint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${nota.referenceCode} • ${nota.date}",
                            style = NotaTheme.typography.label.copy(fontSize = 10.5.sp),
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
                                    style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Compact Footer Strip: Checklist Pengambilan + Menu Aksi Kartu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 10.dp, bottom = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pickupBg = if (nota.isPickedUp) AccentEmerald.copy(alpha = 0.16f) else ProCardSurfaceLight
                val pickupTint = if (nota.isPickedUp) AccentEmerald else Color(0xFFFBBF24)
                val pickupBorder = if (nota.isPickedUp) AccentEmerald.copy(alpha = 0.45f) else ProBorder
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTogglePickedUp() }
                        .testTag("nota_pickup_toggle_${nota.id}"),
                    shape = RoundedCornerShape(8.dp),
                    color = pickupBg,
                    border = BorderStroke(1.dp, pickupBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (nota.isPickedUp) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (nota.isPickedUp) "Pesanan sudah diambil" else "Pesanan belum diambil",
                            tint = pickupTint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (nota.isPickedUp) "Sudah Diambil" else "Belum Diambil",
                            style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = pickupTint
                        )
                    }
                }

                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { menuExpanded = true }
                            .testTag("nota_card_menu_button_${nota.id}"),
                        shape = RoundedCornerShape(8.dp),
                        color = ProCardSurfaceLight.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu aksi nota",
                                tint = TextGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Menu",
                                style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
                                color = TextGray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(ProCardSurface)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isLunas) "Tandai Belum Lunas" else "Tandai Sudah Lunas",
                                    style = NotaTheme.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isLunas) Icons.Default.PendingActions else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isLunas) AccentAmber else AccentEmerald
                                )
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White),
                            onClick = {
                                menuExpanded = false
                                onTogglePaymentStatus()
                            },
                            modifier = Modifier.testTag("nota_menu_payment_status_${nota.id}")
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (nota.isPickedUp) "Tandai Belum Diambil" else "Tandai Sudah Diambil",
                                    style = NotaTheme.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (nota.isPickedUp) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (nota.isPickedUp) AccentEmerald else AccentAmber
                                )
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White),
                            onClick = {
                                menuExpanded = false
                                onTogglePickedUp()
                            },
                            modifier = Modifier.testTag("nota_menu_pickup_${nota.id}")
                        )
                        if (nota.items.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Pesanan Dibuat ($doneCount/$totalCount)",
                                        style = NotaTheme.typography.body.copy(fontSize = 13.sp),
                                        color = Color.White
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = AccentBlue
                                    )
                                },
                                colors = MenuDefaults.itemColors(textColor = Color.White),
                                onClick = {
                                    menuExpanded = false
                                    onToggleChecklistExpand()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Lihat Detail & Struk",
                                    style = NotaTheme.typography.body.copy(fontSize = 13.sp),
                                    color = Color.White
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            },
                            colors = MenuDefaults.itemColors(textColor = Color.White),
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isChecklistExpanded && nota.items.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProBackground.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = ProBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PESANAN SUDAH DIBUAT ($doneCount/$totalCount)",
                            style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                            color = TextMuted
                        )
                        Text(
                            text = if (allItemsDone) "Reset Belum" else "Tandai Semua",
                            style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = if (allItemsDone) TextGray else AccentEmerald,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSetAllItemsDone(!allItemsDone) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("nota_mark_all_${nota.id}")
                        )
                    }

                    nota.items.forEachIndexed { index, item ->
                        val qtyStr = item.quantity.trim()
                        val unitStr = item.unit.trim()
                        val qtyLabel = when {
                            qtyStr.isNotEmpty() && unitStr.isNotEmpty() -> "$qtyStr $unitStr"
                            qtyStr.isNotEmpty() -> "${qtyStr}x"
                            else -> ""
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleItemDone(index) }
                                .testTag("nota_item_check_${nota.id}_$index"),
                            shape = RoundedCornerShape(8.dp),
                            color = if (item.isDone) AccentEmerald.copy(alpha = 0.1f) else ProCardSurfaceLight.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (item.isDone) AccentEmerald.copy(alpha = 0.35f) else ProBorder.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = if (item.isDone) "Sudah dibuat" else "Belum dibuat",
                                    tint = if (item.isDone) AccentEmerald else TextGray,
                                    modifier = Modifier.size(19.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (qtyLabel.isNotEmpty()) "$qtyLabel • ${item.name}" else item.name,
                                        style = NotaTheme.typography.body.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = if (item.isDone) FontWeight.Normal else FontWeight.SemiBold,
                                            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (item.isDone) TextGray else Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = if (item.isDone) AccentEmerald.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = if (item.isDone) "Sudah Dibuat" else "Belum",
                                        style = NotaTheme.typography.label.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                        color = if (item.isDone) AccentEmerald else AccentAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
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
