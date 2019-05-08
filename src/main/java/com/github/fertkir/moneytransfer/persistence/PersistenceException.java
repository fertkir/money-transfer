package com.github.fertkir.moneytransfer.persistence;

public class PersistenceException extends RuntimeException {
    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public PersistenceException(String message) {
        super(message);
    }
}
