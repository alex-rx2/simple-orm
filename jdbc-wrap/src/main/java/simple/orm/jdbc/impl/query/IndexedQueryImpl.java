package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.query.IndexedQuery;
import simple.orm.jdbc.query.QueryType;

/**
 * {@link IndexedQuery} implementation.
 */
public class IndexedQueryImpl extends BaseQueryImpl implements IndexedQuery {

    private final IndexedInjector injector;
    private final IndexedExtractor extractor;

    public IndexedQueryImpl(QueryType queryType, String sql, int queryTimeoutSec,
                            IndexedInjector injector, IndexedExtractor extractor) {
        // validation is supposed to be in QueryFactory
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
