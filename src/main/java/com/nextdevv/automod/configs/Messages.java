package com.nextdevv.automod.configs;

import it.unilix.yaml.YamlComment;
import lombok.Getter;

@Getter
public class Messages {
    @YamlComment("Prefix for all messages.")
    String prefix = "&7[&6AutoMod&7]&r";
    @YamlComment("Notify the player when they are muted.")
    String muted = "{prefix} You are muted!";
    @YamlComment("Notify the player when they are unmuted.")
    String unmuted = "{prefix} You are no longer muted!";
    @YamlComment("Notify the player when they are warned.")
    String warned = "{prefix} You have been warned!";
    @YamlComment("Usage message for the /automod command.")
    String usage = "{prefix} &cUsage: /automod <subcommand> [args]";
    @YamlComment("Notify the player when the subcommand is not found.")
    String subNotFound = "{prefix} &cSubcommand not found!";
    @YamlComment("Notify the player when their message is blocked.")
    String blockMessage = "{prefix} &cYour message has been blocked!";
    @YamlComment("Notify the player when they try to send messages too fast.")
    String spamming = "{prefix} &cYou are spamming!";
    @YamlComment("Notify the player when they try to send commands too fast.")
    String spammingCommand = "{prefix} &cYou are sending commands too fast.";
    @YamlComment("Notify the player when they try to execute a blacklisted command.")
    String blackListedCommand = "{prefix} &cYou are not allowed to use this command.";
    @YamlComment("Usage message for the /msg command.")
    String privateMessageUsage = "{prefix} &cUsage: /msg <player> <message>";
    @YamlComment("Notify the player when they try to message themselves.")
    String cannotMessageSelf = "{prefix} &cYou cannot message yourself!";
    @YamlComment("Notify the player when they try to message a player that is ignoring them or not online.")
    String playerNotOnline = "{prefix} &cPlayer is not online or does not exist!";
    @YamlComment("Notify the player when they are muted and cannot write signs.")
    String cannotPlaceSign = "{prefix} &cYou are muted and cannot place signs!";
    @YamlComment("Notify staff when a player says a toxic message.")
    String notifyStaff = "{prefix} &cPlayer {player} said a toxic message!";
    @YamlComment("Mute confirmation message.")
    String mute = "{prefix} &cPlayer {player} has been muted for {time}!";
    @YamlComment("Ignored Message.")
    String ignored = "{prefix} &c{player} is ignoring you!";
    @YamlComment("Ignoring all confirmation message.")
    String ignoreAll = "{prefix} &cYou are now ignoring all players!";
    @YamlComment("Not ignoring all confirmation message.")
    String notIgnoreAll = "{prefix} &cYou are no longer ignoring all players!";
    @YamlComment("Confirmation Message for the chat clear command")
    String chatClear = "{prefix} The chat has been cleared for {target}";
    @YamlComment("Notify the player that they reported a player.")
    String playerReported = "{prefix} Thank you for reporting {player}!";
    @YamlComment("Notify the player that they have already reported a player.")
    String alreadyReported = "{prefix} You have already reported this player!";
    @YamlComment("Report usage message.")
    String reportUsage = "{prefix} &cUsage: /report <player> <reason>";
    @YamlComment("Notify the player when they try to report themselves.")
    String cantReportSelf = "{prefix} &cYou cannot report yourself!";
    @YamlComment("Notify the player when they report a player.")
    String reportSuccess = "{prefix} &aPlayer has been reported!";
    @YamlComment("Warn the player when he uses too much caps.")
    String caps = "{prefix} &cToo much caps!";
    @YamlComment("Notify the player when they try to use a username that is too long.")
    String usernameTooLong = "{prefix} &cUsername is too long!";
    @YamlComment("Notify the player when its username is not allowed.")
    String blacklistedUsername = "{prefix} Your username is not allowed!";
    @YamlComment("Error message when executing chat logs command")
    String noChatLogsFound = "{prefix} No chat logs were found.";
    @YamlComment("Error message when the page is not found.")
    String pageNotFound = "{prefix} &cPage not found!";
}
