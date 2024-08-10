package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record NotifyEvent(String message, String sender) {
    public NotifyEvent {
        if (message == null || sender == null) {
            throw new IllegalArgumentException("Message and sender cannot be null.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.NOTIFY.getName());
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("sender", sender);
        return jsonObject;
    }

    public void send() {
        AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
        if(plugin.getSettings().isRequiresMultiInstance()) {
            plugin.getRedisManager().publish(toJson());
        }
    }
}
