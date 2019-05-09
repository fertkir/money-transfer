package com.github.fertkir.moneytransfer.dao.impl;

import com.github.fertkir.moneytransfer.ApplicationMain;
import com.github.fertkir.moneytransfer.dao.AccountDao;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.persistence.PersistenceException;
import com.github.fertkir.moneytransfer.persistence.TransactionTemplate;
import com.google.inject.Injector;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AccountDaoImplIntegrationTest {

    private static final Injector injector = ApplicationMain.getInjector();

    private final TransactionTemplate transactionTemplate = injector.getInstance(TransactionTemplate.class);
    private final AccountDao accountDao = injector.getInstance(AccountDao.class);

    @Test
    public void crudTest() {
        executeWithTransactionRollback(() -> {
            // checking there's no accounts initially
            List<Account> accounts = accountDao.findAll();
            assertThat(accounts).isEmpty();
            Optional<Account> missingAccount = accountDao.getById(1);
            assertThat(missingAccount).isEmpty();

            // adding a new one
            Account newAccount = Account.builder().balance(BigDecimal.TEN).build();
            Long id = accountDao.save(newAccount).getId();
            Account account = accountDao.getById(id).get();
            Account expectedAccount = Account.builder().id(id).balance(BigDecimal.TEN).build();
            assertThat(account).isEqualTo(expectedAccount);

            // altering account's balance
            Account toBeUpdated = account.toBuilder().balance(BigDecimal.ONE).build();
            Account updatedAccount = accountDao.save(toBeUpdated);
            assertThat(updatedAccount).isEqualTo(toBeUpdated);

            // adding one more account
            Account savedAccount2 = accountDao.save(Account.builder().balance(new BigDecimal(2)).build());

            // checking there are 2 accounts
            List<Account> twoAccounts = accountDao.findAll();
            assertThat(twoAccounts).containsOnly(updatedAccount, savedAccount2);
        });
    }

    @Test
    public void saveShouldThrowExceptionWhenSavingAccountWithUnknownId() {
        executeWithTransactionRollback(() -> {
            // given
            Account newAccount = Account.builder().id(1000L).balance(BigDecimal.TEN).build();

            try {
                // when
                accountDao.save(newAccount);
                fail("exception should have been thrown");
            } catch (PersistenceException e) {
                // then
                assertThat(e.getMessage()).isEqualTo("Cannot update entity with id 1000");
            }
        });
    }

    private void executeWithTransactionRollback(Runnable runnable) {
        try {
            transactionTemplate.execute(() -> {
                runnable.run();
                throw new RollbackException();
            });
            fail("exception should have been thrown");
        } catch (PersistenceException e) {
            assertThat(e.getCause()).isInstanceOf(RollbackException.class);
        }
    }

    private static class RollbackException extends RuntimeException {}
}