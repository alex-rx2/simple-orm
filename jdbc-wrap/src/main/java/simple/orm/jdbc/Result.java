package simple.orm.jdbc;

import io.vavr.collection.Seq;
import simple.orm.jdbc.exc.JdbcException;

import java.sql.SQLException;

/**
 * ResultSet wrap, providing functionality to traverse through rows, or extract all of them at once.
 * <br><br>
 * Result class prefetches one row from ResultSet on {@link #hasNextRow()} execution (if there is no prefetched row yet).
 * Prefetched row is returned on {@link #nextRow()} call then.
 * <br><br>
 * By default, Result is in mode when it is closed automatically when last row extracted or new query execution requested in Connection.
 * This does not provide any guarantees, and each Result should be used in try-with-resources block.
 * Disabling default behaviour may be used to execute additional queries through same connection (and new JDBC Statement obviously) if needed.
 */
public interface Result<T> extends AutoCloseable {

    /**
     * Returns <code>true</code> if ResultSet should be automatically closed on last row extraction or new query execution in Connection.
     *
     * @return <code>true</code> if ResultSet should be automatically closed on last row extraction or new query execution in Connection.
     */
    boolean shouldBeClosedAutomatically();

    /**
     * Sets whether this Result should be closed automatically on conditions as in {@link #shouldBeClosedAutomatically()}.
     *
     * @param shouldBeClosedAutomatically expected behaviour.
     */
    void setShouldBeClosedAutomatically(boolean shouldBeClosedAutomatically);

    /**
     * Returns <code>true</code> if underlying ResultSet is closed.
     *
     * @return <code>true</code> if underlying ResultSet is closed.
     */
    boolean isClosed();

    /**
     * Close underlying ResultSet.
     *
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    void close();

    /**
     * Returns <code>true</code> if more rows left in ResultSet.
     *
     * @return <code>true</code> if more rows left in ResultSet
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    boolean hasNextRow();

    /**
     * Returns next row of data.
     *
     * @return next row on data.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    T nextRow();

    /**
     * Traverses all rows in ResultSet and collects them into sequence.
     *
     * @return sequence of all rows from ResultSet.
     * @throws JdbcException a wrap around {@link SQLException}.
     */
    Seq<T> extractAll();

    /**
     * If ResultSet has exactly one row - extracts and returns said row.
     * Throws IllegalStateException otherwise.
     *
     * @return sequence of all rows from ResultSet.
     * @throws JdbcException         a wrap around {@link SQLException}.
     * @throws IllegalStateException if ResultSet has no or more than one row.
     */
    T exactlySingleRow();
}
