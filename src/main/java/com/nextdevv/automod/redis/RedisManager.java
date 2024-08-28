package com.nextdevv.automod.redis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import com.nextdevv.automod.events.ErrorEvent;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.redis.redisdata.RedisAbstract;
import com.nextdevv.automod.redis.redisdata.RedisPubSub;
import com.nextdevv.automod.utils.ChatUtils;
import io.lettuce.core.RedisClient;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.nextdevv.automod.manager.MuteManager.handleDiscordIntegration;
import static com.nextdevv.automod.utils.ChatUtils.msg;

public class RedisManager extends RedisAbstract {
    private final AutoMod plugin;

    public RedisManager(RedisClient lettuceRedisClient, int size, AutoMod plugin) {
        super(lettuceRedisClient, size);
        this.plugin = plugin;
        subscribe();
    }

    public void publish(JsonObject jsonObject) {
        if (!plugin.getSettings().isRequiresMultiInstance()) return;
        jsonObject.addProperty("server", getServerName());
        getConnectionAsync(c -> c.publish("auto-mod", jsonObject.toString()));
    }

    public void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<>() {
                @Override
                public void message(String channel, String message) {
                    JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
                    if (jsonObject.get("server").getAsString().equals(getServerName())) return;

                    plugin.getLogger().finest("Received message from " + jsonObject.get("server").getAsString());
                    plugin.getLogger().finest("Message: " + jsonObject);

                    ModEvent event = ModEvent.valueOf(jsonObject.get("event").getAsString());
                    UUID target = jsonObject.has("player") ? UUID.fromString(jsonObject.get("player").getAsString()) : null;

                    handleEvent(event, jsonObject, target);
                }
            });
            c.async().subscribe("auto-mod");
        });
    }

    private void handleEvent(ModEvent event, JsonObject jsonObject, UUID target) {
        switch (event) {
            case MUTE -> MuteManager.mutePlayer(target);
            case UNMUTE -> MuteManager.unmutePlayer(target);
            case WARN -> handleWarnEvent(target);
            case CLEAR_WARNINGS -> MuteManager.clearWarnings(target);
            case CHAT -> handleChatEvent(jsonObject);
            case MSG -> handleMessageEvent(jsonObject);
            case NOTIFY -> handleNotifyEvent(jsonObject);
            case ERROR, SUCCESS -> handleErrorOrSuccessEvent(jsonObject);
        }
    }

    private void handleWarnEvent(UUID target) {
        MuteManager.warnPlayer(target);
        if (MuteManager.getWarnings(target) >= 2) {
            MuteManager.mutePlayer(target);
        }
    }

    private void handleChatEvent(JsonObject jsonObject) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            String message = player.hasPermission("automod.staff") ?
                    jsonObject.get("unfilteredMessage").getAsString() :
                    jsonObject.get("message").getAsString();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        });
    }

    private void handleMessageEvent(JsonObject jsonObject) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getName().equalsIgnoreCase(jsonObject.get("receiver").getAsString())) {
                if (isPlayerIgnoring(jsonObject, player)) return;
                sendPrivateMessage(jsonObject);
                handleDiscordIntegration("AutoMod", "Private message from " + jsonObject.get("sender").getAsString()
                        + " to " + jsonObject.get("receiver").getAsString(), Color.GREEN, jsonObject.get("message").getAsString());
            }
        });
        new ErrorEvent("Player not online.", jsonObject.get("sender").getAsString()).send();
    }

    private boolean isPlayerIgnoring(JsonObject jsonObject, org.bukkit.entity.Player player) {
        AtomicBoolean isIgnoring = new AtomicBoolean(false);
        plugin.getIgnores().getIgnores().stream()
                .filter(i -> i.uuid().equals(player.getUniqueId().toString()))
                .forEach(i -> {
                    if (i.ignoredPlayers().contains(jsonObject.get("senderUuid").getAsString())) {
                        new ErrorEvent("Player is ignoring you.", jsonObject.get("sender").getAsString()).send();
                        isIgnoring.set(true);
                    }
                });
        return isIgnoring.get();
    }

    private void sendPrivateMessage(JsonObject jsonObject) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("automod.spy-dms")) {
                msg(player, plugin.getSettings().getPrivateMessagesFormat()
                        .replace("{sender}", jsonObject.get("sender").getAsString())
                        .replace("{receiver}", jsonObject.get("receiver").getAsString())
                        .replace("{message}", jsonObject.get("message").getAsString()));
            }
        });
        plugin.getMessagesManager().sendMessage(jsonObject.get("sender").getAsString(), jsonObject.get("receiver").getAsString(), jsonObject.get("message").getAsString());
    }

    private void handleNotifyEvent(JsonObject jsonObject) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("automod.staff")) {
                ChatUtils.msg(player, plugin.getMessages().getNotifyStaff()
                        .replace("{sender}", jsonObject.get("sender").getAsString())
                        .replace("{message}", jsonObject.get("message").getAsString()));
            }
        });
    }

    private void handleErrorOrSuccessEvent(JsonObject jsonObject) {
        ChatUtils.msg(Objects.requireNonNull(Bukkit.getPlayer(jsonObject.get("receiver").getAsString())),
                jsonObject.get("message").getAsString());
    }

    public String getServerName() {
        return new File(System.getProperty("user.dir")).getName();
    }
}