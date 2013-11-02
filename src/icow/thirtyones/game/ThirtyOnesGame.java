package icow.thirtyones.game;

import icow.thirtyones.app.App.PlayerConnection;
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
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirtyOnesGame {

    private List<PlayerConnection> playerConnections;
    private Map<ServerEventType, EventProcessor> eventProcessors;
    
    private Deck deck;
    private Pile pile;
    
    public ThirtyOnesGame(List<PlayerConnection> playerConnections ) {
        this.playerConnections = playerConnections;

        eventProcessors = new HashMap<ServerEventType, EventProcessor>();
        eventProcessors.put(ServerEventType.KNOCK, new KnockEventProcessor(playerConnections));
        eventProcessors.put(ServerEventType.GET_CARD, new GetCardEventProcessor(deck, pile));
        eventProcessors.put(ServerEventType.PUT_CARD, new PutCardEventProcessor(pile, playerConnections));
    }
    
    public void start() {
        
        // Initialize and shuffle deck
        deck = new Deck();
        deck.shuffle();
        
        // Initialize pile
        pile = new Pile();
        
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
        
        // Give the first player their turn
        playerConnections.get(0).setMyTurn(true);
        
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
        if (!pc.isMyTurn()) {
            return;
        }
        
        // Process event.
        try {
            eventProcessors.get(message.getEventType()).process(pc, message.getData());
        } catch (Exception e) {
            return;
        }
             
        // End player turn.
        pc.setMyTurn(false);
        
        // Discover next turn.
        PlayerConnection pcWithNextTurn = null;
        
        for (int i = 0; i < playerConnections.size(); i++) {
            if (playerConnections.get(i) == pc) {
                if ((i + 1) == playerConnections.size()) {
                    pcWithNextTurn = playerConnections.get(0);
                } else {
                    pcWithNextTurn = playerConnections.get(i+1);
                }
            }
        }
        
        pcWithNextTurn.setMyTurn(true);
        
        // Notify next player of turn.
        CharBuffer cb = Utils.buildMessage(ClientEventType.START_TURN, null);
       
        // Send to client.
        try {
            pcWithNextTurn.getOutbound().writeTextMessage(cb);
            pcWithNextTurn.getOutbound().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
