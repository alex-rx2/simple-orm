package simple.orm.jdbc;

import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.Query;

import java.sql.SQLException;

/**
 * Abstraction of database connection, able to execute queries and manage transactions.
 */
// TODO transactions management
// TODO api for batch updates
// TODO separate query execution from connection ???
// TODO default query timeout in connection?
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
     * Close connection.
     *
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void close();

    /**
     * Execute DDL query.
     *
     * @param query DDL query.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void executeDDLUpdate(Query query);

    /**
     * Execute DML query having no parameters.
     *
     * @param query DML query.
     * @return execution result (updated row count) as per {@link java.sql.Statement#executeUpdate(String)}.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    int executeDMLUpdate(Query query);

    /**
     * Execute DML query with parameters provided as array of object, injected by index.
     *
     * @param query  DML query.
     * @param params query parameters.
     * @return execution result (updated row count) as per {@link java.sql.Statement#executeUpdate(String)}.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    int executeDMLUpdate(IndexedQuery query, Object... params);

    /**
     * Execute DML query with parameters provided as sequence of object, injected by index.
     *
     * @param query  DML query.
     * @param params query parameters.
     * @return execution result (updated row count) as per {@link java.sql.Statement#executeUpdate(String)}.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    int executeDMLUpdate(IndexedQuery query, Seq<Object> params);

    /**
     * Execute DML query with parameters provided as single object, injected as named properties of said object.
     *
     * @param query DML query.
     * @param input object which properties used as parameters for a query.
     * @param <I>   type of object used as source of parameters.
     * @return execution result (updated row count) as per {@link java.sql.Statement#executeUpdate(String)}.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <I> int executeDMLUpdate(NamedQuery<I, Void> query, I input);

    /**
     * Execute SELECT query with parameters provided as array of object, injected by index.
     * Output ResultSet rows are extracted into a sequence of objects each.
     *
     * @param query  SELECT query.
     * @param params query parameters.
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    Result<Seq<Object>> executeSelect(IndexedQuery query, Object... params);

    /**
     * Execute SELECT query with parameters provided as sequence of object, injected by index.
     * Output ResultSet rows are extracted into a sequence of objects each.
     *
     * @param query  SELECT query.
     * @param params query parameters.
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    Result<Seq<Object>> executeSelect(IndexedQuery query, Seq<Object> params);

    /**
     * Execute SELECT query without parameters.
     * Output ResultSet rows are extracted into a sequence of objects each.
     *
     * @param query SELECT query.
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    Result<Seq<Object>> executeSelect(IndexedQuery query);

    /**
     * Execute SELECT query with parameters provided as single object, injected as named properties of said object.
     * Output ResultSet rows are mapped into object properties.
     *
     * @param query SELECT query.
     * @param input object which properties used as parameters for a query.
     * @param <I>   type of object used as source of parameters.
     * @param <O>   type of object used to collect each ResultSet row parameters.
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <I, O> Result<O> executeSelect(NamedQuery<I, O> query, I input);

    /**
     * Execute SELECT query without parameters.
     * Output ResultSet rows are mapped into object properties.
     *
     * @param query SELECT query.
     * @param <O>   type of object used to collect each ResultSet row parameters.
     * @return {@link Result} object to traverse ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <O> Result<O> executeSelect(NamedQuery<Void, O> query);

}
