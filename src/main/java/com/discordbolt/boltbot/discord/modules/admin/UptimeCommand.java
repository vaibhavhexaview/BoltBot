package com.discordbolt.boltbot.discord.modules.admin;

import com.discordbolt.boltbot.discord.api.commands.CommandContext;
import com.discordbolt.boltbot.discord.api.commands.CustomCommand;
import com.discordbolt.boltbot.discord.api.commands.exceptions.CommandException;

import java.time.Duration;
import java.time.Instant;

public class UptimeCommand extends CustomCommand {

    private static final String[] command = {"uptime"};
    private static final String description = "Get bot uptime";
    private static final String usage = "Uptime";
    private static final String module = "Admin";

    private Instant startTime;

    public UptimeCommand() {
        super(command, description, usage, module);
        super.setAllowDM(true);
        this.startTime = Instant.now();
    }


    @Override
    public void execute(CommandContext commandContext) throws CommandException {
        Instant now = Instant.now();
        Duration difference = Duration.between(startTime, now);
        String fancyTime = difference.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
        commandContext.replyWith(fancyTime).subscribe();
    }
}
