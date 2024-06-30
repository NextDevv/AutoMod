package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import com.nextdevv.automod.utils.ChatUtils;
import com.nextdevv.automod.models.MsgEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrivateMessageCommand implements CommandExecutor, TabExecutor {
    private final AutoMod plugin;

    public PrivateMessageCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!plugin.getSettings().isPrivateMessaging()) return true;
        
        String senderName = sender.getName();
        if(args.length < 2) {
            ChatUtils.msg(sender, plugin.getMessages().getPrivateMessageUsage());
            return true;
        }

        String targetName = args[0];
        if(targetName.equalsIgnoreCase(senderName)) {
            ChatUtils.msg(sender, plugin.getMessages().getCannotMessageSelf());
            return true;
        }

        String message = String.join(" ", List.of(args).subList(1, args.length));

        if(plugin.getServer().getPlayer(targetName) == null) {
            if(plugin.getSettings().isRequiresMultiInstance()) {
                MsgEvent event = new MsgEvent(message, senderName, targetName, ModEvent.MSG);
                plugin.getRedisManager().publish(event.toJson());
            }else {
                ChatUtils.msg(sender, plugin.getMessages().getPlayerNotOnline());
                return true;
            }
        }

        ChatUtils.msg(sender, plugin.getSettings().getPrivateMessagesFormat()
                .replace("{sender}", senderName)
                .replace("{receiver}", targetName)
                .replace("{message}", message)
        );

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
