package com.game.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final int PORT = 8080;
    private static final int MAX_PLAYERS = 4;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static GameEngine gameEngine = new GameEngine(clients);

    public static void main(String[] args) {
        // Test DB Connection
        DBConnection.getConnection();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT + ". Waiting for players...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection attempt: " + clientSocket.getInetAddress());

                if (clients.size() >= MAX_PLAYERS || gameEngine.isGameStarted()) {
                    System.out.println("Connection rejected: Room is full or Game has started.");
                    PrintWriter tempOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    tempOut.println("ERROR|ROOM_FULL");
                    clientSocket.close();
                    continue;
                }

                ClientHandler clientThread = new ClientHandler(clientSocket, clients, gameEngine);
                clients.add(clientThread);
                pool.execute(clientThread);
                
                System.out.println("Players connected: " + clients.size() + "/" + MAX_PLAYERS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
