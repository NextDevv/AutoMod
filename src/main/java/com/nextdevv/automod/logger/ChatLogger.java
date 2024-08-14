package com.nextdevv.automod.logger;

import com.nextdevv.automod.AutoMod;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatLogger {
    private final AutoMod plugin;
    private File currentLogFile;
    private File logFolder;
    private SimpleDateFormat dateFormat;
    private final List<String> logs = new ArrayList<>();

    public ChatLogger(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void init() {
        logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists() && !logFolder.mkdirs()) {
            throw new RuntimeException("Failed to create logs folder.");
        }

        currentLogFile = new File(logFolder, "chat.log");
        if (!currentLogFile.exists()) {
            try {
                if (!currentLogFile.createNewFile()) {
                    throw new RuntimeException("Failed to create chat.log file.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create chat.log file.", e);
            }
        }

        dateFormat = new SimpleDateFormat(plugin.getSettings().getDateFormat());
    }

    public void log(Player player, String message, boolean toxic) {
        String format = toxic ? plugin.getSettings().getToxicChatLogFormat() : plugin.getSettings().getChatLogFormat();
        logs.add(format.replace("{date}", dateFormat.format(new Date()))
                .replace("{message}", message)
                .replace("{player}", player.getName()));
    }

    public void logSign(Player player, Location location, String message, boolean toxic) {
        String format = toxic ? plugin.getSettings().getToxicSignLogFormat() : plugin.getSettings().getSignLogFormat();
        logs.add(format.replace("{date}", dateFormat.format(new Date()))
                .replace("{message}", message)
                .replace("{player}", player.getName())
                .replace("{world}", Objects.requireNonNull(location.getWorld()).getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ())));
    }

    public void save() {
        try {
            if (!currentLogFile.exists()) {
                plugin.getLogger().warning("Chat log file not found.");
                return;
            }
            String fileName = plugin.getSettings().getChatLogFileName().replace("{date}", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
            File newLogFile = new File(logFolder, fileName);
            if (!currentLogFile.renameTo(newLogFile)) {
                throw new RuntimeException("Failed to save chat logs.");
            }
            currentLogFile = newLogFile;
            try (FileWriter writer = new FileWriter(currentLogFile, true)) {
                for (String log : logs) {
                    writer.write(log + "\n");
                }
            }
            logs.clear();
            currentLogFile = new File(logFolder, "chat.log");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save chat logs.", e);
        }
    }

    public List<String> getChatLogs() {
        return logs;
    }

    public List<List<String>> paginate(List<String> logs, int i) {
        List<List<String>> result = new ArrayList<>();
        if (i <= 0 || logs == null || logs.isEmpty()) {
            return result;
        }

        for (int start = 0; start < logs.size(); start += i) {
            result.add(new ArrayList<>(logs.subList(start, Math.min(start + i, logs.size()))));
        }

        return result;
    }
}