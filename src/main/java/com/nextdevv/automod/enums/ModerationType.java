package com.nextdevv.automod.enums;

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
}
