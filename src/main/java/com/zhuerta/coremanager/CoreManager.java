package com.zhuerta.coremanager;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zhuerta.coremanager.announcements.AnnouncementsModule;
import com.zhuerta.coremanager.broadcast.BroadcastModule;
import com.zhuerta.coremanager.commands.CommandManager;
import com.zhuerta.coremanager.config.MessagesConfig;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

@Plugin(id = "coremanager", name = "CoreManager", version = "1.0-SNAPSHOT", authors = {"zhuerta"})
public class CoreManager {

    private final ProxyServer server;
    private AnnouncementsModule announcementsModule;
    private BroadcastModule broadcastModule;
    private TextProcessor textProcessor;
    private MessagesConfig messagesConfig;
    private final Map<String, Long> lastSentTimes = new HashMap<>();

    @Inject
    public CoreManager(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Inicializamos el TextProcessor y MessagesConfig
        this.textProcessor = new TextProcessor(server);
        this.messagesConfig = new MessagesConfig(textProcessor);

        Component startingMessage = messagesConfig.getMessage("plugin.starting");
        if (startingMessage != null) {
            server.getConsoleCommandSource().sendMessage(startingMessage);
        }

        // Crear los metadatos del comando
        CommandMeta commandMeta = server.getCommandManager()
                .metaBuilder("coremanager")
                .aliases("cm")
                .build();

        // Registrar el comando usando el método moderno
        server.getCommandManager().register(commandMeta, new CommandManager(this));

        // Inicializar módulos
        this.announcementsModule = new AnnouncementsModule(this, textProcessor);
        this.announcementsModule.start();

        this.broadcastModule = new BroadcastModule(this, textProcessor);

        Component startedMessage = messagesConfig.getMessage("plugin.started");
        if (startedMessage != null) {
            server.getConsoleCommandSource().sendMessage(startedMessage);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        Component stoppingMessage = messagesConfig.getMessage("plugin.stopping");
        if (stoppingMessage != null) {
            server.getConsoleCommandSource().sendMessage(stoppingMessage);
        }

        if (announcementsModule != null) {
            announcementsModule.stop();
        }

        Component stoppedMessage = messagesConfig.getMessage("plugin.stopped");
        if (stoppedMessage != null) {
            server.getConsoleCommandSource().sendMessage(stoppedMessage);
        }
    }

    public void reload() {
        // Recargar MessagesConfig
        if (messagesConfig != null) {
            messagesConfig.reload();
        }

        // Detener los módulos actuales
        if (announcementsModule != null) {
            announcementsModule.stop();
        }

        // Recargar BroadcastModule
        if (broadcastModule != null) {
            broadcastModule.reload();
        }

        // Reiniciar los módulos
        this.announcementsModule = new AnnouncementsModule(this, textProcessor);
        this.announcementsModule.start();

        this.broadcastModule = new BroadcastModule(this, textProcessor);
    }

    public ProxyServer getServer() {
        return server;
    }

    public TextProcessor getTextProcessor() {
        return textProcessor;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public AnnouncementsModule getAnnouncementsModule() {
        return announcementsModule;
    }

    public BroadcastModule getBroadcastModule() {
        return broadcastModule;
    }

    public Map<String, Long> getLastSentTimes() {
        return lastSentTimes;
    }
}