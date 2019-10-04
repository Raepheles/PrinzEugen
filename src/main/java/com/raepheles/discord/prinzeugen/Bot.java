package com.raepheles.discord.prinzeugen;

import com.raepheles.discord.prinzeugen.commands.api.CommandDispatcher;
import com.raepheles.discord.prinzeugen.commands.api.CommandManager;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Bot {

    public static List<Ship> ships;
    public static Snowflake LOG_CHANNEL_ID = Snowflake.of(489447506808274944L);

    public static void main(String[] args) {
        String token = readFromTextFile("token.txt");

        final DiscordClient client = new DiscordClientBuilder(token).build();
        final CommandManager manager = new CommandManager("com.raepheles.discord.prinzeugen.commands", "!");
        final CommandDispatcher dispatcher = new CommandDispatcher(manager);

        ships = new ArrayList<>();

        try {
            JSONArray shipsArray = readJSONArray("ships.json");
            for(int i = 0; i < shipsArray.length(); i++) {
                JSONObject o = shipsArray.optJSONObject(i);
                ships.add(Ship.parseObjectToShip(o));
            }
        } catch(IOException | InvalidShipObjectException e) {
            e.printStackTrace();
            return;
        }

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(ev -> dispatcher.onMessageEvent(ev)
                        .onErrorResume(t -> Mono.empty()))
                .subscribe();

        client.login().block();
    }

    private static JSONArray readJSONArray(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        StringJoiner sj = new StringJoiner("\n");
        for(String line: lines) {
            sj.add(line);
        }
        return new JSONArray(sj.toString());
    }

    private static String readFromTextFile(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
            return String.join("\n", lines);
        } catch(IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
