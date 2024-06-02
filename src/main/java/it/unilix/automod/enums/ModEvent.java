package it.unilix.automod.enums;

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

    public String getName() {
        return name;
    }

    public static ModEvent fromString(String name) {
        for (ModEvent event : ModEvent.values()) {
            if (event.getName().equalsIgnoreCase(name)) {
                return event;
            }
        }
        return null;
    }
}
