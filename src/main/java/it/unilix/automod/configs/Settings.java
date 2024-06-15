package it.unilix.automod.configs;

import it.unilix.automod.enums.ModerationType;
import it.unilix.yaml.YamlComment;
import lombok.Getter;

import java.util.Map;

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
    @YamlComment("Moderation type. [triwm, cancel, censor], check the documentation for more information. [https://docs.nextdevv.com/automod.html]")
    ModerationType moderationType = ModerationType.TRIWM;
    @YamlComment("Blocked words. If a message contains any of these words, it will not be sent.")
    String[] blockedWords = {"badword1", "badword2"};

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


    @Override
    public String toString() {
        return "Settings{" +
                "redisUri='" + redisUri + '\'' +
                ", perspectiveApiKey='" + perspectiveApiKey + '\'' +
                ", threshold=" + threshold +
                ", muteTime=" + muteTime +
                ", warnExpireTime=" + warnExpireTime +
                '}';
    }
}
