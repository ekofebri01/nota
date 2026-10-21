package com.eco.nota.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.eco.nota.models.BusinessProfile
import com.eco.nota.models.Nota
import com.eco.nota.models.NotaViewModel
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.AccentRose
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBackground
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import com.eco.nota.utils.ImageUtils
import com.eco.nota.utils.ReceiptBitmapGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailNotaScreen(nota: Nota, viewModel: NotaViewModel, onDismiss: () -> Unit, onEditClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val businessProfile by viewModel.businessProfile.collectAsState()

    var isThermalMode by remember { mutableStateOf(true) }
    var showStatusDialog by remember { mutableStateOf(false) }
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = {
                Text(
                    "Ubah Status Nota #${nota.referenceCode}",
                    style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Pilih status pembayaran untuk nota ini:",
                        style = NotaTheme.typography.body.copy(fontSize = 13.5.sp),
                        color = TextGray
                    )

                    // Pilihan 1: Lunas
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateNotaStatus(nota.id, "Lunas")
                                showStatusDialog = false
                                Toast.makeText(context, "✅ Nota #${nota.referenceCode} berhasil diubah jadi LUNAS!", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLunas) AccentEmerald.copy(alpha = 0.2f) else ProBackground,
                        border = BorderStroke(1.5.dp, if (isLunas) AccentEmerald else ProBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                            Column {
                                Text("LUNAS (Sudah Dibayar)", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Tagihan telah diterima dan lunas", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                            }
                        }
                    }

                    // Pilihan 2: Tagihan
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateNotaStatus(nota.id, "Tagihan")
                                showStatusDialog = false
                                Toast.makeText(context, "Nota #${nota.referenceCode} diubah jadi TAGIHAN", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isLunas) Color(0xFFEF4444).copy(alpha = 0.15f) else ProBackground,
                        border = BorderStroke(1.5.dp, if (!isLunas) Color(0xFFEF4444) else ProBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Column {
                                Text("TAGIHAN (Belum Lunas)", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Menunggu pembayaran pelanggan", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Tutup", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color(0xFFA5B4FC))
                }
            },
            containerColor = ProCardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = ProBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Nota #${nota.referenceCode}", style = NotaTheme.typography.heading, color = Color.White)
                            Text(nota.date, style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                    },
                    actions = {
                        if (!isLunas) {
                            IconButton(onClick = {
                                viewModel.updateNotaStatus(nota.id, "Lunas")
                                Toast.makeText(context, "✅ Nota #${nota.referenceCode} berhasil diubah jadi LUNAS!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Ubah Jadi Lunas", tint = AccentEmerald)
                            }
                        }
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Nota", tint = Color.White)
                        }
                        IconButton(onClick = {
                            viewModel.deleteNota(nota.id)
                            Toast.makeText(context, "Nota #${nota.referenceCode} berhasil dihapus", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Nota", tint = AccentRose)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
                )
            },
            bottomBar = {
                Surface(
                    color = ProCardSurface,
                    border = BorderStroke(1.dp, ProBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val bitmap = ReceiptBitmapGenerator.generateReceiptBitmap(
                                            context = context,
                                            nota = nota,
                                            businessProfile = businessProfile,
                                            isThermal = isThermalMode
                                        )
                                        ImageUtils.shareBitmap(context, bitmap, nota.referenceCode)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Nota", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val bitmap = ReceiptBitmapGenerator.generateReceiptBitmap(
                                            context = context,
                                            nota = nota,
                                            businessProfile = businessProfile,
                                            isThermal = isThermalMode
                                        )
                                        ImageUtils.saveBitmapToGallery(context, bitmap, nota.referenceCode)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Gambar", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Mode Selector (Struk Kasir vs Dokumen Digital Pro)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ProCardSurface,
                    border = BorderStroke(1.dp, ProBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isThermalMode = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isThermalMode) AccentBlue else Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Mode Struk Kasir",
                                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                                    color = if (isThermalMode) Color.White else TextGray
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isThermalMode = false },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isThermalMode) AccentBlue else Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Mode Digital Pro",
                                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                                    color = if (!isThermalMode) Color.White else TextGray
                                )
                            }
                        }
                    }
                }

                // STATUS ACTION BANNER (FUNGSI UBAH JADI LUNAS)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLunas) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFF1E293B)
                    ),
                    border = BorderStroke(1.dp, if (isLunas) AccentEmerald.copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isLunas) AccentEmerald.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isLunas) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                        contentDescription = null,
                                        tint = if (isLunas) AccentEmerald else Color(0xFFF87171),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isLunas) "STATUS: SUDAH LUNAS" else "STATUS: TAGIHAN (BELUM LUNAS)",
                                    style = NotaTheme.typography.label.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (isLunas) AccentEmerald else Color(0xFFF87171)
                                )
                                Text(
                                    text = if (isLunas) "Pembayaran telah selesai" else "Total tagihan: ${nota.total}",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = if (isLunas) Color(0xFFA7F3D0) else Color.White
                                )
                            }
                        }

                        if (!isLunas) {
                            Button(
                                onClick = {
                                    viewModel.updateNotaStatus(nota.id, "Lunas")
                                    Toast.makeText(context, "✅ Nota #${nota.referenceCode} berhasil diubah jadi LUNAS!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Ubah Jadi Lunas",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                    color = Color.White
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.clickable { showStatusDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    "Ubah Status",
                                    style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = TextGray,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // RECEIPT PREVIEW CONTAINER
                if (isThermalMode) {
                    ThermalReceiptView(
                        nota = nota,
                        businessProfile = businessProfile,
                        onStatusClick = { showStatusDialog = true }
                    )
                } else {
                    ExecutiveDarkReceiptView(
                        nota = nota,
                        businessProfile = businessProfile,
                        onStatusClick = { showStatusDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ThermalReceiptView(
    nota: Nota,
    businessProfile: BusinessProfile,
    onStatusClick: () -> Unit = {}
) {
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)
    val stempelColor = if (isLunas) Color(0xFFD97706) else Color(0xFFDC2626)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReceiptZigZagEdge(isTop = true, color = Color(0xFFFFFFFF))
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Logo
            if (businessProfile.logoUri != null) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    AsyncImage(
                        model = businessProfile.logoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 2. Store Header
            Text(
                text = businessProfile.name.ifEmpty { "TOKO KASIR" },
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A)),
                textAlign = TextAlign.Center
            )
            if (businessProfile.address.isNotEmpty()) {
                Text(
                    text = businessProfile.address,
                    style = TextStyle(fontSize = 11.sp, color = Color(0xFF64748B)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (businessProfile.notes.isNotEmpty()) {
                Text(
                    text = businessProfile.notes,
                    style = TextStyle(fontSize = 11.sp, color = Color(0xFF64748B)),
                    textAlign = TextAlign.Center
                )
            }

            // 3. POSISI METODE PEMBAYARAN DINAMIS LANGSUNG DI BAWAH NAMA TOKO
            Spacer(modifier = Modifier.height(10.dp))
            DynamicPaymentSection(nota = nota, businessProfile = businessProfile, isDark = false)

            Spacer(modifier = Modifier.height(10.dp))
            ReceiptDashedDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Meta (Precise 2-Column Grid)
            val displayMetode = when (nota.paymentMethod.lowercase()) {
                "transfer" -> "TRANSFER BANK"
                "qris" -> "QRIS DIGITAL"
                "e-wallet" -> "E-WALLET"
                else -> "TUNAI (CASH)"
            }
            val statusDisplay = if (isLunas) "LUNAS" else "TAGIHAN"
            val statusColor = if (isLunas) Color(0xFF059669) else Color(0xFFDC2626)

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val refPrefix = if (nota.referenceCode.startsWith("INV", ignoreCase = true) || nota.referenceCode.startsWith("#")) "" else "#"
                    Text("No. Nota: $refPrefix${nota.referenceCode}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                    Text("Metode: $displayMetode", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tanggal: ${nota.date}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF475569)))
                    Surface(
                        modifier = Modifier.clickable { onStatusClick() },
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("STATUS: $statusDisplay", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusColor))
                            Icon(Icons.Default.Edit, contentDescription = "Ubah Status", tint = statusColor, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                if (nota.customerName.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pelanggan: ${nota.customerName}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                        if (nota.phoneNumber.isNotEmpty()) {
                            Text(nota.phoneNumber, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = Color(0xFF475569)))
                        }
                    }
                }
                if (nota.address.isNotEmpty()) {
                    Text("Alamat: ${nota.address}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, color = Color(0xFF475569)))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ReceiptDashedDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // 5. Table Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("ITEM", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B)), modifier = Modifier.weight(1f))
                Text("TOTAL", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B)), textAlign = TextAlign.End, modifier = Modifier.width(90.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            ReceiptDashedDivider()

            // 6. Items
            nota.items.forEach { item ->
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)), modifier = Modifier.weight(1f))
                        val itemTotal = (item.quantity.toLongOrNull() ?: 1L) * (item.price.toLongOrNull() ?: 0L)
                        Text("Rp ${formatCurrency(itemTotal)}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)), textAlign = TextAlign.End, modifier = Modifier.width(90.dp))
                    }
                    val unitStr = if (item.unit.isNotEmpty()) " ${item.unit}" else ""
                    Text("${item.quantity}$unitStr x Rp ${formatCurrency(item.price.toLongOrNull() ?: 0L)}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF64748B)))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ReceiptDashedDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // 7. Financial Totals with OVERLAY WATERMARK
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF64748B)))
                        Text("Rp ${formatCurrency(nota.subtotal)}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF0F172A)))
                    }
                    if (nota.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF64748B)))
                            Text("- Rp ${formatCurrency(nota.discount)}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF0F172A)))
                        }
                    }
                    if (nota.shippingCost > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Biaya Pengiriman / Ongkir", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF64748B)))
                            Text("+ Rp ${formatCurrency(nota.shippingCost)}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF0F172A)))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL BAYAR", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                            Text(nota.total, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8)))
                        }
                    }
                }

                if (isLunas) {
                    WatermarkOverlay(
                        text = "LUNAS ${nota.date.uppercase()}",
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            // 8. Authentic Circular Badge Stamp (ONLY when !isLunas)
            if (!isLunas) {
                Spacer(modifier = Modifier.height(10.dp))
                ReceiptDashedDivider()
                Spacer(modifier = Modifier.height(12.dp))
                CircularStampView(status = nota.status, date = nota.date, onClick = onStatusClick)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.clickable { onStatusClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDC2626).copy(alpha = 0.08f)
                ) {
                    Text(
                        "👉 Ketuk stempel untuk ubah jadi LUNAS",
                        style = TextStyle(fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (nota.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Catatan: ${nota.notes}", style = TextStyle(fontSize = 11.sp, color = Color(0xFF64748B)), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(10.dp))
            ReceiptDashedDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "- Terima kasih atas kunjungan & kerjasamanya -",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.5.sp, color = Color(0xFF64748B)),
                textAlign = TextAlign.Center
            )
            Text(
                "Nota Kasir Pro Edition",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFF94A3B8)),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            ReceiptZigZagEdge(isTop = false, color = Color(0xFFFFFFFF))
        }
    }
}

@Composable
fun ExecutiveDarkReceiptView(
    nota: Nota,
    businessProfile: BusinessProfile,
    onStatusClick: () -> Unit = {}
) {
    val isLunas = nota.status.equals("Lunas", ignoreCase = true)
    val statusColor = if (isLunas) AccentEmerald else Color(0xFFF59E0B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.dp, ProBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Logo
            if (businessProfile.logoUri != null) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp),
                    border = BorderStroke(2.dp, AccentBlue)
                ) {
                    AsyncImage(
                        model = businessProfile.logoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. Store Info
            Text(
                text = businessProfile.name.ifEmpty { "NOTA KASIR PRO" },
                style = NotaTheme.typography.heading.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (businessProfile.address.isNotEmpty()) {
                Text(
                    text = businessProfile.address,
                    style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }

            // 3. POSISI METODE PEMBAYARAN DINAMIS LANGSUNG DI BAWAH NAMA TOKO
            Spacer(modifier = Modifier.height(12.dp))
            DynamicPaymentSection(nota = nota, businessProfile = businessProfile, isDark = true)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ProBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // 4. Meta Info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("NO. NOTA", style = NotaTheme.typography.label.copy(fontSize = 10.sp), color = TextMuted)
                    Text("#${nota.referenceCode}", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TANGGAL", style = NotaTheme.typography.label.copy(fontSize = 10.sp), color = TextMuted)
                    Text(nota.date, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PELANGGAN", style = NotaTheme.typography.label.copy(fontSize = 10.sp), color = TextMuted)
                    Text(nota.customerName.ifEmpty { "Umum" }, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", style = NotaTheme.typography.label.copy(fontSize = 10.sp), color = TextMuted)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onStatusClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                nota.status,
                                style = NotaTheme.typography.label.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                            Icon(Icons.Default.Edit, contentDescription = "Ubah Status", tint = statusColor, modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ProBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // 5. Items List
            nota.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("${item.quantity} ${item.unit} @ Rp ${formatCurrency(item.price.toLongOrNull() ?: 0L)}", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
                    }
                    val itemTotal = (item.quantity.toLongOrNull() ?: 1L) * (item.price.toLongOrNull() ?: 0L)
                    Text("Rp ${formatCurrency(itemTotal)}", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ProBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // 6. Totals with OVERLAY WATERMARK
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = NotaTheme.typography.body, color = TextGray)
                        Text("Rp ${formatCurrency(nota.subtotal)}", style = NotaTheme.typography.body, color = Color.White)
                    }
                    if (nota.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diskon", style = NotaTheme.typography.body, color = TextGray)
                            Text("- Rp ${formatCurrency(nota.discount)}", style = NotaTheme.typography.body, color = Color.White)
                        }
                    }
                    if (nota.shippingCost > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ongkos Kirim", style = NotaTheme.typography.body, color = TextGray)
                            Text("+ Rp ${formatCurrency(nota.shippingCost)}", style = NotaTheme.typography.body, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL BAYAR", style = NotaTheme.typography.heading, color = Color.White)
                            Text(nota.total, style = NotaTheme.typography.title.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = Color(0xFF60A5FA))
                        }
                    }
                }

                if (isLunas) {
                    WatermarkOverlay(
                        text = "LUNAS ${nota.date.uppercase()}",
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            // 7. Authentic Circular Badge Stamp (ONLY when !isLunas)
            if (!isLunas) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ProBorder)
                Spacer(modifier = Modifier.height(14.dp))
                CircularStampView(status = nota.status, date = nota.date, onClick = onStatusClick)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.clickable { onStatusClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        "👉 Ketuk stempel untuk ubah jadi LUNAS",
                        style = TextStyle(fontSize = 10.sp, color = Color(0xFFFCD34D), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (nota.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Catatan: ${nota.notes}", style = NotaTheme.typography.label.copy(fontSize = 11.sp), color = TextGray)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ProBorder)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "- Terima kasih atas kunjungan & kerjasamanya -",
                style = NotaTheme.typography.label.copy(fontSize = 11.sp, color = TextGray),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Nota Kasir Pro Edition",
                style = NotaTheme.typography.label.copy(fontSize = 10.sp, color = TextMuted),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun DynamicPaymentSection(nota: Nota, businessProfile: BusinessProfile, isDark: Boolean = false) {
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDark) TextGray else Color(0xFF64748B)
    val cardBg = if (isDark) ProBackground.copy(alpha = 0.6f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) ProBorder else Color(0xFFE2E8F0)
    val primaryAccent = if (isDark) Color(0xFF818CF8) else Color(0xFF1E3A8A)

    when {
        nota.paymentMethod.equals("Transfer", ignoreCase = true) -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(16.dp))
                        Text(
                            "TRANSFER BANK: ${businessProfile.bankName.ifEmpty { "BANK TOKO" }}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryAccent)
                        )
                    }
                    if (businessProfile.bankAccount.isNotEmpty()) {
                        Text(
                            businessProfile.bankAccount,
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 1.sp)
                        )
                    } else {
                        Text(
                            "Nomor rekening belum diatur di Profil Toko",
                            style = TextStyle(fontSize = 11.sp, color = mutedColor)
                        )
                    }
                    if (businessProfile.bankHolder.isNotEmpty()) {
                        Text(
                            "a/n ${businessProfile.bankHolder}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = mutedColor)
                        )
                    }
                }
            }
        }
        nota.paymentMethod.equals("QRIS", ignoreCase = true) -> {
            val isLunas = nota.status.equals("Lunas", ignoreCase = true)
            if (isLunas) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Info Pembayaran QRIS: ${businessProfile.name.ifEmpty { "ECO TOBACCO" }.uppercase()} / NMID: ID1024308636892 / A01",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "GPN",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (businessProfile.qrisUri != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(2.5.dp, if (isDark) AccentBlue else Color(0xFF0F172A))
                            ) {
                                AsyncImage(
                                    model = businessProfile.qrisUri,
                                    contentDescription = "QRIS Pembayaran",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                            Text(
                                "SCAN QRIS DI ATAS UNTUK PEMBAYARAN",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF0F172A)),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(18.dp))
                                Text(
                                    "PEMBAYARAN QRIS DIGITAL",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryAccent)
                                )
                            }
                            Text(
                                "(Gambar QRIS belum diunggah di Pengaturan Profil Toko)",
                                style = TextStyle(fontSize = 11.sp, color = mutedColor),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        nota.paymentMethod.equals("E-Wallet", ignoreCase = true) -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Wallet, contentDescription = null, tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669), modifier = Modifier.size(16.dp))
                        Text(
                            "PEMBAYARAN E-WALLET",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF34D399) else Color(0xFF059669))
                        )
                    }
                    Text(
                        businessProfile.eWalletInfo.ifEmpty { "Info E-Wallet belum diatur di Profil Toko" },
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            // Tunai / Cash
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = mutedColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "METODE PEMBAYARAN : TUNAI (CASH)",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = mutedColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptZigZagEdge(
    modifier: Modifier = Modifier,
    isTop: Boolean = false,
    color: Color = Color(0xFFFFFFFF)
) {
    Canvas(modifier = modifier.fillMaxWidth().height(10.dp)) {
        val toothWidth = 14.dp.toPx()
        val toothHeight = 6.dp.toPx()
        val path = Path().apply {
            if (isTop) {
                moveTo(0f, toothHeight)
                var x = 0f
                while (x < size.width) {
                    lineTo(x + toothWidth / 2f, 0f)
                    lineTo(x + toothWidth, toothHeight)
                    x += toothWidth
                }
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                var x = size.width
                while (x > 0f) {
                    lineTo(x - toothWidth / 2f, size.height)
                    lineTo(x - toothWidth, 0f)
                    x -= toothWidth
                }
            }
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun ReceiptDashedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        val strokeWidth = 1.dp.toPx()
        val dashWidth = 6.dp.toPx()
        val dashGap = 4.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = Color(0xFFCBD5E1),
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset((x + dashWidth).coerceAtMost(size.width), 0f),
                strokeWidth = strokeWidth
            )
            x += dashWidth + dashGap
        }
    }
}

