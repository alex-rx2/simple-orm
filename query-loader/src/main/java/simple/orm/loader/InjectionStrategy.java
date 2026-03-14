package simple.orm.loader;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.Query;

/**
 * Class defining parameter injection strategy {@link QueryLoader} should use when creating {@link Query}.
 *
 * @param <T> type of object source of parameters
 *            (<code>Seq&lt;Object></code> for {@link IndexedInjector},
 *            <code>Void</code> for query without parameters).
 */
public final class InjectionStrategy<T> {

    /**
     * Factory method for no-parameters strategy
     * ({@link QueryLoader} should build {@link Query} without injector, query has no parameters to inject).
     *
     * @return no-parameters strategy.
     */
    public static InjectionStrategy<Void> none() {
        return new InjectionStrategy<>(StrategyType.NONE, null);
    }

    /**
     * Factory method for indexed strategy ({@link QueryLoader} should build {@link Query} with {@link IndexedInjector}).
     *
     * @return indexed strategy.
     */
    public static InjectionStrategy<Seq<Object>> indexed() {
        return new InjectionStrategy<>(StrategyType.INDEXED, null);
    }

    /**
     * Factory method for named strategy ({@link QueryLoader} should build {@link Query} with {@link NamedInjector}).
     *
     * @param sourceClass class of source object.
     * @param <T>         type of object serving as source of parameters.
     * @return named strategy.
     */
    public static <T> InjectionStrategy<T> named(Class<T> sourceClass) {
        if (sourceClass == null) {
            throw new NullPointerException("sourceClass is null");
        }
        return new InjectionStrategy<>(StrategyType.NAMED, sourceClass);
    }

    public final StrategyType type;
    public final Class<T> sourceClass;

    private InjectionStrategy(StrategyType type, Class<T> sourceClass) {
        this.type = type;
        this.sourceClass = sourceClass;
    }

}
