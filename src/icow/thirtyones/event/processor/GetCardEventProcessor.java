package icow.thirtyones.event.processor;

import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.model.Card;
import icow.thirtyones.model.Deck;
import icow.thirtyones.model.Pile;
import icow.thirtyones.model.StackableCards;
import icow.thirtyones.net.FrontEndConnection;
import icow.thirtyones.net.PlayerConnection;
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

public class GetCardEventProcessor implements EventProcessor {

	private final List<FrontEndConnection> frontEndConnections;
	private StackableCards deck;
	private StackableCards pile;

	public GetCardEventProcessor(Deck deck, Pile pile, List<FrontEndConnection> frontEndConnections) {
		this.deck = deck;
		this.pile = pile;
		this.frontEndConnections = frontEndConnections;
	}

	@Override
	public void process(PlayerConnection pc, Object data) {

		Boolean getCardFromDeck = (Boolean) data;

		Card card = null;

		if (getCardFromDeck) {
			if (deck.isEmpty()) {
			    Card lastCard = pile.getCard();
				deck = pile;
				((Deck)deck).shuffle();
				((Pile)pile).putCard(lastCard);
			}
			card = deck.getCard();
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

		// Update FrontEnd if taken from pile
		if (!getCardFromDeck) {
			// Tell frontend what the pile card is
	        // Build event
		    CharBuffer frontEndMessage = null;
		    if(pile.isEmpty()) {
		        // send blank card representation
		        frontEndMessage = Utils.buildMessage(ClientEventType.CARD, null);
		    } else {
		        frontEndMessage = Utils.buildMessage(ClientEventType.CARD, ((Pile)pile).peekCard());
		    }
	       
	        // Send to client
	        try {
	        	frontEndConnections.get(0).getOutbound().writeTextMessage(frontEndMessage);
	        	frontEndConnections.get(0).getOutbound().flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

	}
}
