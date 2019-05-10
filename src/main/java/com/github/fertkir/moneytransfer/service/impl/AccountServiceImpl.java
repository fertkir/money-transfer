package com.github.fertkir.moneytransfer.service.impl;

import com.github.fertkir.moneytransfer.dao.AccountDao;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.entity.TransferResult;
import com.github.fertkir.moneytransfer.persistence.TransactionTemplate;
import com.github.fertkir.moneytransfer.service.AccountService;
import com.github.fertkir.moneytransfer.service.exception.AccountingException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;

@Slf4j
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
        log.info("Requested list of accounts");
        return transactionTemplate.execute(accountDao::findAll);
    }

    @Override
    public Account getById(long id) {
        log.info("Requested account by id: {}", id);
        Account account = transactionTemplate.execute(() -> accountDao.getById(id)
                .orElseThrow(() -> createNoAccountException(id)));
        log.info("Returning account: {}", account);
        return account;
    }

    @Override
    public Account createNew() {
        log.info("Requested account creation");
        Account account = transactionTemplate.execute(() ->
                accountDao.save(Account.builder()
                        .balance(BigDecimal.ZERO)
                        .build()));
        log.info("Created new account: {}", account);
        return account;
    }

    @Override
    public Account topUp(long accountId, BigDecimal amount) {
        log.info("Requested top up of amount {} on account id {}", amount, accountId);
        Account result = transactionTemplate.execute(() -> {
            validateAmount(amount);
            Account account = accountDao.getById(accountId)
                    .orElseThrow(() -> createNoAccountException(accountId));
            BigDecimal newBalance = account.getBalance().add(amount);
            Account updatedAccount = account.toBuilder()
                    .balance(newBalance)
                    .build();
            return accountDao.save(updatedAccount);
        });
        log.info("Account data after top up: {}", result);
        return result;
    }

    @Override
    public Account withdraw(long accountId, BigDecimal amount) {
        log.info("Requested withdrawal of amount {} from account id {}", amount, accountId);
        Account result = transactionTemplate.execute(() -> {
            validateAmount(amount);
            Account account = accountDao.getById(accountId)
                    .orElseThrow(() -> createNoAccountException(accountId));
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AccountingException(format("Cannot withdraw %s. Not enough money", amount));
            }
            Account updatedAccount = account.toBuilder()
                    .balance(newBalance)
                    .build();
            return accountDao.save(updatedAccount);
        });
        log.info("Account data after withdrawal: {}", result);
        return result;
    }

    @Override
    public TransferResult transfer(long accountFrom, long accountTo, BigDecimal amount) {
        log.info("Requested transfer of amount {} from account {} to account {}", amount, accountFrom, accountTo);
        TransferResult transferResult = transactionTemplate.execute(() -> {
            validateAmount(amount);

            if (accountFrom == accountTo) {
                throw new AccountingException("Source and destination accounts must be different");
            }

            Account source = accountDao.getById(accountFrom)
                    .orElseThrow(() -> createNoAccountException(accountFrom));
            Account target = accountDao.getById(accountTo)
                    .orElseThrow(() -> createNoAccountException(accountTo));

            BigDecimal newSourceBalance = source.getBalance().subtract(amount);
            if (newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AccountingException(format("Cannot transfer %s. Not enough money", amount));
            }
            BigDecimal newTargetBalance = target.getBalance().add(amount);

            return TransferResult.builder()
                    .source(accountDao.save(source.toBuilder()
                            .balance(newSourceBalance)
                            .build()))
                    .target(accountDao.save(target.toBuilder()
                            .balance(newTargetBalance)
                            .build()))
                    .build();
        });
        log.info("Result of transfer: {}", transferResult);
        return transferResult;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        throw new AccountingException(format("Amount must be positive, but given %s", amount.toString()));
    }

    private AccountingException createNoAccountException(long accountId) {
        return new AccountingException(format("Account id \"%d\" does not exist", accountId));
    }
}
