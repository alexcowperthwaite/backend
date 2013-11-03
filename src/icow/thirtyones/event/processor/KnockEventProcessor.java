package icow.thirtyones.event.processor;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

import icow.thirtyones.app.App;
import icow.thirtyones.app.App.PlayerConnection;
import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.util.Utils;

public class KnockEventProcessor implements EventProcessor {

    private final List<PlayerConnection> playerConnections;
    
    public KnockEventProcessor(List<PlayerConnection> playerConnections) {
        this.playerConnections = playerConnections;
    }
    
    @Override
    public void process(PlayerConnection pc, Object data) {

    	pc.getPlayer().setKnocked(true);
    	
        // Send message to all players that a knock has occurred.
        for (PlayerConnection playerConnection : playerConnections) {
            if (playerConnection != pc) {

                // Build event
                CharBuffer cb = Utils.buildMessage(ClientEventType.KNOCKED, null);
               
                // Send to client
                try {
                    pc.getOutbound().writeTextMessage(cb);
                    pc.getOutbound().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        endTurn(pc);
    }
    
    private void endTurn(PlayerConnection pc) {

		// End player turn.
		pc.setMyTurn(false);

		// Discover index of pc
		int pcIndex = 0;
		
		for (int i = 0; i < playerConnections.size(); i++) {
			if (playerConnections.get(i) == pc) {
				pcIndex = i;
			}
		}
		
		// Discover next turn.
		int nextPlayerIndex = pcIndex + 1 == App.PLAYERS_PER_GAME ? 0 : pcIndex + 1;
		PlayerConnection nextPc = playerConnections.get(nextPlayerIndex);
		
		nextPc.setMyTurn(true);

		// Notify next player of turn.
		CharBuffer cb = Utils.buildMessage(ClientEventType.START_TURN, null);

		// Send to client.
		try {
			nextPc.getOutbound().writeTextMessage(cb);
			nextPc.getOutbound().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
