package icow.thirtyones.event.processor;

import icow.thirtyones.net.PlayerConnection;

public interface EventProcessor {

    public void process(PlayerConnection pc, Object data);
}
