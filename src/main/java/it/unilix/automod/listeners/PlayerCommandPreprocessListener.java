package it.unilix.automod.listeners;

import it.unilix.automod.configs.Messages;
import it.unilix.automod.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerCommandPreprocessListener implements Listener {
    private final HashMap<UUID, String> lastCommand = new HashMap<>();
    private final Messages messages;

    public PlayerCommandPreprocessListener(Messages messages) {
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player.hasPermission("automod.bypass") || player.hasPermission("automod.staff") || player.isOp()) {
            return;
        }

        if (isSpamming(player, message)) {
            event.setCancelled(true);
            ChatUtils.msg(player, messages.getSpammingCommand());
            return;
        }

        lastCommand.put(player.getUniqueId(), message);
    }

    private boolean isSpamming(Player player, String message) {
        String command = message.split(" ")[0];
        String last = lastCommand.get(player.getUniqueId()).split(" ")[0];
        return command.equalsIgnoreCase(last);
    }
}
