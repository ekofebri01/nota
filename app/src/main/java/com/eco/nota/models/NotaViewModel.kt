package com.eco.nota.models

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eco.nota.utils.AppUpdateInfo
import com.eco.nota.utils.BackupContainer
import com.eco.nota.utils.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey val id: String,
    val referenceCode: String,
    val customerName: String,
    val phoneNumber: String,
    val address: String,
    val customerLabel: String,
    val date: String,
    val total: String,
    val status: String,
    val subtotal: Long,
    val taxPercent: Double,
    val discount: Long,
    val shippingCost: Long,
    val deposit: Long,
    val paymentMethod: String,
    val notes: String,
    val itemsJson: String
)

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas ORDER BY id DESC")
    fun getAllNotas(): kotlinx.coroutines.flow.Flow<List<NotaEntity>>

    @Query("SELECT * FROM notas ORDER BY id DESC")
    suspend fun getAllNotasDirect(): List<NotaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNota(nota: NotaEntity)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun deleteNota(id: String)

    @Query("UPDATE notas SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM notas")
    suspend fun deleteAll()
}

@Database(entities = [NotaEntity::class], version = 1, exportSchema = false)
abstract class NotaDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao

    companion object {
        @Volatile
        private var INSTANCE: NotaDatabase? = null

        fun getDatabase(context: Context): NotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotaDatabase::class.java,
                    "nota_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class NotaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NotaDatabase.getDatabase(application).notaDao()
    private val prefs = application.getSharedPreferences("nota_pro_prefs", Context.MODE_PRIVATE)
    private val jsonFormat = Json { ignoreUnknownKeys = true }

    val notas: StateFlow<List<Nota>> = dao.getAllNotas()
        .map { entities ->
            entities.map { entity ->
                val items = try {
                    jsonFormat.decodeFromString<List<NotaItemInput>>(entity.itemsJson)
                } catch (e: Exception) {
                    emptyList()
                }
                Nota(
                    id = entity.id,
                    referenceCode = entity.referenceCode,
                    customerName = entity.customerName,
                    phoneNumber = entity.phoneNumber,
                    address = entity.address,
                    customerLabel = entity.customerLabel,
                    date = entity.date,
                    total = entity.total,
                    status = normalizeNotaStatus(entity.status),
                    subtotal = entity.subtotal,
                    taxPercent = entity.taxPercent,
                    discount = entity.discount,
                    shippingCost = entity.shippingCost,
                    deposit = entity.deposit,
                    paymentMethod = normalizePaymentMethod(entity.paymentMethod),
                    notes = entity.notes,
                    items = items
                )
            }.sortedWith(
                compareByDescending<Nota> { nota ->
                    val timestamp = nota.id.removePrefix("NOTA_").toLongOrNull()
                    if (timestamp != null) timestamp
                    else {
                        try {
                            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(nota.date)?.time
                                ?: SimpleDateFormat("dd MMM yyyy", Locale.US).parse(nota.date)?.time
                                ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    }
                }.thenByDescending { it.id }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _businessProfile = MutableStateFlow(loadProfile())
    val businessProfile: StateFlow<BusinessProfile> = _businessProfile

    private val _invoiceConfig = MutableStateFlow(loadConfig())
    val invoiceConfig: StateFlow<InvoiceConfig> = _invoiceConfig

    private val _deletedUnits = MutableStateFlow(loadDeletedUnits())
    val deletedUnits: StateFlow<Set<String>> = _deletedUnits

    val currentAppVersion: String = "1.5.0"

    private val _githubRepo = MutableStateFlow(prefs.getString("github_repo", "") ?: "")
    val githubRepo: StateFlow<String> = _githubRepo

    private val _autoCheckUpdate = MutableStateFlow(prefs.getBoolean("auto_check_update", true))
    val autoCheckUpdate: StateFlow<Boolean> = _autoCheckUpdate

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate

    private val _updateDialogDismissed = MutableStateFlow(false)
    val updateDialogDismissed: StateFlow<Boolean> = _updateDialogDismissed

    init {
        viewModelScope.launch {
            // Perform auto backup check/initialization if DB has items
            val existing = dao.getAllNotasDirect()
            if (existing.isNotEmpty()) {
                performAutoBackup(showToast = false)
            }
            // Auto-check GitHub updates if configured and enabled
            if (_autoCheckUpdate.value && _githubRepo.value.isNotBlank()) {
                checkForUpdates(manual = false)
            }
        }
    }

    fun setGithubRepo(repo: String) {
        val clean = repo.trim()
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removeSuffix("/")
        _githubRepo.value = clean
        prefs.edit().putString("github_repo", clean).apply()
        if (clean.isNotBlank()) {
            checkForUpdates(manual = false)
        }
    }

    fun setAutoCheckUpdate(enabled: Boolean) {
        _autoCheckUpdate.value = enabled
        prefs.edit().putBoolean("auto_check_update", enabled).apply()
    }

    fun dismissUpdateDialog() {
        _updateDialogDismissed.value = true
    }

    fun showUpdateDialog() {
        _updateDialogDismissed.value = false
    }

    fun checkForUpdates(manual: Boolean = false, onResult: ((Boolean, String) -> Unit)? = null) {
        val repo = _githubRepo.value.trim()
        if (repo.isBlank()) {
            if (manual) {
                onResult?.invoke(false, "Silakan atur nama Repository GitHub terlebih dahulu (misal: username/repo).")
            }
            return
        }

        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val result = UpdateManager.checkUpdateFromGitHub(repo, currentAppVersion)
            _isCheckingUpdate.value = false

            result.onSuccess { info ->
                _updateInfo.value = info
                if (info.hasUpdate) {
                    _updateDialogDismissed.value = false
                    if (manual) {
                        onResult?.invoke(true, "Versi baru tersedia: ${info.latestVersion}")
                    }
                } else {
                    if (manual) {
                        onResult?.invoke(false, "Aplikasi sudah menggunakan versi terbaru (${info.currentVersion})")
                    }
                }
            }.onFailure { error ->
                if (manual) {
                    onResult?.invoke(false, error.localizedMessage ?: "Gagal memeriksa pembaruan.")
                }
            }
        }
    }

    fun saveNota(nota: Nota) {
        viewModelScope.launch {
            val itemsJson = try {
                jsonFormat.encodeToString(nota.items)
            } catch (e: Exception) {
                "[]"
            }
            val entity = NotaEntity(
                id = nota.id,
                referenceCode = nota.referenceCode.ifEmpty { generateRefCode(nota.date) },
                customerName = nota.customerName,
                phoneNumber = nota.phoneNumber,
                address = nota.address,
                customerLabel = nota.customerLabel,
                date = nota.date,
                total = nota.total,
                status = normalizeNotaStatus(nota.status),
                subtotal = nota.subtotal,
                taxPercent = nota.taxPercent,
                discount = nota.discount,
                shippingCost = nota.shippingCost,
                deposit = nota.deposit,
                paymentMethod = normalizePaymentMethod(nota.paymentMethod),
                notes = nota.notes,
                itemsJson = itemsJson
            )
            dao.insertNota(entity)
            performAutoBackup(showToast = true)
        }
    }

    fun deleteNota(id: String) {
        viewModelScope.launch {
            dao.deleteNota(id)
            performAutoBackup(showToast = true)
        }
    }

    fun updateNotaStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            val normalized = normalizeNotaStatus(newStatus)
            dao.updateStatus(id, normalized)
            performAutoBackup(showToast = false)
        }
    }

    fun saveBusinessProfile(profile: BusinessProfile) {
        _businessProfile.value = profile
        prefs.edit().apply {
            putString("bp_name", profile.name)
            putString("bp_address", profile.address)
            putString("bp_notes", profile.notes)
            putString("bp_logo", profile.logoUri)
            putString("bp_bank_name", profile.bankName)
            putString("bp_bank_acc", profile.bankAccount)
            putString("bp_bank_holder", profile.bankHolder)
            putString("bp_qris", profile.qrisUri)
            putString("bp_ewallet", profile.eWalletInfo)
            apply()
        }
        viewModelScope.launch { performAutoBackup(showToast = true) }
    }

    fun saveInvoiceConfig(config: InvoiceConfig) {
        _invoiceConfig.value = config
        prefs.edit().apply {
            putString("cfg_template", config.template)
            putString("cfg_paper", config.paperSize)
            putBoolean("cfg_logo", config.showLogo)
            putBoolean("cfg_footer", config.showFooter)
            putBoolean("cfg_sig", config.showSignature)
            apply()
        }
        viewModelScope.launch { performAutoBackup(showToast = true) }
    }

    fun deleteUnit(unit: String) {
        val current = _deletedUnits.value.toMutableSet()
        if (current.add(unit)) {
            _deletedUnits.value = current
            prefs.edit().putStringSet("deleted_units", current).apply()
        }
    }

    fun restoreUnit(unit: String) {
        val current = _deletedUnits.value.toMutableSet()
        if (current.remove(unit)) {
            _deletedUnits.value = current
            prefs.edit().putStringSet("deleted_units", current).apply()
        }
    }

    private fun loadDeletedUnits(): Set<String> {
        return prefs.getStringSet("deleted_units", emptySet()) ?: emptySet()
    }

    fun triggerAutoBackup(showToast: Boolean = true) {
        viewModelScope.launch { performAutoBackup(showToast = showToast) }
    }

    private fun loadProfile(): BusinessProfile {
        return BusinessProfile(
            name = prefs.getString("bp_name", "ECO TOBACCO") ?: "ECO TOBACCO",
            address = prefs.getString("bp_address", "Jaten") ?: "Jaten",
            notes = prefs.getString("bp_notes", "Terima kasih atas kunjungan Anda") ?: "",
            logoUri = prefs.getString("bp_logo", null),
            bankName = prefs.getString("bp_bank_name", "BCA") ?: "BCA",
            bankAccount = prefs.getString("bp_bank_acc", "1234567890") ?: "1234567890",
            bankHolder = prefs.getString("bp_bank_holder", "Eko Febri") ?: "Eko Febri",
            qrisUri = prefs.getString("bp_qris", null),
            eWalletInfo = prefs.getString("bp_ewallet", "DANA: 081234567890") ?: ""
        )
    }

    private fun loadConfig(): InvoiceConfig {
        return InvoiceConfig(
            template = prefs.getString("cfg_template", "Modern") ?: "Modern",
            paperSize = prefs.getString("cfg_paper", "58mm") ?: "58mm",
            showLogo = prefs.getBoolean("cfg_logo", true),
            showFooter = prefs.getBoolean("cfg_footer", true),
            showSignature = prefs.getBoolean("cfg_signature", false)
        )
    }

    fun generateNextInvoiceNumber(dateStr: String? = null): String {
        return generateRefCode(dateStr)
    }

    private fun generateRefCode(dateStr: String? = null): String {
        val datePattern = try {
            val date = if (!dateStr.isNullOrBlank()) {
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).parse(dateStr)
                    ?: SimpleDateFormat("dd MMM yyyy", Locale.US).parse(dateStr)
                    ?: Date()
            } else {
                Date()
            }
            SimpleDateFormat("yyMMdd", Locale.US).format(date)
        } catch (e: Exception) {
            SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        }

        val prefix = "INV-$datePattern-"
        val currentList = notas.value
        var maxSeq = 0

        for (nota in currentList) {
            val code = nota.referenceCode.trim()
            if (code.startsWith(prefix, ignoreCase = true)) {
                val numPart = code.removePrefix(prefix).takeWhile { it.isDigit() }
                val num = numPart.toIntOrNull()
                if (num != null && num > maxSeq) {
                    maxSeq = num
                }
            }
        }

        val nextNum = maxSeq + 1
        return String.format(Locale.US, "INV-%s-%03d", datePattern, nextNum)
    }

    fun importBackup(container: BackupContainer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                container.businessProfile?.let {
                    saveBusinessProfile(it)
                }
                container.invoiceConfig?.let {
                    saveInvoiceConfig(it)
                }
                for (nota in container.notas) {
                    val itemsJson = try {
                        jsonFormat.encodeToString(nota.items)
                    } catch (e: Exception) {
                        "[]"
                    }
                    val entity = NotaEntity(
                        id = nota.id,
                        referenceCode = nota.referenceCode,
                        customerName = nota.customerName,
                        phoneNumber = nota.phoneNumber,
                        address = nota.address,
                        customerLabel = nota.customerLabel,
                        date = nota.date,
                        total = nota.total,
                        status = normalizeNotaStatus(nota.status),
                        subtotal = nota.subtotal,
                        taxPercent = nota.taxPercent,
                        discount = nota.discount,
                        shippingCost = nota.shippingCost,
                        deposit = nota.deposit,
                        paymentMethod = normalizePaymentMethod(nota.paymentMethod),
                        notes = nota.notes,
                        itemsJson = itemsJson
                    )
                    dao.insertNota(entity)
                }
                performAutoBackup(showToast = false)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Gagal memulihkan data")
            }
        }
    }

    private suspend fun performAutoBackup(showToast: Boolean = false) {
        try {
            val app = getApplication<Application>()
            val allEntities = dao.getAllNotasDirect()
            val fullNotasList = allEntities.map { entity ->
                val items = try {
                    jsonFormat.decodeFromString<List<NotaItemInput>>(entity.itemsJson)
                } catch (e: Exception) {
                    emptyList()
                }
                Nota(
                    id = entity.id,
                    referenceCode = entity.referenceCode,
                    customerName = entity.customerName,
                    phoneNumber = entity.phoneNumber,
                    address = entity.address,
                    customerLabel = entity.customerLabel,
                    date = entity.date,
                    total = entity.total,
                    status = normalizeNotaStatus(entity.status),
                    subtotal = entity.subtotal,
                    taxPercent = entity.taxPercent,
                    discount = entity.discount,
                    shippingCost = entity.shippingCost,
                    deposit = entity.deposit,
                    paymentMethod = normalizePaymentMethod(entity.paymentMethod),
                    notes = entity.notes,
                    items = items
                )
            }

            val container = BackupContainer(
                businessProfile = _businessProfile.value,
                invoiceConfig = _invoiceConfig.value,
                notas = fullNotasList
            )
            val prettyJson = Json { prettyPrint = true; ignoreUnknownKeys = true }.encodeToString(container)

            var writtenToPrimary = false

            // 1. Ekspor format ZIP (Lengkap dengan file gambar Logo & QRIS)
            try {
                com.eco.nota.utils.BackupUtils.exportToZip(
                    context = app,
                    notas = fullNotasList,
                    businessProfile = _businessProfile.value,
                    invoiceConfig = _invoiceConfig.value,
                    showToast = false
                )
            } catch (e: Exception) { }

            // 2. Ekspor format JSON standar
            // Target Utama: /storage/emulated/0/backups/NotaKasir/nota_backup.json (Overwrite selalu)
            try {
                val primaryDir = File("/storage/emulated/0/backups/NotaKasir")
                if (!primaryDir.exists()) primaryDir.mkdirs()
                val primaryFile = File(primaryDir, "nota_backup.json")
                primaryFile.writeText(prettyJson)
                writtenToPrimary = true
            } catch (e: Exception) {
                // Lanjut ke fallback canonical path
            }

            // Canonical Environment Path (External Storage Root)
            try {
                val envDir = File(android.os.Environment.getExternalStorageDirectory(), "backups/NotaKasir")
                if (!envDir.exists()) envDir.mkdirs()
                val envFile = File(envDir, "nota_backup.json")
                envFile.writeText(prettyJson)
                writtenToPrimary = true
            } catch (e: Exception) {
                // Ignore
            }

            // Cadangan App External Files (Selalu aman tanpa izin tambahan)
            try {
                val externalDir = File(app.getExternalFilesDir(null), "backups/NotaKasir")
                if (!externalDir.exists()) externalDir.mkdirs()
                File(externalDir, "nota_backup.json").writeText(prettyJson)
            } catch (e: Exception) { }

            // Cadangan App Internal Files
            try {
                val internalDir = File(app.filesDir, "backups/NotaKasir")
                if (!internalDir.exists()) internalDir.mkdirs()
                File(internalDir, "nota_backup.json").writeText(prettyJson)
            } catch (e: Exception) { }

            // Cadangan Public Documents Folder
            try {
                val publicDocs = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "NotaKasir")
                if (!publicDocs.exists()) publicDocs.mkdirs()
                File(publicDocs, "nota_backup.json").writeText(prettyJson)
            } catch (e: Exception) { }

            if (showToast) {
                withContext(Dispatchers.Main) {
                    val msg = if (writtenToPrimary) {
                        "✅ Auto-Backup (ZIP + Gambar & JSON) disimpan di:\n/storage/emulated/0/backups/NotaKasir/"
                    } else {
                        "✅ Auto-Backup Berhasil (${fullNotasList.size} nota & gambar tersimpan)"
                    }
                    Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            if (showToast) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "❌ Gagal Auto-Backup: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun restoreLatestAutoBackup(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()

                // 1. Coba pulihkan dari file .ZIP terlebih dahulu (karena memuat gambar lengkap)
                val candidateZipFiles = listOf(
                    File("/storage/emulated/0/backups/NotaKasir/nota_backup.zip"),
                    File(android.os.Environment.getExternalStorageDirectory(), "backups/NotaKasir/nota_backup.zip"),
                    File(app.getExternalFilesDir(null), "backups/NotaKasir/nota_backup.zip"),
                    File(app.filesDir, "backups/NotaKasir/nota_backup.zip"),
                    File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "NotaKasir/nota_backup.zip")
                )

                for (zipFile in candidateZipFiles) {
                    if (zipFile.exists() && zipFile.length() > 0) {
                        try {
                            val container = java.io.FileInputStream(zipFile).use { stream ->
                                com.eco.nota.utils.BackupUtils.importFromZip(app, stream)
                            }
                            if (container != null && (container.notas.isNotEmpty() || container.businessProfile != null)) {
                                importBackup(
                                    container = container,
                                    onSuccess = { onSuccess(container.notas.size) },
                                    onError = { onError(it) }
                                )
                                return@launch
                            }
                        } catch (e: Exception) {
                            // Coba file berikutnya jika terjadi masalah
                        }
                    }
                }

                // 2. Fallback: Coba pulihkan dari file .JSON jika ZIP tidak ditemukan
                val candidateFiles = listOf(
                    File("/storage/emulated/0/backups/NotaKasir/nota_backup.json"),
                    File(android.os.Environment.getExternalStorageDirectory(), "backups/NotaKasir/nota_backup.json"),
                    File(app.getExternalFilesDir(null), "backups/NotaKasir/nota_backup.json"),
                    File(app.filesDir, "backups/NotaKasir/nota_backup.json"),
                    File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "NotaKasir/nota_backup.json"),
                    // Fallback nama lama jika pernah dibuat
                    File(app.filesDir, "backups/nota_auto_backup.json"),
                    File(app.getExternalFilesDir(null), "backups/nota_auto_backup.json"),
                    File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS), "NotaKasir/Nota_AutoBackup_Terbaru.json"),
                    File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "NotaKasir/Nota_AutoBackup_Terbaru.json")
                )

                var backupJson: String? = null
                for (file in candidateFiles) {
                    if (file.exists() && file.length() > 0) {
                        try {
                            backupJson = file.readText()
                            if (!backupJson.isNullOrBlank()) break
                        } catch (e: Exception) {
                            // Coba file berikutnya jika terkendala izin akses
                        }
                    }
                }

                if (backupJson.isNullOrBlank()) {
                    onError("Belum ada file backup di /storage/emulated/0/backups/NotaKasir/ (nota_backup.zip atau nota_backup.json)")
                    return@launch
                }

                val container = com.eco.nota.utils.BackupUtils.importFromJson(app, backupJson)
                if (container != null && (container.notas.isNotEmpty() || container.businessProfile != null)) {
                    importBackup(
                        container = container,
                        onSuccess = { onSuccess(container.notas.size) },
                        onError = { onError(it) }
                    )
                } else {
                    onError("File backup kosong atau rusak.")
                }
            } catch (e: Exception) {
                onError("Gagal memulihkan: ${e.localizedMessage}")
            }
        }
    }
}