@Composable
fun SimulatedBarcode(code: String, modifier: Modifier = Modifier, isDark: Boolean = false) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.height(34.dp).fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barColor = if (isDark) Color.White else Color(0xFF0F172A)
            val pattern = listOf(3, 1, 2, 1, 4, 1, 2, 3, 1, 2, 4, 1, 2, 1, 3, 2, 1, 4, 2, 1, 3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 2, 1, 4, 1, 2, 3, 1, 4, 2, 1)
            pattern.forEachIndexed { index, weight ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weight.toFloat())
                        .background(if (index % 2 == 0) barColor else Color.Transparent)
                )
            }
        }
        Text(
            text = "* $code *",
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) TextGray else Color(0xFF0F172A)),
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun CircularStampView(
    status: String,
    date: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isLunas = status.equals("Lunas", ignoreCase = true)
    val stampColor = if (isLunas) Color(0xFFD97706) else Color(0xFFDC2626) // Amber gold / red
    val statusText = if (isLunas) "LUNAS" else "TAGIHAN"

    val clickableModifier = if (onClick != null) {
        Modifier.clip(CircleShape).clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            .size(112.dp)
            .rotate(-9f)
            .then(clickableModifier)
            .background(stampColor.copy(alpha = 0.05f), CircleShape)
            .border(3.2.dp, stampColor, CircleShape)
            .padding(5.dp)
            .border(1.2.dp, stampColor.copy(alpha = 0.75f), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "★ $statusText ★",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = stampColor,
                    letterSpacing = 0.5.sp
                )
            )
            HorizontalDivider(
                color = stampColor,
                thickness = 1.dp,
                modifier = Modifier.width(55.dp).padding(vertical = 2.dp)
            )
            Text(
                text = statusText,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = stampColor,
                    letterSpacing = 1.sp
                )
            )
            HorizontalDivider(
                color = stampColor,
                thickness = 1.dp,
                modifier = Modifier.width(55.dp).padding(vertical = 2.dp)
            )
            Text(
                text = date.uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = stampColor
                )
            )
        }
    }
}

@Composable
fun WatermarkOverlay(
    text: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            alpha = 32 // ~12% opacity: subtle, translucent faint gray
            textSize = 12.sp.toPx()
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            letterSpacing = 0.08f
        }

        val textWidth = textPaint.measureText(text)
        val stepX = textWidth + 24.dp.toPx()
        val stepY = 24.dp.toPx()

        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-18f, size.width / 2f, size.height / 2f)

        val startX = -size.width
        val endX = size.width * 2f
        val startY = -size.height
        val endY = size.height * 2f

        var curY = startY
        var row = 0
        while (curY <= endY) {
            val offsetX = if (row % 2 == 0) 0f else stepX / 2f
            var curX = startX + offsetX
            while (curX <= endX) {
                drawContext.canvas.nativeCanvas.drawText(text, curX, curY, textPaint)
                curX += stepX
            }
            curY += stepY
            row++
        }

        drawContext.canvas.nativeCanvas.restore()
    }
}
