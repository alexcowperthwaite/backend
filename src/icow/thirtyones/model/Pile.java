package icow.thirtyones.model;

import java.util.Stack;

public class Pile extends StackableCards {

    public Pile() {
        cards = new Stack<Card>();
    }
    
    public void putCard(Card card) {
        cards.push(card);
    }
    
    public Card peekCard() {
    	return cards.peek();
    }
}
