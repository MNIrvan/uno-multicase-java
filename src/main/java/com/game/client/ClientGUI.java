package com.game.client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Login Panel Components
    private JTextField ipAddressField;
    private JTextField usernameField;
    private JButton connectButton;

    // Lobby Panel Components
    private DefaultListModel<String> lobbyListModel;
    private JList<String> lobbyList;
    private JButton readyButton;
    private boolean isReady = false;

    // Game Panel Components
    private JLabel topCardLabel;
    private JLabel turnLabel;
    private JPanel handPanel;
    private JTextArea logArea;
    private JButton drawButton;
    private JButton callUnoButton;
    private boolean hasCalledUno = false;

    public ClientGUI() {
        setTitle("Multiplayer UNO Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initLoginPanel();
        initLobbyPanel();
        initGamePanel();

        add(mainPanel);
    }

    private void initLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Welcome to UNO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Server IP: "), gbc);

        ipAddressField = new JTextField("127.0.0.1", 15);
        gbc.gridx = 1;
        loginPanel.add(ipAddressField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Username: "), gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginPanel.add(connectButton, gbc);

        mainPanel.add(loginPanel, "LOGIN");
    }

    private void initLobbyPanel() {
        JPanel lobbyPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Lobby (Waiting for Players)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        lobbyPanel.add(titleLabel, BorderLayout.NORTH);

        lobbyListModel = new DefaultListModel<>();
        lobbyList = new JList<>(lobbyListModel);
        lobbyPanel.add(new JScrollPane(lobbyList), BorderLayout.CENTER);

        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            if (!isReady) {
                out.println("ACTION|READY");
                isReady = true;
                readyButton.setText("Waiting...");
                readyButton.setEnabled(false);
            }
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(readyButton);
        lobbyPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(lobbyPanel, "LOBBY");
    }

    private void initGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());

        // Top area: Info & Top Card
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        turnLabel = new JLabel("Turn: Waiting...", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topCardLabel = new JLabel("Top Card: [None]", SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 20));
        infoPanel.add(turnLabel);
        infoPanel.add(topCardLabel);
        gamePanel.add(infoPanel, BorderLayout.NORTH);

        // Center area: Log/Chat
        logArea = new JTextArea();
        logArea.setEditable(false);
        gamePanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Bottom area: Hand cards and Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        handPanel = new JPanel(new FlowLayout());
        JScrollPane handScroll = new JScrollPane(handPanel);
        handScroll.setPreferredSize(new Dimension(800, 100));
        bottomPanel.add(handScroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(1, 2));
        drawButton = new JButton("Draw Card");
        drawButton.addActionListener(e -> {
            out.println("ACTION|DRAW_CARD");
        });

        callUnoButton = new JButton("Call UNO!");
        callUnoButton.addActionListener(e -> {
            out.println("ACTION|CALL_UNO");
            hasCalledUno = true;
            callUnoButton.setEnabled(false);
            log("You called UNO!");
        });

        actionPanel.add(drawButton);
        actionPanel.add(callUnoButton);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        gamePanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(gamePanel, "GAME");
    }

    private void connectToServer() {
        String user = usernameField.getText().trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty");
            return;
        }

        String ipAddress = ipAddressField.getText().trim();
        if (ipAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Server IP cannot be empty");
            return;
        }

        try {
            socket = new Socket(ipAddress, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send login command
            this.username = user;
            out.println("LOGIN|" + username);

            // Start listener thread
            ServerListener listener = new ServerListener(in, this::handleServerMessage);
            new Thread(listener).start();

            connectButton.setEnabled(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + ex.getMessage());
        }
    }

    private void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split("\\|");
            String type = parts[0];

            if (type.equals("STATE")) {
                if (parts[1].equals("LOGIN_SUCCESS")) {
                    cardLayout.show(mainPanel, "LOBBY");
                } else if (parts[1].equals("PLAYER_JOINED")) {
                    if (!lobbyListModel.contains(parts[2])) {
                        lobbyListModel.addElement(parts[2]);
                    }
                } else if (parts[1].equals("GAME_START")) {
                    cardLayout.show(mainPanel, "GAME");
                    log("Game Started!");
                } else if (parts[1].equals("TOP_CARD")) {
                    topCardLabel.setText("Top Card: " + parts[2]);
                } else if (parts[1].equals("YOUR_HAND")) {
                    updateHand(parts.length > 2 ? parts[2] : "");
                } else if (parts[1].equals("CURRENT_TURN")) {
                    turnLabel.setText("Turn: " + parts[2]);
                    boolean isMyTurn = parts[2].equals(username);
                    drawButton.setEnabled(isMyTurn);
                    if (isMyTurn)
                        hasCalledUno = false; // Reset call uno for new turn if needed, though usually you call it when
                                              // playing 2nd to last card
                } else if (parts.length > 2) {
                    log(parts[1] + ": " + parts[2]);
                }
            } else if (type.equals("ERROR")) {
                if (parts[1].equals("ROOM_FULL")) {
                    JOptionPane.showMessageDialog(this, "Lobby is full or game has started.");
                    connectButton.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + parts[1]);
                }
            } else if (type.equals("GAMEOVER")) {
                JOptionPane.showMessageDialog(this, "Game Over! Winner: " + (parts.length > 2 ? parts[2] : "Unknown"));
                cardLayout.show(mainPanel, "LOBBY");
                isReady = false;
                readyButton.setText("Ready");
                readyButton.setEnabled(true);
            }
        });
    }

    private void updateHand(String handStr) {
        handPanel.removeAll();
        callUnoButton.setEnabled(true);
        if (handStr.isEmpty()) {
            handPanel.revalidate();
            handPanel.repaint();
            return;
        }

        String[] cards = handStr.split(",");
        for (String card : cards) {
            JButton cardBtn = new JButton(card);
            cardBtn.addActionListener(e -> {
                out.println("ACTION|PLAY_CARD|" + card);
            });
            handPanel.add(cardBtn);
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI().setVisible(true);
        });
    }
}
