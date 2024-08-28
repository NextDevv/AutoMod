package com.nextdevv.automod.manager;

import com.google.gson.JsonObject;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.enums.ModEvent;
import com.nextdevv.automod.models.MutedPlayer;
import com.nextdevv.automod.models.WarnedPlayer;
import com.nextdevv.automod.utils.Debug;
import com.nextdevv.automod.utils.DiscordWebhook;
import com.nextdevv.automod.utils.Pair;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuteManager {
    @Getter
    private static final HashMap<UUID, Long> mutedPlayers = new HashMap<>();
    private static final HashMap<UUID, String> mutedReasons = new HashMap<>();
    private static final HashMap<UUID, Pair<Long, Integer>> warnings = new HashMap<>();
    private static final HashMap<UUID, String> warningReasons = new HashMap<>();
    private static final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    public static void handleDiscordIntegration(String username, String title, Color color, String description) {
        if (plugin.getSettings().isDiscordIntegration()) {
            try {
                DiscordWebhook webhook = plugin.getDiscordWebhook();
                webhook.clearEmbeds();
                webhook.setUsername(username);
                webhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle(title).setColor(color).setDescription(description));
                webhook.execute();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to send Discord message: " + e.getMessage());
            }
        }
    }

    private static void publishEvent(ModEvent event, UUID player, String reason) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", event.getName());
        jsonObject.addProperty("player", player.toString());
        jsonObject.addProperty("reason", reason);
        if (plugin.getSettings().isRequiresMultiInstance()) {
            plugin.getRedisManager().publish(jsonObject);
        }
    }

    public static void mutePlayer(UUID player) {
        mutedPlayers.put(player, System.currentTimeMillis() + plugin.getSettings().getMuteTime());
        mutedReasons.put(player, "Muted by AutoMod");
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().mutePlayer(player);
        publishEvent(ModEvent.MUTE, player, "Muted by AutoMod");
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), " has been muted", Color.RED, "Reason: Muted by AutoMod");
    }

    public static void unmutePlayer(UUID player, String reason) {
        mutedPlayers.remove(player);
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().unmutePlayer(player);
        publishEvent(ModEvent.UNMUTE, player, reason);
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), " has been unmuted", Color.GREEN, "Reason: " + reason);
    }

    public static void unmutePlayer(UUID player) {
        unmutePlayer(player, "Unmuted by AutoMod");
    }

    public static boolean isMuted(UUID player) {
        return plugin.getSettings().isLiteBanSupport() && plugin.getLiteBans().isMuted(player) || mutedPlayers.containsKey(player);
    }

    public static long getMuteTime(UUID player) {
        return mutedPlayers.getOrDefault(player, System.currentTimeMillis() + plugin.getSettings().getMuteTime());
    }

    public static void warnPlayer(UUID player) {
        warnings.merge(player, new Pair<>(System.currentTimeMillis(), 1), (oldPair, newPair) -> new Pair<>(oldPair.getFirst(), oldPair.getSecond() + 1));
        warningReasons.put(player, "Warned by AutoMod");
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().warnPlayer(player);
        publishEvent(ModEvent.WARN, player, "Warned by AutoMod");
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), " has been warned", Color.YELLOW, "Reason: Warned by AutoMod");
    }

    public static int getWarnings(UUID player) {
        return Optional.ofNullable(warnings.get(player)).map(Pair::getSecond).orElse(0);
    }

    public static void clearWarnings(UUID player) {
        warnings.remove(player);
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().clearWarnings(player);
        publishEvent(ModEvent.CLEAR_WARNINGS, player, "Cleared by AutoMod");
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), "'s warnings have been cleared", Color.GREEN, "Reason: Cleared by AutoMod");
    }

    public static long getWarnTime(UUID player) {
        return Optional.ofNullable(warnings.get(player)).map(Pair::getFirst).orElse(System.currentTimeMillis());
    }

    public static void checkPlayer(Player player) {
        checkPlayer(player.getUniqueId());
    }

    public static void checkPlayer(UUID player) {
        long currentTime = System.currentTimeMillis();
        if (isMuted(player) && currentTime >= getMuteTime(player)) {
            unmutePlayer(player);
            clearWarnings(player);
        }
        if (currentTime - getWarnTime(player) >= plugin.getSettings().getWarnExpireTime()) {
            clearWarnings(player);
        }
    }

    public static void mutePlayer(String playerName, Long time, String reason) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player == null) return;
        UUID playerId = player.getUniqueId();
        mutedPlayers.put(playerId, System.currentTimeMillis() + time);
        mutedReasons.put(playerId, reason);
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().mutePlayer(playerId);
        publishEvent(ModEvent.MUTE, playerId, reason);
        handleDiscordIntegration("AutoMod", playerName + " has been muted", Color.RED, "Reason: " + reason);
    }

    public static void warnPlayer(UUID uniqueId, String reason) {
        warnings.merge(uniqueId, new Pair<>(System.currentTimeMillis(), 1), (oldPair, newPair) -> new Pair<>(oldPair.getFirst(), oldPair.getSecond() + 1));
        warningReasons.put(uniqueId, reason);
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().warnPlayer(uniqueId);
        publishEvent(ModEvent.WARN, uniqueId, reason);
        handleDiscordIntegration("AutoMod", plugin.getServer().getOfflinePlayer(uniqueId).getName() + " has been warned", Color.YELLOW, "Reason: " + reason);
    }

    public static void mutePlayer(MutedPlayer mutedPlayer) {
        UUID player = UUID.fromString(mutedPlayer.uuid());
        mutedPlayers.put(player, mutedPlayer.mutedUntil());
        mutedReasons.put(player, mutedPlayer.reason());
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().mutePlayer(player);
        publishEvent(ModEvent.MUTE, player, mutedPlayer.reason());
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), " has been muted", Color.RED, "Reason: " + mutedPlayer.reason());
    }

    public static void warnPlayer(WarnedPlayer warnedPlayer) {
        UUID player = UUID.fromString(warnedPlayer.uuid());
        warnings.put(player, new Pair<>(warnedPlayer.warnedAt(), warnedPlayer.warns()));
        warningReasons.put(player, warnedPlayer.reason());
        if (plugin.getSettings().isLiteBanSupport()) plugin.getLiteBans().warnPlayer(player);
        publishEvent(ModEvent.WARN, player, warnedPlayer.reason());
        handleDiscordIntegration(plugin.getServer().getOfflinePlayer(player).getName(), " has been warned", Color.YELLOW, "Reason: " + warnedPlayer.reason());
    }

    public static List<MutedPlayer> getMutedPlayersSave() {
        return mutedPlayers.keySet().stream()
                .map(uuid -> new MutedPlayer(uuid.toString(), mutedPlayers.get(uuid), mutedReasons.get(uuid)))
                .toList();
    }

    public static List<WarnedPlayer> getWarnedPlayersSave() {
        return warnings.keySet().stream()
                .map(uuid -> new WarnedPlayer(uuid.toString(), warnings.get(uuid).getSecond(), warnings.get(uuid).getSecond(), warningReasons.get(uuid)))
                .toList();
    }
}