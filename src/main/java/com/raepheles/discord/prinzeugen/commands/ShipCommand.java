package com.raepheles.discord.prinzeugen.commands;

import com.raepheles.discord.prinzeugen.Bot;
import com.raepheles.discord.prinzeugen.Ship;
import com.raepheles.discord.prinzeugen.Utilities;
import com.raepheles.discord.prinzeugen.commands.api.Command;
import com.raepheles.discord.prinzeugen.commands.api.Flag;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

public class ShipCommand extends Command {

    public ShipCommand() {
        super("ship", "Game Info", true);
        Flag lv120 = new Flag("lv120", 1);
        Flag base = new Flag("base", 1);
        Flag retrofit = new Flag("retrofit", 2);
        this.addFlags(Arrays.asList(lv120, base, retrofit));
        this.setUsage("[prefix]ship [ship-name]");
        this.setDescription("Shows ship data. By default displays level 100 stats. For base and level 120 stats" +
            " use -base or -lv120 flags. For retrofit stats use -retrofit flag");
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        String parameter = this.getParametersAsString();

        return Flux.fromIterable(Bot.ships)
                .filter(ship -> ship.getName().equalsIgnoreCase(parameter))
                .flatMap(ship -> event.getMessage().getChannel()
                    .flatMap(channel -> {
                        if (this.flagExists("retrofit")
                            && !ship.getRetrofit().isPresent()) {
                            return channel.createMessage(String.format("%s doesn't have retrofit", ship.getName()));
                        }
                        return channel.createEmbed(embedCreateSpec -> {
                            embedCreateSpec.setTitle(String.format("%s - %s", ship.getId(), ship.getName()));
                            embedCreateSpec.setUrl(String.format("%s/%s", Utilities.WIKI_BASE_LINK,
                                Utilities.urlEncode(ship.getName())));
                            embedCreateSpec.setColor(Utilities.getShipColorByRarity(ship.getRarity()));
                            embedCreateSpec.setThumbnail(ship.getIcon());
                            embedCreateSpec.addField("Class", ship.getShipClass(), true);
                            embedCreateSpec.addField("Nationality", ship.getNationality(), true);
                            embedCreateSpec.addField("Type", ship.getType(), true);
                            embedCreateSpec.addField("Construction Time", ship.getConstructionTime(), true);
                            String statsKey = "lv100";
                            if (this.flagExists("base")) {
                                statsKey = "base";
                            } else if (this.flagExists("lv120")) {
                                statsKey = "lv120";
                            }
                            boolean retrofit = this.flagExists("retrofit");
                            String[] stats = splitStats(ship, statsKey, retrofit);
                            embedCreateSpec.addField("Stats", stats[0], true);
                            embedCreateSpec.addField("Stats", stats[1], true);
                            embedCreateSpec.addField("Skills", ship.getSkills(), true);
                            embedCreateSpec.addField("Misc", ship.getMisc(), true);
                        });
                    }))
                .switchIfEmpty(event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("Ship not found.")))
                .then();
    }

    private String[] splitStats(Ship ship, String key, boolean retrofit) {
        Map<String, String> stats;
        if (retrofit && ship.getRetrofit().isPresent()) {
            stats = ship.getRetrofit().get().getStats().get(key);
        } else {
            stats = ship.getStats().get(key);
        }
        String[] res = new String[2];
        StringJoiner sj = new StringJoiner("\n");
        int max = stats.keySet().size() / 2;
        if(stats.keySet().size() % 2 != 0) {
            max++;
        }
        int i = 0;
        for(String k: stats.keySet()) {
            if(i == max) {
                res[0] = sj.toString();
                sj = new StringJoiner("\n");
            }
            sj.add(String.format("**%s:** %s", k, stats.get(k)));
            i++;
        }
        res[1] = sj.toString();
        return res;
    }

}
