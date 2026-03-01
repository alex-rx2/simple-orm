package simple.orm.mapping.impl;

import io.vavr.Function1;
import simple.orm.mapping.named.PropertyExtractor;

/**
 * Basic {@link PropertyExtractor} implementation.
 */
public class BasicPropertyExtractor<T, V> implements PropertyExtractor<T, V> {

    private final Class<T> targetClass;
    private final Class<V> valueClass;
    private final String propertyName;
    private final Function1<T, V> extractor;

    public BasicPropertyExtractor(Class<T> targetClass,
                                  Class<V> valueClass,
                                  String propertyName,
                                  Function1<T, V> extractor
    ) {
        this.targetClass = targetClass;
        this.valueClass = valueClass;
        this.propertyName = propertyName;
        this.extractor = extractor;
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
    public V extractValue(T object) {
        return extractor.apply(object);
    }

}
