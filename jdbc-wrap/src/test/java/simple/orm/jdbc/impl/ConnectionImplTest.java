package simple.orm.jdbc.impl;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InOrder;
import simple.orm.jdbc.DatabaseAccessPoint;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.impl.query.BasicQuery;
import simple.orm.jdbc.impl.query.IndexedIndexedQuery;
import simple.orm.jdbc.impl.query.IndexedParamsNoResultSetQuery;
import simple.orm.jdbc.impl.query.NamedNamedQuery;
import simple.orm.jdbc.impl.query.NamedParamsNoResultSetQuery;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.QueryType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests on {@link ConnectionImpl}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConnectionImplTest {

    @Test
    public void testExecuteDDLQuery() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Statement mockStatement = mock();
        // behaviour
        when(mockSQLConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(anyString())).thenReturn(1);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        connection.executeDDLQuery(new BasicQuery<>(QueryType.DDL, "this is SQL query, trust me", 666));
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement);
        inOrder.verify(mockSQLConnection).createStatement();
        inOrder.verify(mockStatement).setQueryTimeout(666);
        inOrder.verify(mockStatement).executeUpdate(eq("this is SQL query, trust me"));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement);
    }

    @Test
    public void testExecuteDMLQuery_indexed_objects() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final IndexedInjector mockIInjector = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(123);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final int result = connection.executeDMLQuery(
                new IndexedParamsNoResultSetQuery<>(QueryType.DML, "still SQL, yes it is", -1, mockIInjector),
                321, "param2", null
        );
        assertThat(result).isEqualTo(123);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockIInjector);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("still SQL, yes it is"));
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockIInjector).injectParameters(same(mockStatement), eq(321), eq("param2"), isNull());
        inOrder.verify(mockStatement).executeUpdate();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockIInjector);
    }

    @Test
    public void testExecuteDMLQuery_indexed_seq() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final IndexedInjector mockIInjector = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(123);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Seq<Object> queryParams = List.of(321, "param2", null);
        final int result = connection.executeDMLQuery(
                new IndexedParamsNoResultSetQuery<>(QueryType.DML, "still SQL, yes it is", -1, mockIInjector),
                queryParams
        );
        assertThat(result).isEqualTo(123);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockIInjector);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("still SQL, yes it is"));
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockIInjector).injectParameters(same(mockStatement), same(queryParams));
        inOrder.verify(mockStatement).executeUpdate();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockIInjector);
    }

    @Test
    public void testExecuteDMLQuery_named() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final NamedInjector<Object> mockNInjector = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(123);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Object queryParams = new Object();
        final int result = connection.executeDMLQuery(
                new NamedParamsNoResultSetQuery<>(QueryType.DML, "still SQL, yes it is", -1, mockNInjector),
                queryParams
        );
        assertThat(result).isEqualTo(123);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockNInjector);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("still SQL, yes it is"));
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockNInjector).injectParameters(same(mockStatement), same(queryParams));
        inOrder.verify(mockStatement).executeUpdate();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockNInjector);
    }

    @Test
    public void testExecuteSelectQuery_named_named() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final java.sql.ResultSet mockResultSet = mock();
        final NamedInjector<Object> mockNInjector = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Result<Object> mockResult = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockRFactory.named(any(), any(), any())).thenReturn(mockResult);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Object queryParams = new Object();
        final Result<Object> result = connection.executeSelect(
                new NamedNamedQuery<>(QueryType.SELECT, "you know, this is SQL, stop staring at me", 0, mockNInjector, mockNExtractor),
                queryParams
        );
        assertThat(result).isSameAs(mockResult);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockNInjector, mockRFactory);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("you know, this is SQL, stop staring at me"));
        inOrder.verify(mockStatement).setQueryTimeout(0);
        inOrder.verify(mockNInjector).injectParameters(same(mockStatement), same(queryParams));
        inOrder.verify(mockStatement).executeQuery();
        inOrder.verify(mockRFactory).named(same(connection), same(mockResultSet), same(mockNExtractor));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockResultSet, mockNInjector, mockNExtractor, mockResult);
    }

    @Test
    public void testExecuteSelectQuery_indexed_indexed() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final java.sql.ResultSet mockResultSet = mock();
        final IndexedInjector mockIInjector = mock();
        final IndexedExtractor mockIExtractor = mock();
        final Result<Seq<Object>> mockResult = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockRFactory.indexed(any(), any(), any())).thenReturn(mockResult);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Result<Seq<Object>> result = connection.executeSelect(
                new IndexedIndexedQuery(QueryType.SELECT, "you know, this is SQL, stop staring at me", 0, mockIInjector, mockIExtractor),
                123, "param2", null
        );
        assertThat(result).isSameAs(mockResult);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockIInjector, mockRFactory);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("you know, this is SQL, stop staring at me"));
        inOrder.verify(mockStatement).setQueryTimeout(0);
        inOrder.verify(mockIInjector).injectParameters(same(mockStatement), eq(123), eq("param2"), isNull());
        inOrder.verify(mockStatement).executeQuery();
        inOrder.verify(mockRFactory).indexed(same(connection), same(mockResultSet), same(mockIExtractor));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockResultSet, mockIInjector, mockIExtractor, mockResult);
    }

    @Test
    public void testExecuteAnyQuery_DDL() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Statement mockStatement = mock();
        // behaviour
        when(mockSQLConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(anyString())).thenReturn(1);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        connection.executeAnyQuery(new BasicQuery<>(QueryType.DDL, "this is SQL query, trust me", 666));
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement);
        inOrder.verify(mockSQLConnection).createStatement();
        inOrder.verify(mockStatement).setQueryTimeout(666);
        inOrder.verify(mockStatement).executeUpdate(eq("this is SQL query, trust me"));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement);
    }


    @Test
    public void testExecuteAnyQuery_DML_indexed_seq() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final IndexedInjector mockIInjector = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(123);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Seq<Object> queryParams = List.of(321, "param2", null);
        final int result = connection.executeAnyQuery(
                new IndexedParamsNoResultSetQuery<>(QueryType.DML, "still SQL, yes it is", -1, mockIInjector),
                queryParams
        );
        assertThat(result).isEqualTo(123);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockIInjector);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("still SQL, yes it is"));
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockIInjector).injectParameters(same(mockStatement), same(queryParams));
        inOrder.verify(mockStatement).executeUpdate();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockIInjector);
    }

    @Test
    public void testExecuteAnyQuery_select_named_named() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.PreparedStatement mockStatement = mock();
        final java.sql.ResultSet mockResultSet = mock();
        final NamedInjector<Object> mockNInjector = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Result<Object> mockResult = mock();
        // behaviour
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockRFactory.named(any(), any(), any())).thenReturn(mockResult);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        final Object queryParams = new Object();
        final Result<Object> result = connection.executeAnyQuery(
                new NamedNamedQuery<>(QueryType.SELECT, "you know, this is SQL, stop staring at me", 0, mockNInjector, mockNExtractor),
                queryParams
        );
        assertThat(result).isSameAs(mockResult);
        // verify
        InOrder inOrder = inOrder(mockSQLConnection, mockStatement, mockNInjector, mockRFactory);
        inOrder.verify(mockSQLConnection).prepareStatement(eq("you know, this is SQL, stop staring at me"));
        inOrder.verify(mockStatement).setQueryTimeout(0);
        inOrder.verify(mockNInjector).injectParameters(same(mockStatement), same(queryParams));
        inOrder.verify(mockStatement).executeQuery();
        inOrder.verify(mockRFactory).named(same(connection), same(mockResultSet), same(mockNExtractor));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement, mockResultSet, mockNInjector, mockNExtractor, mockResult);
    }

    @Test
    public void testExecuteSeveralQueries() throws Exception {
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Statement mockStatement = mock();
        final java.sql.PreparedStatement mockPStatementQ3 = mock();
        final java.sql.ResultSet mockResultSetQ3 = mock();
        final java.sql.ResultSet mockResultSetQ3_2 = mock();
        final IndexedInjector mockIInjector = mock();
        final IndexedExtractor mockIExtractor = mock();
        final Result<Seq<Object>> mockResultQ3 = mock();
        final java.sql.PreparedStatement mockPStatementQ4 = mock();
        final java.sql.ResultSet mockResultSetQ4 = mock();
        final NamedInjector<Object> mockNInjector = mock();
        final NamedExtractor<Object> mockNExtractor = mock();
        final Result<Object> mockResultQ4 = mock();

        // behaviour
        when(mockSQLConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(anyString())).thenReturn(1, 2);
        when(mockSQLConnection.prepareStatement(anyString())).thenReturn(mockPStatementQ3, mockPStatementQ4);
        when(mockPStatementQ3.executeQuery()).thenReturn(mockResultSetQ3, mockResultSetQ3_2);
        when(mockPStatementQ4.executeQuery()).thenReturn(mockResultSetQ4);
        when(mockRFactory.indexed(any(), any(), any())).thenReturn(mockResultQ3);
        when(mockRFactory.named(any(), any(), any())).thenReturn(mockResultQ4);

        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        connection.executeAnyQuery(new BasicQuery<>(QueryType.DDL, "first query, DDL", -1));
        final int resultQ2 =
                connection.executeAnyQuery(new BasicQuery<>(QueryType.DML, "second query, DML, statement should be reused", 22));
        assertThat(resultQ2).isEqualTo(2);
        final String thirdQuery = "third query, SELECT, statement closed, new prepared statement created";
        final Result<Seq<Object>> resultQ3 = connection.executeAnyQuery(new IndexedIndexedQuery(
                        QueryType.SELECT, thirdQuery, 33,
                        mockIInjector, mockIExtractor
                ),
                123, "param2", null
        );
        assertThat(resultQ3).isSameAs(mockResultQ3);
        final Result<Seq<Object>> resultQ3_2 = connection.executeAnyQuery(new IndexedIndexedQuery(
                        QueryType.SELECT, thirdQuery, 332,
                        mockIInjector, mockIExtractor
                ),
                321, "param22", "not null"
        ); // check reuse of prepared statement with same query (the SAME query)
        assertThat(resultQ3_2).isSameAs(mockResultQ3);
        final Object paramQ4 = new Object();
        final Result<Seq<Object>> resultQ4 = connection.executeAnyQuery(new NamedNamedQuery<Object, Object>(
                        QueryType.SELECT, "fourth query, new prepared statement required", 444,
                        mockNInjector, mockNExtractor
                ),
                paramQ4
        );
        assertThat(resultQ4).isSameAs(mockResultQ4);
        connection.releaseResources();

        // verify
        InOrder inOrder = inOrder(
                mockSQLConnection, mockRFactory, mockStatement,
                mockPStatementQ3, mockResultSetQ3, mockResultSetQ3_2, mockIInjector,
                mockPStatementQ4, mockResultSetQ4, mockNInjector
        );
        // - first query
        inOrder.verify(mockSQLConnection).createStatement();
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockStatement).executeUpdate(eq("first query, DDL"));
        // - second query
        inOrder.verify(mockStatement).setQueryTimeout(22);
        inOrder.verify(mockStatement).executeUpdate(eq("second query, DML, statement should be reused"));
        // - third query
        inOrder.verify(mockStatement).close();
        inOrder.verify(mockSQLConnection).prepareStatement("third query, SELECT, statement closed, new prepared statement created");
        inOrder.verify(mockPStatementQ3).setQueryTimeout(33);
        inOrder.verify(mockIInjector).injectParameters(same(mockPStatementQ3), eq(123), eq("param2"), isNull());
        inOrder.verify(mockPStatementQ3).executeQuery();
        inOrder.verify(mockRFactory).indexed(same(connection), same(mockResultSetQ3), same(mockIExtractor));
        // - third query again (reuse of prepared statement)
        inOrder.verify(mockResultSetQ3).close();
        inOrder.verify(mockPStatementQ3).clearParameters();
        inOrder.verify(mockPStatementQ3).setQueryTimeout(332);
        inOrder.verify(mockIInjector).injectParameters(same(mockPStatementQ3), eq(321), eq("param22"), eq("not null"));
        inOrder.verify(mockPStatementQ3).executeQuery();
        inOrder.verify(mockRFactory).indexed(same(connection), same(mockResultSetQ3_2), same(mockIExtractor));
        // - fourth query
        inOrder.verify(mockResultSetQ3_2).close();
        inOrder.verify(mockPStatementQ3).close();
        inOrder.verify(mockSQLConnection).prepareStatement("fourth query, new prepared statement required");
        inOrder.verify(mockPStatementQ4).setQueryTimeout(444);
        inOrder.verify(mockNInjector).injectParameters(same(mockPStatementQ4), same(paramQ4));
        inOrder.verify(mockPStatementQ4).executeQuery();
        inOrder.verify(mockRFactory).named(same(connection), same(mockResultSetQ4), same(mockNExtractor));
        // - release resources
        inOrder.verify(mockResultSetQ4).close();
        inOrder.verify(mockPStatementQ4).close();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(
                mockDAP, mockSQLConnection, mockRFactory, mockStatement,
                mockPStatementQ3, mockResultSetQ3, mockResultSetQ3_2, mockResultQ3, mockIInjector, mockIExtractor,
                mockPStatementQ4, mockResultSetQ4, mockNInjector, mockNExtractor
        );
    }

    @Test
    public void testClose() throws Exception{
        // prepare
        final DatabaseAccessPointImpl mockDAP = mock();
        final java.sql.Connection mockSQLConnection = mock();
        final DatabaseAccessPoint.ResultFactory mockRFactory = mock();
        final java.sql.Statement mockStatement = mock();
        // behaviour
        when(mockSQLConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(anyString())).thenReturn(1);
        // test
        final ConnectionImpl connection = new ConnectionImpl(mockDAP, mockSQLConnection, mockRFactory, 333);
        connection.executeAnyQuery(new BasicQuery<>(QueryType.DDL, "just some random query, please, don't try this in production environment!", -1));
        connection.close();
        // verify
        InOrder inOrder = inOrder(mockDAP, mockSQLConnection, mockStatement);
        inOrder.verify(mockSQLConnection).createStatement();
        inOrder.verify(mockStatement).setQueryTimeout(333);
        inOrder.verify(mockStatement).executeUpdate(eq("just some random query, please, don't try this in production environment!"));
        // - note: ConnectionImpl just closes underlying SQL connection, ignoring anything else
        inOrder.verify(mockSQLConnection).close();
        inOrder.verify(mockDAP).removeConnection(same(connection));
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(mockDAP, mockSQLConnection, mockRFactory, mockStatement);
    }

}
