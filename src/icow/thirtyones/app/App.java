package icow.thirtyones.app;

import icow.thirtyones.game.ThirtyOnesGame;
import icow.thirtyones.model.Player;
import icow.thirtyones.net.FrontEndConnection;
import icow.thirtyones.net.PlayerConnection;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

/**
 * Servlet implementation
 */
@WebServlet("/App")
public class App extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	public static final int PLAYERS_PER_GAME = 1;

	private static final List<PlayerConnection> playerConnections = new ArrayList<PlayerConnection>();
	private static ThirtyOnesGame thirtyOnesGame;

	private static final List<FrontEndConnection> frontEndConnections = new ArrayList<FrontEndConnection>();

	/**
	 * Constructor
	 */
	public App() {

		thirtyOnesGame = new ThirtyOnesGame(playerConnections, frontEndConnections);
	}

	@Override
	protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest arg1) {

		synchronized (playerConnections) {
			
			if (playerConnections.size() == PLAYERS_PER_GAME) {
				return null;
			}

			if (frontEndConnections.size() == 0) {
				// First client is frontend
				FrontEndConnection fc = new FrontEndConnection();
				frontEndConnections.add(fc);
				return fc;
			}

			// New Player connecting
			PlayerConnection pc = new PlayerConnection(new Player(playerConnections.size() + 1), thirtyOnesGame);
			playerConnections.add(pc);

			if (playerConnections.size() == PLAYERS_PER_GAME) {
				thirtyOnesGame.start();
			}

			return pc;
		}
	}
}