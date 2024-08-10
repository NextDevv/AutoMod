package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.enums.ModEvent;

public record MsgEvent(String message, String sender, String receiver, String senderUuid) {
    public MsgEvent {
        if (message == null || sender == null || receiver == null) {
            throw new IllegalArgumentException("Message, sender and receiver cannot be null.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.MSG.getName());
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("sender", sender);
        jsonObject.addProperty("receiver", receiver);
        return jsonObject;
    }
}
