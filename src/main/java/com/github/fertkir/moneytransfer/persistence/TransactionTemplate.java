package com.github.fertkir.moneytransfer.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

@Singleton
public class TransactionTemplate {

    private final DataSource dataSource;
    private final ConnectionKeeper connectionKeeper;

    @Inject
    public TransactionTemplate(DataSource dataSource, ConnectionKeeper connectionKeeper) {
        this.dataSource = dataSource;
        this.connectionKeeper = connectionKeeper;
    }

    public <T> T execute(Supplier<T> supplier) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                connectionKeeper.set(connection);
                connection.setAutoCommit(false);
                T result = supplier.get();
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw new PersistenceException(e);
            } finally {
                connectionKeeper.remove();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
