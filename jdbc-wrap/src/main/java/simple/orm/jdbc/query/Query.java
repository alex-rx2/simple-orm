package simple.orm.jdbc.query;

import simple.orm.jdbc.Connection;

/**
 * SQL Query.
 * <br>
 * Depending on query it should implement corresponding {@link HasIndexedInjector}, {@link HasNamedInjector},
 * {@link HasIndexedExtractor}, {@link HasNamedExtractor} interfaces to be correctly processed.
 *
 * @param <P> type of query parameters;
 *            if query has no parameters <code>Void</code> is expected to be used;
 *            if parameters are injected by index as list of objects then <code>Seq&lt;Object></code> is expected to be used;
 *            if parameters are injected as properties of object by name then class of that object is expected to be used.
 * @param <R> type of query execution result;
 *            for DDL,DML queries this is expected to be <code>Void</code> and <code>Integer</code> respectively;
 *            for SELECT queries this is expected to be the class representing one row of <code>ResultSet</code>.
 */
public interface Query<P, R> {

    /**
     * Returns type of the query.
     *
     * @return type of the query.
     */
    QueryType getType();

    /**
     * Returns SQL query itself.
     *
     * @return SQL query.
     */
    String getSQLQuery();

    /**
     * Desired query timeout in seconds,
     * 0 means no timeout,
     * negative values mean {@link Connection#getDefaultTimeout()} should be used.
     *
     * @return query timeout in seconds.
     */
    int getQueryTimeout();

}
