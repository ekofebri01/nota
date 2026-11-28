package com.eco.nota.ui.screens

import com.eco.nota.ui.theme.ambientGlow
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eco.nota.models.NotaViewModel
import com.eco.nota.models.Product
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
import java.text.NumberFormat
import java.util.Locale

private fun formatRupiah(amount: Long): String {
    return try {
        NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    } catch (e: Exception) {
        amount.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMasterScreen(
    viewModel: NotaViewModel,
    onDismiss: () -> Unit,
    onSelectProduct: ((Product) -> Unit)? = null
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val notas by viewModel.notas.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    val categories = remember(products) {
        listOf("Semua") + products.map { it.category.trim() }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true) ||
                    p.code.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "Semua" ||
                    p.category.equals(selectedCategoryFilter, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        modifier = Modifier.ambientGlow(),
        containerColor = ProBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (onSelectProduct != null) "Pilih Produk" else "Database Produk & Barang",
                            style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${products.size} produk tersimpan",
                            style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    if (notas.isNotEmpty() && onSelectProduct == null) {
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable(enabled = !isImporting) {
                                    isImporting = true
                                    viewModel.importProductsFromNotas { importedCount ->
                                        isImporting = false
                                        if (importedCount > 0) {
                                            Toast.makeText(context, "Berhasil mengimpor $importedCount barang dari riwayat nota!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Semua barang dari nota sudah ada di database produk.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = AccentBlue.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isImporting) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF7DE3FF))
                                } else {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF7DE3FF), modifier = Modifier.size(15.dp))
                                }
                                Text("Impor Nota", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp), color = Color(0xFF7DE3FF))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
                    Text("Tambah Produk", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
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
                                "Cari nama barang, kategori, kode...",
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

            // Categories Filter (if categories exist)
            if (categories.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            modifier = Modifier.clickable { selectedCategoryFilter = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AccentBlue else ProCardSurface,
                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                        ) {
                            Text(
                                text = cat,
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSelected) Color.White else TextGray,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
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
                                        Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Color(0xFF7DE3FF),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (searchQuery.isNotEmpty()) "Barang Tidak Ditemukan" else "Belum Ada Master Produk",
                                style = NotaTheme.typography.heading.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Tidak ada barang dengan kata kunci '$searchQuery'."
                                else
                                    "Simpan daftar barang dan harga standar di sini agar pembuatan nota jadi super cepat tanpa perlu ketik ulang!",
                                style = NotaTheme.typography.body.copy(fontSize = 13.sp),
                                color = TextGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (searchQuery.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(42.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tambah Produk Baru", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp))
                                    }

                                    if (notas.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = {
                                                isImporting = true
                                                viewModel.importProductsFromNotas { importedCount ->
                                                    isImporting = false
                                                    Toast.makeText(context, "Berhasil mengimpor $importedCount barang dari riwayat nota!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f)),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF7DE3FF), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Impor dari Riwayat Nota", color = Color(0xFF7DE3FF), style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
                                        }
                                    }
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
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onCardClick = {
                                if (onSelectProduct != null) {
                                    onSelectProduct(product)
                                } else {
                                    productToEdit = product
                                }
                            },
                            onEditClick = { productToEdit = product },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        ProductFormDialog(
            initialProduct = productToEdit,
            existingCategories = categories.drop(1), // buang "Semua"
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { updatedProduct ->
                viewModel.saveProduct(updatedProduct)
                Toast.makeText(
                    context,
                    if (productToEdit != null) "Produk berhasil diperbarui" else "Produk berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()
                showAddDialog = false
                productToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(
                    "Hapus Produk",
                    style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus '${productToDelete?.name}' dari database produk?",
                    style = NotaTheme.typography.body.copy(fontSize = 14.sp),
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { viewModel.deleteProduct(it.id) }
                    productToDelete = null
                    Toast.makeText(context, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Hapus", color = AccentRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
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
private fun ProductItemCard(
    product: Product,
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
                    shape = RoundedCornerShape(10.dp),
                    color = AccentBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Sell,
                            contentDescription = null,
                            tint = Color(0xFF7DE3FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = product.name,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rp ${formatRupiah(product.price)}",
                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = AccentEmerald
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = product.unit.ifBlank { "Pcs" },
                                style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                color = TextGray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (product.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = product.category,
                                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF7DE3FF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
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
fun ProductFormDialog(
    initialProduct: Product? = null,
    existingCategories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceText by remember { mutableStateOf(if (initialProduct != null && initialProduct.price > 0) initialProduct.price.toString() else "") }
    var unit by remember { mutableStateOf(initialProduct?.unit?.ifBlank { "Pcs" } ?: "Pcs") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "") }
    var categoryFocused by remember { mutableStateOf(false) }

    // Saran kategori yang sudah tersimpan, difilter sesuai teks yang sedang diketik
    val categorySuggestions = remember(category, existingCategories) {
        val query = category.trim()
        existingCategories
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
            .sortedBy { it.lowercase() }
    }
    var code by remember { mutableStateOf(initialProduct?.code ?: "") }

    val quickUnits = listOf("Pcs", "Pack", "Slop", "Bungkus", "Kg", "Box", "Botol", "Lusin", "Gram", "Unit", "Meter", "Porsi")

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
                    .verticalScroll(rememberScrollState())
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
                            imageVector = if (initialProduct != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (initialProduct != null) "Edit Produk" else "Tambah Produk Baru",
                            style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = ProBorder.copy(alpha = 0.7f))

                // 1. Nama Barang
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("NAMA PRODUK / BARANG *", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Contoh: Rokok Sampoerna Mild", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
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

                // 2. Harga Jual
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("HARGA JUAL (RP) *", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            priceText = clean
                        },
                        placeholder = { Text("0", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Text("Rp", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = TextGray, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // 3. Satuan Barang & Quick Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SATUAN BARANG", style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        placeholder = { Text("Pcs / Pack / Kg", color = TextMuted, fontSize = 13.sp) },
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        quickUnits.forEach { u ->
                            val isSelected = unit.equals(u, ignoreCase = true)
                            Surface(
                                modifier = Modifier.clickable { unit = u },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AccentBlue.copy(alpha = 0.25f) else ProCardSurfaceLight,
                                border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                            ) {
                                Text(
                                    text = u,
                                    style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) Color(0xFF7DE3FF) else TextGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Kategori & Kode/SKU (Opsional)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("KATEGORI (OPSIONAL)", style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            placeholder = { Text("Contoh: Rokok / Minuman", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { categoryFocused = it.isFocused },
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

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("KODE / SKU (OPSIONAL)", style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = TextMuted)
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            placeholder = { Text("Contoh: ROK-01", color = TextMuted, fontSize = 12.sp) },
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
                }

                // Preview kategori yang sudah tersimpan (muncul saat kolom Kategori diketik/difokus)
                if (categoryFocused && categorySuggestions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "KATEGORI TERSIMPAN",
                            style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            categorySuggestions.forEach { c ->
                                val isSelected = category.trim().equals(c, ignoreCase = true)
                                Surface(
                                    modifier = Modifier.clickable { category = c },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.25f) else ProCardSurfaceLight,
                                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                                ) {
                                    Text(
                                        text = c,
                                        style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSelected) Color(0xFF7DE3FF) else TextGray,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Batal & Simpan
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
                            val priceLong = priceText.toLongOrNull() ?: 0L
                            val finalProduct = Product(
                                id = initialProduct?.id ?: "",
                                name = name.trim(),
                                price = priceLong,
                                unit = unit.trim().ifBlank { "Pcs" },
                                category = category.trim(),
                                code = code.trim(),
                                createdAt = initialProduct?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(finalProduct)
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
