package icow.thirtyones.net;

import icow.thirtyones.event.Event;
import icow.thirtyones.game.ThirtyOnesGame;
import icow.thirtyones.model.Player;
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class PlayerConnection extends MessageInbound {

	private WsOutbound outbound;
	private Player player;
	private final ThirtyOnesGame thirtyOnesGame;

	public PlayerConnection(Player player,ThirtyOnesGame thirtyOnesGame) {
		this.setPlayer(player);
		this.thirtyOnesGame = thirtyOnesGame;
		player.setMyTurn(false);
	}

	@Override
	public void onOpen(WsOutbound outbound) {
		System.out.println("Client connected.");
		this.setOutbound(outbound);
	}

	@Override
	public void onClose(int status) {
		System.out.println("Client disconnected.");
	}

	@Override
	public void onTextMessage(CharBuffer cb) throws IOException {
		System.out.println("Message Received : " + cb);

		Event event = Utils.gson.fromJson(cb.toString(), Event.class);

		thirtyOnesGame.processEvent(this, event);
	}

	@Override
	public void onBinaryMessage(ByteBuffer bb) throws IOException {
		throw new NotImplementedException();
	}

	public WsOutbound getOutbound() {
		return outbound;
	}

	public void setOutbound(WsOutbound outbound) {
		this.outbound = outbound;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}