package com.zhuerta.coremanager.config;

import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.text.Component;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessagesConfig {

    private final TextProcessor textProcessor;
    private ConfigurationNode config;
    private final Path configPath;
    private String prefix;

    public MessagesConfig(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
        this.configPath = new File("plugins/CoreManager/mensajes.yml").toPath();
        loadConfig();
        loadMessages();
    }

    private void loadConfig() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            if (!Files.exists(configPath)) {
                // Intentar copiar el archivo desde los recursos
                try (InputStream inputStream = getClass().getResourceAsStream("/mensajes.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, configPath);
                    } else {
                        // Si no se encuentra en los recursos, crear un archivo vacío
                        Files.createFile(configPath);
                    }
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

    private void loadMessages() {
        // Cargar el prefijo
        this.prefix = config.getNode("prefix").getString("");
    }

    /**
     * Recarga el archivo mensajes.yml y actualiza los mensajes en memoria.
     */
    public void reload() {
        loadConfig();
        loadMessages();
    }

    /**
     * Obtiene un mensaje del archivo de configuración y lo procesa con TextProcessor.
     *
     * @param path La ruta del mensaje en el archivo (por ejemplo, "plugin.starting").
     * @param replacements Pares clave-valor para reemplazar placeholders (por ejemplo, "%path%", "/ruta").
     * @return El componente de texto procesado, o null si el mensaje es vacío.
     */
    public Component getMessage(String path, Object... replacements) {
        String message = config.getNode((Object[]) path.split("\\.")).getString("Mensaje no encontrado: " + path);

        // Reemplazar el placeholder %prefix% con el valor del prefijo
        message = message.replace("%prefix%", prefix);

        // Verificar si el mensaje es vacío
        if (message.trim().isEmpty()) {
            return null;
        }

        // Reemplazar placeholders personalizados
        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = replacements[i].toString();
            String value = replacements[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        return textProcessor.processText(message);
    }

    /**
     * Obtiene un mensaje del archivo de configuración y lo procesa con TextProcessor, incluyendo información del jugador.
     *
     * @param path La ruta del mensaje en el archivo (por ejemplo, "plugin.starting").
     * @param player El jugador para el cual se procesan los placeholders (puede ser null).
     * @param replacements Pares clave-valor para reemplazar placeholders (por ejemplo, "%path%", "/ruta").
     * @return El componente de texto procesado, o null si el mensaje es vacío.
     */
    public Component getMessage(String path, com.velocitypowered.api.proxy.Player player, Object... replacements) {
        String message = config.getNode((Object[]) path.split("\\.")).getString("Mensaje no encontrado: " + path);

        // Reemplazar el placeholder %prefix% con el valor del prefijo
        message = message.replace("%prefix%", prefix);

        // Verificar si el mensaje es vacío
        if (message.trim().isEmpty()) {
            return null;
        }

        // Reemplazar placeholders personalizados
        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = replacements[i].toString();
            String value = replacements[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        return textProcessor.processText(message, player);
    }
}