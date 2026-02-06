package simple.orm.jdbc;

import simple.orm.jdbc.query.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstraction of database connection, able to execute queries and manage transactions.
 */
// TODO transactions management
// TODO api for batch updates
// TODO separate query execution from connection ???
// TODO how should Result object not closed automatically managed (and their Statement/ResultSet)?
// TODO how should Statements be managed? timeouts? pools? (and mind the transactions management)
public interface Connection extends AutoCloseable {

    /**
     * Get connected database.
     *
     * @return database.
     */
    DatabaseAccessPoint getDatabase();

    /**
     * Get underlying JDBC connection.
     *
     * @return JDBC connection.
     */
    java.sql.Connection getJdbcConnection();

    /**
     * Close JDBC {@link ResultSet} and {@link Statement} if any are open.
     *
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void releaseResources();

    /**
     * Close connection.
     *
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void close();

    /**
     * Returns default timeout for query execution.
     * Default timeout is applied if {@link Query} doesn't specify its own proper timeout.
     *
     * @return query timeout in seconds.
     */
    int getDefaultTimeout();

    /**
     * Execute DDL query.
     *
     * @param query DDL query.
     * @throws JdbcException            a wrap around {@link SQLException}.
     * @throws IllegalArgumentException if query fail validation.
     */
    void executeDDLQuery(Query<Void, Void> query);

    /**
     * Execute DML query.
     *
     * @param query  DML query.
     * @param params query parameters, if any.
     * @param <P>    type of object used as source of parameters
     *               (defined by injector, <code>Seq&lt;Object></code> in case of indexed injector).
     * @return execution result (updated row count) as per {@link Statement#executeUpdate(String)}.
     * @throws JdbcException            a wrap around {@link SQLException}.
     * @throws IllegalArgumentException if query or parameters fail validation.
     */
    <P> int executeDMLQuery(Query<P, Integer> query, Object... params);

    /**
     * Execute SELECT query.
     *
     * @param query  SELECT query.
     * @param params query parameters, if any.
     * @param <P>    type of object used as source of parameters
     *               (relative to injector, <code>Seq&lt;Object></code> in case of indexed injector).
     * @param <R>    type of object used to collect each ResultSet row columns
     *               (defined by extractor, <code>Seq&lt;Object></code> in case of indexed extractor).
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException            a wrap around {@link SQLException}.
     * @throws IllegalArgumentException if query or parameters fail validation.
     */
    <P, R> Result<R> executeSelect(Query<P, R> query, Object... params);

    /**
     * Execute any supported query.
     *
     * @param query  a query.
     * @param params query parameters, if any.
     * @param <QP>   type of object used as source of parameters
     *               (defined by injector, <code>Seq&lt;Object></code> in case of indexed injector).
     * @param <QR>   should <code>Void</code> for DDL queries, <code>Integer</code> for DML queries,
     *               type of object used to collect each ResultSet row columns for SELECT query
     *               (defined by extractor, <code>Seq&lt;Object></code> in case of indexed extractor).
     * @param <ER>   actual result of this method execution, either <code>Void</code> for DDL queries,
     *               <code>Integer</code> for DML queries or <code>Result&lt;QR></code> for SELECT queries.
     * @return result of query execution (see execute method for corresponding query type).
     * @throws JdbcException            a wrap around {@link SQLException}.
     * @throws IllegalArgumentException if query or parameters fail validation.
     */
    <QP, QR, ER> ER executeAnyQuery(Query<QP, QR> query, Object... params);

}
