package com.github.fertkir.moneytransfer.dao.impl;

import com.github.fertkir.moneytransfer.dao.AccountDao;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.persistence.JdbcTemplate;
import com.github.fertkir.moneytransfer.persistence.PersistenceException;
import com.google.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {

    private final JdbcTemplate jdbcTemplate;

    @Inject
    public AccountDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Account> findAll() {
        try {
            String query = "SELECT id, balance FROM account";
            PreparedStatement statement = jdbcTemplate.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            List<Account> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(Account.builder()
                        .id(rs.getLong("ID"))
                        .balance(rs.getBigDecimal("BALANCE"))
                        .build());
            }
            return accounts;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Account> getById(long id) {
        try {
            String query = "SELECT id, balance FROM account where id = ?";
            PreparedStatement statement = jdbcTemplate.prepareStatement(query);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(Account.builder()
                        .id(rs.getLong("ID"))
                        .balance(rs.getBigDecimal("BALANCE"))
                        .build());
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Account create(Account account) {
        try {
            String seqQuery = "SELECT account_seq.nextval FROM dual";
            PreparedStatement seqStatement = jdbcTemplate.prepareStatement(seqQuery);
            ResultSet seqResultSet = seqStatement.executeQuery();
            seqResultSet.next();
            long nextId = seqResultSet.getLong(1);

            Account newAccount = account.toBuilder()
                    .id(nextId)
                    .build();

            String query = "INSERT INTO account (id, balance) VALUES (?, ?)";
            PreparedStatement statement = jdbcTemplate.prepareStatement(query);
            statement.setLong(1, newAccount.getId());
            statement.setBigDecimal(2, newAccount.getBalance());
            statement.execute();

            return newAccount;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Account update(Account account) {
        try {
            String query = "UPDATE account SET balance = ? where id = ?";
            PreparedStatement statement = jdbcTemplate.prepareStatement(query);
            statement.setBigDecimal(1, account.getBalance());
            statement.setLong(2, account.getId());
            statement.execute();
            return account;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }
}
