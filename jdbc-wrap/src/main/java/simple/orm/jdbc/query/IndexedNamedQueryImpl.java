package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.IndexedInjector;
import simple.orm.jdbc.map.out.NamedExtractor;

/**
 * IndexedNamedQuery implementation.
 */
class IndexedNamedQueryImpl<O> extends BaseQueryImpl implements IndexedNamedQuery<O> {

    protected final IndexedInjector injector;
    protected final NamedExtractor<O> extractor;

    public IndexedNamedQueryImpl(QueryType queryType, String sql, int queryTimeoutSec,
                                 IndexedInjector injector, NamedExtractor<O> extractor) {
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
    public NamedExtractor<O> getExtractor() {
        return extractor;
    }

}
