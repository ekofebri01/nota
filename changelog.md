# Changelog — Nota Kasir Pro

Catatan lengkap perkembangan dan pembaruan aplikasi **Nota Kasir Pro** dari awal pembuatan hingga versi terbaru.

---

## [v1.8.2] — Pembaruan Aplikasi (2026-11-28)
- fix: backup via SAF, persempit izin storage dan FileProvider

---

## [v1.8.1] — Auto-Add Produk Baru ke Database Master (2026-11-27)
- **Auto-Tambah Produk ke Database**: Saat Anda mengisi atau mengetik nama produk baru pada formulir pembuatan nota yang belum terdaftar di database, sistem kini secara otomatis menyimpan produk tersebut ke master database produk agar tersimpan permanen dan dapat digunakan kembali di masa mendatang.

---

## [v1.8.0] — Database Master Produk & Optimasi Kecepatan Ketik (2026-11-27)
- **Database Master Produk / Barang**: Menambahkan tabel lokal `products` di Room Database (versi 3, migrasi non-destruktif `MIGRATION_2_3`), lengkap dengan manajemen CRUD (tambah barang, edit harga/satuan/kategori/kode, dan hapus barang).
- **Fitur Impor dari Riwayat Nota**: Fitur otomatis mengekstrak semua barang unik dari nota-nota sebelumnya langsung ke dalam Master Database Produk dengan satu klik.
- **Pilih Produk Langsung**: Penambahan tombol `Pilih Master` pada form pembuatan nota untuk memilih barang langsung dari database produk dengan auto-fill nama, satuan, dan harga jual standar.
- **Optimasi Kecepatan Mengetik (Bebas Lag)**: Memperbaiki penanganan `BringIntoViewRequester` dan isolasi recomposition menggunakan `derivedStateOf` pada kalkulasi total/subtotal, sehingga pengetikan nama pelanggan dan nama barang berjalan mulus tanpa jeda/stutter.
- **Dukungan Backup & Restore**: Data master produk kini otomatis disertakan dalam file cadangan JSON dan file ZIP.

---

## [v1.7.7] — Pembaruan Aplikasi (2026-11-27)
- build(deps): update GitHub Actions to latest versions

---

## [v1.7.6] — Pembaruan Aplikasi (2026-11-27)
- ci: sanitize signing credentials in auto-release

---

## [v1.7.5] — Optimasi Kode & Penyempurnaan Aplikasi
- **Pembaruan Ikon & Navigasi**: Memperbarui penggunaan ikon ke varian `Icons.AutoMirrored` dan membersihkan variabel/parameter navigasi yang tidak terpakai pada `NotaApp`, `CreateNotaScreen`, dan `UpdateDialog`.
- **Null-Safety Generator Struk**: Menghapus assertion `!!` pada pembacaan URI gambar QRIS di `ReceiptBitmapGenerator` agar rendering struk lebih aman dan bebas warning compiler.

---

## [v1.7.4] — Pengaturan Floating Window & Animasi
- **Tampilan Floating Window Pengaturan**: Halaman **Pengaturan** kini tampil sebagai jendela melayang (*floating window*) modern dengan sudut membulat, efek bayangan, dan latar beranda yang diredupkan secara elegan.
- **Animasi Halus Buka & Tutup**: Menambahkan animasi spring pop-in halus (*scale + slide + fade scrim*) saat jendela Pengaturan muncul maupun ditutup, mendukung ketuk luar jendela dan tombol kembali sistem.

---

## [v1.7.3] — Toggle Lunas / Belum Lunas di Menu Riwayat Nota
- **Toggle Status Pembayaran di Menu Kartu**: Menambahkan menu cepat **Tandai Sudah Lunas / Tandai Belum Lunas** langsung pada tombol **Menu (⋮)** di setiap kartu **Riwayat Nota**, sehingga status transaksi dan **Ringkasan Keuangan** langsung terupdate instan tanpa harus membuka halaman Detail Nota.

---

