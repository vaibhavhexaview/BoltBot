package com.discordbolt.boltbot.discord.api.commands;

import com.discordbolt.boltbot.discord.api.commands.exceptions.ExceptionMessage;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

class ValidityCheck {

    private static final Mono<CheckResult> VALID_CHECK = Mono.just(CheckResult.VALID);

    enum CheckResult {
        VALID(""),
        DM_NOT_ALLOWED(ExceptionMessage.EXECUTE_IN_GUILD),
        CHANNEL_ON_BLACKLIST(ExceptionMessage.INVALID_CHANNEL),
        CHANNEL_NOT_ON_WHITELIST(ExceptionMessage.INVALID_CHANNEL),
        TOO_FEW_ARGUMENTS(ExceptionMessage.TOO_FEW_ARGUMENTS),
        TOO_MANY_ARGUMENTS(ExceptionMessage.TOO_MANY_ARGUMENTS),
        INVALID_PERMISSION(ExceptionMessage.PERMISSION_DENIED);

        private final String message;

        CheckResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static Mono<CheckResult> channelDM(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.isDirectMessage().map(isDM -> command.allowDM() || !isDM)).switchIfEmpty(Mono.just(CheckResult.DM_NOT_ALLOWED));
    }

    static Mono<CheckResult> channelBlacklist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel()
                .map(MessageChannel::getId)
                .map(Snowflake::asLong)
                .map(channelID -> !command.getChannelBlacklist().contains(channelID)))
                .switchIfEmpty(Mono.just(CheckResult.CHANNEL_ON_BLACKLIST));
    }

    static Mono<CheckResult> channelNameBlacklist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel()
                .ofType(TextChannel.class)
                .map(TextChannel::getName)
                .map(channelName -> !command.getChannelNameBlacklist().contains(channelName)))
                .switchIfEmpty(Mono.just(CheckResult.CHANNEL_ON_BLACKLIST));
    }

    static Mono<CheckResult> channelWhitelist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel()
                .map(MessageChannel::getId)
                .map(Snowflake::asLong)
                .map(channelID -> command.getChannelWhitelist().isEmpty() || command
                        .getChannelWhitelist()
                        .contains(channelID)))
                .switchIfEmpty(Mono.just(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    static Mono<CheckResult> channelNameWhitelist(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filterWhen(r -> commandContext.getChannel()
                .ofType(TextChannel.class)
                .map(TextChannel::getName)
                .map(channelName -> command.getChannelNameWhitelist()
                        .isEmpty() || command.getChannelNameWhitelist()
                        .contains(channelName)))
                .switchIfEmpty(Mono.just(CheckResult.CHANNEL_NOT_ON_WHITELIST));
    }

    static Mono<CheckResult> argumentLowerBound(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filter(r -> commandContext.getArgCount() >= command.getMinArgCount())
                .switchIfEmpty(Mono.just(CheckResult.TOO_FEW_ARGUMENTS));
    }

    static Mono<CheckResult> argumentUpperBound(CustomCommand command, CommandContext commandContext) {
        return VALID_CHECK.filter(r -> commandContext.getArgCount() <= command.getMaxArgCount())
                .switchIfEmpty(Mono.just(CheckResult.TOO_MANY_ARGUMENTS));
    }

    static Mono<CheckResult> permission(CustomCommand command, CommandContext commandContext) {
        if (command.getPermissions().isEmpty()) {
            return VALID_CHECK;
        }

        Mono<CheckResult> directMessage = VALID_CHECK
                .filterWhen(r -> commandContext.isDirectMessage().map(b -> !b))
                .switchIfEmpty(Mono.just(CheckResult.DM_NOT_ALLOWED));

        Mono<CheckResult> permissionCheck = VALID_CHECK.filterWhen(r -> commandContext.getMember()
                .flatMapMany(Member::getRoles)
                .map(Role::getPermissions)
                .reduce(PermissionSet::or)
                .map(permissionSet -> permissionSet.containsAll(command.getPermissions())))
                .switchIfEmpty(Mono.just(CheckResult.INVALID_PERMISSION));

        return directMessage.filter(checkResult -> checkResult != CheckResult.VALID).switchIfEmpty(permissionCheck);
    }
}
