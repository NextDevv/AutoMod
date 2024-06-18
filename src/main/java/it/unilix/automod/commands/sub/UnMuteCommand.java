package it.unilix.automod.commands.sub;

import it.unilix.automod.commands.ICommand;
import it.unilix.automod.utils.ChatUtils;
import it.unilix.automod.utils.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static javax.swing.UIManager.put;

public class UnMuteCommand implements ICommand {
    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Unmute a player.";
    }

    @Override
    public String getUsage() {
        return "/automod unmute <player>";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        HashMap<Integer, String> args = new HashMap<>();
        args.put(1, "<player>");
        return args;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length < 1) {
            ChatUtils.msg(sender, "Usage: /automod unmute <player>");
            return;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        if(player == null) {
            ChatUtils.msg(sender, "Player not found.");
            return;
        }

        MuteManager.unmutePlayer(player.getUniqueId());
        ChatUtils.msg(sender, "Player unmuted.");
    }
}
