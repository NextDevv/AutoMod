package com.nextdevv.automod;

import com.google.gson.Gson;
import com.nextdevv.automod.api.*;
import com.nextdevv.automod.commands.*;
import com.nextdevv.automod.commands.sub.*;
import com.nextdevv.automod.configs.*;
import com.nextdevv.automod.listeners.*;
import com.nextdevv.automod.logger.ChatLogger;
import com.nextdevv.automod.manager.*;
import com.nextdevv.automod.metrics.Metrics;
import com.nextdevv.automod.redis.RedisManager;
import com.nextdevv.automod.utils.*;
import github.scarsz.discordsrv.DiscordSRV;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import static com.nextdevv.automod.manager.MuteManager.handleDiscordIntegration;

@Getter
public final class AutoMod extends JavaPlugin {
    @Setter private Settings settings;
    @Setter private Messages messages;
    @Setter private Ignores ignores;
    @Setter private PlayerModStatus playerModStatus;
    @Setter private DiscordWebhook discordWebhook;
    @Setter private DiscordWebhook reportDiscordWebhook;
    private RedisManager redisManager;
    private PerspectiveAPI perspectiveAPI;
    private VpnProxyDetector vpnProxyDetector;
    private LiteBans liteBans;
    private VersionChecker versionChecker;
    private ChatLogger chatLogger;

    public static AutoMod instance;
    public AutoMod() {
        instance = this;
    }

    private final CacheManager cacheManager = new CacheManager(this);
    private final CommandManager commandManager = new CommandManager(this);
    private final MessagesManager messagesManager = new MessagesManager(this);
    private final ReportManager reportManager = new ReportManager(this);

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
        enableVpnProxyDetector();
        loadCaches();
        setupCommands();
        hookIntoLiteBans();
        enableChatLogger();
        enableDiscordWebhook();
        checkDiscordSRVIntegration();
        enableMetrics();
        checkUpdates();

        getLogger().info("AutoMod has been enabled.");
        getLogger().info("=== AutoMod ===");
    }

    private void enableVpnProxyDetector() {
        getLogger().info("Enabling VPN Proxy detector...");
        vpnProxyDetector = new VpnProxyDetector(this);
    }

    public void enableDiscordWebhook() {
        if(!settings.isDiscordIntegration()) return;

        getLogger().info("Enabling Discord webhook...");
        discordWebhook = new DiscordWebhook(settings.getDiscordWebhook());
        reportDiscordWebhook = new DiscordWebhook(settings.getReportDiscordWebhook());

        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        embedObject.setColor(Color.GREEN);
        embedObject.setDescription("AutoMod has been enabled.");

        discordWebhook.setUsername("AutoMod");
        discordWebhook.addEmbed(embedObject);
        reportDiscordWebhook.setUsername("AutoMod");
        reportDiscordWebhook.addEmbed(embedObject);

        try {
            discordWebhook.execute();
            reportDiscordWebhook.execute();
        } catch (Exception e) {
            getLogger().warning("Failed to send message to Discord webhook: " + e.getMessage());
        }
    }

    private void enableChatLogger() {
        getLogger().info("Enabling chat logger...");
        chatLogger = new ChatLogger(this);
        chatLogger.init();
    }

    private void checkUpdates() {
        getLogger().info("Checking for updates...");
        versionChecker = new VersionChecker("NextDevv/AutoMod", this);
        getServer().getPluginManager().registerEvents(versionChecker, this);
        versionChecker.checkVersion(getDescription().getVersion());
    }

    private void checkDiscordSRVIntegration() {
        if(!settings.isDiscordSRVIntegration()) return;

        getLogger().info("Checking for DiscordSRV integration...");
        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null)
            getLogger().info("DiscordSRV has been found.");
        else getLogger().warning("DiscordSRV has not been found. Some features may not work properly.");
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

        getLogger().info("Shutting down chat logger...");
        chatLogger.save();

        ConfigLoader configLoader = new ConfigLoader(this);

        getLogger().info("Saving player mod status...");
        PlayerModStatus playerModStatus = new PlayerModStatus();
        playerModStatus.setMutedPlayers(MuteManager.getMutedPlayersSave());
        playerModStatus.setWarnedPlayers(MuteManager.getWarnedPlayersSave());
        configLoader.savePlayerModStatus(playerModStatus);

        getLogger().info("Saving ignores...");
        configLoader.saveIgnores(ignores);

        handleDiscordIntegration("AutoMod", "", Color.RED, "AutoMod has been disabled.");
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
        ignores = configLoader.loadIgnores();
        playerModStatus = configLoader.loadPlayerModStatus();
        playerModStatus.getMutedPlayers().forEach(MuteManager::mutePlayer);
        playerModStatus.getWarnedPlayers().forEach(MuteManager::warnPlayer);

        if(settings.isDebug()) {
            getLogger().info("Loaded settings: " + settings);
            getLogger().info("Loaded messages: " + messages);
            getLogger().info("Loaded ignores: " + ignores);
            getLogger().info("Loaded player mod status: " + playerModStatus);
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
                || ApiKeyValidator.isFormatValid(settings.getPerspectiveApiKey())) {
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
        getServer().getPluginManager().registerEvents(new AsyncSignChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
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

        if(settings.isPrivateMessaging()) {
            setupPrivateMessagingCommands();
        } else {
            unregisterCommand("message");
            unregisterCommand("reply");
            unregisterCommand("ignore");
        }

        if(settings.isReportSystem()) {
            setupReportCommand();
        } else {
            unregisterCommand("report");
        }

        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new UnMuteCommand());
        commandManager.registerCommand(new StatusCommand());
        commandManager.registerCommand(new MuteCommand());
        commandManager.registerCommand(new WarnCommand());
        commandManager.registerCommand(new ClearChatCommand());
        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new ChatLogsCommand());
    }

    private void setupPrivateMessagingCommands() {
        Objects.requireNonNull(getCommand("message")).setExecutor(new MessageCommand(this));
        Objects.requireNonNull(getCommand("message")).setTabCompleter(new MessageCommand(this));
        Objects.requireNonNull(getCommand("reply")).setExecutor(new ReplyCommand(this));
        Objects.requireNonNull(getCommand("reply")).setTabCompleter(new ReplyCommand(this));

        if(settings.isIgnoreEnabled()) {
            Objects.requireNonNull(getCommand("ignore")).setExecutor(new IgnoreCommand(this));
            Objects.requireNonNull(getCommand("ignore")).setTabCompleter(new IgnoreCommand(this));
        } else {
            unregisterCommand("ignore");
        }
    }

    private void setupReportCommand() {
        Objects.requireNonNull(getCommand("report")).setExecutor(new ReportCommand(this));
        Objects.requireNonNull(getCommand("report")).setTabCompleter(new ReportCommand(this));
    }

    private void unregisterCommand(String name) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.remove(name);
            getLogger().info("Disabling command > " + name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void hookIntoLiteBans() {
        if(settings.isLiteBanSupport()) {
            getLogger().info("Hooking into LiteBans...");
            if (getServer().getPluginManager().getPlugin("LiteBans") != null) {
                getLogger().info("LiteBans has been found. Hooking into LiteBans...");
                liteBans = new LiteBans(this);
                liteBans.register();
            } else {
                getLogger().warning("LiteBans has not been found. Some features may not work properly.");
            }
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