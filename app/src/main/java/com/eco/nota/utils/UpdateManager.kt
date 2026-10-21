package com.eco.nota.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
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

object UpdateManager {

    /**
     * Membandingkan dua string versi semantik (misal: "1.5.1" vs "1.5.0", atau "v2.0.0" vs "1.9.9").
     * Mengembalikan true jika [latest] lebih baru dibanding [current].
     */
    fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val cleanLatest = latest.trim().removePrefix("v").removePrefix("V")
            val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")

            val latestParts = cleanLatest.split(".")
                .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }
            val currentParts = cleanCurrent.split(".")
                .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            // Jika parsing gagal, fallback string comparison sederhana
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
                IllegalArgumentException("Format repository harus 'username/nama-repo' (contoh: yourname/notakasir)")
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

            // Cari file asset APK di dalam release
            var apkDownloadUrl: String? = null
            var apkFileName: String? = null
            var apkSize = 0L

            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", null)
                        apkFileName = name
                        apkSize = asset.optLong("size", 0L)
                        break
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
     * Memulai unduhan file APK menggunakan Android DownloadManager sistem.
     */
    fun downloadApkWithDownloadManager(
        context: Context,
        downloadUrl: String,
        fileName: String
    ): Boolean {
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)

            val safeFileName = if (fileName.endsWith(".apk", ignoreCase = true)) fileName else "$fileName.apk"

            val request = DownloadManager.Request(uri).apply {
                setTitle("Mengunduh Pembaruan Nota Kasir Pro")
                setDescription(safeFileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            downloadManager.enqueue(request)
            Toast.makeText(
                context,
                "Mengunduh $safeFileName... Cek bar notifikasi HP Anda.",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal memulai unduhan: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    /**
     * Membuka URL di browser eksternal (bisa untuk halaman rilis atau direct download link).
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
