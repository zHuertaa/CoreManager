package com.zhuerta.coremanager.staffchat;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.text.Component;

public class StaffChatModule {

    private final CoreManager plugin;
    private final TextProcessor textProcessor;
    private final StaffChatConfig config;
    private DiscordWebhookSender webhookSender;
    private DiscordBotListener botListener;

    public StaffChatModule(CoreManager plugin, TextProcessor textProcessor) {
        this.plugin = plugin;
        this.textProcessor = textProcessor;
        this.config = new StaffChatConfig(plugin);
        this.webhookSender = new DiscordWebhookSender(
                plugin.getServer(),
                config.getDiscordWebhookUrl(),
                config.getDiscordFormat(),
                config.getWebhookNameFormat()
        );
        this.botListener = new DiscordBotListener(plugin.getServer(), textProcessor, config);
        plugin.getServer().getEventManager().register(plugin, this);
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!message.startsWith("!")) {
            return;
        }

        if (!player.hasPermission(config.getStaffChatPermission())) {
            return;
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());

        String playerDisplayName = player.getGameProfile().getName();
        String serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Unknown";
        String formattedMessage = config.getStaffChatFormat()
                .replace("%player_name%", playerDisplayName)
                .replace("%message%", message.substring(1))
                .replace("%server%", serverName);
        Component chatMessage = textProcessor.processText(formattedMessage, player);

        for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
            if (onlinePlayer.hasPermission(config.getStaffChatPermission())) {
                onlinePlayer.sendMessage(chatMessage);
            }
        }

        // Enviar al webhook de Discord
        webhookSender.sendMessage(playerDisplayName, message.substring(1), serverName);
    }

    public void shutdown() {
        botListener.shutdown();
    }

    public void reload() {
        shutdown();
        config.reload();
        this.webhookSender = new DiscordWebhookSender(
                plugin.getServer(),
                config.getDiscordWebhookUrl(),
                config.getDiscordFormat(),
                config.getWebhookNameFormat()
        );
        this.botListener = new DiscordBotListener(plugin.getServer(), textProcessor, config);
    }

    public StaffChatConfig getConfig() {
        return config;
    }

    public DiscordBotListener getBotListener() {
        return botListener;
    }
}