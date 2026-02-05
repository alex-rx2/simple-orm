package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.NamedIndexedQuery;
import simple.orm.jdbc.query.NamedParametersMap;
import simple.orm.jdbc.query.QueryType;

/**
 * {@link NamedIndexedQuery} implementation.
 */
public class NamedIndexedQueryImpl<I> extends BaseQueryImpl implements NamedIndexedQuery<I> {

    private final NamedInjector<I> injector;
    private final IndexedExtractor extractor;
    private final NamedParametersMap parametersMap;

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
