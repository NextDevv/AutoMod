package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import com.nextdevv.automod.configs.ConfigLoader;
import com.nextdevv.automod.configs.Settings;
import com.nextdevv.automod.utils.ApiKeyValidator;
import com.nextdevv.automod.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ReloadCommand implements ICommand {
    private final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads the plugin";
    }

    @Override
    public String getUsage() {
        return "reload";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        msg(sender, "&eReloading configs...");
        ConfigLoader loader = new ConfigLoader(plugin);
        Settings settings = plugin.getSettings().clone();
        plugin.setSettings(loader.loadSettings());

        msg(sender, "&eVerifying API key...");
        String apiKey = plugin.getSettings().getPerspectiveApiKey();
        if (Objects.equals(apiKey, "YOUR_API_KEY") || !ApiKeyValidator.isFormatValid(apiKey)) {
            msg(sender, "&cInvalid API key! Please set a valid API key in the config.");
            msg(sender, "&cRolling back changes...");
            plugin.setSettings(settings);
            return;
        }

        plugin.setMessages(loader.loadMessages());
        plugin.setIgnores(loader.loadIgnores());
        plugin.setPlayerModStatus(loader.loadPlayerModStatus());

        msg(sender, "&eLoading Cache...");
        plugin.getCacheManager().load();

        msg(sender, "&eEnabling discord webhook...");
        plugin.enableDiscordWebhook();

        msg(sender, "&aConfigs reloaded!");
    }
}
