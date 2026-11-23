# Changelog — Nota Kasir Pro

Catatan lengkap perkembangan dan pembaruan aplikasi **Nota Kasir Pro** dari awal pembuatan hingga versi terbaru.

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
