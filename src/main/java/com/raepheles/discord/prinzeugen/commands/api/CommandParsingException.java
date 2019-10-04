package com.raepheles.discord.prinzeugen.commands.api;

public class CommandParsingException extends Exception {

    CommandParsingException() {
        super();
    }

    CommandParsingException(String msg) {
        super(msg);
    }
}
