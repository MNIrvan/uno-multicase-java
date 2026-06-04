CREATE DATABASE IF NOT EXISTS db_game_tournament;
USE db_game_tournament;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    total_win INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS matches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    winner_id INT,
    FOREIGN KEY (winner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS match_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    match_id INT,
    user_id INT,
    score INT,
    FOREIGN KEY (match_id) REFERENCES matches(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
