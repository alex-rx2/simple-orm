package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.IndexedInjector;
import simple.orm.jdbc.map.in.IndexedExtractor;

/**
 * Query with in and out parameters represented as sequence of objects. With said parameters mapped by index.
 */
// TODO stub interface, make a proper class
public interface IndexedQuery extends Query {

    IndexedInjector getInjector();

    IndexedExtractor getExtractor();

}
