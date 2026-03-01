package simple.orm.mapping.impl;

import simple.orm.mapping.named.PropertyInjector;

import java.util.function.BiConsumer;

/**
 * Basic {@link PropertyInjector} implementation.
 */
public class BasicPropertyInjector<T, V> implements PropertyInjector<T, V> {

    private final Class<T> targetClass;
    private final Class<V> valueClass;
    private final String propertyName;
    private final BiConsumer<T, V> injector;

    public BasicPropertyInjector(Class<T> targetClass,
                                 Class<V> valueClass,
                                 String propertyName,
                                 BiConsumer<T, V> injector
    ) {
        this.targetClass = targetClass;
        this.valueClass = valueClass;
        this.propertyName = propertyName;
        this.injector = injector;
    }

    @Override
    public Class<T> getTargetClass() {
        return targetClass;
    }

    @Override
    public Class<V> getValueClass() {
        return valueClass;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void injectProperty(T object, V value) {
        injector.accept(object, value);
    }

}
