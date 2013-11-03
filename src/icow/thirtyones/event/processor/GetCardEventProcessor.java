package icow.thirtyones.event.processor;

import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.model.Card;
import icow.thirtyones.model.Deck;
import icow.thirtyones.model.Pile;
import icow.thirtyones.net.PlayerConnection;
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.CharBuffer;

public class GetCardEventProcessor implements EventProcessor {

    private final Deck deck;
    private final Pile pile;
    
    public GetCardEventProcessor(Deck deck, Pile pile) {
        this.deck = deck;
        this.pile = pile;
    }
    
    @Override
    public void process(PlayerConnection pc, Object data) {

    	Boolean getCardFromDeck = (Boolean)data;
    	
    	Card card = null;
    	
    	if (getCardFromDeck) {
    		card = deck.drawCard();
    	} else {
    		card = pile.getCard();
    	}
    	
        // Update the player object
        pc.getPlayer().getHand().add(card);
        
        // Send card to player
        
        // Build event
        CharBuffer cb = Utils.buildMessage(ClientEventType.CARD, card);
       
        // Send to client
        try {
            pc.getOutbound().writeTextMessage(cb);
            pc.getOutbound().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
