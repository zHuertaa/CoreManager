package com.zhuerta.coremanager.staffchat;

import com.zhuerta.coremanager.CoreManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaffChatConfig {

    private File configFile;
    private ConfigurationNode configNode;

    private String staffChatPermission;
    private String staffChatFormat;
    private String discordBotToken;
    private String discordChannelId;
    private String discordWebhookUrl;
    private String discordFormat;
    private String discordToMcFormat;
    private String webhookNameFormat; // Nuevo campo para el nombre del webhook

    public StaffChatConfig(CoreManager plugin) {
        loadConfig();
    }

    private void loadConfig() {
        try {
            configFile = new File("plugins/CoreManager", "staffchat.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                Files.copy(getClass().getResourceAsStream("/staffchat.yml"), configFile.toPath());
            }

            YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder()
                    .setFile(configFile)
                    .build();
            configNode = loader.load();

            staffChatPermission = configNode.getNode("staffchat", "permission").getString("coremanager.staffchat");
            staffChatFormat = configNode.getNode("staffchat", "format").getString("&7[StaffChat] &e%player_name%&7: %message%");
            discordBotToken = configNode.getNode("discord", "bot-token").getString("");
            discordChannelId = configNode.getNode("discord", "channel-id").getString("");
            discordWebhookUrl = configNode.getNode("discord", "webhook-url").getString("");
            discordFormat = configNode.getNode("staffchat", "discord-format").getString("&7[StaffChat] &e%player_name% &7(%server%)&7: %message%");
            discordToMcFormat = configNode.getNode("staffchat", "discord-to-mc-format").getString("&7[StaffChat] &9[Discord] &e%username%&7: %message%");
            webhookNameFormat = configNode.getNode("staffchat", "webhook-name-format").getString("%player_name%"); // Cargamos el nuevo campo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        loadConfig();
    }

    public String getStaffChatPermission() {
        return staffChatPermission;
    }

    public String getStaffChatFormat() {
        return staffChatFormat;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public String getDiscordFormat() {
        return discordFormat;
    }

    public String getDiscordToMcFormat() {
        return discordToMcFormat;
    }

    public String getWebhookNameFormat() { // Nuevo método
        return webhookNameFormat;
    }
}