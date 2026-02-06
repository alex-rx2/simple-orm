package simple.orm.jdbc.impl.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.query.HasIndexedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link IndexedInjector} and no {@link ResultSet} result (DDL or DML query).
 *
 * @param <T> type of query execution result ({@link Void} for DDL, {@link Integer} for DML are expected to be used).
 */
public class IndexedParamsNoResultSetQuery<T> extends BasicQuery<Seq<Object>, T> implements HasIndexedInjector {

    private final IndexedInjector injector;

    public IndexedParamsNoResultSetQuery(QueryType queryType, String sql, int queryTimeoutSec,
                                         IndexedInjector injector) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
    }

    @Override
    public IndexedInjector getInjector() {
        return injector;
    }

}
