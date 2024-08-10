package com.nextdevv.automod.configs;

import com.nextdevv.automod.enums.Attribute;
import com.nextdevv.automod.enums.ModerationType;
import it.unilix.yaml.YamlComment;
import lombok.Getter;

@Getter
public class Settings implements Cloneable {
    // REDIS SECTION
    @YamlComment({
            "=== REDIS SECTION ===",
            "",
            "Redis URI. If you are running multiple instances of this plugin, set this to the same URI."
    })
    String redisUri = "redis://localhost";
    @YamlComment("If you are running multiple instances of this plugin, set this to true. This will enable Redis pub/sub.")
    boolean requiresMultiInstance = false;

    // PERSPECTIVE API SECTION
    @YamlComment({
            "=== PERSPECTIVE API SECTION ===",
            "",
            "[FREE] Perspective API key, check the documentation to how to obtain it. [https://docs.nextdevv.com/automod.html]"
    })
    String perspectiveApiKey = "YOUR_API_KEY";
    @YamlComment("Perspective API threshold. If the toxicity score is greater than this value, the message will be censored.")
    Double threshold = 0.5;
    @YamlComment("Attributes to request from the Perspective API.")
    Attribute[] attributes = {Attribute.TOXICITY, Attribute.PROFANITY};

    // MODERATION SECTION
    @YamlComment({
            "=== MODERATION SECTION ===",
            "",
            "Mute time in milliseconds."
    })
    Long muteTime = 60000L;
    @YamlComment("Warn expire time in milliseconds.")
    Long warnExpireTime = 900000L;
    @YamlComment({
            "Moderation type. [TRIWM, CANCEL, CENSOR], check the documentation for more information. [https://docs.nextdevv.com/automod.html]",
            "TRIWM: 1st Censor message + Warning; 2nd Message block + Warning; 3rd Mute",
            "CANCEL: Cancel the message if it is toxic",
            "CENSOR: Censor the message if it is toxic"
    })
    ModerationType moderationType = ModerationType.TRIWM;
    @YamlComment("Blocked words. If a message contains any of these words, it will be filtered.")
    String[] blockedWords = {"badword1", "badword2"};
    @YamlComment("Enables the moderation on signs too.")
    boolean signModeration = true;
    @YamlComment("Enable caching for sign moderation.")
    boolean signCaching = false;
    @YamlComment("Send messages written on signs to staff members.")
    boolean notifyStaffSigns = true;
    @YamlComment("Format for the message sent to staff members.")
    String notifyStaffSignsFormat = "{prefix} &6{player} &7placed a sign: {toxic} &6{message} &7on &6{world} &7at &6{x}, {y}, {z}";
    @YamlComment("Tp command for staff members.")
    String tpCommand = "/tp {player} {x} {y} {z}";
    @YamlComment("Blacklisted commands. If a player tries to execute one of these commands, it will be blocked.")
    String[] blacklistedCommands = {"/op", "/deop"};
    @YamlComment("Censor characters. If a message is censored, these characters will be used.")
    String censorCharacters = "*";
    @YamlComment("Can muted players write signs.")
    boolean mutedCanWriteSigns = false;
    @YamlComment("Should notify staff members when a player says a toxic message.")
    boolean notifyStaff = true;
    @YamlComment("Max consecutive caps allowed.")
    int maxConsecutiveCaps = 5;

    // LINK DETECTION SECTION
    @YamlComment({
            "=== LINK DETECTION SECTION ===",
            "",
            "Enable link detection."
    })
    boolean linkDetection = true;
    @YamlComment("Allowed links, domains, and IPs.")
    String[] allowedLinks = {"example.com", "example.org/allowed", "0.0.0.0", "2001:db8:3333:4444:5555:6666:7777:8888"};

    // ANTI-SPAM SECTION
    @YamlComment({
            "=== ANTI-SPAM SECTION ===",
            "",
            "Enable anti-spam."
    })
    boolean antiSpam = true;
    @YamlComment("Message interval in milliseconds.")
    Long messageInterval = 1000L;
    @YamlComment("Max words equal to the last message. Greater or equal to this value will be considered spam.")
    int maxWords = 5;
    @YamlComment("Command interval in milliseconds.")
    Long commandInterval = 3000L;

    // PRIVATE MESSAGING
    @YamlComment({
            "=== PRIVATE MESSAGING SECTION ===",
            "",
            "Enable private messaging. (e.g. /msg, /r)"
    })
    boolean privateMessaging = true;
    @YamlComment("Should AutoMod moderate private messages.")
    boolean moderatePrivateMessages = true;
    @YamlComment("Should links, IPs, and domains be filtered based on the moderationType in private messages.")
    boolean filterLinksInPrivateMessages = true;
    @YamlComment("Can muted players send private messages.")
    boolean mutedCanSendPrivateMessages = false;
    @YamlComment("Private messages format")
    String privateMessagesFormat = "&7[&6{sender} &7 -> &6{receiver}&7] {message}";
    @YamlComment("Ignoring enabled.")
    boolean ignoreEnabled = true;

