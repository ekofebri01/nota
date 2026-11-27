# Nota Kasir Pro

Aplikasi kasir & manajemen nota transaksi Android berbasis **Kotlin** dan **Jetpack Compose (Material 3)** yang cepat, modern, dan **100% offline**—dirancang untuk mencatat transaksi, memantau omzet murni, mengelola status pengerjaan pesanan, mencetak/membagikan struk HD (termasuk QRIS), hingga melakukan pembaruan aplikasi langsung di dalam aplikasi via GitHub Releases.

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024--34)-10B981?style=flat-square)
![Language](https://img.shields.io/badge/Kotlin-1.9.24-6366F1?style=flat-square)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-3B82F6?style=flat-square)
![Version](https://img.shields.io/badge/Version-v1.7.7-F59E0B?style=flat-square)

---

## Fitur Utama

### 1. Pencatatan Nota & Manajemen Transaksi
- **Buat & Edit Nota Cepat**: Mendukung banyak item dalam satu transaksi dengan jumlah, satuan custom/preset, dan harga per item.
- **Perhitungan Lengkap**: Dukungan diskon, ongkos kirim, pajak, DP/titipan, catatan tambahan, dan nomor referensi otomatis (`INV-yyMMdd-XXX`).
- **Autocomplete Cerdas & Kontak HP**: Rekomendasi otomatis pelanggan & produk dari transaksi sebelumnya, serta tombol **Ambil Kontak** untuk mengisi nama dan nomor telepon langsung dari daftar kontak perangkat.
- **Status & Metode Pembayaran**: Mendukung status **Lunas** dan **Tagihan** dengan ikon kategori pembayaran (**Tunai**, **Transfer**, **QRIS**, **E-Wallet**).

### 2. Ringkasan Keuangan, Pencarian & Filter
- **Omzet Murni Subtotal Barang**: **Total Omzet Lunas** dan **Total Tagihan** dihitung dari subtotal murni produk (tidak tercampur ongkir maupun diskon).
- **Header & Filter Fixed**: Ringkasan keuangan, kolom pencarian, dan seluruh filter tetap menempel di atas saat daftar riwayat digulir.
- **Pencarian Multi-Kata**: Cari riwayat berdasarkan nama pelanggan, barang yang dibeli, nomor nota, nomor HP, alamat, catatan, hingga metode pembayaran—sekaligus menampilkan item yang dibeli beserta ringkasan omzetnya.
- **Filter Periode Geser Maju/Mundur**: Filter `Harian`, `Bulanan`, `Tahunan`, dan `All Time` yang bisa digeser mundur/maju (mis. Hari Ini ↔ Kemarin, Bulan Ini ↔ Bulan Kemarin) via tombol panah maupun gestur swipe.
- **Default Tampilan Awal**: Atur tampilan beranda bawaan saat aplikasi dibuka (`Harian`, `Bulanan`, `Tahunan`, atau `All Time`) melalui menu **Pengaturan**.

### 3. Checklist Status Pesanan Dibuat & Diambil
- **Progress Pesanan per Nota**: Pantau berapa item dalam satu nota yang sudah selesai dibuat (`Pesanan 1/3` → `Dibuat 3/3`).
- **Tandai per Item**: Buka checklist langsung dari kartu di **Riwayat Nota** atau dari **Detail Nota** untuk mencentang item yang **Sudah Dibuat** maupun yang **Belum** (dilengkapi tombol *Tandai Semua* / *Reset Belum*), tanpa mengubah tampilan struk pelanggan.
- **Menu & Checklist Status Pengambilan**: Setiap kartu **Riwayat Nota** dan halaman **Detail Nota** dilengkapi toggle checklist **Sudah Diambil / Belum Diambil** serta menu aksi cepat (⋮) dan filter khusus (**Belum Diambil** / **Sudah Diambil**).

### 4. Generator Struk HD & QRIS
- **2 Mode Tampilan Struk**: **Mode Struk Kasir (Thermal)** dan **Mode Digital Pro** dengan live preview.
- **QRIS & Info Pembayaran Toko**: Logo toko, alamat, rekening bank, E-Wallet, serta gambar **QRIS HD** di bagian atas struk agar mudah langsung dipindai pelanggan.
- **Simpan & Bagikan Instan**: Ekspor gambar struk beresolusi tajam ke Galeri atau kirim langsung ke WhatsApp.

### 5. Penyimpanan Lokal & Auto-Backup Real-Time
- **100% Offline (Room Database)**: Semua data tersimpan cepat di perangkat tanpa ketergantungan koneksi internet.
- **Auto-Backup Overwrite ZIP + JSON**: Setiap perubahan nota atau profil bisnis otomatis mencadangkan file `nota_backup.zip` (termasuk foto logo & QRIS) dan `nota_backup.json` ke `/storage/emulated/0/backups/NotaKasir/`.
- **Pulihkan 1-Klik**: Impor dan pulihkan seluruh database beserta gambar toko kapan saja.

### 6. Pembaruan 100% In-App (GitHub Releases)
- **Auto-Check Saat Dibuka**: Aplikasi otomatis mengecek rilis terbaru dari repository GitHub (`ekofebri01/nota`).
- **Unduh hingga Instal di Aplikasi**: Progres unduhan real-time (bisa disembunyikan ke banner beranda dan dibatalkan), pengecekan izin *Instal apl yang tidak dikenal*, instalasi 1-ketuk, dan pembersihan file APK sementara otomatis.

---

## Teknologi & Arsitektur

| Komponen | Teknologi |
| :--- | :--- |
| **Bahasa** | Kotlin (`1.9.24`) |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Arsitektur** | MVVM (`AndroidViewModel`, `StateFlow`) |
| **Database Lokal** | Room Database (`2.6.1`) + KSP |
| **Serialisasi & Backup** | Kotlinx Serialization JSON (`1.6.3`), `ZipOutputStream` |
| **Image Loading & Crop** | Coil (`2.6.0`), Android Image Cropper (`4.3.0`) |
| **Min / Target SDK** | API 24 (Android 7.0) / API 34 (Android 14) |
| **Application ID** | `com.eco.nota` |

---

## Struktur Proyek

```text
.github/workflows/
└── auto-release.yml              # GitHub Actions auto-build APK, changelog & release
scripts/
└── auto_release_notes.py         # Sinkronisasi versi, changelog.md & whatsnew.json
app/src/main/
├── assets/
│   └── whatsnew.json             # Sumber data dinamis "Catatan Pembaruan (What's New)"
└── java/com/eco/nota/
    ├── MainActivity.kt               # Entry point (edge-to-edge)
    ├── models/
    │   ├── NotaModels.kt             # Data model Nota, NotaItemInput, WhatsNewEntry, BusinessProfile
    │   └── NotaViewModel.kt          # Room DAO, StateFlow, Auto-Backup, In-App Updater state
    ├── ui/
    │   ├── components/               # Komponen UI reusable & UpdateDialog in-app
    │   ├── navigation/NotaApp.kt     # Navigasi utama & Top Bar
    │   ├── screens/
    │   │   ├── SplashScreen.kt       # Layar pembuka
    │   │   ├── HomeScreen.kt         # Ringkasan keuangan, pencarian, filter & riwayat nota
    │   │   ├── CreateNotaScreen.kt   # Form buat/edit nota & preview struk langsung
    │   │   ├── DetailNotaScreen.kt   # Detail nota, ubah status, checklist & ekspor struk
    │   │   └── SettingsScreen.kt     # Profil toko, tampilan default, backup/restore & What's New
    │   └── theme/                    # Tema, warna, dan tipografi
    └── utils/
        ├── BackupUtils.kt            # Pencadangan & pemulihan ZIP/JSON
        ├── ImageUtils.kt             # Simpan ke galeri & share intent
        ├── ReceiptBitmapGenerator.kt # Render gambar struk HD (Thermal & Digital Pro)
        └── UpdateManager.kt          # GitHub Releases API, APK downloader & installer
```

---

## Cara Build dari Source

### Prasyarat
- Android Studio Jellyfish / Koala atau lebih baru (atau Android SDK + Gradle CLI)
- JDK 17+ / JDK 21
- Android SDK Platform 34 (`compileSdk = 34`, `minSdk = 24`)

### Perintah Build
```bash
# Build APK Debug
gradle assembleDebug

# Build APK Release
gradle assembleRelease
```

Hasil build APK release tersedia di:
```text
app/build/outputs/apk/release/app-release.apk
```

---

## Auto-Build Release APK & Auto-Changelog (GitHub Actions)

Repository ini dilengkapi workflow [`.github/workflows/auto-release.yml`](./.github/workflows/auto-release.yml) yang berjalan otomatis saat **push** ke branch `main`/`master` atau secara manual lewat tab **Actions → Run workflow**:

1. **Deteksi / Kenaikan Versi Otomatis**: Jika tag `v<versionName>` belum ada di GitHub, workflow memakai versi saat ini di `app/build.gradle.kts`. Jika tag sudah ada, versi patch dan `versionCode` otomatis dinaikkan (`+1`).
2. **Auto-Update `changelog.md` & In-App `What's New`**: Skrip [`scripts/auto_release_notes.py`](./scripts/auto_release_notes.py) otomatis mengumpulkan daftar commit sejak tag terakhir, memperbarui [`changelog.md`](./changelog.md), memperbarui [`app/src/main/assets/whatsnew.json`](./app/src/main/assets/whatsnew.json), lalu melakukan commit `[skip ci]`.
3. **Build & Publish GitHub Release**: Workflow menjalankan `gradle assembleRelease`, mengunggah `Nota-Kasir-Pro-vX.Y.Z.apk` sebagai asset di **GitHub Releases** lengkap dengan catatan rilisnya (langsung terbaca oleh fitur In-App Update di dalam aplikasi).

### Opsional: Signing APK Release dengan Upload Key (`my-upload-key.jks`)
Secara default workflow memakai `debug.keystore.base64` sebagai fallback agar build langsung berhasil. Untuk menandatangani APK dengan upload key produksi Anda di GitHub Actions, tambahkan 4 **Repository Secrets** berikut di GitHub (**Settings → Secrets and variables → Actions**):
- `SIGNING_KEYSTORE_BASE64`: Isi file `.jks` yang di-encode Base64 (`base64 -w 0 my-upload-key.jks`)
- `SIGNING_STORE_PASSWORD`: Password keystore
- `SIGNING_KEY_ALIAS`: Alias key
- `SIGNING_KEY_PASSWORD`: Password key

---

## Catatan Pembaruan (Changelog)

Lihat riwayat lengkap perubahan dari rilis awal hingga versi terbaru (**v1.7.7**) di file [`changelog.md`](./changelog.md).
