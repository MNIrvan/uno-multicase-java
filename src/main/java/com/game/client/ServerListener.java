package com.game.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

public class ServerListener implements Runnable {
    private BufferedReader in;
    private Consumer<String> messageCallback;

    public ServerListener(BufferedReader in, Consumer<String> messageCallback) {
        this.in = in;
        this.messageCallback = messageCallback;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (messageCallback != null) {
                    messageCallback.accept(message);
                } else {
                    System.out.println("[SERVER]: " + message);
                }
            }
        } catch (IOException e) {
            if (messageCallback != null) {
                messageCallback.accept("ERROR|CONNECTION_LOST");
            } else {
                System.out.println("Connection to server lost.");
            }
        }
    }
}
