package com.github.fertkir.moneytransfer.persistence;

import com.google.inject.Singleton;

import java.sql.Connection;

@Singleton
class ConnectionKeeper {

    private final ThreadLocal<Connection> connections = new ThreadLocal<>();

    void set(Connection connection) {
        connections.set(connection);
    }

    void remove() {
        connections.remove();
    }

    Connection getConnection() {
        Connection connection = connections.get();
        if (connection == null) {
            throw new PersistenceException("Transaction is not started");
        }
        return connection;
    }
}
