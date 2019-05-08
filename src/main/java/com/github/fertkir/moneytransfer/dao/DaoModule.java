package com.github.fertkir.moneytransfer.dao;

import com.github.fertkir.moneytransfer.persistence.PersistenceException;
import com.github.fertkir.moneytransfer.dao.impl.AccountDaoImpl;
import com.google.inject.AbstractModule;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class DaoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountDao.class).to(AccountDaoImpl.class);

        bind(DataSource.class).toInstance(h2DataSource());
    }

    private DataSource h2DataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        // initializing schema
        try (Connection connection = dataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(getClass().getClassLoader()
                    .getResourceAsStream("create.sql")));
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

        return dataSource;
    }
}
