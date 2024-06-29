package it.unilix.automod.commands.sub;

import it.unilix.automod.AutoMod;
import it.unilix.automod.commands.ICommand;
import it.unilix.automod.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class StatusCommand implements ICommand {
    private final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Check the status of the plugin.";
    }

    @Override
    public String getUsage() {
        return "/automod status";
    }

    @Override
    public HashMap<Integer, String> getArgs() {
        return new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ChatUtils.msg(sender, "&eChecking application status...");
        long start = System.currentTimeMillis();
        boolean redis = plugin.getRedisManager() != null && plugin.getRedisManager().isConnected();
        boolean perspective = plugin.getPerspectiveAPI() != null && plugin.getPerspectiveAPI().isConnected();
        boolean litebans = plugin.getLiteBans() != null;
        boolean cache = plugin.getCacheManager().isLoaded();
        long end = System.currentTimeMillis();

        ChatUtils.msg(sender, "&eApplication status:");
        ChatUtils.msg(sender, "&eRedis: " + (redis ? "&aConnected" : "&cDisconnected"));
        ChatUtils.msg(sender, "&ePerspective API: " + (perspective ? "&aConnected" : "&cDisconnected"));
        ChatUtils.msg(sender, "&eLiteBans: " + (litebans ? "&aConnected" : "&cDisconnected"));
        ChatUtils.msg(sender, "&eCache: " + (cache ? "&aLoaded" : "&cNot loaded"));
        ChatUtils.msg(sender, "&eTime taken: &a" + (end - start) + "ms");
    }
}
