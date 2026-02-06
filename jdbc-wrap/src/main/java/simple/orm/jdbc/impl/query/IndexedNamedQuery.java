package simple.orm.jdbc.impl.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.query.HasIndexedInjector;
import simple.orm.jdbc.query.HasNamedExtractor;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link IndexedInjector}
 * and {@link ResultSet} rows extracted by {@link NamedExtractor}.
 *
 * @param <R> type of java object extracted from ResultSet row.
 */
public class IndexedNamedQuery<R> extends BasicQuery<Seq<Object>, R>
        implements HasIndexedInjector, HasNamedExtractor<R> {

    private final IndexedInjector injector;
    private final NamedExtractor<R> extractor;

    public IndexedNamedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                             IndexedInjector injector, NamedExtractor<R> extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
        this.extractor = extractor;
    }

    @Override
    public IndexedInjector getInjector() {
        return injector;
    }

    @Override
    public NamedExtractor<R> getExtractor() {
        return extractor;
    }

}
