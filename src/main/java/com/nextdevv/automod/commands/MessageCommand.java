package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.events.MsgEvent;
import com.nextdevv.automod.manager.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class MessageCommand implements CommandExecutor, TabExecutor {
    private final AutoMod plugin;

    public MessageCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("DuplicatedCode")
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
        if(args.length < 2) {
            msg(sender, plugin.getMessages().getPrivateMessageUsage());
            return true;
        }

        String targetName = args[0];
        if(targetName.equalsIgnoreCase(senderName)) {
            msg(sender, plugin.getMessages().getCannotMessageSelf());
            return true;
        }

        String message = String.join(" ", List.of(args).subList(1, args.length));

        if(plugin.getServer().getPlayer(targetName) == null) {
            if(plugin.getSettings().isRequiresMultiInstance()) {
                MsgEvent event = new MsgEvent(message, senderName, targetName, sender instanceof HumanEntity ? ((HumanEntity) sender).getUniqueId().toString() : null);
                plugin.getRedisManager().publish(event.toJson());
            }else {
                msg(sender, plugin.getMessages().getPlayerNotOnline());
                return true;
            }
        }

        msg(sender, plugin.getSettings().getPrivateMessagesFormat()
                .replace("{sender}", senderName)
                .replace("{receiver}", targetName)
                .replace("{message}", message)
        );
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.hasPermission("automod.spy-dms")) {
                msg(player, plugin.getSettings().getPrivateMessagesFormat()
                        .replace("{sender}", senderName)
                        .replace("{receiver}", targetName)
                        .replace("{message}", message)
                );
            }
        });
        plugin.getMessagesManager().sendMessage(senderName, targetName, message);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .toList();
        }
        return List.of();
    }
}
