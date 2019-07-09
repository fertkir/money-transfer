package com.github.fertkir.moneytransfer.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionTemplateTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private ConnectionKeeper connectionKeeper;
    @InjectMocks
    private TransactionTemplate transactionTemplate;

    @Mock
    private Supplier<Integer> supplier;

    @Test
    public void shouldHandleSuccessfulTransactionCorrectly() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        transactionTemplate.execute(supplier);

        // then
        InOrder inOrder = inOrder(connection, supplier, connectionKeeper);
        inOrder.verify(connectionKeeper).set(connection);
        inOrder.verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(supplier).get();
        inOrder.verify(connection).commit();
        inOrder.verify(connectionKeeper).remove();
        inOrder.verify(connection).close();
        verifyNoMoreInteractions(connection, supplier, connectionKeeper);
    }

    @Test
    public void shouldHandleUnsuccessfulTransactionCorrectly() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        RuntimeException exception = new RuntimeException();
        when(supplier.get()).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> transactionTemplate.execute(supplier));

        // then
        InOrder inOrder = inOrder(connection, supplier, connectionKeeper);
        inOrder.verify(connectionKeeper).set(connection);
        inOrder.verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(supplier).get();
        inOrder.verify(connection).rollback();
        inOrder.verify(connectionKeeper).remove();
        inOrder.verify(connection).close();
        verifyNoMoreInteractions(connection, supplier, connectionKeeper);

        assertThat(thrown).hasCause(exception);
    }

    @Test
    public void shouldHandleExceptionIfCouldNotGetConnection() throws SQLException {
        // given
        SQLException exception = new SQLException();
        when(dataSource.getConnection()).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> transactionTemplate.execute(supplier));

        // then
        verifyNoMoreInteractions(supplier, connectionKeeper);
        assertThat(thrown).hasCause(exception);
    }
}