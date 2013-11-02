package icow.thirtyones.app;

import icow.thirtyones.event.Event;
import icow.thirtyones.game.ThirtyOnesGame;
import icow.thirtyones.model.Player;
import icow.thirtyones.util.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Servlet implementation
 */
@WebServlet("/App")
public class App extends WebSocketServlet {
    
    private static final long serialVersionUID = 1L;
    
    private static int PLAYERS_PER_GAME = 1;
    
    private static final List<PlayerConnection> playerConnections = new ArrayList<PlayerConnection>();
    private static ThirtyOnesGame thirtyOnesGame;

    /**
     * Constructor
     */
    public App() {
    }
    
    @Override
    protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest arg1) {
        
        synchronized(playerConnections) {
            if (thirtyOnesGame == null) {
                // New Player connecting
                PlayerConnection pc = new PlayerConnection(new Player());
                playerConnections.add(pc);
                
                if (playerConnections.size() == PLAYERS_PER_GAME) {
                    thirtyOnesGame = new ThirtyOnesGame(playerConnections);
                    thirtyOnesGame.start();
                }
                
                return pc;
            }
            return null;
        }
    }
    
    public class PlayerConnection extends MessageInbound {
        
        private WsOutbound outbound;
        private Player player;
        private boolean isMyTurn;
        
        public PlayerConnection(Player player) {
            this.setPlayer(player);
            setMyTurn(false);
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

        public boolean isMyTurn() {
            return isMyTurn;
        }

        public void setMyTurn(boolean isMyTurn) {
            this.isMyTurn = isMyTurn;
        }
    }
}