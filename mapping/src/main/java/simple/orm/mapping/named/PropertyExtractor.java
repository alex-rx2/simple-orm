package simple.orm.mapping.named;

import io.vavr.Function1;
import simple.orm.mapping.impl.BasicPropertyExtractor;

/**
 * Property extractor.
 *
 * @param <T> type of object to extract property value from.
 * @param <V> type of property value.
 */
public interface PropertyExtractor<T, V> {

    /**
     * Returns target class, which property this extractor is extracting.
     *
     * @return target class of property extractor.
     */
    Class<T> getTargetClass();

    /**
     * Returns value class.
     *
     * @return value class.
     */
    Class<V> getValueClass();

    /**
     * Return name of extracted property.
     *
     * @return name of extracted property.
     */
    String getPropertyName();

    /**
     * Returns {@link RefName} reference for this extractor.
     *
     * @return {@link RefName} reference.
     */
    default RefName getRef() {
        return new RefName(getPropertyName(), getTargetClass());
    }

    /**
     * Extract value of property from provided object.
     *
     * @param object object.
     * @return value of object property.
     */
    V extractValue(T object);

    /**
     * Default factory method.
     *
     * @param propName  name of property.
     * @param extractor extractor implementation.
     * @param <T>       type of object to extract property value from.
     * @param <V>       type of property value.
     * @return new {@link PropertyExtractor}.
     */
    static <T, V> PropertyExtractor<T, V> of(Class<T> targetClass,
                                             Class<V> valueClass,
                                             String propName,
                                             Function1<T, V> extractor
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
        if (extractor == null) {
            throw new NullPointerException("extractor is null");
        }
        return new BasicPropertyExtractor<>(targetClass, valueClass, propName, extractor);
    }

}
