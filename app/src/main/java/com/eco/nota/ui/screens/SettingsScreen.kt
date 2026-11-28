package com.eco.nota.ui.screens

import com.eco.nota.ui.theme.ambientGlow
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.eco.nota.models.BusinessProfile
import com.eco.nota.models.NotaViewModel
import com.eco.nota.models.WhatsNewEntry
import com.eco.nota.ui.components.NotaTextField
import com.eco.nota.ui.components.SettingsItem
import com.eco.nota.ui.components.TextButton
import com.eco.nota.ui.theme.*
import com.eco.nota.utils.BackupUtils
import com.eco.nota.utils.SafBackup
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    visible: Boolean = true,
    viewModel: NotaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val businessProfile by viewModel.businessProfile.collectAsState()
    var showBusinessProfile by remember { mutableStateOf(false) }
    var showProductMaster by remember { mutableStateOf(false) }
    var showCustomerMaster by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showBackupScreen by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (!visible) {
            showBusinessProfile = false
            showProductMaster = false
            showCustomerMaster = false
            showAboutDialog = false
            showBackupScreen = false
        }
    }

    BackHandler(enabled = visible && !showBusinessProfile && !showProductMaster && !showCustomerMaster && !showAboutDialog && !showBackupScreen) {
        onDismiss()
    }

    if (showBusinessProfile) {
        BusinessProfileScreen(viewModel = viewModel, onDismiss = { showBusinessProfile = false })
    }

    if (showAboutDialog) {
        AboutDialog(viewModel = viewModel, onDismiss = { showAboutDialog = false })
    }

    if (showBackupScreen) {
        BackupRestoreScreen(viewModel = viewModel, onDismiss = { showBackupScreen = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Backdrop scrim fade
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        // Animated floating window container
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 210)) +
                scaleIn(
                    initialScale = 0.91f,
                    animationSpec = spring(
                        dampingRatio = 0.74f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) +
                slideInVertically(
                    initialOffsetY = { it / 14 },
                    animationSpec = tween(durationMillis = 230)
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 160)) +
                scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(durationMillis = 160)
                ) +
                slideOutVertically(
                    targetOffsetY = { it / 18 },
                    animationSpec = tween(durationMillis = 160)
                ),
            modifier = Modifier.fillMaxSize()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                val maxWindowHeight = maxHeight * 0.88f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .heightIn(max = maxWindowHeight)
                        .testTag("settings_floating_window")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = ProBackground,
                    border = BorderStroke(1.dp, CardBorderGradient),
                    shadowElevation = 24.dp,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Floating Window Top Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ProCardSurface.copy(alpha = 0.72f))
                                .padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
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
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = Color(0xFF7DE3FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Pengaturan",
                                        style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Nota Kasir Pro v${viewModel.currentAppVersion}",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("settings_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup Pengaturan",
                                    tint = TextGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = ProBorder.copy(alpha = 0.7f))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 10.dp)
                        ) {
                // Store Profile Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { showBusinessProfile = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (businessProfile.logoUri != null) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp),
                                border = BorderStroke(1.5.dp, AccentBlue)
                            ) {
                                AsyncImage(
                                    model = businessProfile.logoUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp),
                                color = AccentBlue.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = Color(0xFF7DE3FF),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                businessProfile.name.ifEmpty { "Atur Profil Bisnis / Toko" },
                                style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                businessProfile.address.ifEmpty { "Ketuk untuk melengkapi logo, kontak, bank & QRIS" },
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                color = TextGray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section: Toko
                Text(
                    "BISNIS & TOKO",
                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Storefront,
                        title = "Profil Bisnis",
                        subtitle = "Logo, Nama, Alamat, Kontak, Rekening Bank & QRIS",
                        onClick = { showBusinessProfile = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.Inventory2,
                        title = "Database & Master Produk",
                        subtitle = "Kelola daftar barang, kategori, SKU & harga standar",
                        onClick = { showProductMaster = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.People,
                        title = "Database Kontak Pelanggan",
                        subtitle = "Kelola kontak pelanggan, nomor HP & alamat tersimpan",
                        onClick = { showCustomerMaster = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Tampilan Beranda
                val defaultDateFilter by viewModel.defaultDateFilter.collectAsState()
                val viewOptions = listOf("Harian", "Bulanan", "Tahunan", "All Time")

                Text(
                    "TAMPILAN BERANDA",
                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF7DE3FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Default Tampilan Riwayat",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                                    color = Color.White
                                )
                                Text(
                                    text = when (defaultDateFilter) {
                                        "Harian" -> "Saat app dibuka langsung tampilkan nota Hari Ini"
                                        "Bulanan" -> "Saat app dibuka langsung tampilkan nota Bulan Ini"
                                        "Tahunan" -> "Saat app dibuka langsung tampilkan nota Tahun Ini"
                                        else -> "Saat app dibuka tampilkan Semua Riwayat (All Time)"
                                    },
                                    style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                    color = TextGray
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewOptions.forEach { option ->
                                val isSelected = defaultDateFilter == option
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.setDefaultDateFilter(option)
                                            Toast.makeText(context, "Default tampilan diatur ke: $option", Toast.LENGTH_SHORT).show()
                                        }
                                        .testTag("default_view_${option.lowercase().replace(" ", "_")}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) AccentBlue else ProCardSurfaceLight,
                                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ProBorder)
                                ) {
                                    Text(
                                        text = option,
                                        style = NotaTheme.typography.body.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        ),
                                        color = if (isSelected) Color.White else TextGray,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Data & Cadangan
                Text(
                    "DATA & CADANGAN",
                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Kelola Data & Cadangan",
                        subtitle = "Auto-backup otomatis, ekspor file ZIP (lengkap gambar) & pulihkan data",
                        onClick = { showBackupScreen = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section: Perangkat & Informasi
                Text(
                    "PERANGKAT & APLIKASI",
                    style = NotaTheme.typography.label.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Bluetooth,
                        title = "Printer Thermal Bluetooth",
                        subtitle = "Hubungkan printer kasir 58mm / 80mm",
                        onClick = { Toast.makeText(viewModel.getApplication(), "Mencari printer thermal bluetooth...", Toast.LENGTH_SHORT).show() }
                    )
                    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
                    val updateInfo by viewModel.updateInfo.collectAsState()
                    SettingsItem(
                        icon = Icons.Default.SystemUpdate,
                        title = "Pembaruan Aplikasi (GitHub)",
                        subtitle = if (isCheckingUpdate) "Sedang memeriksa pembaruan..." else if (updateInfo?.hasUpdate == true) "Versi baru ${updateInfo?.latestVersion} tersedia! Ketuk untuk perbarui" else "Versi ${viewModel.currentAppVersion} • Otomatis cek saat app dibuka",
                        onClick = {
                            viewModel.checkForUpdates(manual = true) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Tentang & Catatan Pembaruan",
                        subtitle = "Versi ${viewModel.currentAppVersion} Pro • Lihat What's New & Info",
                        onClick = { showAboutDialog = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))

                // Version Footer at bottom of Settings screen
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Nota Kasir Pro Edition • Versi ${viewModel.currentAppVersion}",
                        style = NotaTheme.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = TextGray
                    )
                    Text(
                        "Sistem Kasir Cepat & Penyimpanan 100% Offline",
                        style = NotaTheme.typography.label.copy(fontSize = 10.5.sp),
                        color = TextMuted
                    )
                }

                        Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Database floating windows rendered above Settings floating window
        ProductMasterScreen(
            visible = showProductMaster,
            viewModel = viewModel,
            onDismiss = { showProductMaster = false }
        )

        CustomerMasterScreen(
            visible = showCustomerMaster,
            viewModel = viewModel,
            onDismiss = { showCustomerMaster = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(viewModel: NotaViewModel, onDismiss: () -> Unit) {
    val currentProfile by viewModel.businessProfile.collectAsState()
    
    var businessName by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var address by remember(currentProfile) { mutableStateOf(currentProfile.address) }
    var notes by remember(currentProfile) { mutableStateOf(currentProfile.notes) }
    var logoUri by remember(currentProfile) { mutableStateOf(currentProfile.logoUri) }
    
    var bankName by remember(currentProfile) { mutableStateOf(currentProfile.bankName) }
    var bankAccount by remember(currentProfile) { mutableStateOf(currentProfile.bankAccount) }
    var bankHolder by remember(currentProfile) { mutableStateOf(currentProfile.bankHolder) }
    var eWalletInfo by remember(currentProfile) { mutableStateOf(currentProfile.eWalletInfo) }
    var qrisUri by remember(currentProfile) { mutableStateOf(currentProfile.qrisUri) }

    val context = LocalContext.current
    val saveProfile = {
        viewModel.saveBusinessProfile(
            BusinessProfile(
                name = businessName,
                address = address,
                notes = notes,
                logoUri = logoUri,
                bankName = bankName,
                bankAccount = bankAccount,
                bankHolder = bankHolder,
                qrisUri = qrisUri,
                eWalletInfo = eWalletInfo
            )
        )
        Toast.makeText(context, "Profil Bisnis disimpan", Toast.LENGTH_SHORT).show()
        onDismiss()
    }

    val pickLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val logoFile = File(context.filesDir, "logo_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    logoFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                logoUri = logoFile.absolutePath
                Toast.makeText(context, "Logo berhasil disimpan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                logoUri = uri.toString()
            }
        }
    }

    val pickQrisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val qrisFile = File(context.filesDir, "qris_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    qrisFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                qrisUri = qrisFile.absolutePath
                Toast.makeText(context, "QRIS berhasil disimpan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                qrisUri = uri.toString()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.ambientGlow(),
            containerColor = ProBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Profil Bisnis", style = NotaTheme.typography.heading, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = saveProfile, text = "Simpan")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Logo Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable {
                                pickLogoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = rememberVectorPainter(Icons.Default.Storefront)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(28.dp).offset(x = 6.dp, y = 6.dp),
                        shape = CircleShape,
                        color = AccentBlue
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(6.dp), tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ketuk untuk ganti logo toko", style = NotaTheme.typography.label, color = TextGray)

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("INFORMASI DASAR", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    NotaTextField(value = businessName, onValueChange = { businessName = it }, placeholder = "Nama Toko / Bisnis", leadingIcon = Icons.Default.Apartment)
                    NotaTextField(value = address, onValueChange = { address = it }, placeholder = "Alamat Toko", leadingIcon = Icons.Default.LocationOn)
                    NotaTextField(value = notes, onValueChange = { notes = it }, placeholder = "Catatan Struk (Contoh: Terima Kasih)", leadingIcon = Icons.AutoMirrored.Filled.Notes)

                    Text("DETAIL BANK & TRANSFER", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    NotaTextField(value = bankName, onValueChange = { bankName = it }, placeholder = "Nama Bank (BCA, Mandiri, dll)")
                    NotaTextField(value = bankAccount, onValueChange = { bankAccount = it }, placeholder = "Nomor Rekening")
                    NotaTextField(value = bankHolder, onValueChange = { bankHolder = it }, placeholder = "Nama Pemilik Rekening")

                    Text("DETAIL E-WALLET", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    NotaTextField(value = eWalletInfo, onValueChange = { eWalletInfo = it }, placeholder = "Info E-Wallet (Contoh: DANA/OVO 08xx a/n Nama)", leadingIcon = Icons.Default.Wallet)

                    Text("QRIS PEMBAYARAN", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gambar QRIS", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Pilih foto QRIS agar tampil di atas nota & discan pelanggan", style = NotaTheme.typography.label, color = TextGray)
                        }
                        Button(
                            onClick = { pickQrisLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pilih QRIS", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }

                    if (qrisUri != null) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier.size(160.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                            ) {
                                AsyncImage(
                                    model = qrisUri,
                                    contentDescription = "QRIS Preview",
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(viewModel: NotaViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current

    // Folder backup pilihan pengguna (SAF) - dipilih sekali, lalu auto-backup menimpa file di sana
    var backupFolderLabel by remember { mutableStateOf(SafBackup.getFolderLabel(context)) }
    val pickBackupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            if (SafBackup.saveTreeUri(context, treeUri)) {
                backupFolderLabel = SafBackup.getFolderLabel(context)
                viewModel.triggerAutoBackup(showToast = true)
            } else {
                Toast.makeText(context, "Gagal menyimpan izin folder. Coba pilih folder lain.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            modifier = Modifier.ambientGlow(),
            containerColor = ProBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Kelola Data & Cadangan", style = NotaTheme.typography.heading, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Target Folder Card (Sesuai Permintaan)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, Color(0xFF10C48C).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lokasi Folder Backup", style = NotaTheme.typography.heading.copy(fontSize = 15.sp), color = Color.White)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10C48C).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "AUTO OVERWRITE",
                                    style = NotaTheme.typography.label.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10C48C),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE2E8F0), // Lighter background for better contrast
                            border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = backupFolderLabel?.let { "📁 $it" } ?: "Belum ada folder dipilih",
                                    style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                )
                                Text(
                                    text = "📦 nota_backup.zip (Lengkap Data + Foto Logo & QRIS)",
                                    style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "📄 nota_backup.json (Berkas Data JSON)",
                                    style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.5.sp, color = Color(0xFF475569))
                                )
                            }
                        }

                        Text(
                            text = "Setiap tambah nota, edit nota, atau ubah profil toko, file ZIP (lengkap foto logo & QRIS) dan JSON otomatis diperbarui (overwrite) tanpa menumpuk berkas.",
                            style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                            color = TextGray
                        )

                        // Pilih folder backup sekali (SAF) - tidak perlu izin "Akses semua file"
                        Button(
                            onClick = {
                                try {
                                    pickBackupFolderLauncher.launch(null)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Pemilih folder tidak tersedia di perangkat ini.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (backupFolderLabel == null) Color(0xFFFFB020) else ProBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = if (backupFolderLabel == null) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (backupFolderLabel == null) "Pilih Folder Backup (cukup sekali)" else "Ganti Folder Backup",
                                style = NotaTheme.typography.body.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (backupFolderLabel == null) Color.Black else Color.White
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.triggerAutoBackup(showToast = true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cadangkan", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }

                            Button(
                                onClick = {
                                    viewModel.restoreLatestAutoBackup(
                                        onSuccess = { count ->
                                            Toast.makeText(context, "Berhasil memulihkan $count nota dan profil usaha!", Toast.LENGTH_LONG).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10C48C)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pulihkan", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }

                        Button(
                            onClick = {
                                BackupUtils.shareBackupZip(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C6FFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan Berkas Backup ZIP", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }

                // Import Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pulihkan Data dari Berkas (.ZIP / .JSON)", style = NotaTheme.typography.heading.copy(fontSize = 16.sp), color = Color.White)
                        Text(
                            "Pilih berkas cadangan (.ZIP atau .JSON) dari memori HP, Google Drive, atau WhatsApp. Berkas ZIP otomatis memulihkan seluruh nota dan file gambar Logo Toko & QRIS.",
                            style = NotaTheme.typography.label,
                            color = TextGray
                        )
                        
                        val pickBackupLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            if (uri != null) {
                                try {
                                    val container = BackupUtils.importAny(context, uri)
                                    if (container != null && (container.notas.isNotEmpty() || container.businessProfile != null)) {
                                        viewModel.importBackup(
                                            container = container,
                                            onSuccess = {
                                                Toast.makeText(context, "✅ Berhasil memulihkan ${container.notas.size} nota dan profil usaha (beserta logo/QRIS)!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "Gagal memulihkan: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else if (container != null) {
                                        Toast.makeText(context, "File kosong atau format tidak sesuai.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal memproses berkas: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        Button(
                            onClick = { pickBackupLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = ProBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pilih File Backup (.ZIP / .JSON)", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsNewItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AccentBlue.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF7DE3FF), modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.White)
            Text(description, style = NotaTheme.typography.label.copy(fontSize = 12.sp), color = TextGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(viewModel: NotaViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isChecking by viewModel.isCheckingUpdate.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.ambientGlow(),
            containerColor = ProBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Tentang Aplikasi", style = NotaTheme.typography.heading, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ProBackground)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp),
                            color = AccentBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, AccentBlue)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF7DE3FF), modifier = Modifier.size(32.dp))
                            }
                        }

                        Text("Nota Kasir Pro Edition", style = NotaTheme.typography.heading.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold), color = Color.White)
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                        ) {
                            Text(
                                "Versi ${viewModel.currentAppVersion} (Pro Edition)",
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF7DE3FF),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            "Aplikasi kasir & pembuatan nota transaksi cepat, jernih, dan 100% aman tersimpan di memori lokal HP tanpa bergantung internet.",
                            style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.checkForUpdates(manual = true) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isChecking,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Memeriksa Pembaruan...", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            } else {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Periksa Pembaruan Sekarang", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }

                // What's New Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurface),
                    border = BorderStroke(1.dp, com.eco.nota.ui.theme.CardBorderGradient)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NewReleases, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                            Text("CATATAN PEMBARUAN (WHAT'S NEW)", style = NotaTheme.typography.label.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = Color.White)
                        }

                        HorizontalDivider(color = ProBorder)

                        val whatsNewEntries = remember(context) {
                            loadWhatsNewEntries(context)
                        }
                        whatsNewEntries.forEachIndexed { index, entry ->
                            WhatsNewItem(
                                icon = resolveWhatsNewIcon(entry.icon),
                                title = entry.title,
                                description = entry.description
                            )
                            if (index < whatsNewEntries.lastIndex) {
                                HorizontalDivider(color = ProBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Tutup", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private val whatsNewJsonParser = Json { ignoreUnknownKeys = true }

private fun loadWhatsNewEntries(context: Context): List<WhatsNewEntry> {
    return try {
        val raw = context.assets.open("whatsnew.json").bufferedReader().use { it.readText() }
        whatsNewJsonParser.decodeFromString<List<WhatsNewEntry>>(raw)
    } catch (e: Exception) {
        listOf(
            WhatsNewEntry(
                version = "1.7.2",
                icon = "new_releases",
                title = "Nota Kasir Pro Edition",
                description = "Lihat catatan rilis terbaru melalui changelog.md atau GitHub Releases."
            )
        )
    }
}

private fun resolveWhatsNewIcon(iconKey: String): ImageVector {
    return when (iconKey.trim().lowercase()) {
        "checkbox" -> Icons.Default.CheckBox
        "date_range" -> Icons.Default.DateRange
        "check_circle" -> Icons.Default.CheckCircle
        "search" -> Icons.Default.Search
        "edit" -> Icons.Default.Edit
        "system_update" -> Icons.Default.SystemUpdate
        "payments" -> Icons.Default.Payments
        "push_pin" -> Icons.Default.PushPin
        "receipt", "receipt_long" -> Icons.AutoMirrored.Filled.ReceiptLong
        "qr_code" -> Icons.Default.QrCode
        "phone" -> Icons.Default.Phone
        "cloud_upload" -> Icons.Default.CloudUpload
        "star" -> Icons.Default.Star
        else -> Icons.Default.NewReleases
    }
}
