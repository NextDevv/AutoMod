package com.nextdevv.automod.configs;

import com.nextdevv.automod.enums.ModerationType;
import it.unilix.yaml.YamlComment;
import lombok.Getter;

@Getter
public class Settings {
    // REDIS SECTION
    @YamlComment("Redis URI. If you are running multiple instances of this plugin, set this to the same URI.")
    String redisUri = "redis://localhost";
    @YamlComment("If you are running multiple instances of this plugin, set this to true. This will enable Redis pub/sub.")
    boolean requiresMultiInstance = true;

    // PERSPECTIVE API SECTION
    @YamlComment("Perspective API key.")
    String perspectiveApiKey = "YOUR_API_KEY";
    @YamlComment("Perspective API threshold. If the toxicity score is greater than this value, the message will be censored.")
    Double threshold = 0.5;

    // MODERATION SECTION
    @YamlComment("Mute time in milliseconds.")
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
    @YamlComment("Blacklisted commands. If a player tries to execute one of these commands, it will be blocked.")
    String[] blacklistedCommands = {"/op", "/deop"};
    @YamlComment("Censor characters. If a message is censored, these characters will be used.")
    String censorCharacters = "*";

    // ANTI-SPAM SECTION
    @YamlComment("Enable anti-spam.")
    boolean antiSpam = true;
    @YamlComment("Message interval in milliseconds.")
    Long messageInterval = 1000L;
    @YamlComment("Max words equal to the last message. Greater or equal to this value will be considered spam.")
    int maxWords = 5;
    @YamlComment("Command interval in milliseconds.")
    Long commandInterval = 3000L;

    // CACHE SECTION
    @YamlComment("Cache expire time in days.")
    int cacheExpireDays = 7;

    // LITEBANS SUPPORT SECTION
    @YamlComment("If you are using LiteBans, set this to true.")
    boolean liteBanSupport = true;
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

    // DEVELOPMENT SECTION
    @YamlComment("Enable debug mode.")
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
}
