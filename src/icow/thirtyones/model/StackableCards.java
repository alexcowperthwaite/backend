package icow.thirtyones.model;

import java.util.Stack;

public class StackableCards {

    protected Stack<Card> cards;
    
    public boolean isEmpty() {
        return (cards.size() == 0);
    }
        
    public Card getCard() {
        return cards.pop();
    }
}
