package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.HasNamedExtractor;
import simple.orm.jdbc.query.HasNamedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link NamedInjector}
 * and {@link ResultSet} rows extracted by {@link NamedExtractor}.
 *
 * @param <P> type of object, which properties are used as query parameters.
 * @param <R> type of java object extracted from ResultSet row.
 */
public class NamedNamedQuery<P, R> extends BasicQuery<P, R>
        implements HasNamedInjector<P>, HasNamedExtractor<R> {

    private final NamedInjector<P> injector;
    private final NamedExtractor<R> extractor;

    public NamedNamedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                           NamedInjector<P> injector, NamedExtractor<R> extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
        this.extractor = extractor;
    }

    @Override
    public NamedInjector<P> getInjector() {
        return injector;
    }

    @Override
    public NamedExtractor<R> getExtractor() {
        return extractor;
    }

}
