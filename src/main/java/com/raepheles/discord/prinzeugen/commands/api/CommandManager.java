package com.raepheles.discord.prinzeugen.commands.api;

import com.raepheles.discord.prinzeugen.Messages;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import org.reflections.Reflections;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {
    private List<Command> commands;
    private String defaultPrefix;
    private Map<Guild, String> guildPrefixes;

    public CommandManager(String packageName, String defaultPrefix) throws CommandParsingException {
        Reflections r = new Reflections(packageName);
        List<String> commandKeywords = new ArrayList<>();
        commands = r.getSubTypesOf(Command.class).stream().filter(c -> !c.isAnonymousClass()).map(c -> {
            try {
                Command cmd = c.getDeclaredConstructor().newInstance();
                commandKeywords.add(cmd.getKeyword());
                return cmd;
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        Set<String> commandKeywordsSet = new HashSet<>(commandKeywords);
        if (commandKeywordsSet.size() < commandKeywords.size()) {
            throw new CommandParsingException("Command keywords must be unique." +
                " You cannot have two commands with the same keyword!");
        }

        commands.add(getHelpCommand());

        if (defaultPrefix.length() == 0) {
            throw new IllegalArgumentException("Default prefix cannot be empty.");
        } else if (defaultPrefix.contains(" ")) {
            throw new IllegalArgumentException("Default prefix cannot contain space.");
        }
        this.defaultPrefix = defaultPrefix;
        guildPrefixes = new HashMap<>();
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public Map<Guild, String> getGuildPrefixes() {
        return guildPrefixes;
    }

    public String getPrefix(Guild guild) {
        return guildPrefixes.getOrDefault(guild, defaultPrefix);
    }

    private Command getHelpCommand() {
        return new Command("help", "Bot", true) {

            @Override
            public Mono<Void> execute(MessageCreateEvent event) {
                if (commands.isEmpty()) return Mono.empty();
                return this.preExecute(event)
                    .filter(parameter -> this.getParametersAsString().isEmpty())
                    .flatMap(hasParameter -> event.getClient().getSelf()
                        .map(User::getAvatarUrl)
                        .flatMap(avatarUrl -> event.getMessage().getGuild()
                            .flatMap(guild -> event.getMessage().getChannel()
                                .flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> {
                                    Map<String, StringJoiner> moduleCommandMap = new HashMap<>();
                                    for (Command command : commands) {
                                        StringJoiner sj = moduleCommandMap.getOrDefault(command.getModule(), new StringJoiner(", "));
                                        sj.add(command.getKeyword());
                                        moduleCommandMap.put(command.getModule(), sj);
                                    }
                                    for (String key : moduleCommandMap.keySet()) {
                                        embedCreateSpec.addField(key, moduleCommandMap.get(key).toString(), false);
                                    }
                                    embedCreateSpec.setColor(Color.red);
                                    embedCreateSpec.setThumbnail(avatarUrl);
                                    embedCreateSpec.setDescription(Messages.getHelpMessage(guildPrefixes.getOrDefault(guild, defaultPrefix)));
                                    embedCreateSpec.setTitle("Prinz Eugen");
                                })))))
                    .switchIfEmpty(Flux.fromIterable(commands)
                        .filter(c -> c.getKeyword().equalsIgnoreCase(this.getParametersAsString()) || c.getAliases().contains(this.getParametersAsString().toLowerCase()))
                        .next()
                        .flatMap(command -> Mono.just(command)
                            .flatMap(channel -> event.getMessage().getChannel())
                            .flatMap(channel -> channel.createEmbed(embedCreateSpec -> {
                                embedCreateSpec.setTitle("Command: " + command.getKeyword());
                                embedCreateSpec.setColor(Color.red);
                                embedCreateSpec.addField("Description", command.getDescription(), false);
                                embedCreateSpec.addField("Usage", command.getUsage(), false);
                                if (!command.getAliases().isEmpty()) embedCreateSpec.addField("Aliases",
                                    String.join(", ", command.getAliases()),
                                    false);
                                if (!command.getAcceptedFlags().isEmpty()) embedCreateSpec.addField("Flags",
                                    command.getAcceptedFlags().stream().map(Flag::getFlagName).collect(Collectors.joining(", ")),
                                    false);
                                if (!command.getRequiredPerms().isEmpty())
                                    embedCreateSpec.addField("Required Permissions",
                                        command.getRequiredPerms().stream().map(Permission::name).collect(Collectors.joining(", ")),
                                        false);
                            }))))
                    .doOnError(e -> e.printStackTrace())
                    .then();
            }
        };
    }

    //String.format("%s\nUsage: %s", command.getDescription(), command.getUsage()))

/*                                    .flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> {
        Map<String, StringJoiner> moduleCommandMap = new HashMap<>();
        for(Command command: commands) {
            StringJoiner sj = moduleCommandMap.getOrDefault(command.getModule(), new StringJoiner(", "));
            sj.add(command.getKeyword());
            moduleCommandMap.put(command.getModule(), sj);
        }
        for(String key: moduleCommandMap.keySet()) {
            embedCreateSpec.addField(key, moduleCommandMap.get(key).toString(), false);
        }
        embedCreateSpec.setColor(Color.red);
        event.getClient().getSelf()
                .map(user -> embedCreateSpec.setThumbnail(user.getAvatarUrl()));
    })))*/
}
