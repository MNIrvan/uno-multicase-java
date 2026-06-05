Alasan mengapa device (perangkat) lain tidak bisa terkoneksi dan mendapat pesan "Connection refused: connect" adalah karena sebelumnya kode klien memiliki pengaturan IP Server yang dikunci (hardcoded) ke alamat 127.0.0.1 (localhost).

Alamat 127.0.0.1 hanya berlaku jika server dan klien dijalankan di komputer yang sama. Jika ada orang lain yang menggunakan laptop/HP berbeda mencoba terhubung ke 127.0.0.1, maka laptop/HP mereka akan mencari server di dalam device mereka sendiri, bukan di komputer Anda (sang host). Akibatnya, koneksi ditolak (Connection refused).

Solusinya: Saya baru saja mengubah ClientGUI.java dan menambahkan kolom Server IP di layar Login. Saya juga sudah memperbarui dokumen 

how-to-play.md

Langkah yang harus Anda lakukan sekarang agar device lain bisa bergabung:

Di komputer Host (komputer Anda yang menjalankan ServerApp), Anda harus mencari tahu IP Address LAN Anda (biasanya diawali dengan 192.168.x.x).
Cara mengeceknya: Buka terminal/cmd, lalu ketik ipconfig dan lihat baris IPv4 Address.
Beritahukan alamat IP tersebut kepada pemain yang berada di perangkat lain.
Di perangkat pemain lain tersebut, saat menjalankan 

ClientGUI.java
, minta mereka untuk menghapus 127.0.0.1 di kolom Server IP dan menggantinya dengan IP LAN komputer Anda (misal: 192.168.1.5).
Pastikan juga Firewall di komputer Anda (sang host) tidak memblokir port 8080 untuk jaringan lokal. Jika masih gagal, matikan sementara (Turn off) Windows Defender Firewall untuk jaringan Private/Local.