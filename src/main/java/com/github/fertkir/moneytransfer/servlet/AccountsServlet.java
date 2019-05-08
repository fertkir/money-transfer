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
import java.util.List;

import static com.github.fertkir.moneytransfer.servlet.AppServletModule.CONTENT_TYPE;

@Singleton
public class AccountsServlet extends HttpServlet {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Inject
    public AccountsServlet(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accountIdParam = req.getParameter("accountId");
        String responseValue;
        if (accountIdParam == null) {
            List<Account> accounts = accountService.list();
            responseValue = objectMapper.writeValueAsString(accounts);
        } else {
            long accountId = Long.parseLong(accountIdParam);
            Account account = accountService.getById(accountId);
            responseValue = objectMapper.writeValueAsString(account);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        resp.getWriter().println(responseValue);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Account result = accountService.createNew();

        resp.setContentType(CONTENT_TYPE);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().println(objectMapper.writeValueAsString(result));
    }
}
