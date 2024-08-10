package com.nextdevv.automod.enums;

import lombok.Getter;

@Getter
public enum ModEvent {
    CHAT("chat"),
    WARN("warn"),
    MUTE("mute"),
    UNMUTE("unmute"),
    CLEAR_WARNINGS("clear_warnings"),
    MSG("msg"),
    NOTIFY("notify"),
    IGNORE("ignore"),
    ERROR("error"),
    SUCCESS("success"),
    REPORT("report");

    private final String name;

    ModEvent(String name) {
        this.name = name;
    }

}
