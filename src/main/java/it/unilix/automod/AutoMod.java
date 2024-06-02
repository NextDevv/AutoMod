package it.unilix.automod;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import it.unilix.automod.api.PerspectiveAPI;
import it.unilix.automod.configs.ConfigLoader;
import it.unilix.automod.configs.Messages;
import it.unilix.automod.configs.Settings;
import it.unilix.automod.listeners.AsyncPlayerChatListener;
import it.unilix.automod.manager.CacheManager;
import it.unilix.automod.redis.RedisManager;
import it.unilix.automod.utils.ApiKeyValidator;
import it.unilix.automod.utils.MuteManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class AutoMod extends JavaPlugin {
    private Settings settings;
    private Messages messages;
    private RedisManager redisManager;
    private PerspectiveAPI perspectiveAPI;
    private final CacheManager cacheManager = new CacheManager(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("=== AutoMod ===");
        getLogger().info("DISCLAIMER: This plugin requires a Redis server to be running. Make sure to have one running before enabling this plugin.");
        getLogger().info("And make sure to have a fast internet connection, as this plugin uses a lot of network I/O.");
        getLogger().info("");

        getLogger().info("Loading settings & messages...");
        ConfigLoader configLoader = new ConfigLoader(this);
        settings = configLoader.loadSettings();
        messages = configLoader.loadMessages();

        getLogger().info("Validating API KEY...");
        if (settings.getPerspectiveApiKey().equals("YOUR_API_KEY")
                || !ApiKeyValidator.isFormatValid(settings.getPerspectiveApiKey())) {
            getLogger().warning("You need to set your Perspective API key in the config.yml file.");
            getLogger().warning("Disabling AutoMod...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Connecting to Redis...");
        try {
            RedisClient redisClient = RedisClient.create(settings.getRedisUri());
            redisManager = new RedisManager(redisClient, 4, this);
        }catch (Exception e) {
            getLogger().severe("Failed to connect to Redis. Make sure to have a Redis server running.");
            getLogger().severe("Disabling AutoMod...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Starting updaters...");

        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);

        getLogger().info("Connecting to Perspective API...");
        perspectiveAPI = new PerspectiveAPI(settings.getPerspectiveApiKey(), settings);

        getLogger().info("Loading caches...");
        cacheManager.load();

        getLogger().info("AutoMod has been enabled.");
        getLogger().info("=== AutoMod ===");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("=== AutoMod ===");

        getLogger().info("Shutting down Redis connection...");
        redisManager.close();

        getLogger().info("Saving caches...");
        cacheManager.save();

        getLogger().info("AutoMod has been disabled.");
        getLogger().info("=== AutoMod ===");
    }

    public Gson getGson() {
        return new Gson();
    }
}
