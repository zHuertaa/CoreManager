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
import com.zhuerta.coremanager.commands.StaffChatCommand;
import com.zhuerta.coremanager.config.MessagesConfig;
import com.zhuerta.coremanager.staffchat.StaffChatModule;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

@Plugin(id = "coremanager", name = "CoreManager", version = "1.0-SNAPSHOT", authors = {"zhuerta"})
public class CoreManager {

    private final ProxyServer server;
    private AnnouncementsModule announcementsModule;
    private BroadcastModule broadcastModule;
    private StaffChatModule staffChatModule;
    private TextProcessor textProcessor;
    private MessagesConfig messagesConfig;
    private CommandManager commandManager;
    private final Map<String, Long> lastSentTimes = new HashMap<>();

    @Inject
    public CoreManager(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.textProcessor = new TextProcessor(server);
        this.messagesConfig = new MessagesConfig(textProcessor);

        Component startingMessage = messagesConfig.getMessage("plugin.starting");
        if (startingMessage != null) {
            server.getConsoleCommandSource().sendMessage(startingMessage);
        }

        // Inicializamos StaffChatModule ANTES de CommandManager
        this.staffChatModule = new StaffChatModule(this, textProcessor);

        // Registrar el comando /coremanager (con alias /cm)
        CommandMeta commandMeta = server.getCommandManager()
                .metaBuilder("coremanager")
                .aliases("cm")
                .build();

        this.commandManager = new CommandManager(this);
        server.getCommandManager().register(commandMeta, commandManager);

        // Registrar el comando /sc
        CommandMeta staffChatMeta = server.getCommandManager()
                .metaBuilder("sc")
                .build();

        server.getCommandManager().register(staffChatMeta, new StaffChatCommand(commandManager));

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

        if (staffChatModule != null) {
            staffChatModule.shutdown();
        }

        Component stoppedMessage = messagesConfig.getMessage("plugin.stopped");
        if (stoppedMessage != null) {
            server.getConsoleCommandSource().sendMessage(stoppedMessage);
        }
    }

    public void reload() {
        if (messagesConfig != null) {
            messagesConfig.reload();
        }

        if (announcementsModule != null) {
            announcementsModule.stop();
        }

        if (broadcastModule != null) {
            broadcastModule.reload();
        }

        if (staffChatModule != null) {
            staffChatModule.reload();
        }

        this.announcementsModule = new AnnouncementsModule(this, textProcessor);
        this.announcementsModule.start();

        this.broadcastModule = new BroadcastModule(this, textProcessor);

        this.staffChatModule = new StaffChatModule(this, textProcessor);
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

    public StaffChatModule getStaffChatModule() {
        return staffChatModule;
    }

    public Map<String, Long> getLastSentTimes() {
        return lastSentTimes;
    }
}