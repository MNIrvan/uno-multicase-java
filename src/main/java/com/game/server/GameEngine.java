package com.game.server;

import com.game.server.model.Card;
import com.game.server.model.Deck;
import com.game.server.model.Player;
import com.game.server.repository.MatchRepository;
import com.game.server.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {
    private List<ClientHandler> clients;
    private Map<ClientHandler, Player> playersData;
    private Deck deck;
    private Card topCard;
    
    private int currentTurn = 0;
    private boolean isReversed = false;
    private boolean isGameStarted = false;

    private Timer turnTimer;
    private static final int TURN_TIMEOUT_MS = 60000; // 1 Minute

    public GameEngine(List<ClientHandler> clients) {
        this.clients = clients;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public synchronized void setPlayerReady(ClientHandler client) {
        if (isGameStarted) return;

        client.setReady(true);
        broadcast("STATE|PLAYER_READY|" + client.getUsername());

        if (clients.size() >= 2) {
            boolean allReady = true;
            for (ClientHandler c : clients) {
                if (!c.isReady()) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) {
                startGame();
            }
        }
    }

    private void startGame() {
        isGameStarted = true;
        this.playersData = new HashMap<>();
        this.deck = new Deck();

        broadcast("STATE|GAME_START|UNO");

        // 1. Inisialisasi pemain dan bagikan 7 kartu
        for (ClientHandler client : clients) {
            Player p = new Player(client.getUserId(), client.getUsername());
            for (int i = 0; i < 7; i++) {
                p.addCard(deck.drawCard());
            }
            playersData.put(client, p);
            
            // Kirim daftar kartu ke masing-masing klien
            client.sendMessage("STATE|YOUR_HAND|" + p.getHandAsString());
        }

        // 2. Tentukan kartu awal di tengah meja
        do {
            topCard = deck.drawCard();
        } while (topCard.getColor() == Card.Color.WILD); // Pastikan kartu awal bukan wild card
        
        broadcast("STATE|TOP_CARD|" + topCard.toString());

        // 3. Mulai giliran pertama
        notifyTurn();
    }

    public synchronized void processAction(ClientHandler client, String action) {
        if (!isGameStarted || playersData == null || !playersData.containsKey(client)) return;

        String[] parts = action.split("\\|");
        String command = parts.length > 1 ? parts[1] : "";

        if (command.equals("CALL_UNO")) {
            Player p = playersData.get(client);
            p.setCalledUno(true);
            broadcast("STATE|PLAYER_CALLED_UNO|" + p.getUsername());
            return;
        }

        // Validasi giliran
        if (!clients.get(currentTurn).equals(client)) {
            client.sendMessage("ERROR|NOT_YOUR_TURN");
            return;
        }

        Player player = playersData.get(client);
        System.out.println("Aksi dari " + player.getUsername() + ": " + action);
        
        if (command.equals("PLAY_CARD") && parts.length == 3) {
            cancelTurnTimer();
            String cardName = parts[2];
            Card cardToPlay = player.getCard(cardName);
            
            if (cardToPlay == null) {
                client.sendMessage("ERROR|CARD_NOT_IN_HAND");
                startTurnTimer();
                return;
            }

            if (!cardToPlay.canPlayOn(topCard)) {
                client.sendMessage("ERROR|INVALID_MOVE");
                startTurnTimer();
                return;
            }

            // Aturan Call UNO
            if (player.getHandSize() == 2 && !player.hasCalledUno()) {
                // Mainkan kartu, sisa 1 tapi lupa bilang UNO
                broadcast("STATE|PLAYER_PENALTY_NO_UNO|" + player.getUsername());
                player.addCard(deck.drawCard());
                player.addCard(deck.drawCard());
            }

            // Memainkan kartu
            player.removeCard(cardName);
            topCard = cardToPlay;
            broadcast("STATE|TOP_CARD|" + topCard.toString());
            broadcast("STATE|PLAYER_PLAYED|" + player.getUsername() + "|" + cardName);
            
            // Reset status UNO untuk keamanan
            player.setCalledUno(false);
            client.sendMessage("STATE|YOUR_HAND|" + player.getHandAsString());

            // Cek kondisi menang
            if (player.getHandSize() == 0) {
                endGame(player);
                return;
            }

            // Terapkan efek kartu spesial
            applyCardEffect(cardToPlay);
            
        } else if (command.equals("DRAW_CARD")) {
            cancelTurnTimer();
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                player.addCard(drawnCard);
                player.setCalledUno(false); // Cancel call UNO jika malah nge-draw
                client.sendMessage("STATE|YOUR_HAND|" + player.getHandAsString());
                broadcast("STATE|PLAYER_DRAWN|" + player.getUsername() + "|1");
            }
            nextTurn();
        } else {
            client.sendMessage("ERROR|UNKNOWN_COMMAND");
        }
    }

    private void applyCardEffect(Card card) {
        if (card.getValue() == Card.Value.REVERSE) {
            isReversed = !isReversed;
            broadcast("STATE|REVERSED");
            if (clients.size() == 2) {
                // Di mode 2 pemain, reverse berlaku seperti skip
                nextTurn();
                nextTurn();
            } else {
                nextTurn();
            }
        } else if (card.getValue() == Card.Value.SKIP) {
            nextTurn(); 
            nextTurn();
        } else if (card.getValue() == Card.Value.DRAW_TWO) {
            nextTurn(); 
            Player victim = playersData.get(clients.get(currentTurn));
            victim.addCard(deck.drawCard());
            victim.addCard(deck.drawCard());
            victim.setCalledUno(false);
            clients.get(currentTurn).sendMessage("STATE|YOUR_HAND|" + victim.getHandAsString());
            broadcast("STATE|PLAYER_PENALTY|" + victim.getUsername() + "|2");
            nextTurn(); 
        } else if (card.getValue() == Card.Value.WILD_DRAW_FOUR) {
            nextTurn(); 
            Player victim = playersData.get(clients.get(currentTurn));
            for(int i=0; i<4; i++) victim.addCard(deck.drawCard());
            victim.setCalledUno(false);
            clients.get(currentTurn).sendMessage("STATE|YOUR_HAND|" + victim.getHandAsString());
            broadcast("STATE|PLAYER_PENALTY|" + victim.getUsername() + "|4");
            nextTurn();
        } else {
            nextTurn();
        }
    }

    private void nextTurn() {
        if (clients.isEmpty()) return;
        
        if (isReversed) {
            currentTurn = (currentTurn - 1 + clients.size()) % clients.size();
        } else {
            currentTurn = (currentTurn + 1) % clients.size();
        }
        notifyTurn();
    }

    private void notifyTurn() {
        if (clients.isEmpty()) return;
        String turnUser = clients.get(currentTurn).getUsername();
        broadcast("STATE|CURRENT_TURN|" + turnUser);
        startTurnTimer();
    }

    private synchronized void startTurnTimer() {
        cancelTurnTimer();
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleTurnTimeout();
            }
        }, TURN_TIMEOUT_MS);
    }

    private synchronized void cancelTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }

    private synchronized void handleTurnTimeout() {
        if (!isGameStarted || clients.isEmpty()) return;
        ClientHandler afkClient = clients.get(currentTurn);
        broadcast("STATE|PLAYER_AFK_KICKED|" + afkClient.getUsername());
        afkClient.closeConnection();
        // Method closeConnection di ClientHandler akan me-trigger handleDisconnect() otomatis.
    }

    public synchronized void handleDisconnect(ClientHandler client) {
        if (clients.contains(client)) {
            clients.remove(client);
        }
        
        if (!isGameStarted) {
            return;
        }

        if (playersData.containsKey(client)) {
            playersData.remove(client);
            broadcast("STATE|PLAYER_DISCONNECTED|" + client.getUsername());
            
            if (clients.size() < 2) {
                // Game Over karena kurang pemain
                cancelTurnTimer();
                if (clients.size() == 1) {
                    endGame(playersData.get(clients.get(0)));
                } else {
                    broadcast("GAMEOVER|CANCELLED|NO_PLAYERS_LEFT");
                    resetGame();
                }
            } else {
                // Sesuaikan urutan turn
                if (currentTurn >= clients.size()) {
                    currentTurn = 0;
                }
                notifyTurn();
            }
        }
    }

    private void endGame(Player winner) {
        cancelTurnTimer();
        broadcast("GAMEOVER|WINNER|" + winner.getUsername());
        
        // Simpan hasil ke database
        UserRepository.incrementWin(winner.getUserId());
        
        Map<Integer, Integer> scores = new HashMap<>();
        for (Player p : playersData.values()) {
            scores.put(p.getUserId(), p.getHandSize());
        }
        MatchRepository.saveMatch(winner.getUserId(), scores);
        
        resetGame();
    }

    private void resetGame() {
        isGameStarted = false;
        if (playersData != null) playersData.clear();
        for (ClientHandler c : clients) {
            c.setReady(false);
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
