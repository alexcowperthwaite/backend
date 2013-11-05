package icow.thirtyones.model;

import java.util.Stack;

public class Pile {

    private Stack<Card> cards = new Stack<Card>();
    
    public void putCard(Card card) {
        cards.push(card);
    }
    
    public Card getCard() {
        return cards.pop();
    }
    
    public Card peekCard() {
    	return cards.peek();
    }
}
