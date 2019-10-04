package com.raepheles.discord.prinzeugen.commands.api;

import com.raepheles.discord.prinzeugen.Messages;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import org.reflections.Reflections;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {
    private List<Command> commands;
    private String defaultPrefix;
    private Map<Guild, String> guildPrefixes;

    public CommandManager(String packageName, String defaultPrefix) {
        Reflections r = new Reflections(packageName);
        commands = r.getSubTypesOf(Command.class).stream().filter(c -> !c.isAnonymousClass()).map(c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

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
                        .filter(c -> c.getKeyword().equalsIgnoreCase(this.getParametersAsString()))
                        .next()
                        .flatMap(command -> Mono.just(command)
                            .flatMap(channel -> event.getMessage().getChannel())
                            .flatMap(channel -> channel.createMessage(command.getHelpMessage()))))
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
