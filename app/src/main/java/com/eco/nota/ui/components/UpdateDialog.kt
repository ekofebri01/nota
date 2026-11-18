package com.eco.nota.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.eco.nota.ui.theme.AccentAmber
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.AccentRose
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import com.eco.nota.utils.AppUpdateInfo
import com.eco.nota.utils.UpdateDownloadState
import com.eco.nota.utils.UpdateManager
import java.io.File
import java.util.Locale

@Composable
fun UpdateAvailableDialog(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstallClick: (File) -> Unit,
    onHide: () -> Unit,
    onDismissAndCleanup: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canInstall by remember { mutableStateOf(UpdateManager.canInstallPackages(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canInstall = UpdateManager.canInstallPackages(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isBusy = downloadState is UpdateDownloadState.Downloading || downloadState is UpdateDownloadState.Installing

    val headerTint = when (downloadState) {
        is UpdateDownloadState.Error -> AccentRose
        else -> AccentEmerald
    }
    val headerIcon = when (downloadState) {
        is UpdateDownloadState.Error -> Icons.Default.ErrorOutline
        is UpdateDownloadState.Downloaded -> Icons.Default.CheckCircle
        is UpdateDownloadState.Installing -> Icons.Default.InstallMobile
        else -> Icons.Default.SystemUpdate
    }
    val headerTitle = when (downloadState) {
        is UpdateDownloadState.Downloading -> "Mengunduh Pembaruan..."
        is UpdateDownloadState.Downloaded -> "Pembaruan Siap Diinstal!"
        is UpdateDownloadState.Installing -> "Memasang Pembaruan..."
        is UpdateDownloadState.Error -> "Unduhan Gagal"
        is UpdateDownloadState.Idle -> "Pembaruan Tersedia!"
    }

    Dialog(
        onDismissRequest = {
            if (isBusy) onHide() else onDismissAndCleanup()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isBusy,
            dismissOnClickOutside = !isBusy
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = ProCardSurface,
            border = BorderStroke(1.5.dp, headerTint.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = headerTint.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, headerTint.copy(alpha = 0.4f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = headerTitle,
                            tint = headerTint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = headerTitle,
                    style = NotaTheme.typography.heading.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentEmerald.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AccentEmerald)
                    ) {
                        Text(
                            text = "Versi Baru: ${updateInfo.latestVersion}",
                            style = NotaTheme.typography.label.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = AccentEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ProCardSurfaceLight,
                        border = BorderStroke(1.dp, ProBorder)
                    ) {
                        Text(
                            text = "Versi Anda: ${updateInfo.currentVersion}",
                            style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                            color = TextGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (updateInfo.releaseTitle.isNotEmpty() && updateInfo.releaseTitle != updateInfo.latestVersion) {
                    Text(
                        text = updateInfo.releaseTitle,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Changelog Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ProCardSurfaceLight),
                    border = BorderStroke(1.dp, ProBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.NewReleases,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Catatan Rilis (Changelog):",
                                style = NotaTheme.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                color = TextGray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "Pembaruan performa dan perbaikan bug." },
                            style = NotaTheme.typography.body.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                if (updateInfo.apkFileName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val sizeMb = if (updateInfo.apkSize > 0) String.format(Locale.US, "%.1f MB", updateInfo.apkSize / (1024f * 1024f)) else ""
                    Text(
                        text = "Paket APK: ${updateInfo.apkFileName} ${if (sizeMb.isNotEmpty()) "($sizeMb)" else ""}",
                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // In-App State Machine UI
                when (downloadState) {
                    is UpdateDownloadState.Downloading -> {
                        val animatedProgress by animateFloatAsState(
                            targetValue = downloadState.progress / 100f,
                            animationSpec = tween(durationMillis = 250),
                            label = "update_download_progress"
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ProCardSurfaceLight),
                            border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mengunduh... ${downloadState.progress}%",
                                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                        color = AccentEmerald
                                    )
                                    val downloadedMb = String.format(Locale.US, "%.1f", downloadState.downloadedBytes / (1024f * 1024f))
                                    val totalMb = if (downloadState.totalBytes > 0L) {
                                        String.format(Locale.US, "%.1f MB", downloadState.totalBytes / (1024f * 1024f))
                                    } else {
                                        "MB"
                                    }
                                    Text(
                                        text = "$downloadedMb / $totalMb",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                        color = TextGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (downloadState.totalBytes > 0L) {
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = AccentEmerald,
                                        trackColor = Color(0xFF1E293B)
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = AccentEmerald,
                                        trackColor = Color(0xFF1E293B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Unduhan berjalan langsung di dalam aplikasi dan tetap aktif jika dialog disembunyikan.",
                                    style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onHide,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ProBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("update_hide_button")
                            ) {
                                Text("Sembunyikan", style = NotaTheme.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onCancelDownload,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("update_cancel_button")
                            ) {
                                Text("Batalkan", style = NotaTheme.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = AccentRose)
                            }
                        }
                    }

                    is UpdateDownloadState.Downloaded -> {
                        if (!canInstall) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.12f)),
                                border = BorderStroke(1.5.dp, AccentAmber.copy(alpha = 0.7f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Izin Instalasi Diperlukan",
                                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "APK selesai diunduh. Aktifkan izin \"Instal apl yang tidak dikenal\" agar pemasangan dapat dilanjutkan langsung di aplikasi.",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                        color = TextGray,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { UpdateManager.openInstallPermissionSettings(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("update_permission_button")
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Buka Pengaturan Izin",
                                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.15f)),
                                border = BorderStroke(1.5.dp, AccentEmerald)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "File Pembaruan Siap Diinstal!",
                                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "File ${downloadState.file.name} telah selesai diunduh dan diverifikasi.",
                                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                        color = TextGray,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onInstallClick(downloadState.file) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("update_install_button")
                                    ) {
                                        Icon(Icons.Default.InstallMobile, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Instal Sekarang",
                                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onDismissAndCleanup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("update_cleanup_later_button")
                        ) {
                            Text("Hapus File & Nanti Saja", style = NotaTheme.typography.body.copy(fontSize = 13.sp), color = TextGray)
                        }
                    }

                    is UpdateDownloadState.Installing -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.14f)),
                            border = BorderStroke(1.5.dp, AccentBlue.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = AccentEmerald,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Membuka Pemasang Aplikasi...",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Konfirmasi \"Update / Instal\" pada jendela sistem Android. File APK sementara akan dibersihkan otomatis setelah selesai.",
                                    style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                                    color = TextGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onInstallClick(downloadState.file) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("update_reopen_installer_button")
                            ) {
                                Text("Buka Ulang", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = Color.Black)
                            }
                            OutlinedButton(
                                onClick = onHide,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ProBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("update_hide_installing_button")
                            ) {
                                Text("Tutup", style = NotaTheme.typography.body.copy(fontSize = 13.sp), color = Color.White)
                            }
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.14f)),
                            border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = downloadState.message,
                                    style = NotaTheme.typography.body.copy(fontSize = 12.5.sp),
                                    color = Color(0xFFFECDD3),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (updateInfo.apkDownloadUrl != null) {
                            Button(
                                onClick = onStartDownload,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("update_retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Coba Unduh Lagi", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onDismissAndCleanup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("update_error_close_button")
                        ) {
                            Text("Tutup", style = NotaTheme.typography.body.copy(fontSize = 13.sp), color = TextGray)
                        }
                    }

                    is UpdateDownloadState.Idle -> {
                        if (updateInfo.apkDownloadUrl != null) {
                            Button(
                                onClick = onStartDownload,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("update_download_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unduh & Perbarui Sekarang",
                                    style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                                    color = Color.Black
                                )
                            }
                        } else {
                            Text(
                                text = "Paket APK langsung tidak ditemukan pada rilis ini.",
                                style = NotaTheme.typography.label.copy(fontSize = 12.sp),
                                color = AccentAmber,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    UpdateManager.openBrowser(context, updateInfo.releasePageUrl)
                                    onDismissAndCleanup()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ProBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buka Halaman Rilis", style = NotaTheme.typography.body.copy(fontSize = 13.5.sp), color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = onDismissAndCleanup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("update_later_button")
                        ) {
                            Text(
                                text = "Nanti Saja",
                                style = NotaTheme.typography.body.copy(fontSize = 13.sp),
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateBannerCard(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, subtitle, accentColor) = when (downloadState) {
        is UpdateDownloadState.Downloading -> Triple(
            "Mengunduh ${updateInfo.latestVersion} (${downloadState.progress}%)",
            "Ketuk untuk melihat progres unduhan di aplikasi",
            AccentBlue
        )
        is UpdateDownloadState.Downloaded -> Triple(
            "Versi ${updateInfo.latestVersion} Siap Diinstal",
            "Ketuk untuk langsung memasang pembaruan APK",
            AccentEmerald
        )
        is UpdateDownloadState.Installing -> Triple(
            "Memasang Versi ${updateInfo.latestVersion}...",
            "Ketuk untuk membuka dialog instalasi",
            AccentBlue
        )
        is UpdateDownloadState.Error -> Triple(
            "Unduhan ${updateInfo.latestVersion} Gagal",
            "Ketuk untuk mencoba mengunduh ulang di aplikasi",
            AccentRose
        )
        is UpdateDownloadState.Idle -> Triple(
            "Pembaruan Versi ${updateInfo.latestVersion}",
            "Ketuk untuk unduh & instal langsung di aplikasi",
            AccentEmerald
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("update_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (downloadState) {
                                is UpdateDownloadState.Downloaded -> Icons.Default.InstallMobile
                                is UpdateDownloadState.Error -> Icons.Default.ErrorOutline
                                else -> Icons.Default.SystemUpdate
                            },
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                        color = accentColor
                    )
                    Text(
                        text = subtitle,
                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                        color = TextGray
                    )
                }

                if (downloadState !is UpdateDownloadState.Downloading) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("update_banner_close_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (downloadState is UpdateDownloadState.Downloading) {
                Spacer(modifier = Modifier.height(8.dp))
                if (downloadState.totalBytes > 0L) {
                    LinearProgressIndicator(
                        progress = { (downloadState.progress / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentEmerald,
                        trackColor = Color(0xFF1E293B)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentEmerald,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
fun GithubRepoDialog(
    initialRepo: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var repoInput by remember { mutableStateOf(initialRepo) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp)),
            color = ProCardSurface,
            border = BorderStroke(1.dp, ProBorder),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Repository GitHub",
                    style = NotaTheme.typography.heading.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tentukan repository GitHub tempat Anda mempublikasikan Release APK aplikasi ini.",
                    style = NotaTheme.typography.body.copy(fontSize = 12.5.sp),
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = repoInput,
                    onValueChange = {
                        repoInput = it
                        errorText = null
                    },
                    label = { Text("Repository (owner/repo)") },
                    placeholder = { Text("contoh: ekofebri/nota-kasir") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ProBorder,
                        focusedContainerColor = ProCardSurfaceLight,
                        unfocusedContainerColor = ProCardSurfaceLight,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextGray
                    ),
                    isError = errorText != null
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorText ?: "",
                        style = NotaTheme.typography.label.copy(fontSize = 11.5.sp),
                        color = Color(0xFFF87171)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Format yang valid: 'username/nama-repo' atau URL lengkap 'https://github.com/username/nama-repo'.",
                    style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextGray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val clean = repoInput.trim()
                                .removePrefix("https://github.com/")
                                .removePrefix("http://github.com/")
                                .removeSuffix("/")

                            if (clean.isNotEmpty() && (!clean.contains("/") || clean.split("/").size != 2)) {
                                errorText = "Format harus username/nama-repo (contoh: ekofebri/nota-kasir)"
                                return@Button
                            }

                            onSave(clean)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan", style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
