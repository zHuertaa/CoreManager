package com.zhuerta.coremanager.placeholders;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlaceholderManager {

    private final ProxyServer proxyServer;
    private LuckPerms luckPerms;

    public PlaceholderManager(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        // Intentamos cargar LuckPerms, pero manejamos el caso en que no esté disponible
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            this.luckPerms = null; // LuckPerms no está cargado
        }
    }

    /**
     * Reemplaza los placeholders en un mensaje para un jugador específico.
     *
     * @param message El mensaje con posibles placeholders.
     * @param player  El jugador para el cual se reemplazan los placeholders.
     * @return El mensaje con los placeholders reemplazados.
     */
    public String replacePlaceholders(String message, Player player) {
        if (message == null || player == null) {
            return message;
        }

        String result = message;

        // Reemplazar %player_name% con el nombre del jugador
        result = result.replace("%player_name%", player.getUsername());

        // Reemplazar %server% con el nombre del servidor actual del jugador
        result = result.replace("%server%", player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("desconocido"));

        // Reemplazar %online_players% con el número de jugadores conectados al servidor actual
        result = result.replace("%online_players%", String.valueOf(player.getCurrentServer()
                .map(server -> server.getServer().getPlayersConnected().size())
                .orElse(0)));

        // Reemplazar %total_online_players% con el número total de jugadores en el proxy
        result = result.replace("%total_online_players%", String.valueOf(proxyServer.getPlayerCount()));

        // Reemplazar %time% con la hora actual
        result = result.replace("%time%", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Reemplazar %ping% con el ping del jugador
        result = result.replace("%ping%", String.valueOf(player.getPing()) + "ms");

        // Reemplazar %player_prefix% con el prefijo del jugador (usando LuckPerms)
        result = result.replace("%player_prefix%", getPlayerPrefix(player));

        return result;
    }

    /**
     * Método para mensajes sin jugador (no se reemplazan placeholders relacionados con el jugador).
     *
     * @param message El mensaje con posibles placeholders.
     * @return El mensaje con los placeholders reemplazados (solo los que no dependen de un jugador).
     */
    public String replacePlaceholders(String message) {
        if (message == null) {
            return message;
        }

        String result = message;

        // Reemplazar %total_online_players% con el número total de jugadores en el proxy
        result = result.replace("%total_online_players%", String.valueOf(proxyServer.getPlayerCount()));

        // Reemplazar %time% con la hora actual
        result = result.replace("%time%", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        return result;
    }

    /**
     * Obtiene el prefijo del jugador usando LuckPerms.
     *
     * @param player El jugador.
     * @return El prefijo del jugador, o una cadena vacía si no tiene prefijo o LuckPerms no está disponible.
     */
    private String getPlayerPrefix(Player player) {
        if (luckPerms == null) {
            return ""; // LuckPerms no está disponible
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                return prefix != null ? prefix : "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}