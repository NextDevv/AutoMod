package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ClearChatCommand implements ICommand {
    AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "clearchat";
    }

    @Override
    public String getDescription() {
        return "Clears the chat.";
    }

    @Override
    public String getUsage() {
        return "clearchat <player, @s, @a>";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        HashMap<Integer, String> args = new HashMap<>();
        args.put(1, "<player, @s, @a>");
        return args;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String target = args.length > 0 ? args[0] : "@a";
        switch (target) {
            case "@a" -> {
                for (int i = 0; i < 100; i++) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(""));
                }
            }
            case "@s" -> {
                for (int i = 0; i < 100; i++) {
                    sender.sendMessage("");
                }
            }
            default -> {
                Player player = sender.getServer().getPlayer(target);
                if (player != null) {
                    for (int i = 0; i < 100; i++) {
                        player.sendMessage("");
                    }
                } else msg(sender, plugin.getMessages().getPlayerNotOnline());
            }
        }

        msg(sender, plugin.getMessages().getChatClear()
                .replace("{target}", target));
    }
}
