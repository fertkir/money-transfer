package com.github.fertkir.moneytransfer.servlet;

import com.google.inject.servlet.ServletModule;

public class AppServletModule extends ServletModule {

    public static final String CONTENT_TYPE = "application/json";

    @Override
    protected void configureServlets() {
        bind(AccountsServlet.class);
        bind(AccountsTopUpServlet.class);
        bind(AccountsTransferServlet.class);
        bind(AccountsWithdrawalServlet.class);

        filter("/accounts*").through(AccountsExceptionHandler.class);

        serve("/accounts").with(AccountsServlet.class);
        serve("/accounts/topup").with(AccountsTopUpServlet.class);
        serve("/accounts/transfer").with(AccountsTransferServlet.class);
        serve("/accounts/withdraw").with(AccountsWithdrawalServlet.class);
    }
}
