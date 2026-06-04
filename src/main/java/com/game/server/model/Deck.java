package com.game.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffle();
    }

    private void initializeDeck() {
        Card.Color[] colors = {Card.Color.RED, Card.Color.BLUE, Card.Color.GREEN, Card.Color.YELLOW};
        
        for (Card.Color color : colors) {
            // Kartu 0 (satu buah)
            cards.add(new Card(color, Card.Value.ZERO));
            
            // Kartu 1-9, Skip, Reverse, Draw Two (dua buah)
            for (int i = 0; i < 2; i++) {
                cards.add(new Card(color, Card.Value.ONE));
                cards.add(new Card(color, Card.Value.TWO));
                cards.add(new Card(color, Card.Value.THREE));
                cards.add(new Card(color, Card.Value.FOUR));
                cards.add(new Card(color, Card.Value.FIVE));
                cards.add(new Card(color, Card.Value.SIX));
                cards.add(new Card(color, Card.Value.SEVEN));
                cards.add(new Card(color, Card.Value.EIGHT));
                cards.add(new Card(color, Card.Value.NINE));
                cards.add(new Card(color, Card.Value.SKIP));
                cards.add(new Card(color, Card.Value.REVERSE));
                cards.add(new Card(color, Card.Value.DRAW_TWO));
            }
        }
        
        // Wild dan Wild Draw Four (masing-masing 4 buah)
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Card.Color.WILD, Card.Value.WILD));
            cards.add(new Card(Card.Color.WILD, Card.Value.WILD_DRAW_FOUR));
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null; // Dalam game aslinya, discard pile dikocok ulang menjadi deck
        }
        return cards.remove(cards.size() - 1);
    }
}
