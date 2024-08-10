package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record ErrorEvent(String message, String receiver) {
    public ErrorEvent {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty.");
        }
        if (receiver == null || receiver.isEmpty()) {
            throw new IllegalArgumentException("Receiver cannot be null or empty.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.ERROR.getName());
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("receiver", receiver);
        return jsonObject;
    }

    public void send() {
        AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
        plugin.getRedisManager().publish(toJson());
    }
}
