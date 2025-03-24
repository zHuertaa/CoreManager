package com.zhuerta.coremanager.staffchat;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class DiscordWebhookSender {

    private final ProxyServer server;
    private final String webhookUrl;
    private final String messageFormat; // Formato para el mensaje
    private final String webhookNameFormat; // Formato para el nombre del webhook
    private final OkHttpClient httpClient;

    public DiscordWebhookSender(ProxyServer server, String webhookUrl, String messageFormat, String webhookNameFormat) {
        this.server = server;
        this.webhookUrl = webhookUrl;
        this.messageFormat = messageFormat;
        this.webhookNameFormat = webhookNameFormat;
        this.httpClient = new OkHttpClient();
    }

    public void sendMessage(String playerName, String message, String serverName) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            server.getConsoleCommandSource().sendMessage(
                    Component.text("StaffChat: Webhook URL is not set in staffchat.yml! Cannot send message to Discord.", NamedTextColor.RED));
            return;
        }

        // Construimos el mensaje usando el formato configurable
        String formattedMessage = messageFormat
                .replace("%player_name%", playerName)
                .replace("%message%", message)
                .replace("%server%", serverName)
                .replaceAll("&[0-9a-fk-or]", ""); // Eliminamos códigos de color de Minecraft

        // Construimos el nombre del webhook usando el formato configurable
        String formattedWebhookName = webhookNameFormat
                .replace("%player_name%", playerName)
                .replace("%server%", serverName)
                .replaceAll("&[0-9a-fk-or]", ""); // Eliminamos códigos de color de Minecraft

        // Construimos el payload JSON para el webhook
        JSONObject payload = new JSONObject();
        payload.put("content", formattedMessage);
        payload.put("username", formattedWebhookName); // Usamos el nombre formateado
        String avatarUrl = "https://mc-heads.net/avatar/" + playerName + "/100";
        payload.put("avatar_url", avatarUrl);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                server.getConsoleCommandSource().sendMessage(
                        Component.text("StaffChat: Failed to send message to Discord webhook: " + e.getMessage(), NamedTextColor.RED));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    server.getConsoleCommandSource().sendMessage(
                            Component.text("StaffChat: Failed to send message to Discord webhook: " + response.message(), NamedTextColor.RED));
                } else {
                    server.getConsoleCommandSource().sendMessage(
                            Component.text("StaffChat: Message sent to Discord webhook: " + formattedMessage, NamedTextColor.GREEN));
                }
                response.close();
            }
        });
    }
}