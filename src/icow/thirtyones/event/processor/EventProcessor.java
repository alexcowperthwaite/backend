package icow.thirtyones.event.processor;

import icow.thirtyones.app.App.PlayerConnection;

public interface EventProcessor {

    public void process(PlayerConnection pc, Object data);
}
