package icow.thirtyones.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class FrontEndConnection extends MessageInbound {

	private WsOutbound outbound;

	@Override
	public void onOpen(WsOutbound outbound) {
		System.out.println("FrontEnd connected.");
		this.setOutbound(outbound);
	}

	@Override
	public void onClose(int status) {
		System.out.println("FrontEnd disconnected.");
	}

	@Override
	public void onTextMessage(CharBuffer cb) throws IOException {
		System.out.println("FrontEnd Message Received : " + cb);
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
}