package com.raepheles.discord.prinzeugen.commands.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Flag {
    private String flagName;
    private int category;
    private List<String> aliases;

    public Flag(String flagName, int category) {
        this.flagName = flagName;
        this.category = category;
        this.aliases = new ArrayList<>();
    }

    public String getFlagName() {
        return flagName;
    }

    public int getCategory() {
        return category;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public Flag addAlias(String alias) {
        this.aliases.add(alias);
        return this;
    }

    public Flag addAllAliases(Collection<String> aliases) {
        this.aliases.addAll(aliases);
        return this;
    }
}
