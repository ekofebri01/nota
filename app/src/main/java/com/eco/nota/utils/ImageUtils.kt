package com.eco.nota.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val filename = "Nota_${safeTitle}_${System.currentTimeMillis()}.png"
        var imageUri: Uri? = null
        var savedSuccessfully = false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NotaPro")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { output ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                        savedSuccessfully = true
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    imageUri = uri
                }
            }

            if (!savedSuccessfully) {
                // Fallback for Android 9 or below, or if MediaStore fails
                val picturesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "NotaPro")
                if (!picturesDir.exists()) picturesDir.mkdirs()
                val imageFile = File(picturesDir, filename)
                FileOutputStream(imageFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    savedSuccessfully = true
                }
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                imageUri = Uri.fromFile(imageFile)
                mediaScanIntent.data = imageUri
                context.sendBroadcast(mediaScanIntent)
            }

            if (savedSuccessfully) {
                Toast.makeText(context, "Nota HD berhasil disimpan ke Galeri Foto!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal menyimpan gambar nota.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Internal storage fallback
            try {
                val appPicDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "NotaPro")
                if (!appPicDir.exists()) appPicDir.mkdirs()
                val fallbackFile = File(appPicDir, filename)
                FileOutputStream(fallbackFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                }
                imageUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fallbackFile)
                Toast.makeText(context, "Disimpan di: ${fallbackFile.name}", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(context, "Gagal menyimpan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
        return imageUri
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            if (!cachePath.exists()) cachePath.mkdirs()
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val file = File(cachePath, "nota_${safeTitle}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Nota Transaksi #$title")
                putExtra(Intent.EXTRA_TEXT, "Berikut adalah bukti nota transaksi #$title.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Bagikan Nota via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
