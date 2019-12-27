package com.discordbolt.boltbot.discord.api.commands;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private static String DEFAULT_PREFIX = "!";

    private DiscordClient client;
    private List<CustomCommand> commands = new ArrayList<>();
    private Map<Long, String> commandPrefixes = new HashMap<>();
    private CustomCommand helpCommand;

    /**
     * Initialize Command API
     *
     * @param client DiscordClient
     * @param packagePrefix package string where commands are located
     */
    public CommandManager(DiscordClient client, String packagePrefix) {
        LOGGER.info("Initializing Command API ");

        // Save DiscordClient
        this.client = client;

        // Set the command manager
        CustomCommand.setCommandManager(this);

        // Get all public static methods with @BotCommand and create CustomCommand objects
        commands.addAll(new Reflections(packagePrefix, new MethodAnnotationsScanner())
                .getMethodsAnnotatedWith(BotCommand.class)
                .stream()
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(method -> new CustomCommand(method.getAnnotation(BotCommand.class)) {
                    @Override
                    public void execute(CommandContext commandContext) {
                        try {
                            method.invoke(null, commandContext);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            LOGGER.error("Unable to execute command \"" + String.join(" ", commandContext.getCustomCommand().getCommands()) + "\"", e);
                        }
                    }
                })
                .collect(Collectors.toList()));

        // Register our help command
        helpCommand = new HelpCommand(this);
        registerCommand(helpCommand);

        LOGGER.info("Loaded {} commands.", commands.size());

        // Register our command listener
        CommandListener commandListener = new CommandListener(this, client);
    }

    public void registerCommand(CustomCommand command) {
        commands.add(command);
        sortCommands();
    }

    /**
     * Set the command prefix of a specified guild
     *
     * @param guild Guild to change the prefix for
     * @param commandPrefix new prefix string all commands must be prefaced with
     */
    public void setCommandPrefix(Guild guild, String commandPrefix) {
        setCommandPrefix(guild.getId().asLong(), commandPrefix);
    }

    public void setCommandPrefix(long guildID, String commandPrefix) {
        commandPrefixes.put(guildID, commandPrefix);
    }

    public void disableHelpCommand() {
        unregisterCommand(helpCommand);
    }

    public void unregisterCommand(CustomCommand command) {
        commands.remove(command);
    }

    public void onCommandExecution(Consumer<CommandContext> consumer) {
        CustomCommand.setCommandConsumer(consumer);
    }

    /**
     * Get the Discord4J client
     *
     * @return IDiscordClient
     */
    DiscordClient getClient() {
        return client;
    }

    /**
     * Get a list of all commands currently registered
     *
     * @return UnmodifiableList of CustomCommands
     */
    List<CustomCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Get the command prefix of a given guild
     *
     * @return char command prefix for given guild
     */
    String getCommandPrefix(Guild guild) {
        if (guild == null) {
            return DEFAULT_PREFIX;
        }
        return getCommandPrefix(guild.getId().asLong());
    }

    String getCommandPrefix(long guildID) {
        return commandPrefixes.getOrDefault(guildID, DEFAULT_PREFIX);
    }

    private void sortCommands() {
        commands.sort(Comparator.comparing(c -> (c.getModule() + " " + String.join(" ", c.getCommands()))));
    }
}
