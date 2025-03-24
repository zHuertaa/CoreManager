package com.zhuerta.coremanager.commands; // Paquete corregido

import com.velocitypowered.api.command.SimpleCommand;

public class StaffChatCommand implements SimpleCommand {

    private final CommandManager commandManager;

    public StaffChatCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(Invocation invocation) {
        // Delegamos la ejecución al método executeStaffChat de CommandManager
        commandManager.executeStaffChat(invocation.source(), invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Requerimos el mismo permiso que el subcomando staffchat
        return invocation.source().hasPermission("coremanager.staffchat");
    }
}