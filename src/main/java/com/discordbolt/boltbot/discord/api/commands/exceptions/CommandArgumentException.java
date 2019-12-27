package com.discordbolt.boltbot.discord.api.commands.exceptions;

public class CommandArgumentException extends CommandException {

    public CommandArgumentException() {
        super(ExceptionMessage.INCORRECT_USAGE);
    }

    public CommandArgumentException(String message) {
        super(message);
    }
}
