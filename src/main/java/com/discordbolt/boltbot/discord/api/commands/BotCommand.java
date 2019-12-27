package com.discordbolt.boltbot.discord.api.commands;

import discord4j.core.object.util.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotCommand {

    /**
     * The commands and optional command arguments that map to a specific action
     */
    String[] command();

    /**
     * The description of what the command does
     */
    String description();

    /**
     * The description of the syntax of the command
     */
    String usage();

    /**
     * The module the command falls into This is used to sort commands in !Help
     */
    String module();

    /**
     * A list of alternative commands that take place of the command at index 0
     */
    String[] aliases() default {};

    /**
     * A list of channel IDs that this command is allowed to execute in
     */
    long[] channelWhitelist() default {};

    /**
     * A list of channel names that this command is allowed to execute in
     */
    String[] channelNameWhitelist() default {};

    /**
     * A list of channel IDs that this command is not allowed to execute in
     */
    long[] channelBlacklist() default {};

    /**
     * A list of channel names that this command is not allowed to execute in
     */
    String[] channelNameBlacklist() default {};

    /**
     * Required permissions a user executing a command must have
     */
    Permission[] permissions() default {};

    /**
     * The required number (or range) or args. This should be a single int or two ints
     */
    int[] args() default {};

    /**
     * Should this command be hidden from the !Help command?
     */
    boolean secret() default false;

    /**
     * Allow executing this command in direct messages
     */
    boolean allowDM() default false;

    /**
     * Delete the message that invoked this command. Note: Sent messages during executing of this
     * command will NOT be deleted.
     */
    boolean deleteCommandMessage() default false;
}
