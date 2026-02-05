package simple.orm.jdbc.impl.query;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.NamedParametersMap;
import simple.orm.jdbc.query.NamedQuery;
import simple.orm.jdbc.query.QueryType;

/**
 * {@link NamedQuery} implementation.
 */
public class NamedQueryImpl<I, O> extends BaseQueryImpl implements NamedQuery<I, O> {

    private final NamedInjector<I> injector;
    private final NamedExtractor<O> extractor;
    private final NamedParametersMap parametersMap;

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
