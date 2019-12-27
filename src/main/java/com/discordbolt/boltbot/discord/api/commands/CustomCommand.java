package com.discordbolt.boltbot.discord.api.commands;

import com.discordbolt.boltbot.discord.api.commands.exceptions.CommandException;
import com.discordbolt.boltbot.discord.api.commands.exceptions.CommandRuntimeException;
import com.sun.istack.internal.NotNull;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.discordbolt.boltbot.discord.api.commands.ValidityCheck.*;

public abstract class CustomCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCommand.class);

    private static Consumer<CommandContext> commandConsumer;
    private static CommandManager manager;

    private List<String> command;

    private String description = "";
    private String usage = "";
    private String module = "";

    private Set<String> aliases = new HashSet<>();
    private Set<Long> channelWhitelist = new HashSet<>();
    private Set<Long> channelBlacklist = new HashSet<>();
    private Set<String> channelNameWhitelist = new HashSet<>();
    private Set<String> channelNameBlacklist = new HashSet<>();
    private PermissionSet permissions = PermissionSet.none();
    private int[] argRange = new int[]{0, Integer.MAX_VALUE};
    private boolean secret, allowDM, deleteTrigger;

    CustomCommand(BotCommand a) {
        this(a.command(), a.description(), a.usage(), a.module());
        setAliases(a.aliases());
        setChannelWhitelist(a.channelWhitelist());
        setChannelBlacklist(a.channelBlacklist());
        setChannelNameWhitelist(a.channelNameWhitelist());
        setChannelNameBlacklist(a.channelNameBlacklist());
        setPermissions(a.permissions());
        if (a.args().length == 1) {
            setArgumentCount(a.args()[0]);
        } else if (a.args().length == 2) {
            setMinArgumentCount(a.args()[0]);
            setMaxArgumentCount(a.args()[1]);
        }
        setSecret(a.secret());
        setAllowDM(a.allowDM());
        setDeleteCommandMessage(a.deleteCommandMessage());
    }

    public CustomCommand(@NotNull String[] command, @NotNull String description, @NotNull String usage, @NotNull String module) {
        this(command);
        setDescription(description);
        setUsage(usage);
        setModule(module);
    }

    /**
     * Constructor for creating a new CustomCommand without details for !Help command
     */
    public CustomCommand(@NotNull String[] command) {
        this.command = Arrays.stream(command).map(String::toLowerCase).collect(Collectors.toList());
    }

    static void setCommandConsumer(Consumer<CommandContext> consumer) {
        CustomCommand.commandConsumer = consumer;
    }

    static void setCommandManager(CommandManager manager) {
        CustomCommand.manager = manager;
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(command);
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getModule() {
        return module;
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Set<Long> getChannelWhitelist() {
        return Collections.unmodifiableSet(channelWhitelist);
    }

    public Set<Long> getChannelBlacklist() {
        return Collections.unmodifiableSet(channelBlacklist);
    }

    public Set<String> getChannelNameWhitelist() {
        return Collections.unmodifiableSet(channelNameWhitelist);
    }

    public Set<String> getChannelNameBlacklist() {
        return Collections.unmodifiableSet(channelNameBlacklist);
    }

    public PermissionSet getPermissions() {
        return permissions;
    }

    public int getMinArgCount() {
        return argRange[0];
    }

    public int getMaxArgCount() {
        return argRange[1];
    }

    public boolean isSecret() {
        return secret;
    }

    public boolean allowDM() {
        return allowDM;
    }

    public boolean shouldDeleteTrigger() {
        return deleteTrigger;
    }

    public CustomCommand setDescription(String description) {
        this.description = description;
        return this;
    }

    public CustomCommand setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CustomCommand setModule(String module) {
        this.module = module;
        return this;
    }

    public CustomCommand setAliases(String... aliases) {
        this.aliases = Arrays.stream(aliases).map(String::toLowerCase).collect(Collectors.toSet());
        return this;
    }

    public CustomCommand setChannelWhitelist(long... channelWhitelist) {
        this.channelWhitelist = LongStream.of(channelWhitelist).boxed().collect(Collectors.toSet());
        return this;
    }

    public CustomCommand setChannelBlacklist(long... channelBlacklist) {
        this.channelBlacklist = LongStream.of(channelBlacklist).boxed().collect(Collectors.toSet());
        return this;
    }

    public CustomCommand setChannelNameWhitelist(String... channelNameWhitelist) {
        Collections.addAll(this.channelNameWhitelist, channelNameWhitelist);
        return this;
    }

    public CustomCommand setChannelNameBlacklist(String... channelNameBlacklist) {
        Collections.addAll(this.channelNameBlacklist, channelNameBlacklist);
        return this;
    }

    public CustomCommand setPermissions(Permission... permissions) {
        this.permissions = PermissionSet.of(permissions);
        return this;
    }

    public CustomCommand setArgumentCount(int argumentCount) {
        this.argRange[0] = argumentCount;
        this.argRange[1] = argumentCount;
        return this;
    }

    public CustomCommand setMinArgumentCount(int minArgumentCount) {
        this.argRange[0] = minArgumentCount;
        return this;
    }

    public CustomCommand setMaxArgumentCount(int maxArgumentCount) {
        this.argRange[1] = maxArgumentCount;
        return this;
    }

    public CustomCommand setSecret(boolean secret) {
        this.secret = secret;
        return this;
    }

    public CustomCommand setAllowDM(boolean allowDM) {
        this.allowDM = allowDM;
        return this;
    }

    public CustomCommand setDeleteCommandMessage(boolean deleteCommandMessage) {
        this.deleteTrigger = deleteCommandMessage;
        return this;
    }

    private Flux<ValidityCheck.CheckResult> allPreChecks(CommandContext commandContext) {
        return Flux.concat(isAllowedChannel(commandContext), isValidArgumentCount(commandContext), hasPermission(commandContext));
    }

    private Flux<ValidityCheck.CheckResult> isAllowedChannel(CommandContext commandContext) {
        return Flux.concat(channelDM(this, commandContext), channelBlacklist(this, commandContext),
                channelNameBlacklist(this, commandContext), channelWhitelist(this, commandContext),
                channelNameWhitelist(this, commandContext));
    }

    private Flux<ValidityCheck.CheckResult> isValidArgumentCount(CommandContext commandContext) {
        return Flux.concat(argumentLowerBound(this, commandContext), argumentUpperBound(this, commandContext));
    }

    private Flux<ValidityCheck.CheckResult> hasPermission(CommandContext commandContext) {
        return Flux.concat(permission(this, commandContext));
    }

    CommandManager getCommandManager() {
        return manager;
    }

    void preexec(Message message) {
        CommandContext cc = new CommandContext(message, this);

        allPreChecks(cc)
                .filter(checkResult -> checkResult != CheckResult.VALID)
                .next()
                .switchIfEmpty(Mono.just(CheckResult.VALID))
                .flatMap(checkResult -> {
                    if (checkResult == CheckResult.VALID) {
                        try {
                            this.execute(cc);
                        } catch (CommandException | CommandRuntimeException e) {
                            return cc.replyWith(e.getMessage());
                        }
                        return Mono.empty();
                    } else {
                        return cc.replyWith(checkResult.getMessage());
                    }
                })
                .doFinally(signal -> {
                    if (shouldDeleteTrigger()) {
                        message.delete().subscribe();
                    }
                    if (commandConsumer != null) {
                        commandConsumer.accept(cc);
                    }
                })
                .subscribe();
    }

    public abstract void execute(CommandContext commandContext) throws CommandException;
}
