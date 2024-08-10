package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.utils.ListUtils;
import com.nextdevv.automod.utils.TimeConverter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class MuteCommand implements ICommand {
    AutoMod plugin = AutoMod.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Mute a player.";
    }

    @Override
    public String getUsage() {
        return "/automod mute <player> <time> [reason]";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        HashMap<Integer, String> args = new HashMap<>();
        args.put(1, "<player>");
        args.put(2, "<time>");
        args.put(3, "[reason]");
        return args;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("automod.mute")) {
            sender.sendMessage("You do not have permission to execute this command.");
            return;
        }

        if(args.length < 2) {
            sender.sendMessage("Usage: /automod mute <player> <time> [reason]");
            return;
        }

        String playerName = args[0];
        Player player = sender.getServer().getPlayer(playerName);
        if(player == null) {
            msg(sender, plugin.getMessages().getPlayerNotOnline());
            return;
        }

        long time = TimeConverter.convertToSeconds(args[1]) * 1000L;
        String reason = args.length > 2 ? ListUtils.toString(ListUtils.subArray(args, 2, args.length), " ")
                : "No reason provided by "+sender.getName()+".";

        MuteManager.mutePlayer(playerName, time, reason);
        msg(sender, plugin.getMessages().getMute()
                .replace("{player}", player.getName())
                .replace("{time}", args[1])
                .replace("{reason}", reason));
    }
}
