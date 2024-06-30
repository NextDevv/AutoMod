package com.nextdevv.automod.commands;

import org.bukkit.command.CommandSender;

import java.util.HashMap;

public interface ICommand {
    String getName();
    String getDescription();
    String getUsage();
    HashMap<Integer, String> getArgs();
    void execute(CommandSender sender, String[] args);
}
