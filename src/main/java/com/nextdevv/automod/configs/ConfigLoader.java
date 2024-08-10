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

    public PlayerModStatus loadPlayerModStatus() {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "playermodstatus.yml"));
        PlayerModStatus playerModStatus;
        if(!file.exists()) {
            playerModStatus = new PlayerModStatus();
            file.setFileObj(playerModStatus);
            file.save();
            return playerModStatus;
        }
        file.load();

        playerModStatus = (PlayerModStatus) file.getFileObj(PlayerModStatus.class);
        return playerModStatus;
    }

    public Ignores loadIgnores() {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "ignores.yml"));
        Ignores ignores;
        if(!file.exists()) {
            ignores = new Ignores();
            file.setFileObj(ignores);
            file.save();
            return ignores;
        }
        file.load();

        ignores = (Ignores) file.getFileObj(Ignores.class);
        return ignores;
    }

    public void saveIgnores(Ignores ignores) {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "ignores.yml"));
        file.setFileObj(ignores);
        file.save();
    }

    public void savePlayerModStatus(PlayerModStatus playerModStatus) {
        if(!folder.exists()) {
            boolean successful = folder.mkdirs();
            if(!successful) {
                throw new RuntimeException("Failed to create plugin folder.");
            }
        }

        YamlFile file = new YamlFile(new File(folder, "playermodstatus.yml"));
        file.setFileObj(playerModStatus);
        file.save();
    }
}
