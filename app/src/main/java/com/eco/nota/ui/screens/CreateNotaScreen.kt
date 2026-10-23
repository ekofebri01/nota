package com.eco.nota.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eco.nota.models.Nota
import com.eco.nota.models.NotaItemInput
import com.eco.nota.models.NotaViewModel
import com.eco.nota.ui.components.NotaTextField
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBackground
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotaScreen(viewModel: NotaViewModel, notaToEdit: Nota? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val savedNotas by viewModel.notas.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()
    var showLiveReceiptPreview by remember { mutableStateOf(false) }

    // 1. Unique Customers Suggestion list
    val customerSuggestions = remember(savedNotas) {
        savedNotas.map {
            com.eco.nota.models.CustomerSuggestion(
                name = it.customerName,
                phoneNumber = it.phoneNumber,
                address = it.address
            )
        }.distinctBy { it.name.lowercase().trim() }
    }

    // 2. Unique Products Suggestion list
    val productSuggestions = remember(savedNotas) {
        savedNotas.flatMap { it.items }.map {
            NotaItemInput(
                name = it.name,
                quantity = "1",
                unit = it.unit,
                price = it.price
            )
        }.distinctBy { it.name.lowercase().trim() }
    }

    // 3. Unique Saved Units list from all previous transactions
    val deletedUnits by viewModel.deletedUnits.collectAsState()
    val savedUnitsList = remember(savedNotas, deletedUnits) {
        savedNotas.flatMap { it.items }.map { it.unit.trim() }.filter { it.isNotEmpty() && !deletedUnits.contains(it) }.distinct()
    }

    var customerName by remember { mutableStateOf(notaToEdit?.customerName ?: "") }
    var phoneNumber by remember { mutableStateOf(notaToEdit?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(notaToEdit?.address ?: "") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                try {
                    val contentResolver = context.contentResolver
                    var pickedName = ""
                    var pickedPhone = ""

                    // 1. Baca langsung dari URI spesifik yang dipilih pengguna.
                    // Melalui ACTION_PICK CommonDataKinds.Phone, Android otomatis memberikan
                    // izin baca sementara (URI permission) ke baris kontak ini tanpa perlu izin READ_CONTACTS global.
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )

                    try {
                        contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                                if (nameIdx != -1) {
                                    pickedName = cursor.getString(nameIdx) ?: ""
                                }
                                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIdx != -1) {
                                    pickedPhone = cursor.getString(numIdx) ?: ""
                                }
                            }
                        }
                    } catch (queryEx: Exception) {
                        // Jika perangkat OEM tertentu memerlukan query tanpa projection spesifik
                    }

                    // 2. Fallback pembacaan kolom alternatif jika belum terisi
                    if (pickedName.isEmpty() || pickedPhone.isEmpty()) {
                        try {
                            contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    if (pickedName.isEmpty()) {
                                        val altNameCols = listOf("display_name", "display_name_primary", "name")
                                        for (col in altNameCols) {
                                            val idx = cursor.getColumnIndex(col)
                                            if (idx != -1) {
                                                val v = cursor.getString(idx)
                                                if (!v.isNullOrBlank()) {
                                                    pickedName = v
                                                    break
                                                }
                                            }
                                        }
                                    }
                                    if (pickedPhone.isEmpty()) {
                                        val altPhoneCols = listOf("data1", "data4", "number", "phone_number")
                                        for (col in altPhoneCols) {
                                            val idx = cursor.getColumnIndex(col)
                                            if (idx != -1) {
                                                val v = cursor.getString(idx)
                                                if (!v.isNullOrBlank()) {
                                                    pickedPhone = v
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Abaikan fallback query jika terjadi proteksi
                        }
                    }

                    if (pickedName.isNotEmpty()) {
                        customerName = pickedName
                    }
                    if (pickedPhone.isNotEmpty()) {
                        val cleanPhone = pickedPhone.replace(Regex("[^0-9+]"), "")
                        phoneNumber = cleanPhone
                    }

                    if (pickedName.isNotEmpty() || pickedPhone.isNotEmpty()) {
                        Toast.makeText(context, "Kontak '$pickedName' berhasil dipilih!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Kontak dipilih, namun detail nomor telepon tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal mengambil kontak: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    
    var selectedDate by remember {
        mutableStateOf(
            notaToEdit?.date?.takeIf { it.isNotBlank() }
                ?: SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        )
    }

    var referenceCode by remember(notaToEdit) {
        mutableStateOf(
            notaToEdit?.referenceCode ?: viewModel.generateNextInvoiceNumber(selectedDate)
        )
    }

    LaunchedEffect(selectedDate) {
        if (notaToEdit == null && (referenceCode.isBlank() || referenceCode.startsWith("INV-"))) {
            referenceCode = viewModel.generateNextInvoiceNumber(selectedDate)
        }
    }

    var items by remember { mutableStateOf(notaToEdit?.items ?: listOf(NotaItemInput(name = "", quantity = "1", unit = "Pack", price = ""))) }
    var status by remember { mutableStateOf(notaToEdit?.status ?: "Lunas") }
    var paymentMethod by remember { mutableStateOf(notaToEdit?.paymentMethod ?: "Tunai") }
    var discount by remember { mutableStateOf(if (notaToEdit != null && notaToEdit.discount > 0) notaToEdit.discount.toString() else "") }
    var shippingCost by remember { mutableStateOf(if (notaToEdit != null && notaToEdit.shippingCost > 0) notaToEdit.shippingCost.toString() else "") }
    var notes by remember { mutableStateOf(notaToEdit?.notes ?: "") }

    val subtotal = items.sumOf {
        val qty = it.quantity.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        val price = it.price.toLongOrNull() ?: 0L
        (qty * price).toLong()
    }

    val discVal = discount.toLongOrNull() ?: 0L
    val shipVal = shippingCost.toLongOrNull() ?: 0L
    val grandTotal = (subtotal - discVal + shipVal).coerceAtLeast(0L)

    val saveNotaAction = {
        if (customerName.isBlank()) {
            Toast.makeText(context, "Nama pelanggan harus diisi", Toast.LENGTH_SHORT).show()
        } else if (items.any { it.name.isBlank() || it.price.isBlank() }) {
            Toast.makeText(context, "Nama barang dan harga wajib diisi", Toast.LENGTH_SHORT).show()
        } else {
            val notaId = notaToEdit?.id ?: "NOTA_${System.currentTimeMillis()}"
            val finalRefCode = referenceCode.ifBlank { viewModel.generateNextInvoiceNumber(selectedDate) }
            val newNota = Nota(
                id = notaId,
                referenceCode = finalRefCode,
                customerName = customerName,
                phoneNumber = phoneNumber,
                address = address,
                date = selectedDate,
                total = "Rp ${formatCurrency(grandTotal)}",
                status = status,
                subtotal = subtotal,
                discount = discVal,
                shippingCost = shipVal,
                paymentMethod = paymentMethod,
                notes = notes,
                items = items
            )
            viewModel.saveNota(newNota)
            Toast.makeText(context, if (notaToEdit != null) "Nota berhasil diperbarui!" else "Nota berhasil dibuat & disimpan!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    Scaffold(
        containerColor = ProBackground,
        topBar = {
            TopAppBar(
                title = { Text(if (notaToEdit != null) "Edit Nota #${notaToEdit.referenceCode}" else "Buat Nota Baru", style = NotaTheme.typography.heading, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showLiveReceiptPreview = true },
                        shape = RoundedCornerShape(10.dp),
                        color = AccentBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Preview Struk", tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                            Text("Preview Struk", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp), color = Color(0xFFA5B4FC))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
            )
        },
        bottomBar = {
            // Dynamic Floating Bottom Bar: Live Total Preview & Save button always visible above keyboard!
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = ProCardSurface,
                border = BorderStroke(1.dp, ProBorder),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
                            Text(
                                "TOTAL BAYAR (${items.size} Item)",
                                style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = TextGray
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Rp ${formatCurrency(grandTotal)}",
                            style = NotaTheme.typography.title.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                            color = AccentEmerald
                        )
                    }

                    Button(
                        onClick = saveNotaAction,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Nota", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. TANGGAL TRANSAKSI / NOTA (PICKUP TANGGAL) & NOMOR NOTA
            DateSection(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                referenceCode = referenceCode,
                onReferenceCodeChange = { referenceCode = it }
            )

            BuyerInfoSection(
                name = customerName,
                onNameChange = { customerName = it },
                phone = phoneNumber,
                onPhoneChange = { phoneNumber = it },
                address = address,
                onAddressChange = { address = it },
                customerSuggestions = customerSuggestions,
                onSuggestionSelected = { sugg ->
                    customerName = sugg.name
                    phoneNumber = sugg.phoneNumber
                    address = sugg.address
                },
                onPickContact = {
                    try {
                        val pickIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                        contactPickerLauncher.launch(pickIntent)
                    } catch (e: Exception) {
                        try {
                            val fallbackIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                            contactPickerLauncher.launch(fallbackIntent)
                        } catch (e2: Exception) {
                            Toast.makeText(context, "Aplikasi kontak tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            ItemsSection(
                items = items,
                onItemChanged = { index, updated ->
                    items = items.toMutableList().also { it[index] = updated }
                },
                onAddItem = {
                    items = items + NotaItemInput(name = "", quantity = "1", unit = "Pack", price = "")
                },
                onDeleteItem = { index ->
                    if (items.size > 1) {
                        items = items.toMutableList().also { it.removeAt(index) }
                    }
                },
                productSuggestions = productSuggestions,
                savedUnitsList = savedUnitsList,
                deletedUnits = deletedUnits,
                onProductSelected = { index, sugg ->
                    items = items.toMutableList().also {
                        it[index] = it[index].copy(
                            name = sugg.name,
                            unit = sugg.unit.ifEmpty { "Pack" },
                            price = sugg.price
                        )
                    }
                    // Auto-restore if unit was deleted
                    if (deletedUnits.contains(sugg.unit.ifEmpty { "Pack" })) {
                        viewModel.restoreUnit(sugg.unit.ifEmpty { "Pack" })
                    }
                },
                onDeleteUnit = { viewModel.deleteUnit(it) },
                onRestoreUnit = { viewModel.restoreUnit(it) }
            )

            // Payment & Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                border = BorderStroke(1.dp, ProBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("STATUS & PEMBAYARAN", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = TextMuted)
                    
                    // Status selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Status Nota", style = NotaTheme.typography.label, color = TextGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Lunas", "Tagihan").forEach { st ->
                                val isSelected = status == st
                                val activeColor = if (st == "Lunas") AccentEmerald else Color(0xFFE11D48)
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { status = st },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) activeColor else ProCardSurfaceLight,
                                    border = BorderStroke(1.dp, if (isSelected) activeColor else ProBorder)
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                        Text(st, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = if (isSelected) Color.White else TextGray)
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Metode Pembayaran", style = NotaTheme.typography.label, color = TextGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Pair("Tunai", Icons.Default.Payments),
                                Pair("Transfer", Icons.Default.CreditCard),
                                Pair("QRIS", Icons.Default.QrCode),
                                Pair("E-Wallet", Icons.Default.Wallet)
                            ).forEach { (pm, icon) ->
                                val isSelected = paymentMethod == pm
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { paymentMethod = pm },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else TextGray, modifier = Modifier.size(16.dp))
                                        Text(pm, style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = if (isSelected) Color.White else TextGray)
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NotaTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            placeholder = "Diskon (Rp)",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        NotaTextField(
                            value = shippingCost,
                            onValueChange = { shippingCost = it },
                            placeholder = "Ongkir (Rp)",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    NotaTextField(value = notes, onValueChange = { notes = it }, placeholder = "Catatan tambahan untuk pelanggan")
                }
            }

            // Summary Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                border = BorderStroke(1.dp, ProBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = NotaTheme.typography.body, color = TextGray)
                        Text("Rp ${formatCurrency(subtotal)}", style = NotaTheme.typography.body, color = Color.White)
                    }
                    if (discVal > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", style = NotaTheme.typography.body, color = TextGray)
                            Text("- Rp ${formatCurrency(discVal)}", style = NotaTheme.typography.body, color = Color.White)
                        }
                    }
                    if (shipVal > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ongkos Kirim", style = NotaTheme.typography.body, color = TextGray)
                            Text("+ Rp ${formatCurrency(shipVal)}", style = NotaTheme.typography.body, color = Color.White)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = ProBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Tagihan", style = NotaTheme.typography.heading, color = Color.White)
                        Text("Rp ${formatCurrency(grandTotal)}", style = NotaTheme.typography.title.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold), color = AccentEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    if (showLiveReceiptPreview) {
        val liveNota = Nota(
            id = notaToEdit?.id ?: "NOTA_${System.currentTimeMillis()}",
            referenceCode = referenceCode.ifBlank { "INV-PREVIEW" },
            customerName = customerName.ifBlank { "Pelanggan Umum" },
            phoneNumber = phoneNumber,
            address = address,
            date = selectedDate,
            total = "Rp ${formatCurrency(grandTotal)}",
            status = status,
            subtotal = subtotal,
            discount = discVal,
            shippingCost = shipVal,
            paymentMethod = paymentMethod,
            notes = notes,
            items = items.filter { it.name.isNotBlank() }.ifEmpty { items }
        )

        Dialog(
            onDismissRequest = { showLiveReceiptPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                containerColor = ProBackground,
                topBar = {
                    TopAppBar(
                        title = { Text("Preview Struk Real-Time", style = NotaTheme.typography.heading, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { showLiveReceiptPreview = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    showLiveReceiptPreview = false
                                    saveNotaAction()
                                }
                            ) {
                                Text("Simpan", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 15.sp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Tampilan struk otomatis terupdate saat Anda mengetik produk & harga.",
                        style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                        color = TextGray
                    )

                    ThermalReceiptView(
                        nota = liveNota,
                        businessProfile = businessProfile
                    )

                    Button(
                        onClick = { showLiveReceiptPreview = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ProCardSurfaceLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Kembali ke Formulir", style = NotaTheme.typography.body.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
