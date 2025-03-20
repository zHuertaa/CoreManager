package com.zhuerta.coremanager.broadcast;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BroadcastModule {

    private final CoreManager plugin;
    private final ProxyServer server;
    private final TextProcessor textProcessor;
    private final BroadcastConfig config;

    public BroadcastModule(CoreManager plugin, TextProcessor textProcessor) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.textProcessor = textProcessor;
        this.config = new BroadcastConfig(plugin, textProcessor);
    }

    public void reload() {
        if (config != null) {
            config.reload();
        }
    }

    /**
     * Envía un anuncio personalizado a los servidores y tipos especificados.
     *
     * @param serversStr Lista de servidores (o "all").
     * @param typesStr Lista de tipos (o "all").
     * @param message Mensaje a enviar (para TITULOS, puede ser "título|subtítulo").
     * @return true si el anuncio se envió correctamente, false si hubo un error.
     */
    public boolean sendBroadcast(String serversStr, String typesStr, String message) {
        // Determinar los servidores objetivo
        Set<RegisteredServer> targetServers = new HashSet<>();
        if (serversStr.equalsIgnoreCase("all")) {
            targetServers.addAll(server.getAllServers());
        } else {
            List<String> serverNames = Arrays.asList(serversStr.split(","));
            for (String serverName : serverNames) {
                Optional<RegisteredServer> server = this.server.getServer(serverName.trim());
                if (server.isPresent()) {
                    targetServers.add(server.get());
                }
            }
        }

        if (targetServers.isEmpty()) {
            return false; // No se encontraron servidores válidos
        }

        // Determinar los tipos de anuncio
        Set<String> targetTypes = new HashSet<>();
        if (typesStr.equalsIgnoreCase("all")) {
            targetTypes.addAll(Arrays.asList("TEXTO", "ACTIONBAR", "BOSSBAR", "TITULOS"));
        } else {
            targetTypes.addAll(Arrays.asList(typesStr.split(",")).stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet()));
        }

        if (targetTypes.isEmpty()) {
            return false; // No se especificaron tipos válidos
        }

        // Procesar el mensaje para TITULOS
        String titleMessage = message;
        String subtitleMessage = config.getSubtitleDefault();
        if (targetTypes.contains("TITULOS") && message.contains("|")) {
            String[] parts = message.split("\\|", 2);
            titleMessage = parts[0].trim();
            subtitleMessage = parts[1].trim();
        }

        // Procesar los componentes del mensaje
        Component messageComponent = textProcessor.processText(message);
        Component titleComponent = textProcessor.processText(titleMessage);
        Component subtitleComponent = textProcessor.processText(subtitleMessage);

        // Enviar el anuncio a los jugadores en los servidores objetivo
        for (RegisteredServer targetServer : targetServers) {
            for (Player player : targetServer.getPlayersConnected()) {
                // Verificar si el jugador tiene el permiso para ver anuncios
                String permission = plugin.getAnnouncementsModule().getConfig().getGlobalPermission();
                if (!player.hasPermission(permission)) {
                    continue;
                }

                // Enviar el anuncio según los tipos especificados
                for (String type : targetTypes) {
                    switch (type) {
                        case "TEXTO":
                            player.sendMessage(messageComponent);
                            break;
                        case "ACTIONBAR":
                            player.sendActionBar(messageComponent);
                            break;
                        case "BOSSBAR":
                            BossBar bossBar = BossBar.bossBar(
                                    messageComponent,
                                    1.0f,
                                    BossBar.Color.valueOf(config.getBossbarColor()),
                                    BossBar.Overlay.valueOf(config.getBossbarStyle())
                            );
                            if (config.isBossbarProgresoDinamico()) {
                                bossBar.progress(0.0f);
                            }
                            player.showBossBar(bossBar);
                            server.getScheduler()
                                    .buildTask(plugin, () -> player.hideBossBar(bossBar))
                                    .delay(Duration.ofSeconds(config.getBossbarDuration()))
                                    .schedule();
                            break;
                        case "TITULOS":
                            Title title = Title.title(titleComponent, subtitleComponent);
                            player.showTitle(title);
                            break;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Obtiene la lista de nombres de servidores disponibles.
     *
     * @return Lista de nombres de servidores.
     */
    public List<String> getServerNames() {
        return server.getAllServers().stream()
                .map(server -> server.getServerInfo().getName())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la lista de tipos de anuncios disponibles.
     *
     * @return Lista de tipos de anuncios.
     */
    public List<String> getAnnouncementTypes() {
        return Arrays.asList("TEXTO", "ACTIONBAR", "BOSSBAR", "TITULOS");
    }
}