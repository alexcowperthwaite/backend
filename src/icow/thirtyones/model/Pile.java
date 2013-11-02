package icow.thirtyones.model;

import java.util.Stack;

public class Pile {

    private Stack<Card> cards;
    
    public void putCard(Card card) {
        cards.push(card);
    }
    
    public Card getCard() {
        return cards.pop();
    }
}
