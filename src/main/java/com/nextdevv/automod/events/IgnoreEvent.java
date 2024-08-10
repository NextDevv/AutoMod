package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record IgnoreEvent(
        String senderUuid,
        String targetName,
        boolean ignore
) {
    public IgnoreEvent {
        if(senderUuid == null || targetName == null) {
            throw new IllegalArgumentException("senderUuid and targetName cannot be null");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.IGNORE.getName());
        jsonObject.addProperty("senderUuid", senderUuid);
        jsonObject.addProperty("targetName", targetName);
        jsonObject.addProperty("ignore", ignore);
        return jsonObject;
    }

    public void send() {
        AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
        plugin.getRedisManager().publish(toJson());
    }
}
