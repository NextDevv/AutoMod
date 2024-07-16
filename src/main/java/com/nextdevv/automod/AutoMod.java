package com.nextdevv.automod;

import com.google.gson.Gson;
import com.nextdevv.automod.configs.ConfigLoader;
import com.nextdevv.automod.listeners.AsyncPlayerChatListener;
import com.nextdevv.automod.manager.CacheManager;
import com.nextdevv.automod.utils.VersionChecker;
import io.lettuce.core.RedisClient;
import com.nextdevv.automod.api.LiteBans;
import com.nextdevv.automod.api.PerspectiveAPI;
import com.nextdevv.automod.commands.CommandManager;
import com.nextdevv.automod.commands.sub.ReloadCommand;
import com.nextdevv.automod.commands.sub.StatusCommand;
import com.nextdevv.automod.commands.sub.UnMuteCommand;
import com.nextdevv.automod.configs.Messages;
import com.nextdevv.automod.configs.Settings;
import com.nextdevv.automod.listeners.PlayerCommandPreprocessListener;
import com.nextdevv.automod.metrics.Metrics;
import com.nextdevv.automod.redis.RedisManager;
import com.nextdevv.automod.utils.ApiKeyValidator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public final class AutoMod extends JavaPlugin {
    @Setter private Settings settings;
    @Setter private Messages messages;
    private RedisManager redisManager;
    private PerspectiveAPI perspectiveAPI;
    private LiteBans liteBans;
    private VersionChecker versionChecker;

    public static AutoMod instance;
    public AutoMod() {
        instance = this;
    }

    private final CacheManager cacheManager = new CacheManager(this);
    private final CommandManager commandManager = new CommandManager(this);
    private AsyncPlayerChatListener asyncPlayerChatListener;

    @Override
    public void onEnable() {
        getLogger().info("=== AutoMod ===");
        printDisclaimer();

        loadConfigurations();
        if (!validateApiKey()) {
            disablePlugin("You need to set your Perspective API key in the config.yml file.");
            return;
        }

        if (!initializeRedis()) {
            disablePlugin("Failed to connect to Redis. Make sure to have a Redis server running.");
            return;
        }

        registerListeners();
        connectToPerspectiveAPI();
        loadCaches();
        setupCommands();
        hookIntoLiteBans();
        enableMetrics();
        checkUpdates();

        getLogger().info("AutoMod has been enabled.");
        getLogger().info("=== AutoMod ===");
    }

    private void checkUpdates() {
        getLogger().info("Checking for updates...");
        versionChecker = new VersionChecker("NextDevv/AutoMod", this);
        getServer().getPluginManager().registerEvents(versionChecker, this);
        versionChecker.checkVersion(getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("=== AutoMod ===");

        if (redisManager != null) {
            getLogger().info("Shutting down Redis connection...");
            redisManager.close();
        }

        getLogger().info("Saving caches...");
        if (cacheManager.folderInitialized()) {
            cacheManager.save();
        } else {
            getLogger().warning("Cache folder is not initialized. Caches will not be saved.");
        }

        getLogger().info("AutoMod has been disabled.");
        getLogger().info("=== AutoMod ===");
    }

    private void printDisclaimer() {
        getLogger().info("§cDISCLAIMER: This plugin uses the Perspective API by Google. By using this plugin, you agree to Google's Privacy Policy.");
        getLogger().info("§cAnd make sure to have a fast internet connection, as this plugin uses a lot of network I/O.");
    }

    private void loadConfigurations() {
        getLogger().info("Loading settings & messages...");
        ConfigLoader configLoader = new ConfigLoader(this);

        settings = configLoader.loadSettings();
        messages = configLoader.loadMessages();

        if(settings.isDebug()) {
            getLogger().info("Loaded settings: " + settings);
            getLogger().info("Loaded messages: " + messages);
        }
    }

    private void enableMetrics() {
        getLogger().info("Enabling basic metrics information...");
        Metrics metrics = new Metrics(this, 22399);
        metrics.addCustomChart(new Metrics.SimplePie("block_message", () -> getCacheManager().getCacheList().size() + " blocked messages"));
    }

    private boolean validateApiKey() {
        getLogger().info("Validating API KEY...");
        if (settings.getPerspectiveApiKey().equals("YOUR_API_KEY")
                || !ApiKeyValidator.isFormatValid(settings.getPerspectiveApiKey())) {
            getLogger().warning("Invalid Perspective API key.");
            return false;
        }
        return true;
    }

    private boolean initializeRedis() {
        getLogger().info("Connecting to Redis...");
        if (settings.isRequiresMultiInstance()) {
            try {
                RedisClient redisClient = RedisClient.create(settings.getRedisUri());
                redisManager = new RedisManager(redisClient, 4, this);
            } catch (Exception e) {
                getLogger().severe("Error connecting to Redis: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private void registerListeners() {
        getLogger().info("Registering listeners...");
        asyncPlayerChatListener = new AsyncPlayerChatListener(this);
        getServer().getPluginManager().registerEvents(asyncPlayerChatListener, this);
        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocessListener(getMessages(), getSettings()), this);
        // TODO: Fix this
        // getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
    }

    private void connectToPerspectiveAPI() {
        getLogger().info("Connecting to Perspective API...");
        perspectiveAPI = new PerspectiveAPI(settings.getPerspectiveApiKey(), settings);
    }

    private void loadCaches() {
        getLogger().info("Loading caches...");
        cacheManager.load();
    }

    private void setupCommands() {
        getLogger().info("Loading commands...");
        Objects.requireNonNull(getCommand("automod")).setExecutor(commandManager);
        Objects.requireNonNull(getCommand("automod")).setTabCompleter(commandManager);

        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new UnMuteCommand());
        commandManager.registerCommand(new StatusCommand());
    }

    private void hookIntoLiteBans() {
        getLogger().info("Hooking into LiteBans...");
        if (getServer().getPluginManager().getPlugin("LiteBans") != null) {
            getLogger().info("LiteBans has been found. Hooking into LiteBans...");
            liteBans = new LiteBans(this);
            liteBans.register();
        } else {
            getLogger().warning("LiteBans has not been found. Some features may not work properly.");
        }
    }

    private void disablePlugin(String message) {
        getLogger().warning(message);
        getLogger().warning("Disabling AutoMod...");
        getServer().getPluginManager().disablePlugin(this);
    }

    public Gson getGson() {
        return new Gson();
    }
}