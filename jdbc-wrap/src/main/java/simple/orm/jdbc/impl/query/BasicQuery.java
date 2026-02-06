package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * Basic {@link Query} implementation. Representing a query without parameters or result in a form of {@link ResultSet}.
 */
public class BasicQuery<P, R> implements Query<P, R> {

    private final QueryType queryType;
    private final String sql;
    private final int queryTimeoutSec;

    public BasicQuery(QueryType queryType, String sql, int queryTimeoutSec) {
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
