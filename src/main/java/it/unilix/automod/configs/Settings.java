package it.unilix.automod.configs;

import lombok.Getter;

@Getter
public class Settings {
    String redisUri = "redis://localhost";
    String perspectiveApiKey = "YOUR_API_KEY";
    Double threshold = 0.5;
    Long muteTime = 60000L;
    Long warnExpireTime = 900000L;
    int cacheExpireDays = 7;

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
