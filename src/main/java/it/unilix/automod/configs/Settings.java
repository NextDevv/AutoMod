package it.unilix.automod.configs;

import lombok.Getter;

import java.util.Map;

@Getter
public class Settings {
    String redisUri = "redis://localhost";
    String perspectiveApiKey = "YOUR_API_KEY";
    Double threshold = 0.5;
    Long muteTime = 60000L;
    Long warnExpireTime = 900000L;
    int cacheExpireDays = 7;
    boolean liteBanSupport = true;
    String muteCommand = "/mute {player} 10m";
    String unmuteCommand = "/unmute {player}";
    String warnCommand = "/warn {player} {reason}";
    String unwarnCommand = "/unwarn {player}";
    boolean requiresMultiInstance = true;

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
