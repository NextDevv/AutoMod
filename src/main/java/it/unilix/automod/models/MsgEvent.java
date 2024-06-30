package it.unilix.automod.models;

import com.google.gson.JsonObject;
import it.unilix.automod.enums.ModEvent;

public record MsgEvent(String message, String sender, String receiver, ModEvent event) {
    public MsgEvent {
        event = ModEvent.MSG;
        if (message == null || sender == null || receiver == null) {
            throw new IllegalArgumentException("Message, sender and receiver cannot be null.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", event.name());
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("sender", sender);
        jsonObject.addProperty("receiver", receiver);
        return jsonObject;
    }
}
