package icow.thirtyones.event.processor;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

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
    }

}
