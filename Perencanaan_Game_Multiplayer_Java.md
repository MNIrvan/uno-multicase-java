# Dokumen Perencanaan Pengembangan Game: Multiplayer Turn-Based Tournament

Dokumen ini merinci rencana pengembangan aplikasi game (seperti Uno atau Trivia) yang mendukung 4 pemain secara real-time menggunakan Java Socket (TCP) dan database MySQL di Laragon.

## 1. Spesifikasi Teknis
- **Bahasa Pemrograman:** Java (JDK 11 atau lebih baru)
- **Protokol Jaringan:** TCP/IP (ServerSocket & Socket)
- **Database:** MySQL (dikelola melalui Laragon)
- **Library Utama:** - `mysql-connector-j` (untuk koneksi JDBC)
  - `Thread` / `ExecutorService` (untuk Multi-threading)

## 2. Struktur Database (MySQL)
Nama Database: `db_game_tournament`

### Tabel `users`
Menyimpan profil pemain.
- `id` (INT, PK, AI)
- `username` (VARCHAR(50), Unique)
- `password` (VARCHAR(255))
- `total_win` (INT, Default 0)

### Tabel `matches`
Mencatat setiap sesi permainan yang selesai.
- `id` (INT, PK, AI)
- `played_at` (TIMESTAMP, Default NOW)
- `winner_id` (INT, FK ke users.id)

### Tabel `match_details`
Statistik individu per pertandingan.
- `id` (INT, PK, AI)
- `match_id` (INT, FK ke matches.id)
- `user_id` (INT, FK ke users.id)
- `score` (INT)

## 3. Arsitektur Kelas Java

### Sisi Server (Backend Logic)
- **`ServerApp.java`**: Titik masuk utama, inisialisasi `ServerSocket`, dan manajemen database.
- **`ClientHandler.java`**: Menangani input/output untuk satu klien individu (Thread per client).
- **`GameEngine.java`**: Mengelola logika permainan (kartu/pertanyaan), validasi giliran, dan sinkronisasi status (State Management).
- **`DBConnection.java`**: Mengurus Query SQL menggunakan JDBC.

### Sisi Klien (Frontend Logic)
- **`ClientApp.java`**: Menghubungkan ke server dan menangani input user (Keyboard/UI).
- **`ServerListener.java`**: Thread yang selalu aktif mendengarkan pesan dari server (Broadcast) untuk memperbarui layar secara real-time.

## 4. Protokol Komunikasi (Format Pesan)
Data dikirim dalam format String dengan pemisah `|` agar mudah di-parsing oleh server maupun klien.

- **Contoh Login:** `LOGIN|username|password`
- **Contoh Aksi:** `ACTION|PLAY_CARD|RED_7`
- **Contoh Broadcast:** `STATE|CURRENT_TURN|Player2`
- **Contoh Selesai:** `GAMEOVER|WINNER|Player3|SCORE|500`

## 5. Rencana Tahapan (Roadmap)

### Minggu 1: Fondasi Jaringan & Database
- [ ] Setup database di Laragon.
- [ ] Implementasi class `DBConnection` di Java.
- [ ] Membuat server dasar yang bisa menerima koneksi dari 4 klien secara bersamaan (Multi-threaded).

### Minggu 2: Mekanisme Giliran (Turn Control)
- [ ] Implementasi logika "Waiting Room" (Game dimulai hanya jika pemain = 4).
- [ ] Membuat sistem antrean giliran (Player 1 -> Player 2 -> dst).
- [ ] Validasi aksi: Menolak input jika bukan giliran pemain tersebut.

### Minggu 3: Logika Game & Broadcast
- [ ] Membuat mekanisme permainan (misal: pengacakan kartu atau soal trivia).
- [ ] Mengimplementasikan fungsi `broadcast()` di server untuk mengirim update ke semua klien.
- [ ] Menangani pemain yang tiba-tiba terputus (Disconnect handling).

### Minggu 4: Penyimpanan Data & Polishing
- [ ] Menyimpan hasil akhir pertandingan ke MySQL secara otomatis saat game selesai.
- [ ] (Opsional) Implementasi GUI menggunakan JavaFX atau Swing.
- [ ] Final Testing dan perbaikan bug.

---
*Dibuat untuk pengembangan projek Java Socket & MySQL.*
