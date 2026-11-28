package com.eco.nota.ui.screens

import com.eco.nota.ui.theme.ambientGlow
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eco.nota.models.Customer
import com.eco.nota.models.NotaViewModel
import com.eco.nota.ui.components.FloatingWindowOverlay
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.AccentRose
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBackground
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMasterScreen(
    visible: Boolean = true,
    viewModel: NotaViewModel,
    onDismiss: () -> Unit,
    onSelectCustomer: ((Customer) -> Unit)? = null
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    BackHandler(enabled = visible && !showAddDialog && customerToEdit == null && customerToDelete == null) {
        onDismiss()
    }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter { c ->
            searchQuery.isBlank() ||
                    c.name.contains(searchQuery, ignoreCase = true) ||
                    c.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                    c.address.contains(searchQuery, ignoreCase = true)
        }
    }

    FloatingWindowOverlay(
        visible = visible,
        onDismiss = onDismiss,
        maxWidth = 580.dp,
        heightFraction = 0.90f,
        fillMaxHeight = true,
        windowTestTag = "customer_master_floating_window"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Floating Window Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ProCardSurface.copy(alpha = 0.78f))
                        .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentBlue.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF7DE3FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (onSelectCustomer != null) "Pilih Pelanggan" else "Database Pelanggan",
                                style = NotaTheme.typography.heading.copy(fontSize = 16.5.sp, fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${customers.size} pelanggan tersimpan",
                                style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("customer_master_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup Database Pelanggan",
                            tint = TextGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                HorizontalDivider(color = ProBorder.copy(alpha = 0.7f))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = ProCardSurface,
                border = BorderStroke(1.dp, if (searchQuery.isNotEmpty()) AccentBlue.copy(alpha = 0.8f) else ProBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = if (searchQuery.isNotEmpty()) AccentBlue else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Cari nama, nomor HP, alamat...",
                                style = NotaTheme.typography.body.copy(fontSize = 13.5.sp),
                                color = TextMuted
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = NotaTheme.typography.body.copy(color = Color.White, fontSize = 13.5.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                        border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF7DE3FF),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (searchQuery.isNotEmpty()) "Pelanggan Tidak Ditemukan" else "Belum Ada Database Pelanggan",
                                style = NotaTheme.typography.heading.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Tidak ada pelanggan dengan kata kunci '$searchQuery'."
                                else
                                    "Simpan daftar pelanggan beserta nomor telepon dan alamat untuk mempercepat pembuatan nota dan manajemen piutang.",
                                style = NotaTheme.typography.body.copy(fontSize = 13.sp),
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )

                            if (searchQuery.isEmpty()) {
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Pelanggan Baru", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            onCardClick = {
                                if (onSelectCustomer != null) {
                                    onSelectCustomer(customer)
                                } else {
                                    customerToEdit = customer
                                }
                            },
                            onEditClick = { customerToEdit = customer },
                            onDeleteClick = { customerToDelete = customer }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_customer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Pelanggan")
                    Text("Tambah Pelanggan", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                }
            }
        }
    }

    if (visible && (showAddDialog || customerToEdit != null)) {
        CustomerFormDialog(
            initialCustomer = customerToEdit,
            onDismiss = {
                showAddDialog = false
                customerToEdit = null
            },
            onSave = { savedCustomer ->
                val isEditing = customerToEdit != null
                viewModel.saveCustomer(savedCustomer) { success, message ->
                    Toast.makeText(
                        context,
                        if (success) (if (isEditing) "Pelanggan berhasil diperbarui" else "Pelanggan berhasil ditambahkan") else message,
                        Toast.LENGTH_SHORT
                    ).show()
                    if (success) {
                        showAddDialog = false
                        customerToEdit = null
                    }
                }
            }
        )
    }

    if (visible && customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = {
                Text(
                    "Hapus Pelanggan",
                    style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus '${customerToDelete?.name}' dari database pelanggan?",
                    style = NotaTheme.typography.body.copy(fontSize = 14.sp),
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                    customerToDelete = null
                    Toast.makeText(context, "Pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Hapus", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Batal", color = Color(0xFF7DE3FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ProCardSurface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        )
    }
}

@Composable
private fun CustomerItemCard(
    customer: Customer,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
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
                    shape = CircleShape,
                    color = AccentBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF7DE3FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = customer.name,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                        color = Color.White
                    )

                    if (customer.phoneNumber.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(
                                text = customer.phoneNumber,
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                color = TextGray
                            )
                        }
                    }

                    if (customer.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Text(
                                text = customer.address,
                                style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                                color = TextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF7DE3FF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = AccentRose,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    initialCustomer: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(initialCustomer?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }

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
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            if (nameIdx != -1) pickedName = cursor.getString(nameIdx) ?: ""
                            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIdx != -1) pickedPhone = cursor.getString(numIdx) ?: ""
                        }
                    }
                    if (pickedName.isNotEmpty()) name = pickedName
                    if (pickedPhone.isNotEmpty()) phoneNumber = pickedPhone.replace(Regex("[^0-9+]"), "")
                    Toast.makeText(context, "Kontak '$pickedName' berhasil dipilih!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal mengambil kontak: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = ProCardSurface,
            border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (initialCustomer != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (initialCustomer != null) "Edit Pelanggan" else "Tambah Pelanggan Baru",
                            style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = ProBorder.copy(alpha = 0.7f))

                // Tombol Ambil dari Kontak Telepon
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                            contactPickerLauncher.launch(intent)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF7DE3FF), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil dari Kontak Telepon", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = Color(0xFF7DE3FF))
                    }
                }

                // Nama Pelanggan
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("NAMA PELANGGAN / TOKO *", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Contoh: Toko Berkah Jaya", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ProBorder,
                            focusedContainerColor = ProCardSurfaceLight,
                            unfocusedContainerColor = ProCardSurfaceLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Nomor Telepon
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("NOMOR TELEPON / WHATSAPP", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = { Text("Contoh: 081234567890", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ProBorder,
                            focusedContainerColor = ProCardSurfaceLight,
                            unfocusedContainerColor = ProCardSurfaceLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Alamat
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ALAMAT", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = { Text("Contoh: Jl. Raya Solo No. 12", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ProBorder,
                            focusedContainerColor = ProCardSurfaceLight,
                            unfocusedContainerColor = ProCardSurfaceLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Batal", color = TextGray, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) return@Button
                            val finalCustomer = Customer(
                                id = initialCustomer?.id ?: "",
                                name = name.trim(),
                                phoneNumber = phoneNumber.trim(),
                                address = address.trim(),
                                label = initialCustomer?.label ?: "",
                                createdAt = initialCustomer?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(finalCustomer)
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Simpan", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
