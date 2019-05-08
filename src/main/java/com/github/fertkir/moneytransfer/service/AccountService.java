package com.github.fertkir.moneytransfer.service;

import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.entity.TransferResult;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    List<Account> list();

    Account getById(long id);

    Account createNew();

    Account topUp(long accountId, BigDecimal amount);

    Account withdraw(long accountId, BigDecimal amount);

    TransferResult transfer(long accountFrom, long accountTo, BigDecimal amount);
}
