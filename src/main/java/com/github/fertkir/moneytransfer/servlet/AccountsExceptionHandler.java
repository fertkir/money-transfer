package com.github.fertkir.moneytransfer.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fertkir.moneytransfer.persistence.PersistenceException;
import com.github.fertkir.moneytransfer.service.exception.AccountingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Builder;
import lombok.Data;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.fertkir.moneytransfer.servlet.AppServletModule.CONTENT_TYPE;

@Singleton
public class AccountsExceptionHandler implements Filter {

    private final ObjectMapper objectMapper;

    @Inject
    public AccountsExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // no implementation
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (AccountingException e) {
            handle(e.getMessage(), response);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof AccountingException) {
                handle(e.getCause().getMessage(), response);
            }
        }
    }

    private void handle(String message, ServletResponse response) throws IOException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType(CONTENT_TYPE);
        resp.setStatus(HttpServletResponse.SC_CONFLICT);
        resp.getWriter().println(objectMapper.writeValueAsString(
                ExceptionResponse.builder()
                        .message(message)
                        .build()));
    }

    @Override
    public void destroy() {
        // no implementation
    }

    @Data
    @Builder
    private static class ExceptionResponse {
        private final String message;
    }
}
