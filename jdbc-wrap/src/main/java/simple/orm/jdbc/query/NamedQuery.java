package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.NamedInjector;
import simple.orm.jdbc.map.in.NamedExtractor;

/**
 * Query with in and out parameters represented as java object properties. With said parameters mapped by properties names.
 */
// TODO stub interface, make a proper class
public interface NamedQuery<I, O> extends Query {

    NamedInjector<I> getInjector();

    NamedExtractor<O> getExtractor();

}
