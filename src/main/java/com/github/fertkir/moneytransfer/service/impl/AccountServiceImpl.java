package com.github.fertkir.moneytransfer.service.impl;

import com.github.fertkir.moneytransfer.dao.AccountDao;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.entity.TransferResult;
import com.github.fertkir.moneytransfer.persistence.TransactionTemplate;
import com.github.fertkir.moneytransfer.service.AccountService;
import com.github.fertkir.moneytransfer.service.exception.AccountingException;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;
    private final TransactionTemplate transactionTemplate;

    @Inject
    public AccountServiceImpl(AccountDao accountDao, TransactionTemplate transactionTemplate) {
        this.accountDao = accountDao;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public List<Account> list() {
        return transactionTemplate.execute(accountDao::findAll);
    }

    @Override
    public Account getById(long id) {
        return transactionTemplate.execute(() -> accountDao.getById(id));
    }

    @Override
    public Account createNew() {
        return transactionTemplate.execute(() ->
                accountDao.create(Account.builder()
                        .balance(BigDecimal.ZERO)
                        .build()));
    }

    @Override
    public Account topUp(long accountId, BigDecimal amount) {
        return transactionTemplate.execute(() -> {
            Account account = accountDao.getById(accountId);
            BigDecimal newBalance = account.getBalance().add(amount);
            Account updatedAccount = account.toBuilder()
                    .balance(newBalance)
                    .build();
            return accountDao.update(updatedAccount);
        });
    }

    @Override
    public Account withdraw(long accountId, BigDecimal amount) {
        return transactionTemplate.execute(() -> {
            Account account = accountDao.getById(accountId);
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AccountingException(String.format("Cannot withdraw %s. Not enough money", amount));
            }
            Account updatedAccount = account.toBuilder()
                    .balance(newBalance)
                    .build();
            return accountDao.update(updatedAccount);
        });
    }

    @Override
    public TransferResult transfer(long accountFrom, long accountTo, BigDecimal amount) {
        return transactionTemplate.execute(() -> {
            Account source = accountDao.getById(accountFrom);
            Account target = accountDao.getById(accountTo);

            BigDecimal newSourceBalance = source.getBalance().subtract(amount);
            if (newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AccountingException(String.format("Cannot transfer %s. Not enough money", amount));
            }
            BigDecimal newTargetBalance = target.getBalance().add(amount);

            return TransferResult.builder()
                    .source(accountDao.update(source.toBuilder()
                            .balance(newSourceBalance)
                            .build()))
                    .target(accountDao.update(target.toBuilder()
                            .balance(newTargetBalance)
                            .build()))
                    .build();
        });
    }
}