package com.raepheles.discord.prinzeugen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ship {
    private String nationality;
    private String name;
    private String id;
    private String type;
    private String rarity;
    private String shipClass;
    private String constructionTime;
    private String skills;
    private String misc;
    private Ship retrofit;

    private String image;
    private String chibi;
    private String icon;

    private Map<String, Map<String, String>> stats;
    private Map<String, String> skins;
    private Map<String, String> chibiSkins;

    private Ship(String name,
                 String id,
                 String type,
                 String rarity,
                 String nationality,
                 String shipClass,
                 String constructionTime,
                 String skills,
                 String misc,
                 Ship retrofit,
                 String image,
                 String chibi,
                 String icon,
                 Map<String, String> skins,
                 Map<String, String> chibiSkins,
                 Map<String, Map<String, String>> stats) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.rarity = rarity;
        this.nationality = nationality;
        this.shipClass = shipClass;
        this.constructionTime = constructionTime;
        this.skills = skills;
        this.misc = misc;
        this.retrofit = retrofit;
        this.image = image;
        this.chibi = chibi;
        this.icon = icon;
        this.skins = skins;
        this.chibiSkins = chibiSkins;
        this.stats = stats;
    }

    public static Ship parseObjectToShip(JSONObject obj) throws InvalidShipObjectException {
        if(obj == null) {
            throw new InvalidShipObjectException("Ship object is null.");
        }
        String name = obj.optString("name");
        String id = obj.optString("id");
        String type = obj.optString("type");
        String rarity = obj.optString("rarity");
        String nationality = obj.optString("nationality");
        String shipClass = obj.optString("class");
        String constructionTime = obj.optString("construction time");
        String skills = obj.optString("skills");
        String misc = obj.optString("misc");
        JSONObject imageObj = obj.optJSONObject("images");
        if(name == null) {
            throw new InvalidShipObjectException("Null name.");
        }
        if(id == null) {
            throw new InvalidShipObjectException("Null id.");
        }
        if(type == null) {
            throw new InvalidShipObjectException("Null type.");
        }
        if(rarity == null) {
            throw new InvalidShipObjectException("Null rarity.");
        }
        if(nationality == null) {
            throw new InvalidShipObjectException("Null nationality.");
        }
        if(shipClass == null) {
            throw new InvalidShipObjectException("Null ship class.");
        }
        if(constructionTime == null) {
            throw new InvalidShipObjectException("Null construction time.");
        }
        if(skills == null) {
            throw new InvalidShipObjectException("Null skills.");
        }
        if(misc == null) {
            throw new InvalidShipObjectException("Null misc info.");
        }
        if(imageObj == null) {
            throw new InvalidShipObjectException("Null images object.");
        }
        String image = imageObj.optString("default");
        String chibi = imageObj.optString("chibi");
        String icon = imageObj.optString("icon");
        JSONArray skinsArray = imageObj.optJSONArray("skins");
        JSONArray chibisArray = imageObj.optJSONArray("chibis");
        if(image == null) {
            throw new InvalidShipObjectException("Null default image.");
        }
        if(chibi == null) {
            throw new InvalidShipObjectException("Null default chibi image.");
        }
        if(icon == null) {
            throw new InvalidShipObjectException("Null default icon image.");
        }
        if(skinsArray == null) {
            throw new InvalidShipObjectException("Null skins array.");
        }
        if(chibisArray == null) {
            throw new InvalidShipObjectException("Null chibi skins array.");
        }
        Map<String, String> skinsMap = new HashMap<>();
        Map<String, String> chibisMap = new HashMap<>();
        for(int i = 0; i < skinsArray.length(); i++) {
            String tmp = skinsArray.getString(i);
            String[] split = tmp.split("\\|", 2);
            skinsMap.put(split[0], split[1]);
        }
        for(int i = 0; i < chibisArray.length(); i++) {
            String tmp = chibisArray.getString(i);
            String[] split = tmp.split("\\|", 2);
            chibisMap.put(split[0], split[1]);
        }
        JSONObject retrofitObj = obj.optJSONObject("retrofit");
        Ship retrofit;
        if(retrofitObj == null) {
            retrofit = null;
        } else {
            retrofit = parseObjectToShip(retrofitObj);
        }
        JSONObject stats = obj.optJSONObject("stats");
        Map<String, Map<String, String>> statsMap = new HashMap<>();
        for(String key: stats.keySet()) {
            statsMap.put(key, jsonToMap(stats.getJSONObject(key)));
        }

        return new Ship(name, id, type, rarity, nationality, shipClass, constructionTime,
                skills, misc, retrofit, image, chibi, icon, skinsMap, chibisMap, statsMap);
    }

    private static Map<String, String> jsonToMap(JSONObject o) {
        Map<String, String> map = new HashMap<>();
        for(String key: o.keySet()) {
            map.put(key, o.getString(key));
        }
        return map;
    }

    public String getNationality() {
        return nationality;
    }

    public String getShipClass() {
        return shipClass;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRarity() {
        return rarity;
    }

    public Optional<Ship> getRetrofit() {
        return Optional.ofNullable(retrofit);
    }

    public String getImage() {
        return image;
    }

    public String getChibi() {
        return chibi;
    }

    public String getIcon() {
        return icon;
    }

    public Map<String, String> getSkins() {
        return skins;
    }

    public Map<String, String> getChibiSkins() {
        return chibiSkins;
    }

    public String getConstructionTime() {
        return constructionTime;
    }

    public String getSkills() {
        return skills;
    }

    public String getMisc() {
        return misc;
    }

    public Map<String, Map<String, String>> getStats() {
        return stats;
    }
}
