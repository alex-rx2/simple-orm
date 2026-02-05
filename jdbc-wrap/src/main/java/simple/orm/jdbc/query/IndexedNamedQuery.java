package simple.orm.jdbc.query;

import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;

/**
 * Query with query parameters represented as sequence of objects, result rows represented as java object properties.
 *
 * @param <O> type of java object extracted from ResultSet row.
 */
public non-sealed interface IndexedNamedQuery<O> extends Query {

    /**
     * Returns parameters injector. <code>Null</code> should be returned if there are no parameters in the query.
     *
     * @return parameters injector.
     */
    IndexedInjector getInjector();

    /**
     * Returns parameters extractor. <code>Null</code> should be returned if query execution return no ResultSet.
     *
     * @return parameters extractor.
     */
    NamedExtractor<O> getExtractor();

}
