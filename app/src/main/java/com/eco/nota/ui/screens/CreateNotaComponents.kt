package com.eco.nota.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import android.app.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eco.nota.models.CustomerSuggestion
import com.eco.nota.models.NotaItemInput
import com.eco.nota.ui.components.NotaTextField
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.AccentRose
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatNumber(amount: Long): String {
    return try {
        NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    } catch (e: Exception) {
        amount.toString()
    }
}

@Composable
fun DateSection(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    referenceCode: String = "",
    onReferenceCodeChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val todayStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
    }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(cal.time)
    }

    val displayFullDate = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(selectedDate)
                ?: SimpleDateFormat("dd MMM yyyy", Locale.US).parse(selectedDate)
            if (parsed != null) {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(parsed)
            } else {
                selectedDate
            }
        } catch (e: Exception) {
            selectedDate
        }
    }

    val openDatePicker = {
        val calendar = Calendar.getInstance()
        try {
            val parsed = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(selectedDate)
                ?: SimpleDateFormat("dd MMM yyyy", Locale.US).parse(selectedDate)
            if (parsed != null) {
                calendar.time = parsed
            }
        } catch (e: Exception) {
            // fallback
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val formatted = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(cal.time)
                onDateChange(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.dp, ProBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Judul & Tombol Ubah Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "TANGGAL NOTA",
                        style = NotaTheme.typography.label.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextMuted
                    )
                }

                Surface(
                    modifier = Modifier.clickable { openDatePicker() },
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.EditCalendar,
                            contentDescription = "Pilih Kalender",
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Pilih Tanggal",
                            style = NotaTheme.typography.label.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFA5B4FC)
                        )
                    }
                }
            }

            // Kartu Tampilan Tanggal (Bisa diklik langsung untuk buka kalender)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker() },
                shape = RoundedCornerShape(12.dp),
                color = ProCardSurfaceLight,
                border = BorderStroke(1.dp, ProBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayFullDate,
                            style = NotaTheme.typography.body.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tercetak di struk: $selectedDate",
                            style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                            color = TextGray
                        )
                    }
                    Icon(
                        Icons.Default.Today,
                        contentDescription = "Pilih Tanggal",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Shortcut Cepat: Hari Ini & Kemarin
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isToday = selectedDate.equals(todayStr, ignoreCase = true)
                Surface(
                    modifier = Modifier.clickable { onDateChange(todayStr) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isToday) AccentEmerald.copy(alpha = 0.2f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (isToday) AccentEmerald else ProBorder
                    )
                ) {
                    Text(
                        text = "Hari Ini",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = NotaTheme.typography.label.copy(
                            fontSize = 11.5.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isToday) AccentEmerald else TextGray
                    )
                }

                val isYesterday = selectedDate.equals(yesterdayStr, ignoreCase = true)
                Surface(
                    modifier = Modifier.clickable { onDateChange(yesterdayStr) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isYesterday) AccentBlue.copy(alpha = 0.2f) else Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (isYesterday) AccentBlue else ProBorder
                    )
                ) {
                    Text(
                        text = "Kemarin",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = NotaTheme.typography.label.copy(
                            fontSize = 11.5.sp,
                            fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isYesterday) Color(0xFFA5B4FC) else TextGray
                    )
                }
            }

            HorizontalDivider(color = ProBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // 2. NOMOR NOTA (TERSTRUKTUR INV)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Sell,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "NOMOR NOTA",
                            style = NotaTheme.typography.label.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            "AUTO INV",
                            style = NotaTheme.typography.label.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                NotaTextField(
                    value = referenceCode,
                    onValueChange = onReferenceCodeChange,
                    placeholder = "INV-YYMMDD-001",
                    leadingIcon = Icons.Default.Sell
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BuyerInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    customerSuggestions: List<CustomerSuggestion> = emptyList(),
    onSuggestionSelected: (CustomerSuggestion) -> Unit = {},
    onPickContact: () -> Unit = {}
) {
    var showSuggestions by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(name, customerSuggestions) {
        if (name.isBlank()) emptyList()
        else customerSuggestions.filter {
            it.name.contains(name, ignoreCase = true) && it.name.lowercase().trim() != name.lowercase().trim()
        }.take(15)
    }
    val customerBringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(showSuggestions, filteredSuggestions.size, name) {
        if (showSuggestions && filteredSuggestions.isNotEmpty()) {
            delay(100)
            try {
                customerBringIntoViewRequester.bringIntoView()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.dp, ProBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Text("INFORMASI PELANGGAN", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted)
                }
                
                Surface(
                    modifier = Modifier.clickable { onPickContact() },
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Pilih dari Kontak", tint = Color(0xFFA5B4FC), modifier = Modifier.size(14.dp))
                        Text("Ambil Kontak", style = NotaTheme.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold), color = Color(0xFFA5B4FC))
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(customerBringIntoViewRequester),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NotaTextField(
                    value = name,
                    onValueChange = {
                        onNameChange(it)
                        showSuggestions = true
                    },
                    placeholder = "Nama Pelanggan / Toko",
                    leadingIcon = Icons.Default.Person
                )
                
                if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ProCardSurfaceLight),
                        border = BorderStroke(1.dp, ProBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ProCardSurface)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "RIWAYAT PELANGGAN (${filteredSuggestions.size})",
                                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFFA5B4FC)
                                )
                                Text(
                                    "Bisa di-scroll ↕",
                                    style = NotaTheme.typography.label.copy(fontSize = 10.sp),
                                    color = TextGray
                                )
                            }
                            HorizontalDivider(color = ProBorder)

                            filteredSuggestions.forEach { sugg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSuggestionSelected(sugg)
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(sugg.name, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
                                        if (sugg.phoneNumber.isNotEmpty() || sugg.address.isNotEmpty()) {
                                            val details = listOfNotNull(
                                                sugg.phoneNumber.takeIf { it.isNotEmpty() },
                                                sugg.address.takeIf { it.isNotEmpty() }
                                            ).joinToString(" • ")
                                            Text(details, style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                                        }
                                    }
                                }
                                HorizontalDivider(color = ProBorder)
                            }
                        }
                    }
                }
            }

            NotaTextField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = "Nomor WhatsApp / Telepon",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            NotaTextField(
                value = address,
                onValueChange = onAddressChange,
                placeholder = "Alamat Pengiriman / Kota",
                leadingIcon = Icons.Default.LocationOn
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemsSection(
    items: List<NotaItemInput>,
    onItemChanged: (Int, NotaItemInput) -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (Int) -> Unit,
    productSuggestions: List<NotaItemInput> = emptyList(),
    savedUnitsList: List<String> = emptyList(),
    deletedUnits: Set<String> = emptySet(),
    onProductSelected: (Int, NotaItemInput) -> Unit = { _, _ -> },
    onDeleteUnit: (String) -> Unit = {},
    onRestoreUnit: (String) -> Unit = {}
) {
    val defaultUnits = listOf("Pack", "Pcs", "Slop", "Bungkus", "Kg", "Box", "Botol", "Lusin", "Gram", "Unit", "Meter", "Ikat", "Porsi")
    val allUnits = remember(savedUnitsList) {
        (savedUnitsList + defaultUnits).distinct().filter { it.isNotBlank() }
    }

    // Which item is actively opened in full detail mode (default: first item if empty, or newly added item)
    var editingIndex by remember { mutableStateOf<Int?>(if (items.size == 1 && items[0].name.isBlank()) 0 else null) }
    var unitToDelete by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    // When editingIndex changes to a valid item, request focus with slight delay
    LaunchedEffect(editingIndex) {
        if (editingIndex != null) {
            delay(100)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.dp, ProBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with count and quick add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Text("DAFTAR BARANG (${items.size})", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted)
                }
                Surface(
                    modifier = Modifier.clickable {
                        onAddItem()
                        editingIndex = items.size // Open the newly created item
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = AccentBlue.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AccentBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                        Text("Tambah", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = Color(0xFFA5B4FC))
                    }
                }
            }

            // Items List (Compact Rows vs Expanded Editing Card)
            items.forEachIndexed { index, item ->
                val isEditing = editingIndex == index
                val currentQty = item.quantity.trim().replace(',', '.').toDoubleOrNull() ?: 1.0
                val currentPrice = item.price.toLongOrNull() ?: 0L
                val itemSubtotal = (currentQty * currentPrice).toLong()

                if (!isEditing) {
                    // ==========================================
                    // 1. COMPACT ROW (Mudah Dilihat & Hemat Ruang)
                    // ==========================================
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingIndex = index },
                        shape = RoundedCornerShape(14.dp),
                        color = ProCardSurfaceLight,
                        border = BorderStroke(1.dp, ProBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AccentBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "#${index + 1}",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFFA5B4FC),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = item.name.ifEmpty { "Barang Belum Dinamai" },
                                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                        color = if (item.name.isNotEmpty()) Color.White else TextMuted
                                    )
                                    Text(
                                        text = "${item.quantity} ${item.unit} × Rp ${formatNumber(currentPrice)}",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                        color = TextGray
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Rp ${formatNumber(itemSubtotal)}",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                    color = AccentEmerald
                                )

                                IconButton(
                                    onClick = { editingIndex = index },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                                }

                                if (items.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteItem(index) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = AccentRose, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ========================================================
                    // 2. EXPANDED EDITING CARD (Form Isian Detail Lengkap)
                    // ========================================================
                    var showProductSuggestions by remember { mutableStateOf(false) }
                    val filteredProdSuggestions = remember(item.name, productSuggestions) {
                        if (item.name.isBlank()) emptyList()
                        else productSuggestions.filter {
                            it.name.contains(item.name, ignoreCase = true) && it.name.lowercase().trim() != item.name.lowercase().trim()
                        }.take(20)
                    }
                    val prodBringIntoViewRequester = remember { BringIntoViewRequester() }

                    LaunchedEffect(showProductSuggestions, filteredProdSuggestions.size, item.name) {
                        if (showProductSuggestions && filteredProdSuggestions.isNotEmpty()) {
                            delay(120)
                            try {
                                prodBringIntoViewRequester.bringIntoView()
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ProCardSurfaceLight),
                        border = BorderStroke(1.5.dp, AccentBlue)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header with badge and Done button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentBlue.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, AccentBlue)
                                ) {
                                    Text(
                                        "MENGISI ITEM #${index + 1}",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFFA5B4FC),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (items.size > 1) {
                                        Surface(
                                            modifier = Modifier.clickable {
                                                onDeleteItem(index)
                                                editingIndex = null
                                            },
                                            shape = CircleShape,
                                            color = AccentRose.copy(alpha = 0.15f)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Hapus Item",
                                                tint = AccentRose,
                                                modifier = Modifier.padding(6.dp).size(16.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.clickable { editingIndex = null },
                                        shape = RoundedCornerShape(8.dp),
                                        color = AccentEmerald.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, AccentEmerald)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                                            Text("Selesai", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = AccentEmerald)
                                        }
                                    }
                                }
                            }

                            // 1. Nama Barang Field (Dengan Auto-Focus & Auto Bring-into-View)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(prodBringIntoViewRequester),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = {
                                        onItemChanged(index, item.copy(name = it))
                                        showProductSuggestions = true
                                    },
                                    placeholder = { Text("Nama Barang / Produk / Layanan", color = TextMuted, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = ProBorder,
                                        focusedContainerColor = ProCardSurface,
                                        unfocusedContainerColor = ProCardSurface,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )
                                
                                if (showProductSuggestions && filteredProdSuggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                                        border = BorderStroke(1.5.dp, AccentBlue.copy(alpha = 0.8f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            // Header
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ProCardSurfaceLight)
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "PILIHAN PRODUK (${filteredProdSuggestions.size})",
                                                    style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                                    color = Color(0xFFA5B4FC)
                                                )
                                                Text(
                                                    "Bisa di-scroll ↕",
                                                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp),
                                                    color = TextGray
                                                )
                                            }
                                            HorizontalDivider(color = ProBorder)

                                            filteredProdSuggestions.forEach { sugg ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            onProductSelected(index, sugg)
                                                            showProductSuggestions = false
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Sell, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(15.dp))
                                                        Text(
                                                            sugg.name,
                                                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                                            color = Color.White
                                                        )
                                                    }
                                                    val priceText = if (sugg.price.isNotEmpty()) "Rp ${formatNumber(sugg.price.toLongOrNull() ?: 0L)}" else ""
                                                    val unitText = if (sugg.unit.isNotEmpty()) " / ${sugg.unit}" else ""
                                                    Text(
                                                        "$priceText$unitText",
                                                        style = NotaTheme.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                        color = AccentEmerald
                                                    )
                                                }
                                                HorizontalDivider(color = ProBorder.copy(alpha = 0.5f))
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Row Qty and Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Qty with Stepper Buttons
                                Column(modifier = Modifier.weight(0.42f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Jumlah (Qty)", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    val current = item.quantity.trim().replace(',', '.').toDoubleOrNull() ?: 1.0
                                                    if (current > 1.0) {
                                                        val next = if (current % 1.0 == 0.0) (current - 1).toLong().toString() else (current - 1).toString()
                                                        onItemChanged(index, item.copy(quantity = next))
                                                    }
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = ProCardSurface,
                                            border = BorderStroke(1.dp, ProBorder)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        NotaTextField(
                                            value = item.quantity,
                                            onValueChange = { onItemChanged(index, item.copy(quantity = it)) },
                                            placeholder = "1",
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                        )

                                        Surface(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    val current = item.quantity.trim().replace(',', '.').toDoubleOrNull() ?: 1.0
                                                    val next = if (current % 1.0 == 0.0) (current + 1).toLong().toString() else (current + 1).toString()
                                                    onItemChanged(index, item.copy(quantity = next))
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = ProCardSurface,
                                            border = BorderStroke(1.dp, ProBorder)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }

                                // Price
                                Column(modifier = Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Harga Satuan (Rp)", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                                    NotaTextField(
                                        value = item.price,
                                        onValueChange = { onItemChanged(index, item.copy(price = it)) },
                                        placeholder = "Contoh: 30000",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }

                            // 3. Quick Saved Units List
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val displayedUnits = allUnits.filter { it !in deletedUnits }

                                if (unitToDelete != null) {
                                    AlertDialog(
                                        onDismissRequest = { unitToDelete = null },
                                        containerColor = ProCardSurface,
                                        title = {
                                            Text("Hapus Satuan?", style = NotaTheme.typography.heading.copy(fontSize = 16.sp), color = Color.White)
                                        },
                                        text = {
                                            Text("Hapus satuan '${unitToDelete}' dari daftar pilihan cepat?", style = NotaTheme.typography.body, color = TextGray)
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    val toDelete = unitToDelete!!
                                                    onDeleteUnit(toDelete)
                                                    if (item.unit.equals(toDelete, ignoreCase = true)) {
                                                        onItemChanged(index, item.copy(unit = ""))
                                                    }
                                                    unitToDelete = null
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Hapus", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                            }
                                        },
                                        dismissButton = {
                                            OutlinedButton(
                                                onClick = { unitToDelete = null },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, ProBorder)
                                            ) {
                                                Text("Batal", style = NotaTheme.typography.label.copy(color = TextGray))
                                            }
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("PILIH SATUAN :", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = TextMuted)
                                        Text("(Tahan lama utk hapus)", style = NotaTheme.typography.label.copy(fontSize = 9.5.sp), color = TextGray)
                                    }

                                    if (item.unit.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("Terpilih: ${item.unit}", style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = Color(0xFFA5B4FC))
                                            if (displayedUnits.contains(item.unit)) {
                                                Surface(
                                                    modifier = Modifier.clickable { unitToDelete = item.unit },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = AccentRose.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.35f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Satuan", tint = AccentRose, modifier = Modifier.size(11.dp))
                                                        Text("Hapus", style = NotaTheme.typography.label.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = AccentRose)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    displayedUnits.forEach { u ->
                                        val isSelected = item.unit.equals(u, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier.combinedClickable(
                                                onClick = { onItemChanged(index, item.copy(unit = u)) },
                                                onLongClick = { unitToDelete = u }
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) AccentBlue else ProCardSurface,
                                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = u,
                                                    style = NotaTheme.typography.label.copy(
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) Color.White else TextGray
                                                )
                                            }
                                        }
                                    }
                                }

                                // Custom Unit input box if user wants to type freely
                                NotaTextField(
                                    value = item.unit,
                                    onValueChange = { 
                                        onItemChanged(index, item.copy(unit = it))
                                        if (deletedUnits.contains(it)) {
                                            onRestoreUnit(it)
                                        }
                                    },
                                    placeholder = "Ketik satuan kustom...",
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                )
                            }

                            // 4. Real-time Subtotal & Selesai Button
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = ProCardSurface,
                                border = BorderStroke(1.dp, ProBorder.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Subtotal: ${item.quantity.ifBlank { "1" }} ${item.unit.ifEmpty { "Pcs" }} × Rp ${formatNumber(currentPrice)}",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                        color = TextGray
                                    )
                                    Text(
                                        "Rp ${formatNumber(itemSubtotal)}",
                                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                        color = AccentEmerald
                                    )
                                }
                            }

                            Button(
                                onClick = { editingIndex = null },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Selesai Mengisi Item Ini", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
                            }
                        }
                    }
                }
            }

            // Big Easy-to-Access "+ Tambah Barang Baru" Button at the bottom of the items list
            OutlinedButton(
                onClick = {
                    onAddItem()
                    editingIndex = items.size
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, AccentBlue.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Tambah Barang Baru", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color(0xFFA5B4FC))
            }
        }
    }
}
