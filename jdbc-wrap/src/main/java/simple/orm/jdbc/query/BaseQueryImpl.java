package simple.orm.jdbc.query;

/**
 * Default {@link Query} implementation.
 */
class BaseQueryImpl implements Query {
    protected final QueryType queryType;
    protected final String sql;
    protected final int queryTimeoutSec;

    protected BaseQueryImpl(QueryType queryType, String sql, int queryTimeoutSec) {
        // validation is supposed to be in QueryFactory
        this.queryType = queryType;
        this.sql = sql;
        this.queryTimeoutSec = queryTimeoutSec;
    }

    @Override
    public QueryType getType() {
        return queryType;
    }

    @Override
    public String getSQLQuery() {
        return sql;
    }

    @Override
    public int getQueryTimeout() {
        return queryTimeoutSec;
    }
}
