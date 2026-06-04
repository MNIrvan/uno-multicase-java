package com.game.server.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int userId;
    private String username;
    private List<Card> hand;
    private boolean hasCalledUno;

    public Player(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.hand = new ArrayList<>();
        this.hasCalledUno = false;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasCalledUno() {
        return hasCalledUno;
    }

    public void setCalledUno(boolean hasCalledUno) {
        this.hasCalledUno = hasCalledUno;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public boolean removeCard(String cardName) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).toString().equalsIgnoreCase(cardName)) {
                hand.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public Card getCard(String cardName) {
        for (Card card : hand) {
            if (card.toString().equalsIgnoreCase(cardName)) {
                return card;
            }
        }
        return null;
    }

    public int getHandSize() {
        return hand.size();
    }

    public String getHandAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(hand.get(i).toString());
            if (i < hand.size() - 1) sb.append(",");
        }
        return sb.toString();
    }
}
