package com.discordbolt.boltbot.discord.api.commands.exceptions;

public class CommandException extends Exception {

    public CommandException() {
        super(ExceptionMessage.COMMAND_PROCESS_EXCEPTION);
    }

    public CommandException(String message) {
        super(message);
    }
}
