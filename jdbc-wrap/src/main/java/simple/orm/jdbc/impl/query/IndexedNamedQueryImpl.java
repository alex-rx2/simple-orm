package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.query.IndexedNamedQuery;
import simple.orm.jdbc.query.QueryType;

/**
 * {@link IndexedNamedQuery} implementation.
 */
public class IndexedNamedQueryImpl<O> extends BaseQueryImpl implements IndexedNamedQuery<O> {

    private final IndexedInjector injector;
    private final NamedExtractor<O> extractor;

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
