package com.discordbolt.boltbot.discord.api.commands.exceptions;

public class CommandBotPermissionException extends CommandException {

    public CommandBotPermissionException() {
        super(ExceptionMessage.BOT_PERMISSION_DENIED);
    }

    public CommandBotPermissionException(String message) {
        super(message);
    }
}
