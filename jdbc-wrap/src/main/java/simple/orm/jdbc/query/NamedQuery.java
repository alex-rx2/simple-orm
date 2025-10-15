package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.NamedInjector;
import simple.orm.jdbc.map.out.NamedExtractor;

/**
 * Query with in and out parameters represented as java object properties. With said parameters mapped by properties names.
 *
 * @param <I> type of object, which properties are used as query parameters.
 * @param <O> type of java object extracted from ResultSet row.
 */
public interface NamedQuery<I, O> extends Query {

    NamedInjector<I> getInjector();

    NamedExtractor<O> getExtractor();

    NamedParametersMap getParametersMap();

}
