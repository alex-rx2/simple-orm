package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.NamedInjector;
import simple.orm.jdbc.map.out.IndexedExtractor;

/**
 * Query with query parameters represented as java object properties, result rows represented as sequence of java objects.
 *
 * @param <I> type of object, which properties are used as query parameters.
 */
public interface NamedIndexedQuery<I> extends Query {

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
    IndexedExtractor getExtractor();

    /**
     * Returns mapping of query parameters (name->index) for injector.
     *
     * @return mapping of query parameters.
     */
    NamedParametersMap getParametersMap();

}
