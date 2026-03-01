package simple.orm.mapping.named;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import simple.orm.mapping.impl.BasicObjectConstructor;

/**
 * Constructor of new objects in accordance with list of its properties values.
 * These properties may be used to create object (set said properties values, decide on exact
 * implementing class to use, etc.). Used properties may be corresponded outside to be excluded from
 * further property injection.
 *
 * @param <T> type of created object.
 */
public interface ObjectConstructor<T> {

    /**
     * Result of object creation.
     *
     * @param <T> target type of object being created.
     */
    final class CreatedObject<T> {

        /**
         * Created object.
         */
        public final T object;
        /**
         * List of properties (their values) consumed during object creation.
         * These properties should be removed from list of properties to inject into created object.
         * <br>
         * E.g. properties values are set during object creation, or some properties values are used to denote
         * the actual class to be created.
         */
        public final Traversable<String> consumedProperties;

        private CreatedObject(T object, Traversable<String> consumedProperties) {
            this.object = object;
            this.consumedProperties = consumedProperties == null ? List.empty() : consumedProperties;
        }

        public static <T> CreatedObject<T> of(T object) {
            return of(object, List.empty());
        }

        public static <T> CreatedObject<T> of(T object, Traversable<String> consumedProperties) {
            return new CreatedObject<>(object, consumedProperties);
        }

    }

    /**
     * Returns target class. Object created with {@link #createNew(Map)} will be assignable to it.
     *
     * @return target class of this constructor.
     */
    Class<T> getTargetClass();

    /**
     * Create new object in accordance with corresponding properties.
     *
     * @param properties values of object properties (and sub-properties, if any).
     * @return new object with list of consumed properties.
     */
    CreatedObject<T> createNew(Map<String, Object> properties);

    /**
     * Factory method to create {@link ObjectConstructor}.
     *
     * @param targetClass       target class.
     * @param simpleConstructor simple implementation, that doesn't require any parameters.
     * @param <T>               type of created object.
     * @return new {@link ObjectConstructor}.
     */
    static <T> ObjectConstructor<T> of(Class<T> targetClass, Function0<T> simpleConstructor) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (simpleConstructor == null) {
            throw new NullPointerException("simpleConstructor is null");
        }
        return new BasicObjectConstructor<>(targetClass, simpleConstructor, null);
    }

    /**
     * Factory method to create {@link ObjectConstructor}.
     *
     * @param targetClass        target class.
     * @param complexConstructor constructor implementation,
     *                           that requires forward knowledge of parameters to create proper object.
     * @param <T>                type of created object.
     * @return new {@link ObjectConstructor}.
     */
    static <T> ObjectConstructor<T> of(Class<T> targetClass,
                                       Function1<Map<String, Object>, CreatedObject<T>> complexConstructor) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (complexConstructor == null) {
            throw new NullPointerException("complexConstructor is null");
        }
        return new BasicObjectConstructor<>(targetClass, null, complexConstructor);
    }

}
