package it.unilix.automod.utils;

import com.google.gson.JsonObject;
import it.unilix.automod.AutoMod;
import it.unilix.automod.enums.ModEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class MuteManager {
    @Getter
    private static final HashMap<UUID, Long> mutedPlayers = new HashMap<>();
    private static final HashMap<UUID, Pair<Long, Integer>> warnings = new HashMap<>();
    private static final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    public static void mutePlayer(UUID player) {
        mutedPlayers.put(player, System.currentTimeMillis());

        if (plugin.getSettings().isLiteBanSupport()) {
            plugin.getLiteBans().mutePlayer(player);
        }

        publishEvent(ModEvent.MUTE, player);
    }

    public static void unmutePlayer(UUID player) {
        mutedPlayers.remove(player);

        if (plugin.getSettings().isLiteBanSupport()) {
            plugin.getLiteBans().unmutePlayer(player);
        }

        publishEvent(ModEvent.UNMUTE, player);
    }

    public static boolean isMuted(UUID player) {
        return plugin.getSettings().isLiteBanSupport() && plugin.getLiteBans().isMuted(player) || mutedPlayers.containsKey(player);
    }

    public static long getMuteTime(UUID player) {
        return mutedPlayers.getOrDefault(player, System.currentTimeMillis());
    }

    public static void warnPlayer(UUID player) {
        if (plugin.getSettings().isLiteBanSupport()) {
            plugin.getLiteBans().warnPlayer(player);
        }

        warnings.merge(player, new Pair<>(System.currentTimeMillis(), 1), (oldPair, newPair) ->
                new Pair<>(oldPair.getFirst(), oldPair.getSecond() + 1));
    }

    public static int getWarnings(UUID player) {
        return Optional.ofNullable(warnings.get(player)).map(Pair::getSecond).orElse(0);
    }

    public static void clearWarnings(UUID player) {
        warnings.remove(player);

        if (plugin.getSettings().isLiteBanSupport()) {
            plugin.getLiteBans().clearWarnings(player);
        }

        publishEvent(ModEvent.CLEAR_WARNINGS, player);
    }

    public static long getWarnTime(UUID player) {
        return Optional.ofNullable(warnings.get(player)).map(Pair::getFirst).orElse(System.currentTimeMillis());
    }

    public static void checkPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (currentTime - getMuteTime(uuid) >= plugin.getSettings().getMuteTime()) {
            unmutePlayer(uuid);
            clearWarnings(uuid);
        }

        if (currentTime - getWarnTime(uuid) >= plugin.getSettings().getWarnExpireTime()) {
            clearWarnings(uuid);
        }
    }

    private static void publishEvent(ModEvent event, UUID player) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", event.getName());
        jsonObject.addProperty("player", player.toString());

        if (plugin.getSettings().isRequiresMultiInstance()) {
            plugin.getRedisManager().publish(jsonObject);
        }
    }
}
