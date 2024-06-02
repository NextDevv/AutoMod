package it.unilix.automod.redis;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import it.unilix.automod.AutoMod;
import it.unilix.automod.enums.ModEvent;
import it.unilix.automod.redis.redisdata.RedisAbstract;
import it.unilix.automod.redis.redisdata.RedisPubSub;
import it.unilix.automod.utils.MuteManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.UUID;


public class RedisManager extends RedisAbstract {
    private final AutoMod plugin;

	public RedisManager(RedisClient lettuceRedisClient, int size, AutoMod plugin) {
		super(lettuceRedisClient, size);
		this.plugin = plugin;
		subscribe();
	}

    public void publish(JsonObject jsonObject) {
        if(!plugin.getSettings().isRequiresMultiInstance()) return;
        jsonObject.addProperty("server", getServerName());
        getConnectionAsync(c -> c.publish("auto-mod", jsonObject.toString()));
    }

    public void subscribe() {
        getPubSubConnection(c -> {
            c.addListener(new RedisPubSub<String, String>() {
                @Override
                public void message(String channel, String message) {
                    JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);
                    if (jsonObject.get("server").getAsString().equals(getServerName())) return;

                    plugin.getLogger().finest("Received message from " + jsonObject.get("server").getAsString());
                    plugin.getLogger().finest("Message: " + jsonObject);

                    ModEvent event = ModEvent.valueOf(jsonObject.get("event").getAsString());
                    UUID target = UUID.fromString(jsonObject.get("player").getAsString());

                    switch (event) {
                        case MUTE:
                            MuteManager.mutePlayer(target);
                            break;
                        case UNMUTE:
                            MuteManager.unmutePlayer(target);
                            break;
                        case WARN:
                            MuteManager.warnPlayer(target);
                            if(MuteManager.getWarnings(target) >= 2)
                                MuteManager.mutePlayer(target);
                            break;
                        case CLEAR_WARNINGS:
                            MuteManager.clearWarnings(target);
                            break;
                        case CHAT:
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString()));
                            break;
                    }
                }
            });
			c.async().subscribe("auto-mod");
        });
    }

	public String getServerName() {
        return new File(System.getProperty("user.dir")).getName();
    }
}
