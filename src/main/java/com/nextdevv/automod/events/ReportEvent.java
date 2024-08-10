package com.nextdevv.automod.events;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import org.bukkit.plugin.java.JavaPlugin;

public record ReportEvent(String reporter, String player, String reason) {
    public ReportEvent {
        if (reporter == null || reporter.isEmpty()) {
            throw new IllegalArgumentException("Reporter cannot be null or empty.");
        }
        if (player == null || player.isEmpty()) {
            throw new IllegalArgumentException("Player cannot be null or empty.");
        }
        if (reason == null || reason.isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty.");
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.REPORT.getName());
        jsonObject.addProperty("reporter", reporter);
        jsonObject.addProperty("player", player);
        jsonObject.addProperty("reason", reason);
        return jsonObject;
    }

    public void send() {
        AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
        plugin.getRedisManager().publish(toJson());
    }
}
