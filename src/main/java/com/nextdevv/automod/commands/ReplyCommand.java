package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.events.MsgEvent;
import com.nextdevv.automod.manager.MuteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ReplyCommand implements CommandExecutor, TabExecutor {
    final AutoMod plugin;

    public ReplyCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!plugin.getSettings().isPrivateMessaging()) return true;

        if(sender instanceof Player player) {
            MuteManager.checkPlayer(player.getUniqueId());
            if(!plugin.getSettings().isMutedCanSendPrivateMessages()) {
                if(MuteManager.isMuted(player.getUniqueId())) {
                    msg(player, plugin.getMessages().getMuted());
                    return true;
                }
            }
        }

        String senderName = sender.getName();
        if(args.length < 1) {
            msg(sender, "Usage: /reply <message>");
            return true;
        }

        String targetName = plugin.getMessagesManager().getLastChatter(senderName);
        if(targetName == null) {
            msg(sender, "You have no one to reply to.");
            return true;
        }

        String message = String.join(" ", args);

        if(plugin.getServer().getPlayer(targetName) == null) {
            if (plugin.getSettings().isRequiresMultiInstance()) {
                MsgEvent event = new MsgEvent(message, senderName, targetName, sender instanceof org.bukkit.entity.HumanEntity ? ((org.bukkit.entity.HumanEntity) sender).getUniqueId().toString() : null);
                plugin.getRedisManager().publish(event.toJson());
            } else if(!targetName.equals("CONSOLE")) {
                msg(sender, "Player not online.");
                return true;
            }
        }

        msg(sender, plugin.getSettings().getPrivateMessagesFormat()
                .replace("{sender}", senderName)
                .replace("{receiver}", targetName)
                .replace("{message}", message)
        );
        plugin.getMessagesManager().sendMessage(senderName, targetName, message);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}
