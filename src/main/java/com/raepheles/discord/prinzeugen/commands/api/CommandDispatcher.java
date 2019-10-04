package com.raepheles.discord.prinzeugen.commands.api;

import com.raepheles.discord.prinzeugen.Bot;
import com.raepheles.discord.prinzeugen.Messages;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CommandDispatcher {
    private final String MISSING_PERMS_TEXT = "You are missing the following permissions: ";
    private final CommandManager manager;

    public CommandDispatcher(CommandManager manager) {
        this.manager = manager;
    }

    public Mono<Void> onMessageEvent(MessageCreateEvent event) {
        return event.getGuild() // Get guild
                .map(manager::getPrefix) // Map to prefix of that guild
                .flatMap(prefix -> getCommandMessage(event, prefix)) // map to command message (without prefix)
                .flatMap(this::getCommand) // map to Command object
                .filterWhen(command -> checkPermissions(event, command)) // Check if author has the permissions for command
                .filterWhen(command -> command.preExecute(event))
                .flatMap(command -> command.execute(event)
                    .then(logMessage(event, command))
                    .onErrorResume(t -> logMessage(event, command, t.getMessage()))); // If exception is caught print error message); // execute the command
    }

    private Mono<Void> logMessage(MessageCreateEvent event, Command command) {
        return logMessage(event, command, null);
    }

    private Mono<Void> logMessage(MessageCreateEvent event, Command command, String error) {
        return event.getClient().getChannelById(Bot.LOG_CHANNEL_ID)
            .map(channel -> (MessageChannel)channel)
            .flatMap(messageChannel -> event.getMessage().getChannel()
                .flatMap(msgChannel -> {
                    String userName = "NULL";
                    String userId = "NULL";
                    if(event.getMessage().getAuthor().isPresent()) {
                        userName = event.getMessage().getAuthor().get().getUsername();
                        userId = event.getMessage().getAuthor().get().getId().asString();
                    }
                    final String finalUserName = userName;
                    final String finalUserId = userId;
                    final String finalCommandText = String.join(" ", command.getKeyword(), command.getParametersAsString()).trim();
                    GuildChannel gChannel = (GuildChannel)msgChannel;
                    if(error == null) {
                        return event.getGuild()
                            .flatMap(guild -> messageChannel.createMessage(Messages.getLogMessage(finalCommandText, finalUserName,
                                finalUserId,
                                guild.getName(),
                                guild.getId().asString(),
                                gChannel.getName())));
                    } else {
                        return event.getGuild()
                            .flatMap(guild -> messageChannel.createMessage(Messages.getLogMessage(finalCommandText, finalUserName,
                                finalUserId,
                                guild.getName(),
                                guild.getId().asString(),
                                gChannel.getName(),
                                error)));
                    }
                }))
            .then();
    }

    private Mono<String> getCommandMessage(MessageCreateEvent event, String prefix) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .filter(content -> content.startsWith(prefix))
                .map(content -> content.substring(prefix.length()));
    }

    private Mono<Command> getCommand(String commandMessage) {
        int i = commandMessage.indexOf(" ");
        if(i != -1) {
            commandMessage = commandMessage.substring(0, i);
        }
        String cMsg = commandMessage;
        return Flux.fromIterable(manager.getCommands())
                .filterWhen(command -> Flux.fromIterable(command.getAliases())
                    .map(cMsg::equalsIgnoreCase)
                    .filter(isAlias -> isAlias)
                    .defaultIfEmpty(command.getKeyword().equalsIgnoreCase(cMsg)))
                .next();
    }

    private Mono<Boolean> checkPermissions(MessageCreateEvent event, Command command) {
        return event.getMessage().getChannel()
                .ofType(GuildMessageChannel.class)
                .flatMap(channel -> Mono.justOrEmpty(event.getMember())
                    .flatMap(Member::getBasePermissions)
                    .flatMap(permissions -> checkPermissions(permissions, command, channel))
                    .hasElement())
                .map(elements -> !elements);
    }

    private Mono<Message> checkPermissions(PermissionSet permissions, Command command, MessageChannel channel) {
        return Flux.fromIterable(command.getRequiredPerms())
                .filter(perm -> !permissions.contains(perm))
                .collectList()
                .filter(perms -> !perms.isEmpty())
                .flatMap(perms -> channel.createMessage(String.format(MISSING_PERMS_TEXT + "%s", perms)));
    }



            /*String prefix = manager.getPrefix(event.getGuild().block());
        if(!event.getMessage().getContent().isPresent()) {
            return;
        } else {
            if(!event.getMessage().getContent().get().startsWith(prefix)) {
                return;
            }
        }

        // Webhooks, discord announcements, user join messages don't have Member
        Member author;
        if(event.getMember().isPresent()) {
            author = event.getMember().get();
        } else {
            return;
        }

        String commandMsg = event.getMessage()
                .getContent()
                .get()
                .substring(prefix.length());
        Command command = null;

        for(Command cmd: manager.getCommands()) {
            if(cmd.getKeyword().equalsIgnoreCase(commandMsg)) {
                command = cmd;
            }
            for(String alias: cmd.getAliases()) {
                if(commandMsg.equalsIgnoreCase(alias)) {
                    command = cmd;
                    break;
                }
            }
            if(command != null) break;
        }

        if(command == null) return;

        if(event.getMessage().getChannel().block().getType() == Channel.Type.DM) {
            if(!command.isAllowDm()) {
                return;
            }
        } else {
            boolean hasPerms = true;
            List<Permission> missingPerms = new ArrayList<>();
            for(Permission perm: command.getRequiredPerms()) {
                if(!author.getBasePermissions().block().contains(perm)) {
                    missingPerms.add(perm);
                    hasPerms = false;
                }
            }
            if(!hasPerms) {
                event.getMessage().getChannel().flatMap(c -> c.createMessage(String.format(MISSING_PERMS_TEXT + "%s", missingPerms))).subscribe();
                return;
            }
        }
        command.execute(event);*/
}
