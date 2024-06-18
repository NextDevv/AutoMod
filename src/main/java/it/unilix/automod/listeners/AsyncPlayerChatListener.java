package it.unilix.automod.listeners;

import it.unilix.automod.AutoMod;
import it.unilix.automod.models.Cache;
import it.unilix.automod.models.ChatEvent;
import it.unilix.automod.utils.LinkDetector;
import it.unilix.automod.utils.MuteManager;
import it.unilix.automod.utils.Pair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AsyncPlayerChatListener implements Listener {

    private static final int MAX_WARNINGS = 3;
    private final AutoMod plugin;

    public AsyncPlayerChatListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("automod.bypass") || player.isOp()) {
            return;
        }
        MuteManager.checkPlayer(player);
        event.setCancelled(true);

        if(Arrays.stream(plugin.getSettings().getBlockedWords()).anyMatch(event.getMessage()::contains))
            return;
        if (handleMute(player)) return;
        if (handleLinkDetection(event, player)) return;

        processMessageAsync(event, player);
    }

    private boolean handleMute(Player player) {
        if (MuteManager.isMuted(player.getUniqueId())) {
            msg(player, plugin.getMessages().getMuted());
            return true;
        }
        return false;
    }

    private boolean handleLinkDetection(AsyncPlayerChatEvent event, Player player) {
        if (!LinkDetector.detect(event.getMessage()).isEmpty()) {
            MuteManager.warnPlayer(player.getUniqueId());
            int warnings = MuteManager.getWarnings(player.getUniqueId());

            if (warnings >= 2) {
                MuteManager.mutePlayer(player.getUniqueId());
                MuteManager.clearWarnings(player.getUniqueId());
                msg(player, plugin.getMessages().getMuted());
            } else {
                msg(player, plugin.getMessages().getWarned());
            }
            return true;
        }
        return false;
    }

    private void processMessageAsync(AsyncPlayerChatEvent event, Player player) {
        new BukkitRunnable() {
            final String message = event.getMessage();
            final String format = String.format(event.getFormat(), player.getDisplayName(), message);

            @Override
            public void run() {
                try {
                    handleMessage(event, player, message, format);
                } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void handleMessage(AsyncPlayerChatEvent event, Player player, String message, String format)
            throws InterruptedException, ExecutionException, URISyntaxException {
        boolean isToxic;
        String censoredMessage;

        if (plugin.getCacheManager().isCached(message)) {
            Cache cache = plugin.getCacheManager().getCache(message);
            isToxic = cache.toxic();
            censoredMessage = cache.censored();
        } else {
            Pair<String, Boolean> censoredPair = plugin.getPerspectiveAPI().censorAsync(message).get();
            censoredMessage = censoredPair.getFirst();
            isToxic = censoredPair.getSecond();
        }

        if (isToxic) {
            handleToxicMessage(event, player, message, censoredMessage, format);
        } else {
            broadcastMessage(event, player, message, censoredMessage, format);
        }
    }

    private void handleToxicMessage(AsyncPlayerChatEvent event, Player player, String message, String censoredMessage, String format) {
        MuteManager.warnPlayer(player.getUniqueId());
        int warnings = MuteManager.getWarnings(player.getUniqueId());

        switch (plugin.getSettings().getModerationType()) {
            case CENSOR:
                broadcastMessage(event, player, message, censoredMessage, format);
                break;
            case TRIWM:
                if (warnings == 1) {
                    msg(player, plugin.getMessages().getWarned());
                    broadcastMessage(event, player, message, censoredMessage, format);
                } else if (warnings == 2) {
                    msg(player, plugin.getMessages().getWarned());
                } else if (warnings >= MAX_WARNINGS) {
                    MuteManager.mutePlayer(player.getUniqueId());
                    MuteManager.clearWarnings(player.getUniqueId());
                    msg(player, plugin.getMessages().getMuted());
                }
                break;

            default:
                break;
        }

        plugin.getCacheManager().addCache(new Cache(message, censoredMessage, true));
    }

    private void broadcastMessage(
            AsyncPlayerChatEvent event, Player player, String message, String censoredMessage, String format
    ) {
        event.getRecipients().forEach(recipient -> {
            if (!recipient.hasPermission("automod.staff")) {
                recipient.sendMessage(format.replace(message, censoredMessage));
            } else {
                recipient.sendMessage(format);
            }
        });

        ChatEvent chatEvent = new ChatEvent(player, format.replace(message, censoredMessage), format);
        if (plugin.getSettings().isRequiresMultiInstance()) {
            plugin.getRedisManager().publish(plugin.getGson().toJsonTree(chatEvent).getAsJsonObject());
        }
    }

    private void msg(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
