package com.game.server.repository;

import com.game.server.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserRepository {
    public static int getOrCreateUser(String username) {
        Connection conn = DBConnection.getConnection();
        String selectSql = "SELECT id FROM users WHERE username = ?";
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                // Buat user baru jika belum ada (password kosong sementara)
                String insertSql = "INSERT INTO users (username, password) VALUES (?, '')";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, username);
                    insertStmt.executeUpdate();
                    
                    ResultSet genKeys = insertStmt.getGeneratedKeys();
                    if (genKeys.next()) {
                        return genKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void incrementWin(int userId) {
        Connection conn = DBConnection.getConnection();
        String updateSql = "UPDATE users SET total_win = total_win + 1 WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("Win counter updated for User ID: " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
