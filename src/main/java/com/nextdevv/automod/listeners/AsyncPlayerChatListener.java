package com.nextdevv.automod.listeners;

import com.nextdevv.automod.events.ChatEvent;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.models.Cache;
import com.nextdevv.automod.utils.Debug;
import com.nextdevv.automod.utils.ListUtils;
import com.nextdevv.automod.utils.Pair;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.utils.LinkDetector;
import github.scarsz.discordsrv.DiscordSRV;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.joor.Reflect;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.nextdevv.automod.manager.MuteManager.handleDiscordIntegration;
import static com.nextdevv.automod.utils.ChatUtils.msg;

public class AsyncPlayerChatListener implements Listener {

    private static final int MAX_WARNINGS = 3;
    private final AutoMod plugin;
    private final HashMap<UUID, String> lastMessage = new HashMap<>();
    private final List<RegisteredListener> handlers = new ArrayList<>();

    public AsyncPlayerChatListener(AutoMod plugin) {
        this.plugin = plugin;


        Bukkit.getScheduler().runTask(plugin, () -> {
            Debug.log("Chat Handlers: " + Arrays.toString(plugin.getSettings().getChatHandlers()));

            Arrays.stream(AsyncPlayerChatEvent.getHandlerList().getRegisteredListeners()).forEach(handler -> {
                Debug.log("Registered chat handler: " + handler.getPlugin().getName());

                if(Arrays.asList(plugin.getSettings().getChatHandlers()).contains(handler.getPlugin().getName())) {
                    try {
                        Reflect.on(handler).set("ignoreCancelled", true);
                        EventPriority priority = Reflect.on(handler).get("priority");
                        if(priority == EventPriority.LOWEST) {
                            Reflect.on(handler).set("priority", EventPriority.LOW);
                        }

                        handlers.add(handler);

                        Debug.log("ignoreCancelled: " + handler.isIgnoringCancelled());
                        Debug.log("Priority: " + handler.getPriority());
                        Debug.log("Added chat handler: " + handler.getPlugin().getName());

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    private String applyReplacements(String message) {
        return message
                .replace("4", "a")
                .replace("0", "o")
                .replace("1", "i")
                .replace("3", "e")
                .replace("5", "s")
                .replace("7", "t")
                .replace("8", "b")
                .replace("9", "g")
                .replace("6", "b")
                .replace("2", "z")
                .replace("!", "i")
                .toLowerCase();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasPermission("automod.bypass") || player.isOp()) {
            broadcastMessage(event, player, event.getMessage(), event.getMessage());
            return;
        }

        MuteManager.checkPlayer(player);
        String message = applyReplacements(event.getMessage());

        if(isSpamming(player, event.getMessage())) {
            lastMessage.put(player.getUniqueId(), event.getMessage());
            return;
        }

        if(Arrays.stream(plugin.getSettings().getBlockedWords()).anyMatch(event.getMessage().toLowerCase()::contains)) {
            String blockedWord = Arrays.stream(plugin.getSettings().getBlockedWords())
                    .filter(message::contains)
                    .findFirst()
                    .orElse(null);

            if(blockedWord == null) {
                Debug.log("Blocked word not found in message: " + message);
                Debug.verbose("Blocked words: " + Arrays.toString(plugin.getSettings().getBlockedWords()));
                return;
            }

            String censoredMessage = message.replace(blockedWord, "*".repeat(blockedWord.length()));

            Debug.log("Blocked word found: " + blockedWord);
            Debug.log("Censored message: " + censoredMessage);

            handleToxicMessage(event, player, event.getMessage(), censoredMessage);

            lastMessage.put(player.getUniqueId(), event.getMessage());
            return;
        }
        if (handleMute(player)) return;
        if (handleLinkDetection(event, player)) return;

        int consecutiveCaps = 0;
        for (int i = 0; i < event.getMessage().length(); i++) {
            if (Character.isUpperCase(event.getMessage().charAt(i))) {
                consecutiveCaps++;
            } else consecutiveCaps = 0;

            if (consecutiveCaps >= plugin.getSettings().getMaxConsecutiveCaps()) {
                msg(player, plugin.getMessages().getCaps());
                return;
            }
        }

        processMessageAsync(event, player);
    }

    public boolean isSpamming(Player player, String message) {
        if(player.hasPermission("automod.staff")) return false;

        if(!lastMessage.containsKey(player.getUniqueId())) {
            lastMessage.put(player.getUniqueId(), message);
            return false;
        }

        if(lastMessage.getOrDefault(player.getUniqueId(), "").equalsIgnoreCase(message) || lastMessage.getOrDefault(player.getUniqueId(), "").toLowerCase().contains(message.toLowerCase())) {
            msg(player, plugin.getMessages().getSpamming());
            return true;
        }

        String[] words = message.split(" ");
        String[] lastWords = lastMessage.getOrDefault(player.getUniqueId(), "").split(" ");

        Debug.log("Message: " + Arrays.toString(words));
        Debug.log("Last message: " + Arrays.toString(lastWords));

        var ref = new Object() {
            int count = 0;
        };

        Arrays.stream(words).forEach(word -> {
            Debug.log("Checking word: " + word);

            if(Arrays.stream(lastWords).anyMatch(word::equalsIgnoreCase)) {
                Debug.log("Word found: " + word);

                ref.count++;
            }
        });

        if(ref.count >= plugin.getSettings().getMaxWords()) {
            Debug.log("Spamming detected: " + ref.count);

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
        String[] links = LinkDetector.detect(applyReplacements(event.getMessage())).toArray(new String[0]);
        if (links.length == 0) {
            return false;
        }

        int notAllowed = 0;
        for (String link : links) {
            if (Arrays.stream(plugin.getSettings().getAllowedLinks()).noneMatch(link::contains)) {
                notAllowed++;
            }
        }

        if (notAllowed == 0)
            return false;

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
        handleDiscordIntegration("AutoMod","Player " + player.getName() + " sent a message with a link(s)", Color.RED, ListUtils.toString(links, ", "));
        return true;
    }

    public void processMessageAsync(AsyncPlayerChatEvent event, Player player) {
        new BukkitRunnable() {
            final String message = event.getMessage();

            @Override
            public void run() {
                try {
                    handleMessage(event, player, applyReplacements(message));
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
            Pair<String, Boolean> censoredPair = plugin.getPerspectiveAPI().censorAsync(event.getMessage()).get();
            censoredMessage = censoredPair.getSecond() ? censoredPair.getFirst() : event.getMessage();
            isToxic = censoredPair.getSecond();
        }

        if (isToxic) {
            handleToxicMessage(event, player, message, censoredMessage);
        } else {
            broadcastMessage(event, player, event.getMessage(), censoredMessage);
        }

        plugin.getLogger().info(player.getName() + ": " + event.getMessage());
        plugin.getCacheManager().addCache(new Cache(event.getMessage(), censoredMessage, isToxic));
    }

    public void handleToxicMessage(AsyncPlayerChatEvent event, Player player, String message, String censoredMessage) {
        switch (plugin.getSettings().getModerationType()) {
            case CENSOR:
                broadcastMessage(event, player, message, censoredMessage);
                break;
            case TRIWM:
                MuteManager.warnPlayer(player.getUniqueId(), "Toxic message (AutoMod)");
                int warnings = MuteManager.getWarnings(player.getUniqueId());

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

        // plugin.getChatLogger().log(player, message, true);
    }

    public void broadcastMessage(
            AsyncPlayerChatEvent event, Player player, String message, String censoredMessage
    ) {
        if(!Arrays.stream(plugin.getSettings().getChatHandlers()).toList().isEmpty()) {
            AsyncPlayerChatEvent newEvent = new AsyncPlayerChatEvent(
                    event.isAsynchronous(),
                    player,
                    censoredMessage,
                    event.getRecipients()
            );
            newEvent.setCancelled(false);
            newEvent.setFormat(event.getFormat());

            handlers.forEach(handler -> {
                try {
                    handler.callEvent(newEvent);
                } catch (EventException e) {
                    handleDiscordIntegration(player.getName(), "Exception Occurred", Color.RED, "Failed to handle message: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            plugin.getChatLogger().log(player, message, !censoredMessage.equals(message));
            if(censoredMessage.equals(message))
                handleDiscordIntegration(player.getName(), "", Color.GRAY, message);
            else handleDiscordIntegration(player.getName(), "", Color.RED, message);
            return;
        }

        String format = String.format(event.getFormat(), player.getDisplayName(), message);
        event.getRecipients().forEach(recipient -> {
            if (!recipient.hasPermission("automod.staff")) {
                recipient.sendMessage(format.replace(message, censoredMessage));
            } else {
                recipient.sendMessage(format);
            }
        });

        Debug.log("Message: " + message);
        Debug.log("Censored message: " + censoredMessage);
        Debug.log("Format: " + format);
        ChatEvent chatEvent = new ChatEvent(player, format.replace(message, censoredMessage), format);
        if (plugin.getSettings().isRequiresMultiInstance()) {
            plugin.getRedisManager().publish(plugin.getGson().toJsonTree(chatEvent).getAsJsonObject());
        }

        plugin.getChatLogger().log(player, message, !censoredMessage.equals(message));

        if(plugin.getSettings().isDiscordSRVIntegration()) {
            DiscordSRV.getPlugin().processChatMessage(player, message, DiscordSRV.getPlugin().getOptionalChannel("global"), false, event);
        }else if(censoredMessage.equals(message))
            handleDiscordIntegration(player.getName(), "", Color.GRAY, message);
        else handleDiscordIntegration(player.getName(), "", Color.RED, message);
    }
}
