package com.github.fertkir.moneytransfer.dao;

import com.github.fertkir.moneytransfer.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountDao {

    List<Account> findAll();

    Optional<Account> getById(long id);

    Account save(Account account);
}
