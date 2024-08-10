package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.utils.ListUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class WarnCommand implements ICommand {
    AutoMod plugin = AutoMod.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "warn";
    }

    @Override
    public String getDescription() {
        return "Warn a player.";
    }

    @Override
    public String getUsage() {
        return "/automod warn <player> [reason]";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        HashMap<Integer, String> args = new HashMap<>();
        args.put(1, "<player>");
        args.put(2, "[reason]");
        return args;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("automod.warn")) {
            sender.sendMessage("You do not have permission to execute this command.");
            return;
        }

        if(args.length < 1) {
            sender.sendMessage("Usage: /automod warn <player> [reason]");
            return;
        }

        String playerName = args[0];
        Player player = sender.getServer().getPlayer(playerName);
        if(player == null) {
            sender.sendMessage(plugin.getMessages().getPlayerNotOnline());
            return;
        }

        String reason = args.length > 1 ? ListUtils.toString(ListUtils.subArray(args, 1, args.length), " ")
                : "No reason provided.";
        MuteManager.warnPlayer(player.getUniqueId(), reason);
        sender.sendMessage("Player warned.");
    }
}
