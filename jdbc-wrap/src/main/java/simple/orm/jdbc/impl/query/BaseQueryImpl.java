package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

/**
 * Default {@link Query} implementation.
 */
public non-sealed class BaseQueryImpl implements Query {

    private final QueryType queryType;
    private final String sql;
    private final int queryTimeoutSec;

    public BaseQueryImpl(QueryType queryType, String sql, int queryTimeoutSec) {
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
