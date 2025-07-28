# Inventori Toko | UAS Pemrograman UMBanten
Aplikasi Inventori Toko sederhana berbasis Android menggunakan Kotlin.

## DATA DIRI
---
**Nama**: Bayu Sebastian
**NIM**: 220320001
**Prodi**: Informatika

## Fitur Aplikasi
---
Aplikasi ini dikhususkan untuk customer dengan menyediakan berbagai fitur inti untuk melihat produk, keranjang belanja, dan riwayat pembelian, serta fungsionalitas autentikasi pengguna:

**Autentikasi Pengguna**:
- Registrasi: Pengguna baru dapat membuat akun dengan username, email, dan password.
- Login: Pengguna yang sudah terdaftar dapat masuk ke aplikasi.
- Lupa Password: Memungkinkan pengguna untuk meminta token reset password melalui email.
- Reset Password: Memungkinkan pengguna untuk mengatur ulang password menggunakan token yang diterima.
- Logout: Mengakhiri sesi pengguna dan menghapus token autentikasi.

**Menampilkan Produk**:
- Daftar Produk: Menampilkan daftar semua produk yang tersedia dengan nama, harga, stok, dan gambar.
- Detail Produk: Menampilkan informasi lebih rinci tentang produk tertentu.

**Keranjang Belanja**:
- Tambah ke Keranjang: Pengguna dapat menambahkan produk dari daftar produk ke keranjang belanja mereka. Jika produk sudah ada, kuantitas akan diperbarui.
- Lihat Keranjang: Menampilkan semua item di keranjang dengan kuantitas dan subtotal.
- Perbarui Kuantitas: Memungkinkan pengguna untuk menambah atau mengurangi kuantitas produk di keranjang.
- Hapus Item dari Keranjang: Menghapus produk tertentu dari keranjang.
- Kosongkan Keranjang: Menghapus semua item dari keranjang.

**Proses Pembelian**:
- Beli Langsung: Melakukan pembelian tanpa dimasukkan ke keranjang terlebih dahulu.
- Checkout: Memproses pembelian item yang dipilih, mengurangi stok produk, dan mencatat transaksi.
- Riwayat Pembelian: Menampilkan daftar semua transaksi dan produk yang pernah dibeli oleh pengguna.


## Teknologi yang Digunakan
---
**Backend**:
Rest-Api -> https://github.com/BAYBAS00/inventoriToko-api.git

**Frontend (Aplikasi Android)**:
- Platform Android: Sistem operasi target.
- Kotlin: Bahasa pemrograman utama.
- Jetpack Compose: Toolkit UI deklaratif.
- MVVM (Model-View-ViewModel): Pola arsitektur.
- Retrofit: HTTP client untuk API calls.
- Gson: Library untuk parsing JSON.
- OkHttp: HTTP client yang efisien (digunakan oleh Retrofit).
- Coil: Library pemuatan gambar.
- Preferences DataStore: Untuk penyimpanan data persisten (token autentikasi).
- Android Navigation Component: Untuk navigasi antar layar.
- Kotlin Coroutines: Untuk manajemen asinkron.


## Alur Login & Register
---
<img src="ss/UAS-PEMROGRAMAN-4.drawio.png" />


## UI Aplikasi pada Emulator
---
OTENTIKASI
---
**Login**
<img src="ss/login.png" width="400"/>

**Register**
<img src="ss/regis.png" width="400"/>

**Lupa Passwword**
<img src="ss/lupa-pass.png" width="400"/>

**Reset Password**
<img src="ss/reset-pass.png" width="400"/>

INVENTORI
---
**Daftar Produk**
<img src="ss/daftar-produk.png" width="400"/>

**Detail Produk**
<img src="ss/detail-produk.png" width="400"/>

**Keranjang**
<img src="ss/keranjang.png" width="400"/>

**Checkout**
<img src="ss/cekot.png" width="400"/>

**Pembayaran**
<img src="ss/pembayaran.png" width="400"/>

**Riwayat Pembelian**
<img src="ss/riwayat-beli.png" width="400"/>


### Note
---
Ubah code "0.0.0.0" pada file Constants.kt dan network_security_config.xml dengan ip address device anda sendiri.


