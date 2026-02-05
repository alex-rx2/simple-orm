package simple.orm.jdbc.query;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;

/**
 * Query with in and out parameters represented as java object properties. With said parameters mapped by properties names.
 *
 * @param <I> type of object, which properties are used as query parameters.
 * @param <O> type of java object extracted from ResultSet row.
 */
public non-sealed interface NamedQuery<I, O> extends Query {

    /**
     * Returns parameters injector. <code>Null</code> should be returned if there are no parameters in the query.
     *
     * @return parameters injector.
     */
    NamedInjector<I> getInjector();

    /**
     * Returns parameters extractor. <code>Null</code> should be returned if query execution return no ResultSet.
     *
     * @return parameters extractor.
     */
    NamedExtractor<O> getExtractor();

    /**
     * Returns mapping of query parameters (name->index) for injector.
     *
     * @return mapping of query parameters.
     */
    NamedParametersMap getParametersMap();

}
