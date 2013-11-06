package icow.thirtyones.model;

import java.util.Collections;
import java.util.Stack;

public class Deck extends StackableCards {

    public Deck() {
        cards = new Stack<Card>();
        
        for (Suit suit : Suit.values()) {
            for (Value value : Value.values()) {
                cards.add(new Card(suit, value));
            }
        }
    }
    
    public void shuffle() {
        Collections.shuffle(cards);
    }

}
