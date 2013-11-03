package icow.thirtyones.event.processor;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

import com.google.gson.internal.LinkedTreeMap;

import icow.thirtyones.app.App;
import icow.thirtyones.app.App.PlayerConnection;
import icow.thirtyones.event.ClientEventType;
import icow.thirtyones.model.Card;
import icow.thirtyones.model.Pile;
import icow.thirtyones.model.Suit;
import icow.thirtyones.model.Value;
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

		LinkedTreeMap<String, String> ltm = (LinkedTreeMap<String, String>) data;

		Card card = new Card(Suit.valueOf(ltm.get("suit")), Value.valueOf(ltm.get("value")));

		pile.putCard(card);

		// Check the user's hand for a 31-win (any 2 of 10, J, Q, K with 1 ACE
		// all same suit)
		// If winner, send END_GAME to everyone
		if (pc.getPlayer().evaluate() == 31) {

			endGame(pc);
		}

		// Otherwise check to see if someone knocked and it's the last turn;
		// evaluate all hands and send winner
		int playerPos = pc.getPlayer().getPosition();
		int nextPlayerPos = playerPos == App.PLAYERS_PER_GAME ? 0 : playerPos + 1;

		if (playerConnections.get(nextPlayerPos).getPlayer().isKnocked()) {
			int max = 0;
			PlayerConnection curWinner = null;

			for (PlayerConnection playerConnection : playerConnections) {
				int handValue = playerConnection.getPlayer().evaluate();
				if (handValue > max) {
					max = handValue;
					curWinner = playerConnection;
				}
			}

			// Send win message to Client
			endGame(curWinner);
		}
		endTurn(pc);
	}

	public void endGame(PlayerConnection winner) {

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
