package icow.thirtyones.event.processor;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

import icow.thirtyones.app.App.PlayerConnection;
import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.model.Card;
import icow.thirtyones.model.Pile;
import icow.thirtyones.util.Utils;

public class PutCardEventProcessor implements EventProcessor {

    private final Pile pile;
    private final List<PlayerConnection> playerConnections;
    
    private static final String WINNER = "You win.";
    private static final String LOSER = "You lose.";
    
    public PutCardEventProcessor(Pile pile, List<PlayerConnection> playerConnections) {
        this.pile = pile;
        this.playerConnections = playerConnections;
    }
    
    @Override
    public void process(PlayerConnection pc, Object data) {
        
        Card card = (Card)data;

        pile.putCard(card);
        
        // Check the user's hand for a 31-win (any 2 of 10, J, Q, K with 1 ACE all same suit)
        // If winner, send END_GAME to everyone
        if (pc.getPlayer().evaluate() == 31) {
        	
        	// Build event
            CharBuffer cb = Utils.buildMessage(ClientEventType.END_GAME, WINNER);
           
            // Send to client
            try {
                pc.getOutbound().writeTextMessage(cb);
                pc.getOutbound().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            CharBuffer cbLoser = Utils.buildMessage(ClientEventType.END_GAME, LOSER);
                        
            for (PlayerConnection playerConnection : playerConnections) {
            	if (playerConnection != pc) {
                    // Send to client
                    try {
                        pc.getOutbound().writeTextMessage(cbLoser);
                        pc.getOutbound().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            	}
            }
        }
    }
}
