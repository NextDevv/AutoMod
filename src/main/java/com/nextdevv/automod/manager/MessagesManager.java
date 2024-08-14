package com.nextdevv.automod.manager;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.models.PlayerIgnore;
import com.nextdevv.automod.utils.ChatUtils;
import com.nextdevv.automod.utils.Debug;
import com.nextdevv.automod.utils.LinkDetector;
import com.nextdevv.automod.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.nextdevv.automod.manager.MuteManager.handleDiscordIntegration;

public class MessagesManager {
    private final AutoMod plugin;
    private final Map<Pair<String, String>, Pair<Long, List<String>>> messages = new HashMap<>();

    public MessagesManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(String sender, String receiver, String message) {
        Pair<String, String> messagePair = new Pair<>(sender, receiver);
        long currentTime = System.currentTimeMillis();
        Player receiverPlayer = Bukkit.getPlayer(receiver);
        Player senderPlayer = Bukkit.getPlayer(sender);

        if (receiverPlayer != null && plugin.getSettings().isIgnoreEnabled()) {
            Optional<PlayerIgnore> ignore = plugin.getIgnores().getIgnores().stream()
                    .filter(i -> i.uuid().equals(receiverPlayer.getUniqueId().toString()))
                    .findAny();

            // DEBUG LOGS
            Debug.log("Ignore: " + ignore);
            Debug.log("Present: " + ignore.isPresent());
            Debug.log("Ignored Players: " + (ignore.map(PlayerIgnore::ignoredPlayers).orElse(null)));
            Debug.log("Sender: " + sender);
            Debug.log("Sender Player: " + senderPlayer);
            Debug.log("Ignore All: " + (ignore.isPresent() && ignore.get().ignoreAll()));
            Debug.log("Condition: " + (ignore.isPresent() && ignore.get().ignoredPlayers().contains(sender) && !sender.equals("CONSOLE") && senderPlayer != null));
            // END OF DEBUG LOGS

            if (ignore.isPresent() && ignore.get().ignoreAll() && senderPlayer != null) {
                ChatUtils.msg(senderPlayer, plugin.getMessages().getIgnored().replace("{player}", receiverPlayer.getName()));
                return;
            }

            if (ignore.isPresent() && ignore.get().ignoredPlayers().contains(senderPlayer != null ? senderPlayer.getUniqueId().toString() : null) && !sender.equals("CONSOLE") && senderPlayer != null) {
                ChatUtils.msg(senderPlayer, plugin.getMessages().getIgnored().replace("{player}", receiverPlayer.getName()));
                return;
            }
        }

        messages.compute(messagePair, (key, value) -> {
            if (value == null) {
                return new Pair<>(currentTime, new ArrayList<>(List.of(message)));
            } else {
                value.getSecond().add(message);
                value.setFirst(currentTime);
                return value;
            }
        });

        if (receiverPlayer != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String moderatedMessage = message;

                    if (plugin.getSettings().isModeratePrivateMessages()) {
                        try {
                            boolean cached = false;
                            if (plugin.getCacheManager().isCached(message)) {
                                moderatedMessage = plugin.getCacheManager().getCache(message).censored();
                                cached = true;
                            }

                            if (!cached) {
                                if (plugin.getSettings().isFilterLinksInPrivateMessages()) {
                                    moderatedMessage = LinkDetector.censor(message, plugin.getSettings().getCensorCharacters());
                                }

                                CompletableFuture<Pair<String, Boolean>> censored = plugin.getPerspectiveAPI().censorAsync(message);
                                Pair<String, Boolean> censoredPair = censored.get();
                                if (censoredPair.getSecond()) {
                                    moderatedMessage = censoredPair.getFirst();
                                }
                            }
                        } catch (URISyntaxException | ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    String formattedMessage = plugin.getSettings().getPrivateMessagesFormat()
                            .replace("{sender}", sender.equals("CONSOLE") ? "CONSOLE" : Objects.requireNonNull(senderPlayer).getName())
                            .replace("{receiver}", receiverPlayer.getName())
                            .replace("{message}", moderatedMessage);

                    ChatUtils.msg(receiverPlayer, formattedMessage);
                    assert senderPlayer != null;
                    handleDiscordIntegration(senderPlayer.getName() + " to " + receiverPlayer.getName(), "", Color.GREEN, message);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    public List<String> getMessages(String sender, String receiver) {
        Pair<String, String> messagePair = new Pair<>(sender, receiver);
        Pair<Long, List<String>> messageData = messages.get(messagePair);
        return messageData != null ? messageData.getSecond() : new ArrayList<>();
    }

    public void clearMessages(String sender, String receiver) {
        messages.remove(new Pair<>(sender, receiver));
    }

    public String getLastChatter(String sender) {
        return messages.keySet().stream()
                .filter(pair -> pair.getFirst().equals(sender) || pair.getSecond().equals(sender))
                .max(Comparator.comparingLong(pair -> messages.get(pair).getFirst()))
                .map(pair -> pair.getFirst().equals(sender) ? pair.getSecond() : pair.getFirst())
                .orElse(null);
    }

    public void clearAllMessages() {
        messages.clear();
    }
}