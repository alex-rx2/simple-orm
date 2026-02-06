package simple.orm.jdbc.impl.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.query.HasIndexedExtractor;
import simple.orm.jdbc.query.HasIndexedInjector;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link IndexedInjector}
 * and {@link ResultSet} rows extracted by {@link IndexedExtractor}.
 */
public class IndexedIndexedQuery extends BasicQuery<Seq<Object>, Seq<Object>>
        implements HasIndexedInjector, HasIndexedExtractor {

    private final IndexedInjector injector;
    private final IndexedExtractor extractor;

    public IndexedIndexedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                               IndexedInjector injector, IndexedExtractor extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
        this.extractor = extractor;
    }

    @Override
    public IndexedInjector getInjector() {
        return injector;
    }

    @Override
    public IndexedExtractor getExtractor() {
        return extractor;
    }

}
