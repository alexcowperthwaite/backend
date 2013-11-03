package icow.thirtyones.game;

import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.event.Event;
import icow.thirtyones.event.ServerEventType;
import icow.thirtyones.event.processor.EventProcessor;
import icow.thirtyones.event.processor.GetCardEventProcessor;
import icow.thirtyones.event.processor.KnockEventProcessor;
import icow.thirtyones.event.processor.PutCardEventProcessor;
import icow.thirtyones.model.Card;
import icow.thirtyones.model.Deck;
import icow.thirtyones.model.Pile;
import icow.thirtyones.net.FrontEndConnection;
import icow.thirtyones.net.PlayerConnection;
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirtyOnesGame extends Thread {

    private final List<PlayerConnection> playerConnections;
    private final FrontEndConnection frontEndConnection;
    private Map<ServerEventType, EventProcessor> eventProcessors;
    
    private Deck deck;
    private Pile pile;
    
    public ThirtyOnesGame(List<PlayerConnection> playerConnections, FrontEndConnection frontEndConnection) {
        
    	this.playerConnections = playerConnections;
    	this.frontEndConnection = frontEndConnection;
    }
    
    @Override
    public void run() {
        
    	// Wait for last thread
    	synchronized(this) {
	    	try {
		        wait(1000);
	        } catch (InterruptedException e1) {
		        e1.printStackTrace();
	        }
    	}
    	
        // Initialize and shuffle deck
        deck = new Deck();
        deck.shuffle();
        
        // Initialize pile
        pile = new Pile();
        
        // Register the event processors
        eventProcessors = new HashMap<ServerEventType, EventProcessor>();
        eventProcessors.put(ServerEventType.KNOCK, new KnockEventProcessor(playerConnections));
        eventProcessors.put(ServerEventType.GET_CARD, new GetCardEventProcessor(deck, pile));
        eventProcessors.put(ServerEventType.PUT_CARD, new PutCardEventProcessor(pile, playerConnections));

        
        // Send out BEGIN_GAME event to all players; give them cards.
        for (PlayerConnection pc : playerConnections) {
            
            // Set the player's hand to 3 cards.
            List<Card> hand = new ArrayList<Card>();
            hand.add(deck.drawCard());
            hand.add(deck.drawCard());
            hand.add(deck.drawCard());
            pc.getPlayer().setHand(hand);
            
            // Build event
            CharBuffer cb = Utils.buildMessage(ClientEventType.BEGIN_GAME, hand);
           
            try {
                pc.getOutbound().writeTextMessage(cb);
                pc.getOutbound().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Add the first card to the pile
        Card topCard = deck.drawCard();
        pile.putCard(topCard);
        
        // Tell frontend what the pile card is
        // Build event
        CharBuffer frontEndMessage = Utils.buildMessage(ClientEventType.CARD, topCard);
       
        // Send to client
        try {
        	frontEndConnection.getOutbound().writeTextMessage(frontEndMessage);
        	frontEndConnection.getOutbound().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Give the first player their turn
        playerConnections.get(0).getPlayer().setMyTurn(true);
        
        CharBuffer cb = Utils.buildMessage(ClientEventType.START_TURN, null);
        try {
            playerConnections.get(0).getOutbound().writeTextMessage(cb);
            playerConnections.get(0).getOutbound().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void processEvent(PlayerConnection pc, Event message) {

        // Do not process requests from clients whos turn it isn't.
        if (!pc.getPlayer().isMyTurn()) {
            return;
        }
        
        // Process event.
        try {
            eventProcessors.get(message.getEventType()).process(pc, message.getData());
        } catch (Exception e) {
            return;
        }
        
    }
}
