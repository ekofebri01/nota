package com.eco.nota.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

/**
 * Backup ke folder pilihan pengguna lewat Storage Access Framework (SAF).
 *
 * Pengguna memilih folder SEKALI. Izin akses disimpan permanen (persistable URI permission),
 * sehingga auto-backup berikutnya bisa menimpa file tanpa dialog dan tanpa izin
 * "Akses semua file" (MANAGE_EXTERNAL_STORAGE).
 *
 * Hanya memakai API bawaan Android (DocumentsContract) agar tidak perlu dependency tambahan.
 */
object SafBackup {
    private const val PREFS_NAME = "nota_pro_prefs"
    private const val KEY_TREE_URI = "backup_tree_uri"
    private val lock = Any()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** URI folder yang dipilih pengguna, atau null bila belum dipilih / izin sudah dicabut. */
    fun getTreeUri(context: Context): Uri? {
        val saved = prefs(context).getString(KEY_TREE_URI, null)?.takeIf { it.isNotBlank() } ?: return null
        val uri = Uri.parse(saved)
        val stillGranted = context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission && it.isWritePermission
        }
        return if (stillGranted) uri else null
    }

    fun hasFolder(context: Context): Boolean = getTreeUri(context) != null

    /** Simpan folder pilihan dan minta izin akses permanen. */
    fun saveTreeUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            prefs(context).edit().putString(KEY_TREE_URI, uri.toString()).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clear(context: Context) {
        val old = prefs(context).getString(KEY_TREE_URI, null)
        prefs(context).edit().remove(KEY_TREE_URI).apply()
        if (old != null) {
            try {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(old),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) { }
        }
    }

    /** Nama folder yang mudah dibaca untuk ditampilkan di UI (mis. "Documents/NotaKasir"). */
    fun getFolderLabel(context: Context): String? {
        val tree = getTreeUri(context) ?: return null
        return try {
            val docId = DocumentsContract.getTreeDocumentId(tree)
            // Format umum: "primary:Documents/NotaKasir"
            val readable = Uri.decode(docId).substringAfter(":", docId)
            if (readable.isBlank()) "Penyimpanan internal" else readable
        } catch (e: Exception) {
            tree.lastPathSegment
        }
    }

    private fun findChild(context: Context, tree: Uri, displayName: String): Uri? {
        val treeDocId = DocumentsContract.getTreeDocumentId(tree)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, treeDocId)
        context.contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == displayName) {
                    return DocumentsContract.buildDocumentUriUsingTree(tree, cursor.getString(0))
                }
            }
        }
        return null
    }

    /**
     * Tulis (timpa) file ke folder pilihan. Mengembalikan true bila berhasil.
     * Aman dipanggil kapan saja: bila folder belum dipilih / gagal, hanya mengembalikan false.
     */
    fun write(context: Context, fileName: String, mimeType: String, bytes: ByteArray): Boolean {
        val tree = getTreeUri(context) ?: return false
        return synchronized(lock) {
            try {
                val resolver = context.contentResolver
                val target = findChild(context, tree, fileName)
                    ?: DocumentsContract.createDocument(
                        resolver,
                        DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree)),
                        mimeType,
                        fileName
                    )
                    ?: return@synchronized false

                // "wt" = write + truncate, agar file lama benar-benar tertimpa
                resolver.openOutputStream(target, "wt")?.use { it.write(bytes) } ?: return@synchronized false
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /** Baca file dari folder pilihan, atau null bila tidak ada. */
    fun read(context: Context, fileName: String): ByteArray? {
        val tree = getTreeUri(context) ?: return null
        return try {
            val doc = findChild(context, tree, fileName) ?: return null
            context.contentResolver.openInputStream(doc)?.use { it.readBytes() }?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }
}
