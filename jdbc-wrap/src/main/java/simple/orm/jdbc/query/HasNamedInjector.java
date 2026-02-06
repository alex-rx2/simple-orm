package simple.orm.jdbc.query;

import simple.orm.jdbc.map.NamedInjector;

/**
 * Interface-provider of named injector for a query.
 * Such a query has parameters presented as properties of a java object.
 *
 * @param <T> class of object used as source of parameters' values for query.
 */
public interface HasNamedInjector<T> {

    /**
     * Returns parameters injector.
     *
     * @return parameters injector.
     */
    NamedInjector<T> getInjector();

    /**
     * Returns mapping of query parameters (name->index) for injector.
     *
     * @return mapping of query parameters.
     */
    NamedParametersMap getParametersMap();
}
