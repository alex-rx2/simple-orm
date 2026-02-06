package simple.orm.jdbc.query;

import simple.orm.jdbc.map.IndexedInjector;

/**
 * Interface-provider of indexed injector for a query.
 * Such a query has parameters presented as sequence of objects, and they are injected by index.
 */
public interface HasIndexedInjector {

    /**
     * Returns parameters injector.
     *
     * @return parameters injector.
     */
    IndexedInjector getInjector();

}
