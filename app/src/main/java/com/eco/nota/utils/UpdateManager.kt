package com.eco.nota.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?,
    val apkFileName: String?,
    val apkSize: Long = 0L,
    val releasePageUrl: String,
    val publishedAt: String = ""
)

sealed class UpdateDownloadState {
    data object Idle : UpdateDownloadState()
    data class Downloading(
        val progress: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateDownloadState()
    data class Downloaded(val file: File) : UpdateDownloadState()
    data class Installing(val file: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

object UpdateManager {

    /**
     * Membandingkan dua string versi semantik (misal: "1.5.1" vs "1.5.0", atau "v2.0.0" vs "1.9.9").
     * Mengembalikan true jika [latest] lebih baru dibanding [current].
     */
    fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val cleanLatest = latest.trim().dropWhile { !it.isDigit() }
            val cleanCurrent = current.trim().dropWhile { !it.isDigit() }

            val latestParts = cleanLatest.split(".")
                .mapNotNull { part -> part.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toIntOrNull() }
            val currentParts = cleanCurrent.split(".")
                .mapNotNull { part -> part.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toIntOrNull() }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            return latest.trim() != current.trim()
        }
        return false
    }

    /**
     * Mengecek GitHub Releases API untuk repository yang ditentukan (format: "owner/repo").
     */
    suspend fun checkUpdateFromGitHub(
        repo: String,
        currentVersion: String
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
        if (!cleanRepo.contains("/") || cleanRepo.split("/").size != 2) {
            return@withContext Result.failure(
                IllegalArgumentException("Format repository harus 'username/nama-repo' (contoh: ekofebri01/nota)")
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val endpoint = "https://api.github.com/repos/$cleanRepo/releases/latest"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "NotaKasirPro-Android-App")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = connection.responseCode
            if (responseCode == 404) {
                return@withContext Result.failure(
                    IllegalStateException("Belum ada Release publik yang ditemukan di GitHub: $cleanRepo")
                )
            }

            if (responseCode == 403) {
                return@withContext Result.failure(
                    IllegalStateException("Batas kuota akses GitHub API tercapai. Silakan coba lagi beberapa saat.")
                )
            }

            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("Gagal menghubungi GitHub (Kode: $responseCode)")
                )
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseText = reader.use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "").trim()
            val releaseTitle = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "Pembaruan versi terbaru dari GitHub.")
            val htmlUrl = json.optString("html_url", "https://github.com/$cleanRepo/releases")
            val publishedAt = json.optString("published_at", "")

            var apkDownloadUrl: String? = null
            var apkFileName: String? = null
            var apkSize = 0L

            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "").takeIf { it.isNotBlank() }
                        apkFileName = name
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
                if (apkDownloadUrl == null) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".bin", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "").takeIf { it.isNotBlank() }
                            apkFileName = name.substringBeforeLast(".") + ".apk"
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }
            }

            val hasUpdate = isNewerVersion(tagName, currentVersion)

            val updateInfo = AppUpdateInfo(
                hasUpdate = hasUpdate,
                currentVersion = currentVersion,
                latestVersion = tagName,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkDownloadUrl,
                apkFileName = apkFileName,
                apkSize = apkSize,
                releasePageUrl = htmlUrl,
                publishedAt = publishedAt
            )

            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Mengunduh APK secara penuh di dalam aplikasi dengan dukungan progress real-time & pembatalan.
     */
    suspend fun downloadApkToInternalStorage(
        context: Context,
        downloadUrl: String,
        fileName: String,
        expectedSize: Long = 0L,
        onProgress: (progress: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        val updatesDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
        val safeFileName = if (fileName.endsWith(".apk", ignoreCase = true)) fileName else "$fileName.apk"
        val tempFile = File(updatesDir, "$safeFileName.tmp")
        val targetFile = File(updatesDir, safeFileName)

        try {
            cleanupOldApks(context)
            updatesDir.mkdirs()

            val url = URL(downloadUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "NotaKasirPro-Android-App")
                connectTimeout = 15000
                readTimeout = 30000
                connect()
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("Gagal mengunduh file (HTTP $responseCode)")
                )
            }

            val contentLength = connection.contentLength.toLong().let {
                if (it > 0L) it else expectedSize
            }

            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(tempFile).use { output ->
                    val data = ByteArray(8192)
                    var total = 0L
                    var count: Int
                    var lastProgress = -1
                    var lastBytesEmit = 0L

                    while (input.read(data).also { count = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        total += count.toLong()
                        output.write(data, 0, count)

                        val progress = if (contentLength > 0L) {
                            ((total * 100L) / contentLength).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        if (progress != lastProgress || (total - lastBytesEmit) >= 65536L) {
                            lastProgress = progress
                            lastBytesEmit = total
                            onProgress(progress, total, contentLength)
                        }
                    }
                    output.flush()
                    onProgress(100, total, if (contentLength > 0L) contentLength else total)
                }
            }

            if (!tempFile.exists() || tempFile.length() < 100_000L) {
                tempFile.delete()
                return@withContext Result.failure(
                    IllegalStateException("File APK yang diunduh tidak valid atau korup.")
                )
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            Result.success(targetFile)
        } catch (e: kotlinx.coroutines.CancellationException) {
            tempFile.delete()
            targetFile.delete()
            throw e
        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun cleanupOldApks(context: Context) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (updatesDir.exists()) {
                updatesDir.listFiles()?.forEach { it.delete() }
            }
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".apk", ignoreCase = true) ||
                        file.name.endsWith(".bin", ignoreCase = true) ||
                        file.name.endsWith(".tmp", ignoreCase = true))
                ) {
                    file.delete()
                }
            }
            context.getExternalFilesDir(null)?.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".apk", ignoreCase = true) ||
                        file.name.endsWith(".bin", ignoreCase = true))
                ) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Buka Pengaturan > Aplikasi > Nota Kasir Pro > Instal apl yang tidak dikenal",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun installApk(context: Context, file: File): Boolean {
        return try {
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "File APK tidak ditemukan. Silakan unduh ulang.", Toast.LENGTH_SHORT).show()
                return false
            }
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            // Tandai file agar otomatis dibersihkan saat aplikasi keluar / dibuka ulang setelah update
            file.deleteOnExit()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memulai instalasi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Membuka URL di browser eksternal (hanya fallback jika rilis GitHub tidak melampirkan APK).
     */
    fun openBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka browser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

