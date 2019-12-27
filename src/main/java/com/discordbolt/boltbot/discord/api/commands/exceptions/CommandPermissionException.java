package com.discordbolt.boltbot.discord.api.commands.exceptions;

public class CommandPermissionException extends CommandException {

    public CommandPermissionException() {
        super(ExceptionMessage.PERMISSION_DENIED);
    }

    public CommandPermissionException(String message) {
        super(message);
    }
}
