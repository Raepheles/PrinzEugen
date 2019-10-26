package com.raepheles.discord.prinzeugen.commands.api;

import com.raepheles.discord.prinzeugen.Messages;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Command {
    private String keyword;
    private String module;
    private String description;
    private String usage;
    private List<Permission> requiredPerms;
    private List<String> aliases;
    private List<String> parameters;
    private List<Flag> acceptedFlags;
    private List<Flag> flags;
    private boolean allowDm;
    private boolean isAdminCommand;
    private boolean requireParameters;

    public Command(String keyword, String module, boolean allowDm) {
        this.keyword = keyword;
        this.module = module;
        this.requiredPerms = new ArrayList<>();
        this.allowDm = allowDm;
        this.isAdminCommand = false;
        this.description = "This command's description hasn't been defined.";
        this.usage = "This command's usage hasn't been defined.";
        this.aliases = new ArrayList<>();
        this.acceptedFlags = new ArrayList<>();
        this.requireParameters = false;
    }

    public Command(String keyword, String module) {
        this.keyword = keyword;
        this.module = module;
        this.requiredPerms = new ArrayList<>();
        this.allowDm = false;
        this.isAdminCommand = false;
        this.description = "This command's description hasn't been defined.";
        this.usage = "This command's usage hasn't been defined.";
        this.aliases = new ArrayList<>();
        this.acceptedFlags = new ArrayList<>();
        this.requireParameters = false;
    }

    Mono<Boolean> preExecute(MessageCreateEvent event) {
        String msg = event.getMessage().getContent().get().trim();

        String[] split = msg.split(" ");
        List<String> parameters = new ArrayList<>();
        int flagsIndex = -1;
        for(int i = 1; i < split.length; i++) {
            if(split[i].startsWith("-")) {
                flagsIndex = i;
                break;
            }
            parameters.add(split[i]);
        }

        this.parameters = parameters;

        if(requireParameters && this.parameters.isEmpty()) {
            return event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(Messages.getMissingParameterMessage(this.usage)))
                    .then(Mono.just(Boolean.FALSE));
        }

        // Get flags
        List<Flag> flags = new ArrayList<>();
        if(flagsIndex != -1) {
            for(int i = flagsIndex; i < split.length; i++) {
                if(!split[i].startsWith("-")) {
                    return event.getMessage().getChannel()
                            .flatMap(channel -> channel.createMessage(Messages.INVALID_COMMAND_PATTERN))
                            .then(Mono.just(Boolean.FALSE));
                }
                String flag = split[i].substring(1);
                if(!this.getAcceptedFlags().stream()
                        .map(Flag::getFlagName)
                        .collect(Collectors.toList())
                        .contains(split[i].substring(1))) {
                    return event.getMessage().getChannel()
                            .flatMap(channel -> channel.createMessage(Messages.getInvalidCommandFlagMessage(flag)))
                            .then(Mono.just(Boolean.FALSE));
                } else {
                    flags.add(this.getAcceptedFlags()
                            .stream()
                            .filter(f -> f.getFlagName().equalsIgnoreCase(flag))
                            .collect(Collectors.toList())
                            .get(0));
                }
            }
        }

        this.flags = flags;

        // Check for conflicting flags
        for(int i = 0; i < flags.size(); i++) {
            Flag f1 = flags.get(i);
            for(int j = i+1; j < flags.size(); j++) {
                Flag f2 = flags.get(j);
                if(f1.getCategory() == f2.getCategory()) {
                    return event.getMessage().getChannel()
                            .flatMap(channel -> channel.createMessage(Messages.getConflictingFlagsMessage(f1.getFlagName(), f2.getFlagName())))
                            .then(Mono.just(Boolean.FALSE));
                }
            }
        }

        return Mono.just(Boolean.TRUE);
    }

    public abstract Mono<Void> execute(MessageCreateEvent event);

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public String getKeyword() {
        return keyword;
    }

    public String getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public List<Permission> getRequiredPerms() {
        return requiredPerms;
    }

    public void addRequiredPermission(Permission perm) {
        this.getRequiredPerms().add(perm);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isAllowDm() {
        return allowDm;
    }

    public boolean isAdminCommand() {
        return isAdminCommand;
    }

    public void setAdminCommand(boolean adminCommand) {
        isAdminCommand = adminCommand;
    }

    public List<Flag> getAcceptedFlags() {
        return acceptedFlags;
    }

    public void addFlag(Flag flag) {
        this.acceptedFlags.add(flag);
    }

    public void addFlags(Collection<? extends Flag> flags) {
        this.acceptedFlags.addAll(flags);
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getParametersAsString() {
        return String.join(" ", this.getParameters());
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public void requireParameters() {
        requireParameters = true;
    }

    public Boolean flagExists(String name) {
        return this.getFlags().stream().map(Flag::getFlagName)
                .collect(Collectors.toList())
                .contains(name);
    }
}
