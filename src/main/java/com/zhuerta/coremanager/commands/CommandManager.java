package com.zhuerta.coremanager.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.zhuerta.coremanager.CoreManager;
import com.zhuerta.coremanager.announcements.config.AnnouncementsConfig;
import com.zhuerta.coremanager.config.MessagesConfig;
import com.zhuerta.coremanager.staffchat.DiscordWebhookSender;
import com.zhuerta.coremanager.utils.TextProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CommandManager implements SimpleCommand {

    private final CoreManager plugin;
    private final MessagesConfig messagesConfig;
    private final TextProcessor textProcessor;
    private final MiniMessage miniMessage;
    private final Map<String, Subcommand> subcommands;
    private final DiscordWebhookSender webhookSender;

    public CommandManager(CoreManager plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
        this.textProcessor = plugin.getTextProcessor();
        this.miniMessage = MiniMessage.miniMessage();
        // Pasamos el formato del mensaje y el formato del nombre del webhook
        this.webhookSender = new DiscordWebhookSender(
                plugin.getServer(),
                plugin.getStaffChatModule().getConfig().getDiscordWebhookUrl(),
                plugin.getStaffChatModule().getConfig().getDiscordFormat(),
                plugin.getStaffChatModule().getConfig().getWebhookNameFormat()
        );

        this.subcommands = new HashMap<>();
        subcommands.put("reload", new Subcommand("coremanager.reload", this::executeReload));
        subcommands.put("list", new Subcommand("coremanager.list", this::executeList));
        subcommands.put("broadcast", new Subcommand("coremanager.broadcast", this::executeBroadcast));
        subcommands.put("staffchat", new Subcommand("coremanager.staffchat", this::executeStaffChat));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            Component usageMessage = messagesConfig.getMessage("commands.usage");
            if (usageMessage != null) {
                String messageText = miniMessage.serialize(usageMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        String subCommand = args[0].toLowerCase();
        Subcommand subcommand = subcommands.get(subCommand);
        if (subcommand == null) {
            Component unknownSubcommandMessage = messagesConfig.getMessage("commands.unknown-subcommand");
            if (unknownSubcommandMessage != null) {
                String messageText = miniMessage.serialize(unknownSubcommandMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        if (!source.hasPermission(subcommand.permission)) {
            Component noPermissionMessage = messagesConfig.getMessage("commands.no-permission");
            if (noPermissionMessage != null) {
                String messageText = miniMessage.serialize(noPermissionMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        subcommand.executor.accept(source, invocation);
    }

    private void executeReload(CommandSource source, Invocation invocation) {
        Component reloadingMessage = messagesConfig.getMessage("commands.reloading");
        if (reloadingMessage != null) {
            String messageText = miniMessage.serialize(reloadingMessage);
            source.sendMessage(textProcessor.processText(messageText));
        }

        try {
            plugin.reload();
            Component reloadedMessage = messagesConfig.getMessage("commands.reloaded");
            if (reloadedMessage != null) {
                String messageText = miniMessage.serialize(reloadedMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
        } catch (Exception e) {
            Component errorMessage = messagesConfig.getMessage("commands.reload-error", "%error%", e.getMessage());
            if (errorMessage != null) {
                String messageText = miniMessage.serialize(errorMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            e.printStackTrace();
        }
    }

    private void executeList(CommandSource source, Invocation invocation) {
        List<AnnouncementsConfig.Announcement> announcements = plugin.getAnnouncementsModule().getConfig().getAnnouncements();

        if (announcements.isEmpty()) {
            Component noAnnouncementsMessage = messagesConfig.getMessage("commands.list.no-announcements");
            if (noAnnouncementsMessage != null) {
                String messageText = miniMessage.serialize(noAnnouncementsMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        Component headerMessage = messagesConfig.getMessage("commands.list.header");
        if (headerMessage != null) {
            String messageText = miniMessage.serialize(headerMessage);
            source.sendMessage(textProcessor.processText(messageText));
        }

        for (int i = 0; i < announcements.size(); i++) {
            AnnouncementsConfig.Announcement announcement = announcements.get(i);

            Component entryMessage = messagesConfig.getMessage("commands.list.entry",
                    "%id%", announcement.getId(),
                    "%type%", announcement.getType());

            if (entryMessage != null) {
                Component hoverMessage = messagesConfig.getMessage("commands.list.entry-hover",
                        "%id%", announcement.getId(),
                        "%type%", announcement.getType(),
                        "%cooldown%", announcement.getCooldown(),
                        "%servers%", announcement.getServers().toString(),
                        "%days%", announcement.getDays().toString());

                Component clickMessage = messagesConfig.getMessage("commands.list.entry-click");
                String clickMessageText = miniMessage.serialize(clickMessage);
                hoverMessage = hoverMessage.append(Component.newline()).append(textProcessor.processText(clickMessageText));

                String entryMessageText = miniMessage.serialize(entryMessage);
                String hoverMessageText = miniMessage.serialize(hoverMessage);
                entryMessage = textProcessor.processText(entryMessageText)
                        .hoverEvent(HoverEvent.showText(textProcessor.processText(hoverMessageText)))
                        .clickEvent(ClickEvent.suggestCommand("/coremanager list " + announcement.getId()));

                source.sendMessage(entryMessage);
            }

            if (i < announcements.size() - 1) {
                Component separatorMessage = messagesConfig.getMessage("commands.list.separator");
                if (separatorMessage != null) {
                    String messageText = miniMessage.serialize(separatorMessage);
                    source.sendMessage(textProcessor.processText(messageText));
                }
            }
        }

        if (headerMessage != null) {
            String messageText = miniMessage.serialize(headerMessage);
            source.sendMessage(textProcessor.processText(messageText));
        }
    }

    private void executeBroadcast(CommandSource source, Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length < 4) {
            Component usageMessage = messagesConfig.getMessage("commands.broadcast.usage");
            if (usageMessage != null) {
                String messageText = miniMessage.serialize(usageMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        String serversStr = args[1];
        String typesStr = args[2];
        String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        List<String> serverNames = plugin.getBroadcastModule().getServerNames();
        List<String> specifiedServers = serversStr.equalsIgnoreCase("all") ?
                Collections.singletonList("all") :
                Arrays.asList(serversStr.split(",")).stream()
                        .map(String::trim)
                        .collect(Collectors.toList());

        if (!serversStr.equalsIgnoreCase("all")) {
            for (String server : specifiedServers) {
                if (!serverNames.contains(server)) {
                    Component invalidServersMessage = messagesConfig.getMessage("commands.broadcast.invalid-servers", "%servers%", server);
                    if (invalidServersMessage != null) {
                        String messageText = miniMessage.serialize(invalidServersMessage);
                        source.sendMessage(textProcessor.processText(messageText));
                    }
                    return;
                }
            }
        }

        List<String> validTypes = plugin.getBroadcastModule().getAnnouncementTypes();
        List<String> specifiedTypes = typesStr.equalsIgnoreCase("all") ?
                validTypes :
                Arrays.asList(typesStr.split(",")).stream()
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .collect(Collectors.toList());

        if (!typesStr.equalsIgnoreCase("all")) {
            for (String type : specifiedTypes) {
                if (!validTypes.contains(type)) {
                    Component invalidTypesMessage = messagesConfig.getMessage("commands.broadcast.invalid-types", "%types%", type);
                    if (invalidTypesMessage != null) {
                        String messageText = miniMessage.serialize(invalidTypesMessage);
                        source.sendMessage(textProcessor.processText(messageText));
                    }
                    return;
                }
            }
        }

        String typesForMessage = String.join(", ", specifiedTypes);

        boolean success = plugin.getBroadcastModule().sendBroadcast(serversStr, typesStr, message);
        if (success) {
            Component successMessage = messagesConfig.getMessage("commands.broadcast.success",
                    "%servers%", serversStr,
                    "%types%", typesForMessage);
            if (successMessage != null) {
                String messageText = miniMessage.serialize(successMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
        }
    }

    public void executeStaffChat(CommandSource source, Invocation invocation) {
        if (!(source instanceof Player)) {
            Component playerOnlyMessage = messagesConfig.getMessage("commands.player-only");
            if (playerOnlyMessage != null) {
                String messageText = miniMessage.serialize(playerOnlyMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        Player player = (Player) source;
        String[] args = invocation.arguments();

        if (args.length < 1) {
            Component usageMessage = messagesConfig.getMessage("commands.staffchat.usage");
            if (usageMessage != null) {
                String messageText = miniMessage.serialize(usageMessage);
                source.sendMessage(textProcessor.processText(messageText));
            }
            return;
        }

        String message = String.join(" ", args);
        String serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Unknown";
        String formattedMessage = plugin.getStaffChatModule().getConfig().getStaffChatFormat()
                .replace("%player_name%", player.getGameProfile().getName())
                .replace("%message%", message)
                .replace("%server%", serverName);
        Component chatMessage = textProcessor.processText(formattedMessage, player);

        for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
            if (onlinePlayer.hasPermission(plugin.getStaffChatModule().getConfig().getStaffChatPermission())) {
                onlinePlayer.sendMessage(chatMessage);
            }
        }

        // Envía a Discord usando el webhook
        webhookSender.sendMessage(player.getGameProfile().getName(), message, serverName);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            return subcommands.keySet().stream()
                    .filter(cmd -> args.length == 0 || cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("broadcast")) {
            if (args.length == 2) {
                List<String> suggestions = new ArrayList<>(plugin.getBroadcastModule().getServerNames());
                suggestions.add("all");
                return suggestions.stream()
                        .filter(s -> args[1].isEmpty() || s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                List<String> suggestions = new ArrayList<>(plugin.getBroadcastModule().getAnnouncementTypes());
                suggestions.add("all");
                return suggestions.stream()
                        .filter(s -> args[2].isEmpty() || s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    private static class Subcommand {
        private final String permission;
        private final BiConsumer<CommandSource, Invocation> executor;

        Subcommand(String permission, BiConsumer<CommandSource, Invocation> executor) {
            this.permission = permission;
            this.executor = executor;
        }
    }
}