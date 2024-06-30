package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import com.nextdevv.automod.configs.ConfigLoader;
import com.nextdevv.automod.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

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
        ChatUtils.msg(sender, "&aReloading configs...");
        ConfigLoader loader = new ConfigLoader(plugin);
        plugin.setSettings(loader.loadSettings());
        plugin.setMessages(loader.loadMessages());
        ChatUtils.msg(sender, "&aConfigs reloaded!");
    }
}
