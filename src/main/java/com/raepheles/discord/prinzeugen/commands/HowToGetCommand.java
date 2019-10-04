package com.raepheles.discord.prinzeugen.commands;

import com.raepheles.discord.prinzeugen.Bot;
import com.raepheles.discord.prinzeugen.Utilities;
import com.raepheles.discord.prinzeugen.commands.api.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class HowToGetCommand extends Command {

    public HowToGetCommand() {
        super("howtoget", "Game Info", true);
        this.addAlias("htg");
        this.setUsage("[prefix]howtoget [ship-name]");
        this.setDescription("Shows how can you get the ship (construction, drop locations, events etc.)");
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        String parameter = this.getParametersAsString();

        return Flux.fromIterable(Bot.ships)
            .filter(ship -> ship.getName().equalsIgnoreCase(parameter))
            .flatMap(ship -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createEmbed(embedCreateSpec -> {
                    embedCreateSpec.setTitle(String.format("%s - %s", ship.getId(), ship.getName()));
                    embedCreateSpec.setUrl(String.format("%s/%s", Utilities.WIKI_BASE_LINK,
                        Utilities.urlEncode(ship.getName())));
                    embedCreateSpec.setColor(Utilities.getShipColorByRarity(ship.getRarity()));
                    embedCreateSpec.setThumbnail(ship.getIcon());
                    embedCreateSpec.addField("Construction", ship.getConstructionInfo(), false);
                    if (ship.getDropInfo().isEmpty()) {
                        embedCreateSpec.addField("Drop", "This ship cannot be acquired as drop.", false);
                    } else {
                        embedCreateSpec.addField("Drop", ship.getDropInfo(), false);
                    }
                    if (!ship.getAdditionalDropInfo().isEmpty()) {
                        embedCreateSpec.addField("Additional", ship.getAdditionalDropInfo(), false);
                    }
                })))
            .switchIfEmpty(event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Ship not found.")))
            .then();
    }
}
