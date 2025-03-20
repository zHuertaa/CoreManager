package com.zhuerta.coremanager;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zhuerta.coremanager.announcements.AnnouncementsModule;
import net.kyori.adventure.text.Component; // Import para Component

@Plugin(id = "coremanager", name = "CoreManager", version = "1.0-SNAPSHOT", authors = {"zhuerta"})
public class CoreManager {

    private final ProxyServer server;
    private AnnouncementsModule announcementsModule;

    @Inject
    public CoreManager(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Mensaje de inicio
        server.getConsoleCommandSource().sendMessage(
            Component.text("[CoreManager] Iniciando Plugin...")
        );

        this.announcementsModule = new AnnouncementsModule(this);
        this.announcementsModule.start();

        // Mensaje de confirmación
        server.getConsoleCommandSource().sendMessage(
            Component.text("[CoreManager] ¡Iniciado correctamente!")
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // Mensaje de cierre
        server.getConsoleCommandSource().sendMessage(
            Component.text("[CoreManager] Deteniendo Plugin...")
        );

        if (announcementsModule != null) {
            announcementsModule.stop();
        }

        // Mensaje de confirmación
        server.getConsoleCommandSource().sendMessage(
            Component.text("[CoreManager] ¡Detenido correctamente!")
        );
    }

    public ProxyServer getServer() {
        return server;
    }
}