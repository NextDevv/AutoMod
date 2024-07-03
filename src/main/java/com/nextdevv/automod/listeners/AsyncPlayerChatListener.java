package com.nextdevv.automod.listeners;

import com.nextdevv.automod.models.Cache;
import com.nextdevv.automod.utils.Pair;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.models.ChatEvent;
import com.nextdevv.automod.utils.LinkDetector;
import com.nextdevv.automod.utils.MuteManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AsyncPlayerChatListener implements Listener {

    private static final int MAX_WARNINGS = 3;
    private final AutoMod plugin;
    private final HashMap<UUID, String> lastMessage = new HashMap<>();

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

        if(isSpamming(player, event.getMessage())) {
            lastMessage.put(player.getUniqueId(), event.getMessage());
            return;
        }

        if(Arrays.stream(plugin.getSettings().getBlockedWords()).anyMatch(event.getMessage().toLowerCase()::contains)) {
            String message = event.getMessage().toLowerCase();
            String blockedWord = Arrays.stream(plugin.getSettings().getBlockedWords())
                    .filter(message::contains)
                    .findFirst()
                    .orElse(null);

            if(blockedWord == null) {
                if(plugin.getSettings().isDebug()) {
                    plugin.getLogger().warning("[DEBUG] Blocked word not found in message: " + message);
                    if(plugin.getSettings().isVerbose()) {
                        plugin.getLogger().warning("[DEBUG/VERBOSE] Blocked words: " + Arrays.toString(plugin.getSettings().getBlockedWords()));
                    }
                }
                return;
            }

            String censoredMessage = message.replace(blockedWord, "*".repeat(blockedWord.length()));
            if(plugin.getSettings().isDebug()) {
                plugin.getLogger().warning("[DEBUG] Blocked word found in message: " + message);
                plugin.getLogger().warning("[DEBUG] Censored message: " + censoredMessage);
            }

            handleToxicMessage(event, player, event.getMessage(), censoredMessage);

            lastMessage.put(player.getUniqueId(), event.getMessage());
            return;
        }
        if (handleMute(player)) return;
        if (handleLinkDetection(event, player)) return;

        processMessageAsync(event, player);
    }

    public boolean isSpamming(Player player, String message) {
        if(player.hasPermission("automod.staff")) return false;

        if(!lastMessage.containsKey(player.getUniqueId())) {
            lastMessage.put(player.getUniqueId(), message);
            return false;
        }

        if(lastMessage.getOrDefault(player.getUniqueId(), "").equalsIgnoreCase(message)) {
            msg(player, plugin.getMessages().getSpamming());
            return true;
        }

        if(lastMessage.getOrDefault(player.getUniqueId(), "").toLowerCase().contains(message.toLowerCase())) {
            msg(player, plugin.getMessages().getSpamming());
            return true;
        }

        String[] words = message.split(" ");
        String[] lastWords = lastMessage.getOrDefault(player.getUniqueId(), "").split(" ");

        if(plugin.getSettings().isDebug()) {
            plugin.getLogger().warning("[DEBUG] Message: " + Arrays.toString(words));
            plugin.getLogger().warning("[DEBUG] Last message: " + Arrays.toString(lastWords));
        }

        var ref = new Object() {
            int count = 0;
        };

        Arrays.stream(words).forEach(word -> {
            if(plugin.getSettings().isDebug()) {
                plugin.getLogger().warning("[DEBUG] Checking word: " + word);
            }

            if(Arrays.stream(lastWords).anyMatch(word::equalsIgnoreCase)) {
                if(plugin.getSettings().isDebug()) {
                    plugin.getLogger().warning("[DEBUG] Word found: " + word);
                }

                ref.count++;
            }
        });

        if(ref.count >= plugin.getSettings().getMaxWords()) {
            if(plugin.getSettings().isDebug()) {
                plugin.getLogger().warning("[DEBUG] Spamming detected: " + ref.count);
            }

            msg(player, plugin.getMessages().getSpamming());
            return true;
        }

        lastMessage.put(player.getUniqueId(), message);
        return false;
    }

    public boolean handleMute(Player player) {
        if (MuteManager.isMuted(player.getUniqueId())) {
            msg(player, plugin.getMessages().getMuted());
            return true;
        }
        return false;
    }

    public boolean handleLinkDetection(AsyncPlayerChatEvent event, Player player) {
        if (!LinkDetector.detect(event.getMessage()).isEmpty()) {
            switch (plugin.getSettings().getModerationType()) {
                case CENSOR:
                    // broadcastMessage(event, player, event.getMessage(), LinkDetector.censor(event.getMessage()));
                    event.setMessage(LinkDetector.censor(event.getMessage()));
                    processMessageAsync(event, player);
                    break;
                case TRIWM:
                    MuteManager.warnPlayer(player.getUniqueId());
                    int warnings = MuteManager.getWarnings(player.getUniqueId());

                    if (warnings >= 2) {
                        MuteManager.mutePlayer(player.getUniqueId());
                        MuteManager.clearWarnings(player.getUniqueId());
                        msg(player, plugin.getMessages().getMuted());
                    } else {
                        msg(player, plugin.getMessages().getWarned());
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
        return false;
    }

    public void processMessageAsync(AsyncPlayerChatEvent event, Player player) {
        new BukkitRunnable() {
            final String message = event.getMessage();

            @Override
            public void run() {
                try {
                    handleMessage(event, player, message);
                } catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    Throwable throwable = e.fillInStackTrace();
                    plugin.getLogger().severe("Failed to handle message: " + throwable.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void handleMessage(AsyncPlayerChatEvent event, Player player, String message)
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
            handleToxicMessage(event, player, message, censoredMessage);
        } else {
            broadcastMessage(event, player, message, censoredMessage);
        }

        plugin.getLogger().info(player.getName() + ": " + message);
        plugin.getCacheManager().addCache(new Cache(message, censoredMessage, isToxic));
    }

    public void handleToxicMessage(AsyncPlayerChatEvent event, Player player, String message, String censoredMessage) {
        MuteManager.warnPlayer(player.getUniqueId());
        int warnings = MuteManager.getWarnings(player.getUniqueId());

        switch (plugin.getSettings().getModerationType()) {
            case CENSOR:
                broadcastMessage(event, player, message, censoredMessage);
                break;
            case TRIWM:
                if (warnings == 1) {
                    msg(player, plugin.getMessages().getWarned());
                    broadcastMessage(event, player, message, censoredMessage);
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

    }

    public void broadcastMessage(
            AsyncPlayerChatEvent event, Player player, String message, String censoredMessage
    ) {
        String format = String.format(event.getFormat(), player.getDisplayName(), message);
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

    public void msg(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
