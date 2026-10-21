package com.eco.nota.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.eco.nota.models.Nota
import com.eco.nota.models.NotaViewModel
import com.eco.nota.ui.screens.CreateNotaScreen
import com.eco.nota.ui.screens.DetailNotaScreen
import com.eco.nota.ui.screens.HomeScreen
import com.eco.nota.ui.screens.NotaListScreen
import com.eco.nota.ui.screens.SettingsScreen
import com.eco.nota.ui.screens.SplashScreen
import com.eco.nota.ui.theme.AccentRose
import com.eco.nota.ui.theme.NotaTheme
import com.eco.nota.ui.theme.PrimaryGradient
import com.eco.nota.ui.theme.ProCardSurface
import com.eco.nota.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaApp(viewModel: NotaViewModel) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("home") }
    var selectedNota by remember { mutableStateOf<Nota?>(null) }
    var showCreateNota by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var notaToEdit by remember { mutableStateOf<Nota?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    val notas by viewModel.notas.collectAsState()

    // Back handler for confirmation before exiting app on home screen
    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Keluar Aplikasi",
                    style = NotaTheme.typography.heading.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar dari Nota Kasir Pro?",
                    style = NotaTheme.typography.body.copy(fontSize = 14.sp),
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    (context as? Activity)?.finish()
                }) {
                    Text(
                        text = "Keluar",
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = AccentRose
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(
                        text = "Batal",
                        style = NotaTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFA5B4FC)
                    )
                }
            },
            containerColor = ProCardSurface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        )
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    if (showCreateNota) {
        CreateNotaScreen(viewModel = viewModel, onDismiss = { showCreateNota = false })
        return
    }

    if (notaToEdit != null) {
        CreateNotaScreen(
            viewModel = viewModel,
            notaToEdit = notaToEdit,
            onDismiss = { notaToEdit = null }
        )
        return
    }

    if (showSettings) {
        SettingsScreen(viewModel = viewModel, onDismiss = { showSettings = false })
        return
    }

    selectedNota?.let { selected ->
        val currentNota = notas.find { it.id == selected.id } ?: selected
        DetailNotaScreen(
            nota = currentNota,
            viewModel = viewModel,
            onDismiss = { selectedNota = null },
            onEditClick = {
                notaToEdit = currentNota
                selectedNota = null
            }
        )
        return
    }

    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateDialogDismissed by viewModel.updateDialogDismissed.collectAsState()

    if (updateInfo != null && updateInfo!!.hasUpdate && !updateDialogDismissed) {
        com.eco.nota.ui.components.UpdateAvailableDialog(
            updateInfo = updateInfo!!,
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }

    Scaffold(
        containerColor = NotaTheme.colors.backgroundMain,
        topBar = {
            MainAppBar(
                title = "Nota Kasir",
                onStatsClicked = { },
                onSettingsClicked = { showSettings = true }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HomeScreen(
                notas = notas,
                onNotaClick = { selectedNota = it },
                onCreateNotaClick = { showCreateNota = true },
                updateInfo = updateInfo,
                onUpdateClick = { viewModel.showUpdateDialog() },
                onDismissUpdate = { viewModel.dismissUpdateDialog() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(title: String, onStatsClicked: () -> Unit, onSettingsClicked: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        title,
                        style = NotaTheme.typography.heading.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        color = NotaTheme.colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "PRO",
                            style = NotaTheme.typography.label.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Pengaturan",
                    tint = NotaTheme.colors.textPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NotaTheme.colors.backgroundMain
        )
    )
}
