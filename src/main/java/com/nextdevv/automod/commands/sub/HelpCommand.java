package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.CommandManager;
import com.nextdevv.automod.commands.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class HelpCommand implements ICommand {
    AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows a list of commands.";
    }

    @Override
    public String getUsage() {
        return "help";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandManager manager = plugin.getCommandManager();
        msg(sender, "&7=== &eAutoMod+ Commands (by NextDevv) &7===");
        manager.getCommands().forEach(command -> {
            msg(sender, "&e" + command.getUsage() + " &7- " + command.getDescription());
        });
    }
}
