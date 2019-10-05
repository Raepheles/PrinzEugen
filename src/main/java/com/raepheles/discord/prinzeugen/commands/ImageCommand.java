package com.raepheles.discord.prinzeugen.commands;

import com.raepheles.discord.prinzeugen.Bot;
import com.raepheles.discord.prinzeugen.Messages;
import com.raepheles.discord.prinzeugen.Utilities;
import com.raepheles.discord.prinzeugen.commands.api.Command;
import com.raepheles.discord.prinzeugen.commands.api.Flag;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ImageCommand extends Command {

    public ImageCommand() {
        super("image", "Game Info", true);
        Flag chibiFlag = new Flag("chibi", 1);
        Flag skinsFlag = new Flag("skins", 2)
            .addAlias("skin");
        this.addFlags(Arrays.asList(chibiFlag, skinsFlag));
        this.setUsage("[prefix]image [ship-name]");
        this.setDescription("Shows image of the ship. Use -chibi flag for chibi image, -skins flag for skin images.");
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        String parameter = this.getParametersAsString();

        if (this.flagExists("skins")) {
            return Flux.fromIterable(Bot.ships)
                .filter(ship -> ship.getName().equalsIgnoreCase(parameter))
                .flatMap(ship -> event.getMessage().getChannel()
                    .flatMap(channel -> {
                        Map<String, String> map;
                        if (this.flagExists("chibi")) {
                            map = ship.getChibiSkins();
                        } else {
                            map = ship.getSkins();
                        }
                        int i = 1;
                        StringJoiner sj = new StringJoiner("\n");
                        Map<Integer, String> idToSkinKey = new HashMap<>();
                        sj.add(Messages.REACTION_TIMEOUT_MESSAGE);
                        for (String key : map.keySet()) {
                            idToSkinKey.put(i, key);
                            sj.add(String.format("%d. %s", i++, key));
                        }
                        final int skinCount = i - 1;
                        return channel.createMessage(sj.toString())
                            .flatMap(message -> addReactions(message, skinCount))
                            .flatMap(message -> event.getClient().getEventDispatcher().on(ReactionAddEvent.class)
                                .filter(ev -> ev.getMessageId().equals(message.getId()))
                                .filter(ev -> ev.getUserId().equals(event.getMessage().getAuthor().get().getId()))
                                .next()
                                .flatMap(reactionAddEvent -> {
                                    String skinUrl = "";
                                    int skinNum = getNumberFromEmoji(reactionAddEvent.getEmoji());
                                    if(skinNum == 0) {
                                        return Mono.empty();
                                    }
                                    skinUrl = map.get(idToSkinKey.get(skinNum));
                                    final String skin = skinUrl;
                                    return message.delete()
                                        .then(channel.createEmbed(embedCreateSpec -> {
                                            embedCreateSpec.setTitle(String.format("%s - %s", ship.getId(), ship.getName()));
                                            embedCreateSpec.setUrl(skin);
                                            embedCreateSpec.setImage(skin);
                                            if(event.getMessage().getAuthor().isPresent()) {
                                                User author = event.getMessage().getAuthor().get();
                                                embedCreateSpec.setFooter("Requested by " +
                                                    author.getUsername() + "#" + author.getDiscriminator(), null);
                                            }
                                        }));
                                })
                                .timeout(Duration.ofSeconds(20))
                                .doOnError(t -> message.delete().subscribe()));
                    }))
                .switchIfEmpty(event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("Ship not found.")))
                .then();
        }

        return Flux.fromIterable(Bot.ships)
            .filter(ship -> ship.getName().equalsIgnoreCase(parameter))
            .flatMap(ship -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createEmbed(embedCreateSpec -> {
                    embedCreateSpec.setTitle(String.format("%s - %s", ship.getId(), ship.getName()));
                    embedCreateSpec.setUrl(String.format("%s/%s", Utilities.WIKI_BASE_LINK,
                        ship.getName().replaceAll(" ", "_")));
                    embedCreateSpec.setColor(Utilities.getShipColorByRarity(ship.getRarity()));
                    if (this.flagExists("chibi")) {
                        embedCreateSpec.setImage(ship.getChibi());
                    } else {
                        embedCreateSpec.setImage(ship.getImage());
                    }
                })))
            .switchIfEmpty(event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Ship not found.")))
            .then();
    }

    private Mono<Message> addReactions(Message message, int skinCount) {
        return Flux.range(1, skinCount)
            .flatMap(num -> message.addReaction(getEmoji(num)))
            .then(Mono.just(message));
    }

    private ReactionEmoji getEmoji(int num) {
        switch (num) {
            case 1:
                return ReactionEmoji.unicode("\u0031\u20E3");
            case 2:
                return ReactionEmoji.unicode("\u0032\u20E3");
            case 3:
                return ReactionEmoji.unicode("\u0033\u20E3");
            case 4:
                return ReactionEmoji.unicode("\u0034\u20E3");
            case 5:
                return ReactionEmoji.unicode("\u0035\u20E3");
            case 6:
                return ReactionEmoji.unicode("\u0036\u20E3");
            case 7:
                return ReactionEmoji.unicode("\u0037\u20E3");
            case 8:
                return ReactionEmoji.unicode("\u0038\u20E3");
            case 9:
                return ReactionEmoji.unicode("\u0039\u20E3");
            default:
                return ReactionEmoji.unicode("\u0030\u20E3");
        }
    }

    private int getNumberFromEmoji(ReactionEmoji emoji) {
        if(emoji.equals(ReactionEmoji.unicode("\u0031\u20E3"))) {
            return 1;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0032\u20E3"))) {
            return 2;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0033\u20E3"))) {
            return 3;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0034\u20E3"))) {
            return 4;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0035\u20E3"))) {
            return 5;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0036\u20E3"))) {
            return 6;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0037\u20E3"))) {
            return 7;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0038\u20E3"))) {
            return 8;
        } else if(emoji.equals(ReactionEmoji.unicode("\u0039\u20E3"))) {
            return 9;
        } else {
            return 0;
        }
    }
}
