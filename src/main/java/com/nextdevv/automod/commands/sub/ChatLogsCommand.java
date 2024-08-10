package com.nextdevv.automod.commands.sub;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ChatLogsCommand implements ICommand {
    private final AutoMod plugin = AutoMod.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "chat-logs";
    }

    @Override
    public String getDescription() {
        return "Returns the chat logs";
    }

    @Override
    public String getUsage() {
        return "chat-logs [save]";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        LinkedHashMap<Integer, String> args = new LinkedHashMap<>();
        args.put(1, "save");
        return args;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> logs = plugin.getChatLogger().getChatLogs();
        if (logs.isEmpty()) {
            msg(sender, plugin.getMessages().getNoChatLogsFound());
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
            plugin.getChatLogger().save();
            msg(sender, "&aChat logs saved.");
            return;
        }

        int index = 0;
        if(args.length == 1 && args[0].matches("\\d+")) {
            index = Integer.parseInt(args[0]) - 1;
        }

        List<List<String>> pages = plugin.getChatLogger().paginate(logs, 10);
        if (index < 0 || index >= pages.size()) {
            msg(sender, plugin.getMessages().getPageNotFound());
            return;
        }

        List<String> page = pages.get(index);
        msg(sender, "&7Page " + (index + 1) + "/" + pages.size() + ":");
        for (String line : page) {
            msg(sender, line);
        }
        msg(sender, "&7Type &f/am chat-logs <page> &7to view a specific page.");
    }
}
