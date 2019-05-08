package com.github.fertkir.moneytransfer.service;

import com.github.fertkir.moneytransfer.service.impl.AccountServiceImpl;
import com.google.inject.AbstractModule;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountService.class).to(AccountServiceImpl.class);
    }
}
