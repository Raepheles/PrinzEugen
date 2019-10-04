package com.raepheles.discord.prinzeugen;

public class InvalidShipObjectException extends Exception {

    public InvalidShipObjectException() {
        super();
    }

    public InvalidShipObjectException(String message) {
        super("message");
    }
}
