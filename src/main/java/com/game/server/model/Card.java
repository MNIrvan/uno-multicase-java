package com.game.server.model;

public class Card {
    public enum Color { RED, BLUE, GREEN, YELLOW, WILD }
    public enum Value {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
        SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    private Color color;
    private Value value;

    public Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }

    public Color getColor() { return color; }
    public Value getValue() { return value; }

    @Override
    public String toString() {
        if (color == Color.WILD) {
            return value.name();
        }
        return color.name() + "_" + value.name();
    }
    
    // Mengecek apakah kartu ini valid untuk diletakkan di atas kartu topCard
    public boolean canPlayOn(Card topCard) {
        return this.color == Color.WILD || 
               this.color == topCard.getColor() || 
               this.value == topCard.getValue() || 
               topCard.getColor() == Color.WILD; // Aturan dasar (bisa diperluas)
    }
}
