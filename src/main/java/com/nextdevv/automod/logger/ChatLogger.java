package com.nextdevv.automod.logger;

import com.nextdevv.automod.AutoMod;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatLogger {
    private final AutoMod plugin;
    private File currentLogFile;
    private File logFolder;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<String> logs = new ArrayList<>();

    public ChatLogger(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void init() {
        logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) {
            boolean successful = logFolder.mkdirs();
            if (!successful) {
                throw new RuntimeException("Failed to create logs folder.");
            }
        }

        currentLogFile = new File(logFolder, "chat.log");
        if(!currentLogFile.exists()) {
            try {
                boolean successful = currentLogFile.createNewFile();
                if (!successful) {
                    throw new RuntimeException("Failed to create chat.log file.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create chat.log file.", e);
            }
        }

        dateFormat = new SimpleDateFormat(plugin.getSettings().getDateFormat());
    }

    public void log(Player player, String message, boolean toxic) {
        String format;
        if(toxic) {
            format = plugin.getSettings().getToxicChatLogFormat();
        }else format = plugin.getSettings().getChatLogFormat();
        format = format
                .replace("{date}", dateFormat.format(new Date()))
                .replace("{message}", message)
                .replace("{player}", player.getName());

        logs.add(format);
    }

    public void logSign(Player player, Location location, String message, boolean toxic) {
        String format;
        if(toxic) {
            format = plugin.getSettings().getToxicSignLogFormat();
        }else format = plugin.getSettings().getSignLogFormat();
        format = format
                .replace("{date}", dateFormat.format(new Date()))
                .replace("{message}", message)
                .replace("{player}", player.getName())
                .replace("{world}", Objects.requireNonNull(location.getWorld()).getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ()));

        logs.add(format);
    }

    public void save() {
        try {
            if(!currentLogFile.exists()) {
                plugin.getLogger().warning("Chat log file not found.");
                return;
            }
            String fileNameFormat = plugin.getSettings().getChatLogFileName();
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fileName = fileNameFormat.replace("{date}", format.format(date));
            File newLogFile = new File(logFolder, fileName);
            boolean successful = currentLogFile.renameTo(newLogFile);
            if (!successful) {
                throw new RuntimeException("Failed to save chat logs.");
            }
            currentLogFile = newLogFile;
            FileWriter writer = new FileWriter(currentLogFile, true);
            for(String log : logs) {
                writer.write(log + "\n");
            }
            writer.close();
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
            int end = Math.min(start + i, logs.size());
            result.add(new ArrayList<>(logs.subList(start, end)));
        }

        return result;
    }
}
