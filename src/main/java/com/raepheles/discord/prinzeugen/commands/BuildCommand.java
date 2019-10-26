package com.raepheles.discord.prinzeugen.commands;

import com.raepheles.discord.prinzeugen.Bot;
import com.raepheles.discord.prinzeugen.Utilities;
import com.raepheles.discord.prinzeugen.commands.api.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.StringJoiner;

public class BuildCommand extends Command {

    public BuildCommand() {
        super("build", "Game Info", true);
        this.setUsage("[prefix]build [hh:mm:ss]");
        this.setDescription("Shows the ships you can construct in the given time.\n" +
            "Example: build 1:20:00\n" +
            "You can just use [hh:mm] format as well.\n" +
            "Example: build 1:20");
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        String parameter = this.getParametersAsString();
        String[] parts = parameter.split(":", 3);
        if(parts.length != 3 && parts.length != 2) {
            return event.getMessage().getChannel()
                .flatMap(messageChannel -> messageChannel.createMessage("Invalid time format."))
                .then();
        }

        int hours, minutes, seconds;
        String hoursString = parts[0];
        String minutesString = parts[1];
        String secondsString;
        if(parts.length == 3) {
            secondsString = parts[2];
        } else {
            secondsString = "0";
        }
        try {
            hours = Integer.parseInt(hoursString);
            minutes = Integer.parseInt(minutesString);
            seconds = Integer.parseInt(secondsString);
        } catch(NumberFormatException e) {
            return event.getMessage().getChannel()
                .flatMap(messageChannel -> messageChannel.createMessage("Error while parsing integers."))
                .then();
        }

        if(minutes >= 60 || seconds >= 60 || minutes < 0 || hours < 0 || seconds < 0) {
            return event.getMessage().getChannel()
                .flatMap(messageChannel -> messageChannel.createMessage("Build time is invalid."))
                .then();
        }

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return Flux.fromIterable(Bot.ships)
            .filter(ship -> ship.getConstructionTime().equalsIgnoreCase(timeString))
            .reduce(new StringJoiner("\n"), (joiner, ship) -> joiner.add(String.format("[`%s - %s`](%s)",
                ship.getId(), ship.getName(),
                Utilities.WIKI_BASE_LINK + "/" + Utilities.urlEncode(ship.getName()))))
            .flatMap(stringJoiner -> event.getMessage().getChannel()
                .flatMap(messageChannel -> messageChannel.createEmbed(embedCreateSpec -> {
                    if(stringJoiner.toString().isEmpty()) {
                        embedCreateSpec.setDescription("Couldn't find any ship with given build time.");
                    } else {
                        embedCreateSpec.setDescription(String.format("Following ships have the build time of %02d:%02d:%02d\n\n%s",
                            hours, minutes, seconds, stringJoiner.toString()));
                    }
                    embedCreateSpec.setColor(Color.WHITE);
                })))
            .then();
    }
}
