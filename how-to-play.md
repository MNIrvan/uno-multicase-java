# Panduan Bermain Game UNO Multiplayer

Dokumen ini berisi langkah-langkah untuk menyiapkan, menjalankan, dan bermain game UNO Multiplayer berbasis Java.

## 1. Persiapan Database (Laragon/XAMPP)
1. Buka **Laragon** (atau XAMPP) dan pastikan **MySQL** sudah berjalan (Start).
2. Anda **TIDAK PERLU** membuat database secara manual, karena `DBConnection.java` akan otomatis membuat database `db_game_tournament` dan seluruh tabelnya saat server dijalankan pertama kali.
3. Pastikan username MySQL Anda adalah `root` dan password kosong (secara default di Laragon/XAMPP).

## 2. Menjalankan Server
1. Buka project di IDE Anda (misal IntelliJ IDEA, Eclipse, atau VSCode).
2. Cari file `src/main/java/com/game/server/ServerApp.java`.
3. Jalankan file tersebut (Run `ServerApp.main()`).
4. Pastikan di console/terminal muncul tulisan:
   ```
   Database connected successfully.
   Server started on port 8080. Waiting for players...
   ```

## 3. Menjalankan Klien (Pemain)
1. Buka file `src/main/java/com/game/client/ClientGUI.java`.
2. Jalankan file tersebut. Sebuah jendela aplikasi berbasis Java Swing akan muncul.
3. Masukkan `Username` Anda dan klik **Connect**.
4. Anda akan masuk ke halaman **Lobby**. Daftar pemain yang sudah connect akan muncul di sini.
5. Klik tombol **Ready**. 
6. Game akan otomatis dimulai setelah ada minimal 2 pemain yang terhubung dan menekan **Ready**.
7. Ulangi langkah 1-5 untuk pemain lainnya (maksimal 4 pemain).

## 4. Cara Bermain (In-Game)
- **Top Card**: Menunjukkan kartu yang ada di tengah meja saat ini.
- **Kartu di Tangan**: Akan muncul sebagai tombol-tombol di bagian bawah layar.
- **Mendapat Giliran**: Jika tulisan *Turn* menunjukkan nama Anda, Anda dapat mengklik salah satu kartu di tangan yang warnanya atau angkanya cocok dengan *Top Card*.
- **Draw Card**: Jika Anda tidak punya kartu yang cocok, tekan tombol **Draw Card** untuk mengambil 1 kartu baru dari tumpukan, lalu giliran akan berpindah ke pemain berikutnya.
- **Aturan Call UNO (1 Kartu)**: Jika kartu Anda tersisa 2, dan Anda akan mengeluarkan kartu sehingga sisa 1, **JANGAN LUPA** tekan tombol **Call UNO!** terlebih dahulu. Jika tidak, saat kartu sisa 1 Anda otomatis akan dihukum menarik 2 kartu tambahan.
- Game berakhir saat salah satu pemain menghabiskan seluruh kartunya. Hasil akan tersimpan di database MySQL otomatis.
