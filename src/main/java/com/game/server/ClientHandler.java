package com.game.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.game.server.repository.UserRepository;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClientHandler> clients;
    private GameEngine gameEngine;
    private String username;
    private int userId;
    private boolean isReady = false;

    public ClientHandler(Socket socket, List<ClientHandler> clients, GameEngine gameEngine) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.gameEngine = gameEngine;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public String getUsername() {
        return username != null ? username : "Unknown";
    }

    public int getUserId() {
        return userId;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }
    
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                String[] parts = message.split("\\|");
                
                if (parts[0].equals("LOGIN") && parts.length >= 2) {
                    this.username = parts[1];
                    this.userId = UserRepository.getOrCreateUser(this.username);
                    sendMessage("STATE|LOGIN_SUCCESS|" + username);
                    broadcast("STATE|PLAYER_JOINED|" + username);
                } else if (parts[0].equals("ACTION")) {
                    if (parts.length > 1 && parts[1].equals("READY")) {
                        gameEngine.setPlayerReady(this);
                    } else {
                        gameEngine.processAction(this, message);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + getUsername());
        } finally {
            closeConnection();
            gameEngine.handleDisconnect(this);
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
