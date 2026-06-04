package com.game.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    // Koneksi dasar ke MySQL (tanpa nama database spesifik terlebih dahulu)
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "db_game_tournament";
    private static final String URL = BASE_URL + DB_NAME;
    
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // 1. Buat Database otomatis jika belum ada
                try (Connection tempConn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                     Statement stmt = tempConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                }

                // 2. Hubungkan ke database db_game_tournament
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully.");
                
                // 3. Inisialisasi tabel secara otomatis
                initializeTables();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void initializeTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "total_win INT DEFAULT 0" +
                ")";
                
        String createMatches = "CREATE TABLE IF NOT EXISTS matches (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "winner_id INT, " +
                "FOREIGN KEY (winner_id) REFERENCES users(id)" +
                ")";

        String createMatchDetails = "CREATE TABLE IF NOT EXISTS match_details (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "match_id INT, " +
                "user_id INT, " +
                "score INT, " +
                "FOREIGN KEY (match_id) REFERENCES matches(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createMatches);
            stmt.execute(createMatchDetails);
            System.out.println("Table structure verified and ready.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
