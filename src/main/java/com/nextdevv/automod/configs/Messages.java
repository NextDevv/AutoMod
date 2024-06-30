package com.nextdevv.automod.configs;

import lombok.Getter;

@Getter
public class Messages {
    String muted = "You are muted!";
    String unmuted = "You are no longer muted!";
    String warned = "You have been warned!";
    String usage = "&cUsage: /automod <subcommand> [args]";
    String subNotFound = "&cSubcommand not found!";
    String blockMessage = "&cYour message has been blocked!";
    String spamming = "&cYou are spamming!";
    String spammingCommand = "&cYou are sending commands too fast.";
    String blackListedCommand = "&cYou are not allowed to use this command.";
    String privateMessageUsage = "&cUsage: /msg <player> <message>";
    String cannotMessageSelf = "&cYou cannot message yourself!";
    String playerNotOnline = "&cPlayer is not online!";
}
