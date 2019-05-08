package com.github.fertkir.moneytransfer.dao;

import com.github.fertkir.moneytransfer.entity.Account;

import java.util.List;

public interface AccountDao {

    List<Account> findAll();

    Account getById(long id);

    Account create(Account account);

    Account update(Account account);
}
