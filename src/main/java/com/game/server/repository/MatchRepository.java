package com.game.server.repository;

import com.game.server.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class MatchRepository {
    
    public static void saveMatch(int winnerId, Map<Integer, Integer> playerScores) {
        Connection conn = DBConnection.getConnection();
        String insertMatchSql = "INSERT INTO matches (winner_id) VALUES (?)";
        String insertDetailSql = "INSERT INTO match_details (match_id, user_id, score) VALUES (?, ?, ?)";
        
        try {
            // Menonaktifkan autocommit untuk transaksi (opsional, tapi lebih baik)
            conn.setAutoCommit(false);
            
            int matchId = -1;
            // 1. Simpan ke tabel matches
            try (PreparedStatement matchStmt = conn.prepareStatement(insertMatchSql, Statement.RETURN_GENERATED_KEYS)) {
                matchStmt.setInt(1, winnerId);
                matchStmt.executeUpdate();
                
                ResultSet rs = matchStmt.getGeneratedKeys();
                if (rs.next()) {
                    matchId = rs.getInt(1);
                }
            }
            
            // 2. Simpan detail masing-masing pemain ke match_details
            if (matchId != -1) {
                try (PreparedStatement detailStmt = conn.prepareStatement(insertDetailSql)) {
                    for (Map.Entry<Integer, Integer> entry : playerScores.entrySet()) {
                        detailStmt.setInt(1, matchId);
                        detailStmt.setInt(2, entry.getKey());
                        detailStmt.setInt(3, entry.getValue()); // Score merepresentasikan sisa kartu (0 = menang)
                        detailStmt.addBatch();
                    }
                    detailStmt.executeBatch();
                }
            }
            
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Match result saved to database. Match ID: " + matchId);
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
