package it.unilix.automod;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import it.unilix.automod.api.LiteBans;
import it.unilix.automod.api.PerspectiveAPI;
import it.unilix.automod.commands.CommandManager;
import it.unilix.automod.commands.sub.ReloadCommand;
import it.unilix.automod.configs.ConfigLoader;
import it.unilix.automod.configs.Messages;
import it.unilix.automod.configs.Settings;
import it.unilix.automod.listeners.AsyncPlayerChatListener;
import it.unilix.automod.manager.CacheManager;
import it.unilix.automod.redis.RedisManager;
import it.unilix.automod.utils.ApiKeyValidator;
import it.unilix.automod.utils.MuteManager;
import litebans.api.Database;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

@Getter
public final class AutoMod extends JavaPlugin {
    @Setter
    private Settings settings;
    @Setter
    private Messages messages;
    private RedisManager redisManager;
    private PerspectiveAPI perspectiveAPI;
    private LiteBans liteBans;

    private final CacheManager cacheManager = new CacheManager(this);
    private final CommandManager commandManager = new CommandManager(this);

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
            if(settings.isRequiresMultiInstance()) {
                RedisClient redisClient = RedisClient.create(settings.getRedisUri());
                redisManager = new RedisManager(redisClient, 4, this);
            }
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

        getLogger().info("Loading commands...");
        Objects.requireNonNull(getCommand("automod")).setExecutor(commandManager);
        Objects.requireNonNull(getCommand("automod")).setTabCompleter(commandManager);
        commandManager.registerCommand(new ReloadCommand());

        getLogger().info("Hooking into LiteBans...");
        if(getServer().getPluginManager().getPlugin("LiteBans") != null) {
            getLogger().info("LiteBans has been found. Hooking into LiteBans...");
            liteBans = new LiteBans(JavaPlugin.getPlugin(AutoMod.class));
            liteBans.register();
        }else {
            getLogger().warning("LiteBans has not been found. Some features may not work properly.");
        }

        getLogger().info("AutoMod has been enabled.");
        getLogger().info("=== AutoMod ===");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("=== AutoMod ===");

        getLogger().info("Shutting down Redis connection...");
        if(redisManager != null)
            redisManager.close();

        getLogger().info("Saving caches...");
        if(cacheManager.folderInitialized())
            cacheManager.save();
        else getLogger().warning("Cache folder is not initialized. Caches will not be saved.");

        getLogger().info("AutoMod has been disabled.");
        getLogger().info("=== AutoMod ===");
    }

    public Gson getGson() {
        return new Gson();
    }
}