## [v1.7.2] — Auto Build Release APK & Auto Changelog / What's New
- **GitHub Actions Auto-Release APK**: Menambahkan workflow `.github/workflows/auto-release.yml` untuk otomatis membuild APK release (`Nota-Kasir-Pro-vX.Y.Z.apk`) dan menerbitkannya ke **GitHub Releases** setiap kali ada push ke branch `main`/`master` atau dijalankan manual lewat `workflow_dispatch`.
- **Auto-Changelog & Auto-What's New**: Menambahkan skrip `scripts/auto_release_notes.py` untuk otomatis menaikkan versi (jika tag versi sudah ada), mencatat poin perubahan dari git commit ke **`changelog.md`**, serta menyuntikkan pembaruan ke **`app/src/main/assets/whatsnew.json`**.
- **In-App What's New Dinamis**: Halaman **Catatan Pembaruan (What's New)** di Pengaturan kini membaca langsung dari `assets/whatsnew.json`, sehingga setiap rilis otomatis dari GitHub langsung memuat riwayat pembaruan terbaru di dalam aplikasi.
- **Dukungan Signing Upload Key Opsional via GitHub Secrets**: Workflow otomatis menggunakan fallback `debug.keystore` atau memakai upload key (`my-upload-key.jks`) bila secrets `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, dan `SIGNING_KEY_PASSWORD` diisi di GitHub.

---

## [v1.7.1] — Menu & Checklist Pesanan Sudah Diambil
- **Menu & Toggle Pengambilan di Riwayat Nota**: Setiap kartu pada **Riwayat Nota** kini memiliki toggle checklist cepat (**Sudah Diambil / Belum Diambil**) serta tombol **Menu (⋮)** untuk mengubah status pengambilan, membuka checklist item pesanan, atau membuka detail struk.
- **Status Pengambilan di Detail Nota**: Menambahkan kontrol status pengambilan pesanan di halaman **Detail Nota** yang tersinkron langsung dengan Riwayat Nota.
- **Filter & Pencarian Status Pengambilan**: Menambahkan filter **Belum Diambil** dan **Sudah Diambil** di baris filter status beranda serta dukungan kata kunci pencarian `sudah diambil` / `belum diambil`.
- **Migrasi Database Aman (Room v2) & Auto-Backup**: Penambahan kolom `isPickedUp` dilengkapi migrasi database otomatis tanpa menghapus data transaksi lama, dan tersimpan penuh di backup ZIP/JSON.

---

## [v1.7.0] — Default Tampilan Awal Beranda
- **Pengaturan Default Tampilan Riwayat**: Menambahkan menu **Tampilan Beranda** di halaman **Pengaturan** untuk memilih tampilan awal otomatis saat aplikasi dibuka (`Harian`, `Bulanan`, `Tahunan`, atau `All Time`).
- **Sinkronisasi Otomatis**: Pilihan tersimpan permanen di preferensi perangkat (`SharedPreferences`) dan langsung diterapkan ke filter beranda serta Ringkasan Keuangan.

---

## [v1.6.9] — Checklist Pesanan Sudah Dibuat
- **Checklist Status Pesanan per Item**: Di bagian **Riwayat Nota** (dan halaman **Detail Nota**), kini tersedia checklist per item pesanan untuk menandai mana pesanan yang **Sudah Dibuat** dan mana yang **Belum**.
- **Badge Progres pada Kartu Nota**: Setiap kartu riwayat menampilkan ringkasan progres (contoh: `Pesanan 0/3`, `Pesanan 1/3`, hingga `Dibuat 3/3`), lengkap dengan tombol cepat **Tandai Semua / Reset Belum**.
- **Kompatibel Data Lama & Backup**: Status `isDone` tersimpan otomatis di database lokal Room dan ikut tercadangkan di Auto-Backup ZIP/JSON tanpa mengganggu data lama maupun tampilan gambar struk pelanggan.

---

## [v1.6.8] — Header Compact & Fitur Pencarian Cepat
- **Pencarian Pelanggan, Barang & Nota**: Kolom pencarian multi-kata di layar utama untuk memfilter riwayat berdasarkan nama pelanggan, barang yang dibeli, kode nota (`INV-...`), nomor HP, alamat, label pelanggan, catatan, tanggal, dan metode pembayaran.
- **Ringkasan Otomatis & Item Terbeli**: Saat mencari nama pelanggan, kartu riwayat menampilkan daftar barang yang dibeli hingga 3 baris (lengkap dengan jumlah/satuannya) dan Ringkasan Keuangan otomatis menghitung total omzet lunas & tagihan hasil pencarian.
- **Tampilan Header & Ringkasan Lebih Compact**: Menghilangkan jarak kosong berlebih di atas layar (double window insets pada nested Scaffold), merampingkan bar atas menjadi `48dp`, serta merapatkan kartu Ringkasan Keuangan dan baris filter agar lebih banyak riwayat nota terlihat dalam satu layar.

---

## [v1.6.7] — Filter Periode Geser & Update 100% In-App
- **Filter Periode Geser Maju / Mundur**: Filter `Harian`, `Bulanan`, dan `Tahunan` dilengkapi bar periode dengan tombol panah `<` / `>`, ketuk untuk `Reset` ke periode berjalan, serta gestur geser kiri/kanan untuk berpindah antar periode (*Hari Ini / Kemarin / Besok*, *Bulan Kemarin*, *Tahun Kemarin*).
- **Update 100% In-App (Unduh hingga Instal)**: Seluruh alur pembaruan berjalan langsung di dalam aplikasi—cek rilis GitHub, unduh APK dengan progres animasi & ukuran MB (aman disembunyikan ke banner beranda dan bisa dibatalkan), panduan izin instalasi (*Instal apl yang tidak dikenal*), pemasangan 1-ketuk, dan pembersihan file sementara otomatis.

---

## [v1.6.6] — Ikon Pembayaran, Filter Bayar & Header Fixed
- **Ikon & Filter Jenis Pembayaran**: Ikon kartu pada riwayat nota kini menampilkan kategori metode bayar (**Tunai**, **Transfer**, **QRIS**, **E-Wallet**) dengan warna status transaksi, ditambah baris filter cepat jenis pembayaran di layar utama.
- **Ringkasan Omzet & Filter Fixed**: Bagian Header, Ringkasan Keuangan, seluruh baris filter, dan judul Riwayat Nota dibuat tetap (*sticky/fixed*) di atas saat daftar riwayat nota digulir.
- **Auto-Cek Pembaruan Saat App Dibuka**: Pengecekan rilis terbaru dari GitHub berjalan otomatis di latar belakang tepat saat aplikasi pertama kali dibuka (dengan retry otomatis jika koneksi baru siap).

---

## [v1.6.5] — Perhitungan Omzet dari Subtotal Murni
- **Omzet Murni Subtotal Barang**: Perhitungan **Total Omzet Lunas** dan **Total Tagihan** pada Ringkasan Keuangan kini menghitung Subtotal murni item/barang saja—biaya ongkir dan diskon tidak lagi memengaruhi angka omzet.

---

## [v1.6.4] — Animasi Unduh APK, Bebas Ongkir & Penataan Satuan
- **Animasi Progres Unduh & Auto-Cleanup**: Bar progres unduhan APK real-time lengkap dengan persentase & MB, dialog siap instal, dan pembersihan APK lama otomatis.
- **Ringkasan Keuangan Bebas Ongkir**: Pemisahan biaya pengiriman (ongkir) dari perhitungan omzet agar laporan pendapatan penjualan lebih akurat.
- **Penataan Pilihan Satuan Barang**: Perbaikan tata letak tombol hapus pada chip satuan barang saat membuat/mengedit nota agar tidak tertekan secara tidak sengaja.

---

## Rilis Awal hingga v1.6.3 — Pondasi Nota Kasir Pro Edition
- **Pembuatan & Manajemen Nota Cepat**: Form pembuatan/edit nota lengkap dengan detail pelanggan, daftar barang dinamis (qty, satuan, harga), diskon, ongkir, pajak, DP/titipan, catatan, metode pembayaran (Tunai, Transfer Bank, QRIS, E-Wallet), serta status **Lunas** / **Tagihan**.
- **Rekomendasi Produk & Pelanggan Cerdas**: Autocomplete nama pelanggan dan daftar produk yang otomatis menyesuaikan posisi di atas keyboard, ditambah fitur **Ambil dari Kontak** HP.
- **Generator Struk HD & Live Preview**: Mendukung **Mode Struk Kasir (Thermal 58mm/80mm)** dan **Mode Digital Pro**, lengkap dengan tampilan **QRIS Pembayaran** di atas struk, **Simpan ke Galeri**, dan **Bagikan ke WhatsApp** tanpa gagal.
- **Profil Bisnis & Pengaturan Ringkas**: Pengaturan logo toko, nama, alamat, rekening bank, info E-Wallet, dan gambar QRIS.
- **Penyimpanan 100% Offline (Room Database)**: Seluruh transaksi tersimpan cepat di perangkat tanpa ketergantungan internet, dengan urutan transaksi terbaru di atas.
- **Auto-Backup Real-Time & 1-Klik Pulihkan**: Pencadangan otomatis ke folder lokal (`/backups/NotaKasir/nota_backup.zip` lengkap dengan logo & QRIS serta `nota_backup.json`) setiap kali data berubah, plus impor/ekspor ZIP & JSON.
- **Integrasi Cek Pembaruan GitHub Releases**: Pemeriksaan pembaruan aplikasi otomatis dan manual langsung dari rilis GitHub repository.
