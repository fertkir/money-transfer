package com.github.fertkir.moneytransfer.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Singleton
public class JdbcTemplate {

    private final ConnectionKeeper connectionKeeper;

    @Inject
    public JdbcTemplate(ConnectionKeeper connectionKeeper) {
        this.connectionKeeper = connectionKeeper;
    }

    public PreparedStatement prepareStatement(String sql) {
        try {
            return connectionKeeper.getConnection()
                    .prepareStatement(sql);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
