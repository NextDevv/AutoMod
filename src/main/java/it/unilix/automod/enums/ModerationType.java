package it.unilix.automod.enums;

import lombok.Getter;

@Getter
public enum ModerationType {
    TRIWM("triwm"),
    CANCEL("cancel"),
    CENSOR("censor")
    ;

    final String moderationType;

    ModerationType(String type) {
        this.moderationType = type;
    }

    public static ModerationType fromString(String type) {
        for (ModerationType moderationType : ModerationType.values()) {
            if (moderationType.getModerationType().equalsIgnoreCase(type)) {
                return moderationType;
            }
        }
        return null;
    }
}
