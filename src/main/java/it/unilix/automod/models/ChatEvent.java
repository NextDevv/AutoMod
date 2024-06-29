package it.unilix.automod.models;

import it.unilix.automod.enums.ModEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class ChatEvent {
    private final UUID player;
    private final String displayName;
    private final String message;
    private final String unfilteredMessage;
    private final ModEvent event = ModEvent.CHAT;

    public ChatEvent(Player player, String message, String unfilteredMessage) {
        this.player = player.getUniqueId();
        this.displayName = player.getDisplayName();
        this.message = message;
        this.unfilteredMessage = unfilteredMessage;
    }
}
