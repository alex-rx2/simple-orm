package simple.orm.jdbc.query;

import simple.orm.jdbc.Connection;
import simple.orm.jdbc.impl.query.BaseQueryImpl;

/**
 * SQL Query.
 */
public sealed interface Query
        permits IndexedQuery, IndexedNamedQuery, NamedQuery, NamedIndexedQuery, BaseQueryImpl {

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
     *   0 means no timeout,
     *   negative values mean {@link Connection#getDefaultTimeout()} should be used.
     *
     * @return query timeout in seconds.
     */
    int getQueryTimeout();

}
