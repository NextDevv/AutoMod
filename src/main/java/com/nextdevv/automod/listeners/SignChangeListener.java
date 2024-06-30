package com.nextdevv.automod.listeners;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.api.PerspectiveAPI;
import com.nextdevv.automod.utils.ListUtils;
import com.nextdevv.automod.utils.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.net.URISyntaxException;

public class SignChangeListener implements Listener {
    private final AutoMod plugin;

    public SignChangeListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines().clone();

        if(player.hasPermission("automod.bypass") || player.isOp()) return;
        if(!plugin.getSettings().isSignModeration()) return;

        PerspectiveAPI api = plugin.getPerspectiveAPI();
        if (api == null)
            return;

        for(int i = 0; i < 4; i++)
            event.setLine(i, "");

        try {
            if(plugin.getSettings().isDebug()) {
                plugin.getLogger().info("[DEBUG] Censoring sign text: " + ListUtils.toString(lines, ", "));
            }

            api.censorAsync(ListUtils.toString(lines, " ")).thenAccept(pair -> {
                String[] censored = StringUtils.chunked(pair.getFirst(), 15);
                Block block = event.getBlock();
                Sign sign = (Sign) block.getState();
                Side side = event.getSide();

                if (!(block.getState() instanceof Sign)) return;
                if (!pair.getSecond()) {
                    censored = lines;
                }

                for (int i = 0; i < 4; i++) {
                    sign.getSide(side).setLine(i, censored[i]);
                    sign.update();
                }
            });
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
