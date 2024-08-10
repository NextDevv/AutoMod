package com.nextdevv.automod.commands;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.utils.ListUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
public class CommandManager implements CommandExecutor, TabExecutor {
    private final List<ICommand> commands = new ArrayList<>();
    private final AutoMod plugin;

    public CommandManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(ICommand command) {
        commands.add(command);
    }

    public ICommand getCommand(String name) {
        return commands.stream().filter(command -> command.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String sub = args.length > 0 ? args[0] : "";
        if(sub.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getUsage()));
            return false;
        }

        ICommand cmd = getCommand(sub);
        if(cmd == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getSubNotFound()));
            return false;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        cmd.execute(sender, newArgs);

        return true;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For player tab-completing a
     *                command inside a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            commands.forEach(cmd -> subCommands.add(cmd.getName()));
            return subCommands;
        }

        String sub = args[0];
        ICommand cmd = getCommand(sub);
        if(cmd != null) {
            HashMap<Integer, String> arguments = cmd.getArgs();
            if(arguments.isEmpty()) return List.of();

            List<String> completions = new ArrayList<>();
            arguments.forEach((index, arg) -> {
                if(args.length - 1 == index) {
                    String add = arg;
                    add = switch (add) {
                        case "<player, @s, @a>" -> {
                            List<String> players = new ArrayList<>();
                            players.add("@a");
                            players.add("@s");
                            players.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                            yield ListUtils.toString(players.toArray(String[]::new), ", ");
                        }
                        case "<player>" ->
                                ListUtils.toString(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new), ", ");
                        case "<time>" -> "1d, 1h, 1m, 1s";
                        default -> add;
                    };

                    completions.addAll(Arrays.stream((add.split(", "))).toList());
                }
            });

            return completions;
        }

        return List.of();
    }
}
