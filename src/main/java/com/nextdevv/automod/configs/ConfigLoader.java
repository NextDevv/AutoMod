package com.nextdevv.automod.configs;

import com.nextdevv.automod.AutoMod;
import it.unilix.yaml.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ConfigLoader {
    @NotNull
    private final File folder;

    public ConfigLoader(@NotNull AutoMod plugin) {
        this.folder = plugin.getDataFolder();
    }

    public @NotNull Settings loadSettings() {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "config.yml"));
        Settings settings;
        if(!file.exists()) {
            settings = new Settings();
            file.setFileObj(settings);
            file.save();
            return settings;
        }
        file.load();

        settings = (Settings) file.getFileObj(Settings.class);
        return settings;
    }

    public @NotNull Messages loadMessages() {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "messages.yml"));
        Messages messages;
        if(!file.exists()) {
            messages = new Messages();
            file.setFileObj(messages);
            file.save();
            return messages;
        }
        file.load();

        messages = (Messages) file.getFileObj(Messages.class);
        return messages;
    }
}
