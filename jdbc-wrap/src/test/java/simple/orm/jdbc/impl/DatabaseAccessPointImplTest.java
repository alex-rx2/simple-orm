package simple.orm.jdbc.impl;

import io.vavr.collection.HashSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.jdbc.Connection;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.DatabaseClosedException;
import simple.orm.jdbc.FakeDriver;
import simple.orm.jdbc.JdbcException;
import simple.orm.util.Mutable;

import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link DatabaseAccessPointImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseAccessPointImplTest {

    @BeforeEach
    public void setUp() {
        deregisterFakeDrivers();
    }

    @AfterAll
    public static void afterAll() {
        deregisterFakeDrivers();
    }

    private static void deregisterFakeDrivers() {
        DriverManager.drivers()
                .filter(d -> d instanceof FakeDriver)
                .forEach(d -> {
                    try {
                        DriverManager.deregisterDriver(d);
                    } catch (SQLException e) {
                        throw new JdbcException(e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void assertConnections(DatabaseAccessPointImpl dap, Connection... connections) throws NoSuchFieldException, IllegalAccessException {
        Field field = dap.getClass().getDeclaredField("connections");
        field.trySetAccessible();
        Mutable<HashSet<Connection>> dapConnections = (Mutable<HashSet<Connection>>) field.get(dap);
        assertThat(dapConnections.get()).containsExactlyInAnyOrder(connections);
    }

    @Test
    public void testConnect() throws Exception {
        // prepare
        final FakeDriver mockDriver = mock();
        DriverManager.registerDriver(mockDriver);
        final DatabaseAccessPoint.ConnectionFactory mockCFactory = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final Connection mockConnection = mock();
        // behaviour
        when(mockDriver.connect(anyString(), any())).thenReturn(mockSQLConnection);
        when(mockCFactory.connection(any(), any(), any(), anyInt())).thenReturn(mockConnection);
        // test
        final Properties connProps = new Properties();
        final DatabaseAccessPointImpl dap =
                new DatabaseAccessPointImpl(FakeDriver.class, "blah blah blah", connProps, mockCFactory, mockRFactory);
        assertThat(dap.getDriverClass()).isEqualTo(FakeDriver.class);
        assertThat(dap.getConnectionURL()).isEqualTo("blah blah blah");
        assertThat(dap.getConnectionProperties()).isSameAs(connProps);
        assertThat(dap.isClosed()).isFalse();
        final Connection connection = dap.connect();
        assertThat(connection).isSameAs(mockConnection);
        assertConnections(dap, connection);
        // verify mocks
        verify(mockDriver).connect(eq("blah blah blah"), same(connProps));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection), same(mockRFactory), eq(0));
        verifyNoMoreInteractions(mockDriver, mockCFactory, mockRFactory, mockSQLConnection, mockConnection);
    }

    @Test
    public void testConnectWithTimeout() throws Exception {
        // prepare
        final FakeDriver mockDriver = mock();
        DriverManager.registerDriver(mockDriver);
        final DatabaseAccessPoint.ConnectionFactory mockCFactory = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final Connection mockConnection = mock();
        // behaviour
        when(mockDriver.connect(anyString(), any())).thenReturn(mockSQLConnection);
        when(mockCFactory.connection(any(), any(), any(), anyInt())).thenReturn(mockConnection);
        // test
        final Properties connProps = new Properties();
        final DatabaseAccessPointImpl dap =
                new DatabaseAccessPointImpl(FakeDriver.class, "blah blah blah", connProps, mockCFactory, mockRFactory);
        assertThat(dap.getDriverClass()).isEqualTo(FakeDriver.class);
        assertThat(dap.getConnectionURL()).isEqualTo("blah blah blah");
        assertThat(dap.getConnectionProperties()).isSameAs(connProps);
        assertThat(dap.isClosed()).isFalse();
        final Connection connection = dap.connect(123123);
        assertThat(connection).isSameAs(mockConnection);
        assertConnections(dap, connection);
        // verify mocks
        verify(mockDriver).connect(eq("blah blah blah"), same(connProps));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection), same(mockRFactory), eq(123123));
        verifyNoMoreInteractions(mockDriver, mockCFactory, mockRFactory, mockSQLConnection, mockConnection);
    }

    @Test
    public void testRemoveConnection() throws Exception {
        // prepare
        final FakeDriver mockDriver = mock();
        DriverManager.registerDriver(mockDriver);
        final DatabaseAccessPoint.ConnectionFactory mockCFactory = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Connection mockSQLConnection1 = mock();
        final java.sql.Connection mockSQLConnection2 = mock();
        final Connection mockConnection1 = mock();
        final Connection mockConnection2 = mock();
        // behaviour
        when(mockDriver.connect(anyString(), any())).thenReturn(mockSQLConnection1, mockSQLConnection2);
        when(mockCFactory.connection(any(), same(mockSQLConnection1), any(), anyInt())).thenReturn(mockConnection1);
        when(mockCFactory.connection(any(), same(mockSQLConnection2), any(), anyInt())).thenReturn(mockConnection2);
        // test
        final Properties connProps = new Properties();
        final DatabaseAccessPointImpl dap =
                new DatabaseAccessPointImpl(FakeDriver.class, "blah blah blah", connProps, mockCFactory, mockRFactory);
        final Connection connection1 = dap.connect();
        assertThat(connection1).isSameAs(mockConnection1);
        assertConnections(dap, connection1);
        final Connection connection2 = dap.connect();
        assertThat(connection2).isSameAs(mockConnection2);
        assertConnections(dap, connection1, connection2);
        dap.removeConnection(connection1);
        assertConnections(dap, connection2);
        dap.removeConnection(connection1);
        assertConnections(dap, connection2);
        dap.removeConnection(connection2);
        assertConnections(dap);
        // verify mocks
        verify(mockDriver, times(2)).connect(eq("blah blah blah"), same(connProps));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection1), same(mockRFactory), eq(0));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection2), same(mockRFactory), eq(0));
        verifyNoMoreInteractions(mockDriver, mockCFactory, mockRFactory, mockSQLConnection1, mockSQLConnection2, mockConnection1, mockConnection2);
    }

    @Test
    public void testClose() throws Exception {
        // prepare
        final FakeDriver mockDriver = mock();
        DriverManager.registerDriver(mockDriver);
        final DatabaseAccessPoint.ConnectionFactory mockCFactory = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Connection mockSQLConnection1 = mock();
        final java.sql.Connection mockSQLConnection2 = mock();
        final Connection mockConnection1 = mock();
        final Connection mockConnection2 = mock();
        // behaviour
        when(mockDriver.connect(anyString(), any())).thenReturn(mockSQLConnection1, mockSQLConnection2);
        when(mockCFactory.connection(any(), same(mockSQLConnection1), any(), anyInt())).thenReturn(mockConnection1);
        when(mockCFactory.connection(any(), same(mockSQLConnection2), any(), anyInt())).thenReturn(mockConnection2);
        // test
        final Properties connProps = new Properties();
        final DatabaseAccessPointImpl dap =
                new DatabaseAccessPointImpl(FakeDriver.class, "blah blah blah", connProps, mockCFactory, mockRFactory);
        dap.connect();
        dap.connect();
        dap.close();
        assertThat(dap.isClosed()).isTrue();
        assertConnections(dap);
        assertThatCode(() -> dap.connect()).isInstanceOf(DatabaseClosedException.class);
        assertThatCode(() -> dap.connect(123)).isInstanceOf(DatabaseClosedException.class);
        // verify mocks
        verify(mockDriver, times(2)).connect(eq("blah blah blah"), same(connProps));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection1), same(mockRFactory), eq(0));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection2), same(mockRFactory), eq(0));
        verify(mockConnection1).close();
        verify(mockConnection2).close();
        verifyNoMoreInteractions(mockDriver, mockCFactory, mockRFactory, mockSQLConnection1, mockSQLConnection2, mockConnection1, mockConnection2);
    }

    @Test
    public void testCloseWithExceptions() throws Exception {
        // prepare
        final FakeDriver mockDriver = mock();
        DriverManager.registerDriver(mockDriver);
        final DatabaseAccessPoint.ConnectionFactory mockCFactory = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Connection mockSQLConnection1 = mock();
        final java.sql.Connection mockSQLConnection2 = mock();
        final java.sql.Connection mockSQLConnection3 = mock();
        final Connection mockConnection1 = mock();
        final Connection mockConnection2 = mock();
        final Connection mockConnection3 = mock();
        // behaviour
        when(mockDriver.connect(anyString(), any())).thenReturn(mockSQLConnection1, mockSQLConnection2, mockSQLConnection3);
        when(mockCFactory.connection(any(), same(mockSQLConnection1), any(), anyInt())).thenReturn(mockConnection1);
        when(mockCFactory.connection(any(), same(mockSQLConnection2), any(), anyInt())).thenReturn(mockConnection2);
        when(mockCFactory.connection(any(), same(mockSQLConnection3), any(), anyInt())).thenReturn(mockConnection3);
        doThrow(new JdbcException(new SQLException())).when(mockConnection1).close();
        doThrow(new JdbcException(new SQLException())).when(mockConnection2).close();
        doThrow(new JdbcException(new SQLException())).when(mockConnection3).close();
        // test
        final Properties connProps = new Properties();
        final DatabaseAccessPointImpl dap =
                new DatabaseAccessPointImpl(FakeDriver.class, "blah blah blah", connProps, mockCFactory, mockRFactory);
        dap.connect();
        dap.connect();
        dap.connect();
        assertThatCode(dap::close)
                .isInstanceOf(JdbcException.class)
                .matches(exc -> exc.getSuppressed().length == 2);
        assertThat(dap.isClosed()).isTrue();
        assertConnections(dap);
        // verify mocks
        verify(mockDriver, times(3)).connect(eq("blah blah blah"), same(connProps));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection1), same(mockRFactory), eq(0));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection2), same(mockRFactory), eq(0));
        verify(mockCFactory).connection(same(dap), same(mockSQLConnection3), same(mockRFactory), eq(0));
        verify(mockConnection1).close();
        verify(mockConnection2).close();
        verify(mockConnection3).close();
        verifyNoMoreInteractions(mockDriver, mockCFactory, mockRFactory, mockSQLConnection1, mockSQLConnection2, mockConnection1, mockConnection2);
    }

}
