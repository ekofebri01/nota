package com.eco.nota.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eco.nota.ui.theme.AccentBlue
import com.eco.nota.ui.theme.AccentEmerald
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.ProBorder
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.ProCardSurfaceLight
import com.eco.nota.ui.theme.TextGray
import com.eco.nota.ui.theme.TextMuted
import com.eco.nota.utils.AppUpdateInfo
import com.eco.nota.utils.UpdateManager

@Composable
fun UpdateAvailableDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = ProCardSurface,
            border = BorderStroke(1.5.dp, AccentEmerald.copy(alpha = 0.6f)),
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
                    color = AccentEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, AccentEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pembaruan Tersedia!",
                    style = NotaTheme.typography.heading.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Changelog Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
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
                    val sizeMb = if (updateInfo.apkSize > 0) String.format("%.1f MB", updateInfo.apkSize / (1024f * 1024f)) else ""
                    Text(
                        text = "File: ${updateInfo.apkFileName} ${if (sizeMb.isNotEmpty()) "($sizeMb)" else ""}",
                        style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                if (updateInfo.apkDownloadUrl != null) {
                    Button(
                        onClick = {
                            val fileName = updateInfo.apkFileName ?: "NotaKasirPro-${updateInfo.latestVersion}.apk"
                            UpdateManager.downloadApkWithDownloadManager(
                                context = context,
                                downloadUrl = updateInfo.apkDownloadUrl,
                                fileName = fileName
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download APK Sekarang",
                            style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp),
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = {
                        UpdateManager.openBrowser(context, updateInfo.releasePageUrl)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ProBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat Rilis di GitHub",
                        style = NotaTheme.typography.body.copy(fontSize = 13.5.sp),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
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

@Composable
fun UpdateBannerCard(
    updateInfo: AppUpdateInfo,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProCardSurface),
        border = BorderStroke(1.5.dp, AccentEmerald.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AccentEmerald.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pembaruan Versi ${updateInfo.latestVersion}",
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                        color = AccentEmerald
                    )
                }
                Text(
                    text = "Ketuk untuk melihat catatan rilis & unduh APK",
                    style = NotaTheme.typography.label.copy(fontSize = 11.sp),
                    color = TextGray
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
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
