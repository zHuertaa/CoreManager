package com.zhuerta.coremanager.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zhuerta.coremanager.placeholders.PlaceholderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public class TextProcessor {

    private final PlaceholderManager placeholderManager;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    public TextProcessor(ProxyServer proxyServer) {
        this.placeholderManager = new PlaceholderManager(proxyServer);
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
    }

    /**
     * Procesa un texto, reemplazando placeholders y aplicando formatos (MiniMessage o legacy).
     *
     * @param text   El texto a procesar.
     * @param player El jugador para el cual se reemplazan los placeholders (puede ser null si no se necesitan placeholders).
     * @return El componente de texto procesado.
     */
    public Component processText(String text, Player player) {
        if (text == null) {
            return Component.empty();
        }

        // Primero reemplazamos los placeholders
        String processedText = player != null
                ? placeholderManager.replacePlaceholders(text, player)
                : placeholderManager.replacePlaceholders(text);

        // Luego procesamos el texto con MiniMessage o el formato legacy
        boolean hasMiniMessageTags = Pattern.compile("<[^>]+>").matcher(processedText).find();
        if (hasMiniMessageTags) {
            return miniMessage.deserialize(processedText);
        } else {
            return legacySerializer.deserialize(processedText);
        }
    }

    /**
     * Procesa un texto sin reemplazar placeholders (útil para mensajes que no dependen de un jugador).
     *
     * @param text El texto a procesar.
     * @return El componente de texto procesado.
     */
    public Component processText(String text) {
        return processText(text, null);
    }
}