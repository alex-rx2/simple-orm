package simple.orm.jdbc.query;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;

/**
 * Query with in and out parameters represented as sequence of objects. With said parameters mapped by index.
 */
public non-sealed interface IndexedQuery extends Query {

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
    IndexedExtractor getExtractor();

}
