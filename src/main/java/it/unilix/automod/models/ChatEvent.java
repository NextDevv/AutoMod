package it.unilix.automod.models;

import it.unilix.automod.enums.ModEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class ChatEvent {
    private UUID player;
    private String displayName;
    private String message;
    private ModEvent event = ModEvent.CHAT;

    public ChatEvent(Player player, String message) {
        this.player = player.getUniqueId();
        this.displayName = player.getDisplayName();
        this.message = message;
    }

    public ChatEvent(UUID player, String displayName, String message) {
        this.player = player;
        this.displayName = displayName;
        this.message = message;
    }
}
