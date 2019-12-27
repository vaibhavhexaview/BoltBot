package com.discordbolt.boltbot.discord.api.commands;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CommandContext {

    private Message message;
    private List<String> arguments;
    private CustomCommand customCommand;

    CommandContext(Message message, CustomCommand customCommand) {
        this.message = message;
        getGuild().subscribe(guild -> this.arguments = Arrays.asList(getMessageContent().substring(customCommand.getCommandManager().getCommandPrefix(guild).length()).split(" ")));
        this.customCommand = customCommand;
    }

    public CustomCommand getCustomCommand() {
        return customCommand;
    }

    public Message getMessage() {
        return message;
    }

    public Optional<User> getUser() {
        return message.getAuthor();
    }

    public Mono<Member> getMember() {
        return getMessage().getAuthorAsMember();
    }

    public Mono<Guild> getGuild() {
        return getMessage().getGuild();
    }

    public Mono<MessageChannel> getChannel() {
        return message.getChannel();
    }

    public Mono<Boolean> isDirectMessage() {
        return getChannel().ofType(PrivateChannel.class).hasElement();
    }

    public String getMessageContent() {
        // The message has to have content otherwise the command wouldn't have been fired.
        return message.getContent().get();
    }

    public List<String> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public int getArgCount() {
        return getArguments().size();
    }

    public String combineArgs(int lowIndex, int highIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(getArguments().get(lowIndex));
        for (int i = lowIndex + 1; i <= highIndex; i++) {
            sb.append(' ').append(getArguments().get(i));
        }

        return sb.toString();
    }

    public DiscordClient getClient() {
        return message.getClient();
    }

    public Optional<Snowflake> getUserId() {
        return message.getAuthor().map(User::getId);
    }

    public Snowflake getChannelId() {
        return message.getChannelId();
    }

    public Snowflake getMessageId() {
        return message.getId();
    }

    /**
     * Reply to the command with a given message Note: Make sure to subscribe to the result or no
     * message will be sent.
     *
     * @param message Message to send
     */
    public Mono<Message> replyWith(String message) {
        return getChannel().flatMap(channel -> channel.createMessage(message));
    }

    /**
     * Reply to the command with a given embed Note: Make sure to subscribe to the result or no
     * embed will be sent.
     *
     * @param embed Embed to send
     */
    public Mono<Message> replyWith(Consumer<EmbedCreateSpec> embed) {
        return getChannel().flatMap(channel -> channel.createEmbed(embed));
    }

    /**
     * Reply to the command with a given message and embed Note: Make sure to subscribe to the
     * result or no message will be sent.
     *
     * @param message Message to send
     * @param embed Embed to send
     */
    public Mono<Message> replyWith(String message, Consumer<EmbedCreateSpec> embed) {
        return getChannel().flatMap(channel -> channel.createMessage(spec -> spec.setContent(message).setEmbed(embed)));
    }

    /**
     * Reply to the command with the usage Note: Make sure to subscribe to the result or no message
     * will be sent.
     */
    public Mono<Message> sendUsage() {
        return getGuild().map(guild -> customCommand.getCommandManager().getCommandPrefix(guild)).map(prefix -> prefix + customCommand.getUsage()).flatMap(this::replyWith);
    }
}
