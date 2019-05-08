package com.github.fertkir.moneytransfer.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fertkir.moneytransfer.entity.TransferResult;
import com.github.fertkir.moneytransfer.service.AccountService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import static com.github.fertkir.moneytransfer.servlet.AppServletModule.AccountsParameters.*;
import static com.github.fertkir.moneytransfer.servlet.AppServletModule.CONTENT_TYPE;

@Singleton
public class AccountsTransferServlet extends HttpServlet {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    @Inject
    public AccountsTransferServlet(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        long accountFrom = Long.parseLong(req.getParameter(FROM));
        long accountTo = Long.parseLong(req.getParameter(TO));
        BigDecimal amount = new BigDecimal(req.getParameter(AMOUNT));

        TransferResult transferResult = accountService.transfer(accountFrom, accountTo, amount);

        resp.setContentType(CONTENT_TYPE);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(objectMapper.writeValueAsString(transferResult));
    }
}
