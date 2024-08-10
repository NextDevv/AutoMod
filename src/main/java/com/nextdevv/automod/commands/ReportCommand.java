package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.events.ReportEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ReportCommand implements CommandExecutor, TabExecutor {
    final AutoMod plugin;

    public ReportCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) {
            return true;
        }

        if(strings.length < 2) {
            msg(player, plugin.getMessages().getReportUsage());
            return true;
        }

        String targetName = strings[0];
        String message = String.join(" ", strings).replace(targetName + " ", "");

        ReportEvent report = new ReportEvent(player.getName(), targetName, message);
        if(plugin.getSettings().isRequiresMultiInstance()) {
            report.send();
        }

        msg(player, plugin.getMessages().getReportSuccess());
        plugin.getReportManager().add(report);
        plugin.getReportManager().notify(report);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        } else if(strings.length >= 2) {
            return Arrays.stream(plugin.getSettings().getDefaultReports()).toList();
        }

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
