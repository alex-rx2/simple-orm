package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.NamedInjector;
import simple.orm.jdbc.map.out.NamedExtractor;

/**
 * {@link NamedQuery} implementation.
 */
class NamedQueryImpl<I, O> extends BaseQueryImpl implements NamedQuery<I, O> {

    protected final NamedInjector<I> injector;
    protected final NamedExtractor<O> extractor;
    protected final NamedParametersMap parametersMap;

    public NamedQueryImpl(QueryType queryType, String sql, int queryTimeoutSec,
                          NamedInjector<I> injector, NamedExtractor<O> extractor, NamedParametersMap parametersMap) {
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
    public NamedExtractor<O> getExtractor() {
        return extractor;
    }

    @Override
    public NamedParametersMap getParametersMap() {
        return parametersMap;
    }

}
