package com.nextdevv.automod.api;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.models.Cache;
import com.nextdevv.automod.utils.LinkDetector;
import com.nextdevv.automod.utils.MuteManager;
import com.nextdevv.automod.utils.Pair;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class AutoModAPI {
    private final AutoMod autoMod;

    public AutoModAPI(AutoMod autoMod) {
        this.autoMod = autoMod;
    }

    public CompletableFuture<Pair<String, Boolean>> censor(String message) throws URISyntaxException {
        return autoMod.getPerspectiveAPI().censorAsync(message);
    }

    public String[] detectLinks(String message) {
        return LinkDetector.detect(message).toArray(new String[0]);
    }

    public String censorLinks(String message) {
        return LinkDetector.censor(message);
    }

    /**
     * Checks if the player is spamming.
     *
     * @param player The player to check.
     * @param message The message sent by the player.
     * @return True if the player is spamming, false otherwise.
     */
    public boolean isSpamming(Player player, String message) {
        return autoMod.getAsyncPlayerChatListener().isSpamming(player, message);
    }

    /**
     * Handles the message sent by the player.
     *
     * @param player The player who sent the message.
     * @param message The message sent by the player.
     * @return The handled message. Null if the message should be canceled.
     */
    public CompletableFuture<@Nullable String> handleMessage(@NotNull Player player,@NotNull String message) {
        return CompletableFuture.supplyAsync(() -> {

            try {
                MuteManager.checkPlayer(player);
                Pair<String, Boolean> pair = censor(message).join();
                String handledMessage = pair.getFirst();

                if(pair.getSecond()) {
                    MuteManager.warnPlayer(player.getUniqueId());
                    int warnings = MuteManager.getWarnings(player.getUniqueId());

                    switch (autoMod.getSettings().getModerationType()) {
                        case CENSOR -> {
                            return handledMessage;
                        }

                        case CANCEL -> {
                            return null;
                        }
                    }
                }

                return handledMessage;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Cache the message.
     * @param cache The cache to add.
     */
    public void addCache(Cache cache) {
        autoMod.getCacheManager().addCache(cache);
    }


}
