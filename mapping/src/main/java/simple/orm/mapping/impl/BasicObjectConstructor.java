package simple.orm.mapping.impl;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.collection.Map;
import simple.orm.mapping.named.ObjectConstructor;

/**
 * Basic {@link ObjectConstructor} implementation.
 */
public class BasicObjectConstructor<T> implements ObjectConstructor<T> {

    private final Class<T> targetClass;
    private final Function0<T> simpleConstructor;
    private final Function1<Map<String, Object>, CreatedObject<T>> complexConstructor;

    public BasicObjectConstructor(Class<T> targetClass,
                                  Function0<T> simpleConstructor,
                                  Function1<Map<String, Object>, CreatedObject<T>> complexConstructor
    ) {
        this.targetClass = targetClass;
        this.simpleConstructor = simpleConstructor;
        this.complexConstructor = complexConstructor;
    }

    @Override
    public Class<T> getTargetClass() {
        return targetClass;
    }

    @Override
    public CreatedObject<T> createNew(Map<String, Object> properties) {
        return simpleConstructor == null ?
                complexConstructor.apply(properties) :
                CreatedObject.of(simpleConstructor.apply());
    }

}
