package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.HasNamedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link NamedInjector} and no {@link ResultSet} result (DDL or DML query).
 *
 * @param <P> type of object, which properties are used as query parameters.
 * @param <T> type of query execution result ({@link Void} for DDL, {@link Integer} for DML are expected to be used).
 */
public class NamedParamsNoResultSetQuery<P, T> extends BasicQuery<P, T> implements HasNamedInjector<P> {

    private final NamedInjector<P> injector;

    public NamedParamsNoResultSetQuery(QueryType queryType, String sql, int queryTimeoutSec, NamedInjector<P> injector) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
    }

    @Override
    public NamedInjector<P> getInjector() {
        return injector;
    }

}
