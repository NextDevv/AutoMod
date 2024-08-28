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
    private final AutoMod plugin;

    public ReportCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length < 2) {
            msg(player, plugin.getMessages().getReportUsage());
            return true;
        }

        String targetName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        ReportEvent report = new ReportEvent(player.getName(), targetName, message);
        if (plugin.getSettings().isRequiresMultiInstance()) {
            report.send();
        }

        msg(player, plugin.getMessages().getReportSuccess());
        plugin.getReportManager().add(report);
        plugin.getReportManager().notify(report);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        } else if (args.length >= 2) {
            return Arrays.asList(plugin.getSettings().getDefaultReports());
        }

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}