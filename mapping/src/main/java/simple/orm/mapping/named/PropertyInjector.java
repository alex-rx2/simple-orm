package simple.orm.mapping.named;

import simple.orm.mapping.impl.BasicPropertyInjector;

import java.util.function.BiConsumer;

/**
 * Property injector.
 *
 * @param <T> type of object to set its property value.
 * @param <V> type of property value.
 */
public interface PropertyInjector<T, V> {

    /**
     * Returns target class, which property this injector do inject.
     *
     * @return target class of property injector.
     */
    Class<T> getTargetClass();

    /**
     * Returns value class.
     *
     * @return value class.
     */
    Class<V> getValueClass();

    /**
     * Returns name of injecting property.
     *
     * @return name of injecting property.
     */
    String getPropertyName();

    /**
     * Returns {@link RefName} reference for this injector.
     *
     * @return {@link RefName} reference.
     */
    default RefName getRef() {
        return new RefName(getPropertyName(), getTargetClass());
    }

    /**
     * Inject property value into object property.
     *
     * @param object the object.
     * @param value  the value for target property.
     */
    void injectProperty(T object, V value);

    /**
     * Factory method.
     *
     * @param targetClass target class of property injector.
     * @param propName    name of injecting property.
     * @param injector    injector implementation.
     * @param <T>         type of object to set its property value.
     * @param <V>         type of property value.
     * @return new {@link PropertyInjector}.
     */
    static <T, V> PropertyInjector<T, V> of(Class<T> targetClass,
                                            Class<V> valueClass,
                                            String propName,
                                            BiConsumer<T, V> injector
    ) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (valueClass == null) {
            throw new NullPointerException("valueClass is null");
        }
        if (propName == null) {
            throw new NullPointerException("propName is null");
        }
        if (propName.isEmpty()) {
            throw new NullPointerException("propName is empty");
        }
        if (injector == null) {
            throw new NullPointerException("injector is null");
        }
        return new BasicPropertyInjector<>(targetClass, valueClass, propName, injector);
    }

}