    // REPORT SECTION
    @YamlComment({
            "=== REPORT SECTION ===",
            "",
            "Enable report system."
    })
    boolean reportSystem = true;
    @YamlComment("Default Reports")
    String[] defaultReports = {"Spamming", "Toxicity", "Inappropriate Language", "Inappropriate Content"};
    @YamlComment("Report message format. Multiple lines are supported. {player} will be replaced with the reporter's name, {target} with the target's name, and {message} with the message.")
    String[] reportMessageFormat = {"&7[&6{player} &7reported &6{target}&7] {message}"};
    @YamlComment("Report cooldown in milliseconds.")
    long reportCooldown = 300000L;

    // CACHE SECTION
    @YamlComment({
            "=== CACHE SECTION ===",
            "",
            "Cache expire time in days."
    })
    int cacheExpireDays = 7;

    // CHAT LOGGING SECTION
    @YamlComment({
            "=== CHAT LOGGING SECTION ===",
            "",
            "Enable chat logging."
    })
    boolean chatLogging = true;
    @YamlComment("Chat log format.")
    String chatLogFormat = "[{date}] {player}: {message}";
    @YamlComment("Chat log format for toxic messages.")
    String toxicChatLogFormat = "[TOXIC] [{date}] {player}: {message}";
    @YamlComment("Show original message if toxic.")
    boolean showOriginalMessage = false;
    @YamlComment("Date format for chat logs.")
    String dateFormat = "dd/MM/yyyy HH:mm:ss";
    @YamlComment("Chat log file name.")
    String chatLogFileName = "chat_{date}.log";
    @YamlComment("Should log signs text.")
    boolean logSigns = true;
    @YamlComment("Sign log format.")
    String signLogFormat = "[{date}] {player} placed a sign: {message} on {world} at {x}, {y}, {z}";
    @YamlComment("Sign log format for toxic signs.")
    String toxicSignLogFormat = "[TOXIC] [{date}] {player} placed a sign: {message} on {world} at {x}, {y}, {z}";

    // LITEBANS SUPPORT SECTION
    @YamlComment({
            "=== LITEBANS SUPPORT SECTION ===",
            "",
            "If you are using LiteBans, set this to true."
    })
    boolean liteBanSupport = false;
    @YamlComment(
            {
                    "Commands to execute when a player is muted, unmuted, warned, or unwarned. {player} will be replaced with the player's name.",
                    "These commands are used only if liteBanSupport is set to true."
            }
    )
    String muteCommand = "/mute {player} 10m";
    String unmuteCommand = "/unmute {player}";
    String warnCommand = "/warn {player} {reason}";
    String unwarnCommand = "/unwarn {player}";

    // FIX SECTIONS
    @YamlComment({
            "=== FIX SECTIONS ===",
            "",
            "So what this will do? If there's another plugin that modifies chat behavior and bugs the chat, you can add the plugin's name here",
            "This will fix the chat behavior. If you don't have any plugins that modify chat behavior, you can leave this empty."
    })
    String[] chatHandlers = {};

    // DISCORD SECTION
    @YamlComment({
            "=== DISCORD SECTION ===",
            "",
            "Enable Discord integration."
    })
    boolean discordIntegration = false;
    @YamlComment("Discord webhook URL.")
    String discordWebhook = "https://discord.com/api/webhooks/1234567890/yourwebhook";
    @YamlComment("Reports Discord webhook URL.")
    String reportDiscordWebhook = "https://discord.com/api/webhooks/1234567890/yourwebhook";
    
    // USERNAME MODERATION
    @YamlComment({
            "=== USERNAME MODERATION ===",
            "",
            "Enable username moderation."
    })
    boolean usernameModeration = true;
    @YamlComment("Blocked usernames.")
    String[] blockedUsernames = {"adolf", "hitler", "ihate"};
    @YamlComment("Allow the AI to moderate usernames.")
    boolean allowAiUsernameModeration = true;
    @YamlComment("Max username length.")
    int maxUsernameLength = 16;

    // DEVELOPMENT SECTION
    @YamlComment({
            "=== DEVELOPMENT SECTION ===",
            "",
            "Enable debug mode."
    })
    boolean debug = false;
    @YamlComment("Enable verbose mode.")
    boolean verbose = false;

    @Override
    public String toString() {
        return "Settings{" +
                "redisUri='" + redisUri + '\'' +
                ", requiresMultiInstance=" + requiresMultiInstance +
                ", perspectiveApiKey='" + perspectiveApiKey + '\'' +
                ", threshold=" + threshold +
                ", muteTime=" + muteTime +
                ", warnExpireTime=" + warnExpireTime +
                ", moderationType=" + moderationType +
                ", blockedWords=[" + String.join(", ", blockedWords) +
                "], antiSpam=" + antiSpam +
                ", messageInterval=" + messageInterval +
                ", maxWords=" + maxWords +
                ", commandInterval=" + commandInterval +
                ", cacheExpireDays=" + cacheExpireDays +
                ", liteBanSupport=" + liteBanSupport +
                ", muteCommand='" + muteCommand + '\'' +
                ", unmuteCommand='" + unmuteCommand + '\'' +
                ", warnCommand='" + warnCommand + '\'' +
                ", unwarnCommand='" + unwarnCommand + '\'' +
                ", debug=" + debug +
                ", verbose=" + verbose +
                '}';
    }

    @Override
    public Settings clone() {
        try {
            return (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
