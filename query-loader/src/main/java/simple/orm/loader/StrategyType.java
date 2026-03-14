package simple.orm.loader;

import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;

/**
 * Type of strategy for {@link InjectionStrategy} and {@link ExtractionStrategy}.
 */
public enum StrategyType {

    /**
     * Query has no parameters or no result respectively.
     */
    NONE,
    /**
     * Parameters should be injected with {@link IndexedInjector},
     * result should be extracted with {@link IndexedExtractor}.
     */
    INDEXED,
    /**
     * Parameters should be injected with {@link NamedInjector},
     * result should be extracted with {@link NamedExtractor}.
     */
    NAMED

}
