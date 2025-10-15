package simple.orm.jdbc.query;

/**
 * SQL Query.
 */
public interface Query {

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
     * Desired query timeout in seconds, 0 means no timeout.
     *
     * @return query timeout in seconds.
     */
    int getQueryTimeout();

}
