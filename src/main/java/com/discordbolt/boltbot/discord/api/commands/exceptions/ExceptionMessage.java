package com.discordbolt.boltbot.discord.api.commands.exceptions;

public interface ExceptionMessage {

    /**
     * The default message to send when the discord rate limit is hit.
     */
    String API_LIMIT = "Sending Discord too many requests. Rate limit hit.";

    /**
     * The default message to respond with when the user does not have permission for a command
     */
    String PERMISSION_DENIED = "You do not have permission for this command!";

    /**
     * The default message to respond with when the bot does not have permission to do something
     */
    String BOT_PERMISSION_DENIED = "I do not have permission to perform this action!";

    /**
     * The default message to respond with when an uncaught exception is thrown during execution of a command
     */
    String COMMAND_PROCESS_EXCEPTION = "An error has occurred while processing your command. Please try again later.";

    /**
     * The default message to respond with when a CommandStateException is thrown
     */
    String BAD_STATE = "I'm sorry Dave, I'm afraid I can't do that";

    /**
     * The default message to respond with when a message must be executed in a guild
     */
    String EXECUTE_IN_GUILD = "You must execute this command in a guild.";

    /**
     * The default message to respond with when a user provided command does not match the required argument count of the command
     */
    String TOO_FEW_ARGUMENTS = "Your command had too few arguments.";


    /**
     * The default message to respond with when a user provided command does not match the required argument count of the command
     */
    String TOO_MANY_ARGUMENTS = "Your command had too many arguments.";

    /**
     * The default message to respond with when a command is executed in a non-allowed channel
     */
    String INVALID_CHANNEL = "This command can not be executed in this channel!";

    /**
     * The default message to respond with when a user command does not match any expected commands
     */
    String INCORRECT_USAGE = "Your command did not match expected input. Please check !Help for usage.";
}