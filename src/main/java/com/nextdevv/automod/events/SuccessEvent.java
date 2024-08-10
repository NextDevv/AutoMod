package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record SuccessEvent(
        String message,
        String receiverUuid
) {
    public SuccessEvent {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty.");
        }
        if (receiverUuid == null || receiverUuid.isEmpty()) {
            throw new IllegalArgumentException("Receiver UUID cannot be null or empty.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.SUCCESS.getName());
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("receiverUuid", receiverUuid);
        return jsonObject;
    }

    public void send() {
        AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
        plugin.getRedisManager().publish(toJson());
    }
}
