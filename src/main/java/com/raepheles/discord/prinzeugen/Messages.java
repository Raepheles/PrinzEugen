package com.raepheles.discord.prinzeugen;

public class Messages {
    public static String INVALID_COMMAND_PATTERN = "Invalid command pattern.\n" +
            "Please use the following pattern: [prefix][command] [arguments] (flags)\n" +
            "Keywords inside [] are arguments. You have to use them if command has any.\n" +
            "Keywords inside () are optional arguments in other word flags. You have to add - before each flag.\n" +
            "Order of the keywords are important. You cannot use flags before arguments.\n" +
            "Example: `!ship prinz eugen -base`";

    public static String REACTION_TIMEOUT_MESSAGE = "React with the appropriate emoji. This message will delete itself " +
        "in 20 seconds if you don't react.";

    public static String getInvalidCommandFlagMessage(String flagName) {
        return String.format("This command doesn't accept the following flag: `%s`", flagName);
    }

    public static String getConflictingFlagsMessage(String f1, String f2) {
        return String.format("Following flags cannot be used at the same time: `%s`, `%s`", f1, f2);
    }

    public static String getMissingParameterMessage(String usage) {
        return String.format("There are missing parameters. Please check the command usage below.\n\n%s", usage);
    }

    public static String getHelpMessage(String prefix) {
        return String.format("Use `%shelp [command-name]` for further help on the command. " +
            "Example: `%shelp help`", prefix, prefix);
    }

    public static String getLogMessage(String commandName, String userName, String userId, String guildName, String guildId, String channelName) {
        return String.format("`%s(%s)` used `%s` command in guild `%s(%s)`, channel `%s`",
            userName, userId, commandName, guildName, guildId, channelName);
    }

    public static String getLogMessage(String commandName, String userName, String userId, String guildName, String guildId, String channelName, String error) {
        return String.format("`%s(%s)` used `%s` command in guild `%s(%s)`, channel `%s`\n`ERROR: %s`",
            userName, userId, commandName, guildName, guildId, channelName, error);
    }
}
