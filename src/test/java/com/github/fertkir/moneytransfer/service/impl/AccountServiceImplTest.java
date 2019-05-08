package com.github.fertkir.moneytransfer.service.impl;

import com.github.fertkir.moneytransfer.dao.AccountDao;
import com.github.fertkir.moneytransfer.entity.Account;
import com.github.fertkir.moneytransfer.entity.TransferResult;
import com.github.fertkir.moneytransfer.persistence.TransactionTemplate;
import com.github.fertkir.moneytransfer.service.exception.AccountingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @Mock
    private AccountDao accountDao;
    @Mock
    private TransactionTemplate transactionTemplate;
    @InjectMocks
    private AccountServiceImpl accountService;

    @Captor
    private ArgumentCaptor<Supplier<Account>> accountCaptor;
    @Captor
    private ArgumentCaptor<Supplier<List<Account>>> accountListCaptor;
    @Captor
    private ArgumentCaptor<Supplier<TransferResult>> transferResultCaptor;

    @Before
    public void setUp() {
        when(transactionTemplate.execute(Mockito.<Supplier>any())).thenAnswer((Answer) invocation -> {
            Object[] args = invocation.getArguments();
            Supplier arg = (Supplier) args[0];
            return arg.get();
        });
    }

    @Test
    public void list() {
        // given
        List<Account> mockAccounts = singletonList(mock(Account.class));
        when(accountDao.findAll()).thenReturn(mockAccounts);

        // when
        List<Account> actualAccounts = accountService.list();

        // then
        verify(accountDao).findAll();
        verify(transactionTemplate).execute(accountListCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        assertThat(accountListCaptor.getValue().get()).isEqualTo(mockAccounts);
        assertThat(actualAccounts).isEqualTo(mockAccounts);
    }

    @Test
    public void getById() {
        // given
        long id = 1;
        Account mockAccount = mock(Account.class);
        when(accountDao.getById(id)).thenReturn(mockAccount);

        // when
        Account actualAccount = accountService.getById(id);

        // then
        verify(accountDao).getById(1);
        verify(transactionTemplate).execute(accountCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        assertThat(accountCaptor.getValue().get()).isEqualTo(mockAccount);
        assertThat(actualAccount).isEqualTo(mockAccount);
    }

    @Test
    public void createNew() {
        // given
        Account mockAccount = mock(Account.class);
        when(accountDao.create(any(Account.class))).thenReturn(mockAccount);

        // when
        Account actualAccount = accountService.createNew();

        // then
        Account passedAccount = Account.builder()
                .balance(BigDecimal.ZERO)
                .build();
        verify(accountDao).create(passedAccount);
        verify(transactionTemplate).execute(accountCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        assertThat(accountCaptor.getValue().get()).isEqualTo(mockAccount);
        assertThat(actualAccount).isEqualTo(mockAccount);
    }

    @Test
    public void topUp() {
        // given
        long accountId = 1;
        BigDecimal amount = BigDecimal.valueOf(100);

        Account initialAccount = Account.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(50))
                .build();
        when(accountDao.getById(accountId)).thenReturn(initialAccount);

        Account returnMock = mock(Account.class);
        when(accountDao.update(any(Account.class))).thenReturn(returnMock);

        // when
        Account actualAccount = accountService.topUp(accountId, amount);

        // then
        Account passedAccount = Account.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(150))
                .build();
        verify(accountDao).getById(accountId);
        verify(accountDao).update(passedAccount);
        verify(transactionTemplate).execute(accountCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        assertThat(accountCaptor.getValue().get()).isEqualTo(returnMock);
        assertThat(actualAccount).isEqualTo(returnMock);
    }

    @Test
    public void withdraw() {
        // given
        long accountId = 1;
        BigDecimal amount = BigDecimal.valueOf(30);

        Account initialAccount = Account.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(100))
                .build();
        when(accountDao.getById(accountId)).thenReturn(initialAccount);

        Account returnMock = mock(Account.class);
        when(accountDao.update(any(Account.class))).thenReturn(returnMock);

        // when
        Account actualAccount = accountService.withdraw(accountId, amount);

        // then
        Account passedAccount = Account.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(70))
                .build();
        verify(accountDao).getById(accountId);
        verify(accountDao).update(passedAccount);
        verify(transactionTemplate).execute(accountCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        assertThat(accountCaptor.getValue().get()).isEqualTo(returnMock);
        assertThat(actualAccount).isEqualTo(returnMock);
    }

    @Test
    public void withdrawNotEnoughMoney() {
        // given
        long accountId = 1;
        BigDecimal amount = BigDecimal.valueOf(100);

        Account initialAccount = Account.builder()
                .id(accountId)
                .balance(BigDecimal.valueOf(50))
                .build();
        when(accountDao.getById(accountId)).thenReturn(initialAccount);

        try {
            // when
            accountService.withdraw(accountId, amount);
            fail("exception should have been thrown");
        } catch (AccountingException e) {
            // then
            verify(accountDao).getById(accountId);
            verify(transactionTemplate).execute(accountCaptor.capture());
            verifyNoMoreInteractions(accountDao, transactionTemplate);

            assertThat(e.getMessage()).isEqualTo("Cannot withdraw 100. Not enough money");
        }
    }

    @Test
    public void transfer() {
        // given
        long accountFrom = 1;
        long accountTo = 2;
        BigDecimal amount = BigDecimal.valueOf(30);

        Account initialAccountFrom = Account.builder()
                .id(accountFrom)
                .balance(BigDecimal.valueOf(100))
                .build();
        when(accountDao.getById(accountFrom)).thenReturn(initialAccountFrom);
        Account initialAccountTo = Account.builder()
                .id(accountTo)
                .balance(BigDecimal.valueOf(200))
                .build();
        when(accountDao.getById(accountTo)).thenReturn(initialAccountTo);

        Account passedAccountFrom = Account.builder()
                .id(accountFrom)
                .balance(BigDecimal.valueOf(70))
                .build();
        Account passedAccountTo = Account.builder()
                .id(accountTo)
                .balance(BigDecimal.valueOf(230))
                .build();
        when(accountDao.update(passedAccountFrom)).thenReturn(passedAccountFrom);
        when(accountDao.update(passedAccountTo)).thenReturn(passedAccountTo);

        // when
        TransferResult transferResult = accountService.transfer(accountFrom, accountTo, amount);

        // then
        verify(accountDao).getById(accountFrom);
        verify(accountDao).getById(accountTo);
        verify(accountDao).update(passedAccountFrom);
        verify(accountDao).update(passedAccountTo);
        verify(transactionTemplate).execute(transferResultCaptor.capture());
        verifyNoMoreInteractions(accountDao, transactionTemplate);

        TransferResult expectedTransferResult = TransferResult.builder()
                .source(passedAccountFrom)
                .target(passedAccountTo)
                .build();

        assertThat(transferResultCaptor.getValue().get()).isEqualTo(expectedTransferResult);
        assertThat(transferResult).isEqualTo(expectedTransferResult);
    }

    @Test
    public void transferNotEnoughMoney() {
        // given
        long accountFrom = 1;
        long accountTo = 2;
        BigDecimal amount = BigDecimal.valueOf(30);

        Account initialAccountFrom = Account.builder()
                .id(accountFrom)
                .balance(BigDecimal.valueOf(20))
                .build();
        when(accountDao.getById(accountFrom)).thenReturn(initialAccountFrom);
        Account initialAccountTo = Account.builder()
                .id(accountTo)
                .balance(BigDecimal.valueOf(200))
                .build();
        when(accountDao.getById(accountTo)).thenReturn(initialAccountTo);

        // when
        try {
            accountService.transfer(accountFrom, accountTo, amount);
            fail("exception should have been thrown");
        } catch (AccountingException e) {
            // then
            verify(accountDao).getById(accountFrom);
            verify(accountDao).getById(accountTo);
            verify(transactionTemplate).execute(transferResultCaptor.capture());
            verifyNoMoreInteractions(accountDao, transactionTemplate);

            assertThat(e.getMessage()).isEqualTo("Cannot transfer 30. Not enough money");
        }
    }
}