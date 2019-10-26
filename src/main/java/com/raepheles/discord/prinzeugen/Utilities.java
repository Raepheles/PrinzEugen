package com.raepheles.discord.prinzeugen;


import java.awt.*;

public class Utilities {

    public static String WIKI_BASE_LINK = "https://azurlane.koumakan.jp";

    public static Color getShipColorByRarity(String rarity) {
        if(rarity.equalsIgnoreCase("elite")) {
            return new Color(14524637);
        }
        if(rarity.equalsIgnoreCase("super rare")) {
            return new Color(15657130);
        }
        if(rarity.equalsIgnoreCase("normal")) {
            return new Color(14474460);
        }
        if(rarity.equalsIgnoreCase("rare")) {
            return new Color(11591910);
        }
        if(rarity.equalsIgnoreCase("legendary")) {
            return new Color(8557801);
        }
        if(rarity.equalsIgnoreCase("priority")) {
            return new Color(15657130);
        }
        return new Color(0);
    }

    public static String urlEncode(String text) {
        return text.replaceAll(" ", "_")
            .replaceAll("ö", "%C3%B6");
    }
}
