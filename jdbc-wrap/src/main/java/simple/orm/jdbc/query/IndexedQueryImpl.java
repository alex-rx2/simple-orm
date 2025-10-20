package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.IndexedInjector;
import simple.orm.jdbc.map.out.IndexedExtractor;

/**
 * {@link IndexedQuery} implementation.
 */
class IndexedQueryImpl extends BaseQueryImpl implements IndexedQuery {

    protected final IndexedInjector injector;
    protected final IndexedExtractor extractor;

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
