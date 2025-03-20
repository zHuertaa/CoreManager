package com.zhuerta.coremanager.broadcast;

import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.config.MessagesConfig;
import com.zhuerta.coremanager.utils.TextProcessor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BroadcastConfig {

    private final CoreManager plugin;
    private final MessagesConfig messagesConfig;
    private ConfigurationNode config;
    private final Path configPath;

    // Valores por defecto para BOSSBAR
    private int bossbarDuration;
    private boolean bossbarProgresoDinamico;
    private String bossbarColor;
    private String bossbarStyle;

    // Valores por defecto para TITULOS
    private String titleDefault;
    private String subtitleDefault;

    public BroadcastConfig(CoreManager plugin, TextProcessor textProcessor) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
        this.configPath = new File("plugins/CoreManager/broadcast.yml").toPath();
        loadConfig();
        loadSettings();
    }

    private void loadConfig() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
                Component creatingDirectoryMessage = messagesConfig.getMessage("config.creating-directory");
                if (creatingDirectoryMessage != null) {
                    plugin.getServer().getConsoleCommandSource().sendMessage(creatingDirectoryMessage);
                }
            }
            if (!Files.exists(configPath)) {
                // Intentar copiar el archivo desde los recursos
                try (InputStream inputStream = getClass().getResourceAsStream("/broadcast.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, configPath);
                        Component copyingFileMessage = messagesConfig.getMessage("config.copying-file", "%path%", configPath.toString());
                        if (copyingFileMessage != null) {
                            plugin.getServer().getConsoleCommandSource().sendMessage(copyingFileMessage);
                        }
                    } else {
                        // Si no se encuentra en los recursos, crear un archivo vacío
                        Files.createFile(configPath);
                        Component fileNotFoundMessage = messagesConfig.getMessage("config.file-not-found", "%path%", configPath.toString());
                        if (fileNotFoundMessage != null) {
                            plugin.getServer().getConsoleCommandSource().sendMessage(fileNotFoundMessage);
                        }
                    }
                }
            } else {
                Component fileExistsMessage = messagesConfig.getMessage("config.file-exists", "%path%", configPath.toString());
                if (fileExistsMessage != null) {
                    plugin.getServer().getConsoleCommandSource().sendMessage(fileExistsMessage);
                }
            }
            YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder()
                    .setPath(configPath)
                    .build();
            this.config = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        // Cargar configuraciones para BOSSBAR
        ConfigurationNode bossbarNode = config.getNode("bossbar");
        this.bossbarDuration = bossbarNode.getNode("duracion").getInt(5);
        this.bossbarProgresoDinamico = bossbarNode.getNode("progreso-dinamico").getBoolean(false);
        this.bossbarColor = bossbarNode.getNode("color").getString("YELLOW");
        this.bossbarStyle = bossbarNode.getNode("estilo").getString("NOTCHED_10");

        // Cargar configuraciones para TITULOS
        ConfigurationNode titulosNode = config.getNode("titulos");
        this.titleDefault = titulosNode.getNode("titulo").getString("");
        this.subtitleDefault = titulosNode.getNode("subtitulo").getString("");
    }

    public void reload() {
        loadConfig();
        loadSettings();
    }

    // Getters
    public int getBossbarDuration() {
        return bossbarDuration;
    }

    public boolean isBossbarProgresoDinamico() {
        return bossbarProgresoDinamico;
    }

    public String getBossbarColor() {
        return bossbarColor;
    }

    public String getBossbarStyle() {
        return bossbarStyle;
    }

    public String getTitleDefault() {
        return titleDefault;
    }

    public String getSubtitleDefault() {
        return subtitleDefault;
    }
}