package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.NamedInjector;
import simple.orm.jdbc.map.out.IndexedExtractor;

/**
 * NamedIndexedQuery implementation.
 */
class NamedIndexedQueryImpl<I> extends BaseQueryImpl implements NamedIndexedQuery<I> {

    protected final NamedInjector<I> injector;
    protected final IndexedExtractor extractor;
    protected final NamedParametersMap parametersMap;

    public NamedIndexedQueryImpl(QueryType queryType, String sql, int queryTimeoutSec,
                                 NamedInjector<I> injector, IndexedExtractor extractor, NamedParametersMap parametersMap) {
        // validation is supposed to be in QueryFactory
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
        this.extractor = extractor;
        this.parametersMap = parametersMap;
    }

    @Override
    public NamedInjector<I> getInjector() {
        return injector;
    }

    @Override
    public IndexedExtractor getExtractor() {
        return extractor;
    }

    @Override
    public NamedParametersMap getParametersMap() {
        return parametersMap;
    }

}
