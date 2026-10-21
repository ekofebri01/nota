package com.eco.nota.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.eco.nota.models.BusinessProfile
import com.eco.nota.models.InvoiceConfig
import com.eco.nota.models.Nota
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Serializable
data class BackupContainer(
    val businessProfile: BusinessProfile? = null,
    val invoiceConfig: InvoiceConfig? = null,
    val notas: List<Nota> = emptyList(),
    val logoBase64: String? = null,
    val qrisBase64: String? = null
)

object BackupUtils {
    private val jsonFormat = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun getBytesFromUriOrPath(context: Context, uriOrPath: String?): ByteArray? {
        if (uriOrPath.isNullOrBlank()) return null
        return try {
            if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("file://")) {
                val uri = Uri.parse(uriOrPath)
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else {
                val file = File(uriOrPath)
                if (file.exists() && file.canRead()) file.readBytes() else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Export all data (Notas, Profile, Config) + raw physical image files (Logo, QRIS) into a compressed ZIP file.
     */
    fun exportToZip(
        context: Context,
        notas: List<Nota>,
        businessProfile: BusinessProfile? = null,
        invoiceConfig: InvoiceConfig? = null,
        showToast: Boolean = false
    ): String? {
        return try {
            val filename = "nota_backup.zip"

            val logoBytes = getBytesFromUriOrPath(context, businessProfile?.logoUri)
            val qrisBytes = getBytesFromUriOrPath(context, businessProfile?.qrisUri)

            val logoBase64 = logoBytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
            val qrisBase64 = qrisBytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }

            val container = BackupContainer(
                businessProfile = businessProfile,
                invoiceConfig = invoiceConfig,
                notas = notas,
                logoBase64 = logoBase64,
                qrisBase64 = qrisBase64
            )
            val jsonStr = jsonFormat.encodeToString(container)

            val tempZip = File(context.cacheDir, "temp_backup_${System.currentTimeMillis()}.zip")
            ZipOutputStream(FileOutputStream(tempZip)).use { zos ->
                // Entry 1: data.json
                val jsonEntry = ZipEntry("data.json")
                zos.putNextEntry(jsonEntry)
                zos.write(jsonStr.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // Entry 2: Physical logo image
                if (logoBytes != null && logoBytes.isNotEmpty()) {
                    val logoEntry = ZipEntry("images/logo.png")
                    zos.putNextEntry(logoEntry)
                    zos.write(logoBytes)
                    zos.closeEntry()
                }

                // Entry 3: Physical QRIS image
                if (qrisBytes != null && qrisBytes.isNotEmpty()) {
                    val qrisEntry = ZipEntry("images/qris.png")
                    zos.putNextEntry(qrisEntry)
                    zos.write(qrisBytes)
                    zos.closeEntry()
                }
            }

            val zipBytes = tempZip.readBytes()
            tempZip.delete()

            // 1. Primary Target Directory: /storage/emulated/0/backups/NotaKasir/nota_backup.zip
            try {
                val directDir = File("/storage/emulated/0/backups/NotaKasir")
                if (!directDir.exists()) directDir.mkdirs()
                val directFile = File(directDir, filename)
                directFile.writeBytes(zipBytes)
            } catch (e: Exception) { }

            // 2. Canonical Environment Path
            try {
                val envDir = File(Environment.getExternalStorageDirectory(), "backups/NotaKasir")
                if (!envDir.exists()) envDir.mkdirs()
                val envFile = File(envDir, filename)
                envFile.writeBytes(zipBytes)
            } catch (e: Exception) { }

            // 3. External App Storage
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups/NotaKasir")
                if (!backupDir.exists()) backupDir.mkdirs()
                val file = File(backupDir, filename)
                file.writeBytes(zipBytes)
            } catch (e: Exception) { }

            // 4. Internal App Storage
            try {
                val internalDir = File(context.filesDir, "backups/NotaKasir")
                if (!internalDir.exists()) internalDir.mkdirs()
                File(internalDir, filename).writeBytes(zipBytes)
            } catch (e: Exception) { }

            // 5. Public Documents
            try {
                val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "NotaKasir")
                if (!publicDocs.exists()) publicDocs.mkdirs()
                File(publicDocs, filename).writeBytes(zipBytes)
            } catch (e: Exception) { }

            if (showToast) {
                Toast.makeText(
                    context,
                    "✅ Backup ZIP (Data + Gambar) tersimpan di:\n/storage/emulated/0/backups/NotaKasir/nota_backup.zip",
                    Toast.LENGTH_LONG
                ).show()
            }
            "/storage/emulated/0/backups/NotaKasir/nota_backup.zip"
        } catch (e: Exception) {
            if (showToast) {
                Toast.makeText(context, "❌ Gagal ekspor ZIP: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    /**
     * Legacy & standard export to JSON
     */
    fun exportToJson(context: Context, notas: List<Nota>, businessProfile: BusinessProfile? = null, invoiceConfig: InvoiceConfig? = null): String? {
        return try {
            val filename = "nota_backup.json"
            
            val logoBytes = getBytesFromUriOrPath(context, businessProfile?.logoUri)
            val qrisBytes = getBytesFromUriOrPath(context, businessProfile?.qrisUri)

            val logoBase64 = logoBytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
            val qrisBase64 = qrisBytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }

            val container = BackupContainer(businessProfile, invoiceConfig, notas, logoBase64, qrisBase64)
            val jsonStr = jsonFormat.encodeToString(container)

            // 1. Target Utama: /storage/emulated/0/backups/NotaKasir/nota_backup.json
            try {
                val directDir = File("/storage/emulated/0/backups/NotaKasir")
                if (!directDir.exists()) directDir.mkdirs()
                val directFile = File(directDir, filename)
                directFile.writeText(jsonStr)
            } catch (e: Exception) { }

            // 2. Canonical Environment Path
            try {
                val envDir = File(Environment.getExternalStorageDirectory(), "backups/NotaKasir")
                if (!envDir.exists()) envDir.mkdirs()
                val envFile = File(envDir, filename)
                envFile.writeText(jsonStr)
            } catch (e: Exception) { }

            // 3. External App Storage
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups/NotaKasir")
                if (!backupDir.exists()) backupDir.mkdirs()
                val file = File(backupDir, filename)
                file.writeText(jsonStr)
            } catch (e: Exception) { }

            // 4. Public Documents Folder
            try {
                val publicDocs = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "NotaKasir")
                if (!publicDocs.exists()) publicDocs.mkdirs()
                File(publicDocs, filename).writeText(jsonStr)
            } catch (e: Exception) { }

            Toast.makeText(context, "✅ Backup berhasil disimpan di:\n/storage/emulated/0/backups/NotaKasir/nota_backup.json", Toast.LENGTH_LONG).show()
            "/storage/emulated/0/backups/NotaKasir/nota_backup.json"
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gagal ekspor berkas: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * Unpacks a ZIP backup stream, extracts images to app internal filesDir, and restores container.
     */
    fun importFromZip(context: Context, inputStream: InputStream): BackupContainer? {
        return try {
            var jsonContent: String? = null
            var extractedLogoPath: String? = null
            var extractedQrisPath: String? = null

            val zis = ZipInputStream(inputStream)
            var entry: ZipEntry? = zis.nextEntry

            while (entry != null) {
                val name = entry.name.lowercase()
                val outBytes = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    outBytes.write(buffer, 0, len)
                }
                val bytes = outBytes.toByteArray()

                if (name.endsWith(".json") || name == "data.json" || name == "backup.json") {
                    jsonContent = String(bytes, Charsets.UTF_8)
                } else if (name.contains("logo") && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp"))) {
                    try {
                        val file = File(context.filesDir, "logo_${System.currentTimeMillis()}.png")
                        file.writeBytes(bytes)
                        extractedLogoPath = file.absolutePath
                    } catch (e: Exception) { }
                } else if (name.contains("qris") && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp"))) {
                    try {
                        val file = File(context.filesDir, "qris_${System.currentTimeMillis()}.png")
                        file.writeBytes(bytes)
                        extractedQrisPath = file.absolutePath
                    } catch (e: Exception) { }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            if (jsonContent.isNullOrBlank()) {
                return null
            }

            val baseContainer = importFromJson(context, jsonContent) ?: return null

            var updatedProfile = baseContainer.businessProfile
            if (extractedLogoPath != null) {
                updatedProfile = updatedProfile?.copy(logoUri = extractedLogoPath)
            }
            if (extractedQrisPath != null) {
                updatedProfile = updatedProfile?.copy(qrisUri = extractedQrisPath)
            }

            baseContainer.copy(businessProfile = updatedProfile)
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gagal ekstrak backup ZIP: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Smart import that auto-detects whether the picked URI is a .ZIP or a .JSON file.
     */
    fun importAny(context: Context, uri: Uri): BackupContainer? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            // Check ZIP magic header bytes: 0x50, 0x4B, 0x03, 0x04 ("PK..")
            val isZip = bytes.size >= 4 &&
                    bytes[0] == 0x50.toByte() &&
                    bytes[1] == 0x4B.toByte() &&
                    bytes[2] == 0x03.toByte() &&
                    bytes[3] == 0x04.toByte()

            if (isZip) {
                importFromZip(context, ByteArrayInputStream(bytes))
            } else {
                val jsonStr = String(bytes, Charsets.UTF_8)
                importFromJson(context, jsonStr)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gagal memproses berkas: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun importFromJson(context: Context, jsonStr: String): BackupContainer? {
        val processed = jsonStr
            .replace("\"customer_name\"", "\"customerName\"")
            .replace("\"phone_number\"", "\"phoneNumber\"")
            .replace("\"customer_label\"", "\"customerLabel\"")
            .replace("\"payment_method\"", "\"paymentMethod\"")
            .replace("\"shipping_cost\"", "\"shippingCost\"")
            .replace("\"reference_code\"", "\"referenceCode\"")
            .replace("\"tax_percent\"", "\"taxPercent\"")
            .replace("\"business_profile\"", "\"businessProfile\"")
            .replace("\"invoice_config\"", "\"invoiceConfig\"")
            .replace("\"logo_uri\"", "\"logoUri\"")
            .replace("\"bank_name\"", "\"bankName\"")
            .replace("\"bank_account\"", "\"bankAccount\"")
            .replace("\"bank_holder\"", "\"bankHolder\"")
            .replace("\"qris_uri\"", "\"qrisUri\"")
            .replace("\"ewallet_info\"", "\"eWalletInfo\"")
            .replace("\"e_wallet_info\"", "\"eWalletInfo\"")
            .replace("\"paper_size\"", "\"paperSize\"")
            .replace("\"show_logo\"", "\"showLogo\"")
            .replace("\"show_footer\"", "\"showFooter\"")
            .replace("\"show_signature\"", "\"showSignature\"")

        val trimmed = processed.trim()
        return try {
            val container = if (trimmed.startsWith("[")) {
                val list = jsonFormat.decodeFromString<List<Nota>>(trimmed)
                BackupContainer(notas = list)
            } else {
                jsonFormat.decodeFromString<BackupContainer>(trimmed)
            }

            // Restore images if present in base64
            var newProfile = container.businessProfile
            
            if (container.logoBase64 != null) {
                try {
                    val bytes = android.util.Base64.decode(container.logoBase64, android.util.Base64.DEFAULT)
                    val file = File(context.filesDir, "logo_${System.currentTimeMillis()}.png")
                    file.writeBytes(bytes)
                    newProfile = newProfile?.copy(logoUri = file.absolutePath)
                } catch (e: Exception) { }
            }
            
            if (container.qrisBase64 != null) {
                try {
                    val bytes = android.util.Base64.decode(container.qrisBase64, android.util.Base64.DEFAULT)
                    val file = File(context.filesDir, "qris_${System.currentTimeMillis()}.png")
                    file.writeBytes(bytes)
                    newProfile = newProfile?.copy(qrisUri = file.absolutePath)
                } catch (e: Exception) { }
            }
            
            if (newProfile != container.businessProfile) {
                container.copy(businessProfile = newProfile)
            } else {
                container
            }
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Gagal memproses berkas backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Share backup ZIP file with external apps (WhatsApp, Drive, Email, etc.)
     */
    fun shareBackupZip(context: Context) {
        try {
            val candidateFiles = listOf(
                File("/storage/emulated/0/backups/NotaKasir/nota_backup.zip"),
                File(Environment.getExternalStorageDirectory(), "backups/NotaKasir/nota_backup.zip"),
                File(context.getExternalFilesDir(null), "backups/NotaKasir/nota_backup.zip"),
                File(context.filesDir, "backups/NotaKasir/nota_backup.zip"),
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "NotaKasir/nota_backup.zip")
            )
            val fileToShare = candidateFiles.firstOrNull { it.exists() && it.length() > 0 }
            if (fileToShare == null) {
                Toast.makeText(context, "Berkas backup ZIP belum ada. Silakan buat cadangan terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return
            }
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                fileToShare
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Nota Kasir (ZIP Lengkap)")
                putExtra(Intent.EXTRA_TEXT, "Berkas cadangan kompresi ZIP Nota Kasir Pro (Data Nota + Logo + QRIS).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Berkas Backup ZIP"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan berkas: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
