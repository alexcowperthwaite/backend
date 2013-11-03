package icow.thirtyones.util;

import icow.thirtyones.event.Event;
import icow.thirtyones.event.EventType;
import icow.thirtyones.event.EventTypeDeserializer;

import java.nio.CharBuffer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

    public static Gson gson = new GsonBuilder().registerTypeAdapter(EventType.class, new EventTypeDeserializer()).create();
    
    public static CharBuffer buildMessage(EventType eventType, Object data) {
        
        // Build event
        Event event = new Event(eventType, data);
        final String jsonEvent = Utils.gson.toJson(event);
        
        // Create buffer
        CharBuffer cb = CharBuffer.wrap(jsonEvent);
        
        return cb;
    }
}
