package simple.orm.jdbc;

import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.exc.NamedParamsNotSupportedException;
import simple.orm.jdbc.query.Query;

import java.sql.SQLException;

/**
 * Abstraction of database connection, able to execute queries and manage transactions.
 * <br>
 * TODO transactions management
 * <br>
 * TODO batch processing (special QueryType?)
 */
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
     * Execute query. Without any injected parameters.
     *
     * @param query query to execute.
     * @param <T>   type of result.
     * @return execution result.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <T> T execute(Query<T> query);

    /**
     * Execute query. Parameters are injected by index.
     *
     * @param query  query to execute.
     * @param params parameters to inject into query (by index).
     * @param <T>    type of result.
     * @return execution result.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <T> T execute(Query<T> query, Object... params);

    /**
     * Execute query. Parameters are injected by index.
     *
     * @param query  query to execute.
     * @param params parameters to inject into query (by index).
     * @param <T>    type of result.
     * @return execution result.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    <T> T execute(Query<T> query, Traversable<Object> params);

    /**
     * Execute query. Parameters are injected by name.
     * <p>
     * TODO describe named parameters usage.
     *
     * @param query  query to execute.
     * @param params parameters to inject into query (by name).
     * @param <T>    type of result.
     * @return execution result.
     * @throws JdbcException                    a wrap around {@link SQLException}.
     * @throws NamedParamsNotSupportedException named parameters not supported in provided {@code query}.
     */
    <T> T execute(Query<T> query, Map<String, Object> params) throws NamedParamsNotSupportedException;

}
