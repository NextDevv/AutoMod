package it.unilix.automod.configs;

import it.unilix.automod.AutoMod;
import it.unilix.json.JsonFile;
import it.unilix.yaml.YamlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ConfigLoader {
    @NotNull
    private final File folder;

    public ConfigLoader(@NotNull AutoMod plugin) {
        this.folder = plugin.getDataFolder();
    }

    public Settings loadSettings() {
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

    public Messages loadMessages() {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        JsonFile file = new JsonFile(new File(folder, "messages.json"));
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
