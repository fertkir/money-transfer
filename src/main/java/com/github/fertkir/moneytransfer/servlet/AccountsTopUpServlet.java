package com.github.fertkir.moneytransfer.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.service.AccountService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import static com.github.fertkir.moneytransfer.servlet.AppServletModule.AccountsParameters.ACCOUNT_ID;
import static com.github.fertkir.moneytransfer.servlet.AppServletModule.AccountsParameters.AMOUNT;
import static com.github.fertkir.moneytransfer.servlet.AppServletModule.CONTENT_TYPE;

@Singleton
public class AccountsTopUpServlet extends HttpServlet {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Inject
    public AccountsTopUpServlet(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long accountId = Long.parseLong(req.getParameter(ACCOUNT_ID));
        BigDecimal amount = new BigDecimal(req.getParameter(AMOUNT));

        Account result = accountService.topUp(accountId, amount);

        resp.setContentType(CONTENT_TYPE);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(objectMapper.writeValueAsString(result));
    }
}
