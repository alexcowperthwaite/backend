package icow.thirtyones.event.processor;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

import icow.thirtyones.app.App.PlayerConnection;
import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.util.Utils;

public class EndGameEvent {

	private static final String WINNER = "You win.";
	private static final String LOSER = "You lose.";
	
	public void endGame(PlayerConnection winner, List<PlayerConnection> playerConnections) {

		// Build event
		CharBuffer cb = Utils.buildMessage(ClientEventType.END_GAME, WINNER);

		// Send to client
		try {
			winner.getOutbound().writeTextMessage(cb);
			winner.getOutbound().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		CharBuffer cbLoser = Utils.buildMessage(ClientEventType.END_GAME, LOSER);

		for (PlayerConnection playerConnection : playerConnections) {
			if (playerConnection != winner) {
				// Send to client
				try {
					winner.getOutbound().writeTextMessage(cbLoser);
					winner.getOutbound().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
