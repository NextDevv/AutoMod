package com.nextdevv.automod.listeners;

import com.nextdevv.automod.configs.Messages;
import com.nextdevv.automod.utils.ChatUtils;
import com.nextdevv.automod.utils.Pair;
import com.nextdevv.automod.utils.StringUtils;
import com.nextdevv.automod.configs.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class PlayerCommandPreprocessListener implements Listener {
    private final HashMap<UUID, Pair<String, Long>> lastCommand = new HashMap<>();
    private final Messages messages;
    private final Settings settings;

    public PlayerCommandPreprocessListener(Messages messages, Settings settings) {
        this.messages = messages;
        this.settings = settings;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (player.hasPermission("automod.bypass") || player.hasPermission("automod.staff") || player.isOp()) {
            return;
        }

        if(Arrays.stream(settings.getBlacklistedCommands()).anyMatch(message::startsWith)) {
            event.setCancelled(true);
            ChatUtils.msg(player, messages.getBlackListedCommand());
            return;
        }

        if (isSpamming(player, message)) {
            event.setCancelled(true);
            ChatUtils.msg(player, messages.getSpammingCommand());
            return;
        }

        lastCommand.put(player.getUniqueId(), new Pair<>(message, System.currentTimeMillis()));
    }

    private boolean isSpamming(Player player, String message) {
        String command = StringUtils.getSplitOrEmpty(message, " ", 0);
        Pair<String, Long> last = lastCommand.getOrDefault(player.getUniqueId(), new Pair<>("", System.currentTimeMillis()));
        String lastCommand = StringUtils.getSplitOrEmpty(last.getFirst(), " ", 0);
        Long lastTime = last.getSecond();
        return lastCommand.equalsIgnoreCase(command) && System.currentTimeMillis() - lastTime < settings.getCommandInterval();
    }
}
