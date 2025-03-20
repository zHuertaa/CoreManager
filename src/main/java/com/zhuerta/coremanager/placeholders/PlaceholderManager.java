package com.zhuerta.coremanager.placeholders;

import com.velocitypowered.api.proxy.Player;

public class PlaceholderManager {

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

        // Reemplazar %player_name% con el nombre del jugador
        return message.replace("%player_name%", player.getUsername());
    }

    /**
     * Método para agregar soporte a más placeholders en el futuro.
     * Por ejemplo, podrías añadir %server%, %online_players%, etc.
     */
    public String replacePlaceholders(String message, Player player, Object... additionalData) {
        // Por ahora, solo llamamos al método básico
        return replacePlaceholders(message, player);
    }
}