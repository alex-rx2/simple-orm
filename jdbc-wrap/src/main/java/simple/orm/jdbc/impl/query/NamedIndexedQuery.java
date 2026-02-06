package simple.orm.jdbc.impl.query;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.HasIndexedExtractor;
import simple.orm.jdbc.query.HasNamedInjector;
import simple.orm.jdbc.query.NamedParametersMap;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.sql.ResultSet;

/**
 * A {@link Query} with parameters inserted by {@link NamedInjector}
 * and {@link ResultSet} rows extracted by {@link IndexedExtractor}.
 *
 * @param <P> type of object, which properties are used as query parameters.
 */
public class NamedIndexedQuery<P> extends BasicQuery<P, Seq<Object>>
        implements HasNamedInjector<P>, HasIndexedExtractor {

    private final NamedInjector<P> injector;
    private final NamedParametersMap parametersMap;
    private final IndexedExtractor extractor;

    public NamedIndexedQuery(QueryType queryType, String sql, int queryTimeoutSec,
                             NamedInjector<P> injector, NamedParametersMap parametersMap, IndexedExtractor extractor) {
        super(queryType, sql, queryTimeoutSec);
        this.injector = injector;
        this.parametersMap = parametersMap;
        this.extractor = extractor;
    }

    @Override
    public NamedInjector<P> getInjector() {
        return injector;
    }

    @Override
    public NamedParametersMap getParametersMap() {
        return parametersMap;
    }

    @Override
    public IndexedExtractor getExtractor() {
        return extractor;
    }

}
