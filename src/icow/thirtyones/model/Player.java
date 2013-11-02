package icow.thirtyones.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {

    private List<Card> hand;

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }
    
    public int evaluate() {
        
        Map<Suit, Integer> values = new HashMap<Suit, Integer>();
        
        for (Card card : hand) {
            if (values.containsKey(card.getSuit())) {
                Integer cur = values.get(card.getSuit());
                cur += card.getValue().getValue();
                values.put(card.getSuit(), cur);
            } else {
            	values.put(card.getSuit(), new Integer(card.getValue().getValue()));
            }
        }
        
        int maxValue = 0;
        
        for (Suit suit : Suit.values()) {
        	if (values.containsKey(suit) && values.get(suit) > maxValue) {
        		maxValue = values.get(suit);
        	}
        }
                
        return maxValue;
    }
}
