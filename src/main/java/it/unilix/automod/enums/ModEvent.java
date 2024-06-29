package it.unilix.automod.enums;

import lombok.Getter;

@Getter
public enum ModEvent {
    CHAT("chat"),
    WARN("warn"),
    MUTE("mute"),
    UNMUTE("unmute"),
    CLEAR_WARNINGS("clear_warnings");

    private final String name;

    ModEvent(String name) {
        this.name = name;
    }

}
