package com.zhuerta.coremanager.staffchat;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.Component;

import javax.annotation.Nonnull; // Añadimos la importación

public class DiscordBotListener extends ListenerAdapter {

    private final ProxyServer server;
    private final TextProcessor textProcessor;
    private final StaffChatConfig config;
    private JDA jda;
    private TextChannel discordChannel;

    public DiscordBotListener(ProxyServer server, TextProcessor textProcessor, StaffChatConfig config) {
        this.server = server;
        this.textProcessor = textProcessor;
        this.config = config;
        initializeBot();
    }

    private void initializeBot() {
        String botToken = config.getDiscordBotToken();
        String channelId = config.getDiscordChannelId();

        if (botToken == null || botToken.isEmpty()) {
            server.getConsoleCommandSource().sendMessage(
                    Component.text("StaffChat: Discord bot token is not set in staffchat.yml! Bot will not start.", net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(this)
                    .build()
                    .awaitReady();

            discordChannel = jda.getTextChannelById(channelId);
            if (discordChannel == null) {
                server.getConsoleCommandSource().sendMessage(
                        Component.text("StaffChat: Discord channel with ID " + channelId + " not found! Bot will not function properly.", net.kyori.adventure.text.format.NamedTextColor.RED));
                return;
            }

            server.getConsoleCommandSource().sendMessage(
                    Component.text("StaffChat: Discord bot started successfully. Listening on channel: " + discordChannel.getName(), net.kyori.adventure.text.format.NamedTextColor.GREEN));
        } catch (Exception e) {
            server.getConsoleCommandSource().sendMessage(
                    Component.text("StaffChat: Failed to start Discord bot: " + e.getMessage(), net.kyori.adventure.text.format.NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) { // Añadimos @Nonnull
        if (event.getAuthor().isBot() || !event.getChannel().getId().equals(config.getDiscordChannelId())) {
            return;
        }

        String username = event.getAuthor().getName();
        String message = event.getMessage().getContentRaw();
        String formattedMessage = config.getDiscordToMcFormat()
                .replace("%username%", username)
                .replace("%message%", message);
        Component chatMessage = textProcessor.processText(formattedMessage);

        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission(config.getStaffChatPermission())) {
                player.sendMessage(chatMessage);
            }
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            server.getConsoleCommandSource().sendMessage(
                    Component.text("StaffChat: Discord bot shut down.", net.kyori.adventure.text.format.NamedTextColor.GREEN));
        }
    }

    public TextChannel getDiscordChannel() {
        return discordChannel;
    }
}