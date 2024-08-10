package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.events.IgnoreEvent;
import com.nextdevv.automod.models.PlayerIgnore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class IgnoreCommand implements CommandExecutor, TabExecutor {
    AutoMod plugin;

    public IgnoreCommand(AutoMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1) {
            msg(sender, "Usage: /ignore <player>");
            return true;
        }

        String targetName = args[0];
        Player target = plugin.getServer().getPlayer(targetName);
        if(target == null && !targetName.equals("all")) {
            if (plugin.getSettings().isRequiresMultiInstance()) {
                if(!(sender instanceof Player player)) {
                    msg(sender, "Player not online.");
                    return true;
                }

                IgnoreEvent event = new IgnoreEvent(player.getUniqueId().toString(), targetName, true);
                event.send();
            }else {
                msg(sender, "Player not online.");
            }
            return true;
        }

        if(!(sender instanceof Player player)) {
            msg(sender, "You must be a player to ignore someone.");
            return true;
        }

        Optional<PlayerIgnore> ignore = plugin.getIgnores().getIgnores()
                .stream()
                .filter(i -> Objects.equals(i.uuid(), player.getUniqueId().toString()))
                .findAny();

        PlayerIgnore i;
        if(ignore.isPresent()) {
            i = ignore.get();
            if(targetName.equals("all")) {
                i.setIgnoreAll(!i.ignoreAll());
                if(i.ignoreAll()) {
                    msg(sender, plugin.getMessages().getIgnoreAll());
                }else msg(sender, plugin.getMessages().getNotIgnoreAll());
                return true;
            }

            if(i.ignoredPlayers().contains(target.getUniqueId().toString())) {
                boolean removed = i.ignoredPlayers().remove(target.getUniqueId().toString());
                if(removed)
                    msg(player, "You are no longer ignoring " + target.getName());
                return true;
            }

            i.ignoredPlayers().add(Objects.requireNonNull(target).getUniqueId().toString());
        }else {
            i = new PlayerIgnore(player.getUniqueId().toString(), new ArrayList<>(List.of()), false);

            if(targetName.equals("all")) {
                i.setIgnoreAll(true);
                msg(player, plugin.getMessages().getIgnoreAll());
                return true;
            }

            i.ignoredPlayers().add(target.getUniqueId().toString());
            plugin.getIgnores().getIgnores().add(i);
        }
        msg(player, "You are now ignoring " + target.getName());

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }

        return List.of();
    }
}
