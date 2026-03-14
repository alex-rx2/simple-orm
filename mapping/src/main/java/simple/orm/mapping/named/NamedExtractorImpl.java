package simple.orm.mapping.named;

import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import io.vavr.control.Either;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.ReflectionsException;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link NamedExtractor} implementation.
 */
public class NamedExtractorImpl<T> extends AbstractNamedExtractor<T> {

    protected final Class<T> targetClass;
    protected final ReflectionsFinder reflectionsFinder;

    public NamedExtractorImpl(MappersFinder mappersFinder,
                              ReflectionsFinder reflectionsFinder,
                              Seq<NamedParameter> parameters,
                              Class<T> targetClass,
                              Traversable<ObjectConstructor<?>> constructors,
                              Traversable<PropertyInjector<?, ?>> injectors) {
        super(mappersFinder, parameters, targetClass, constructors, injectors);
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        this.targetClass = targetClass;
        this.reflectionsFinder = reflectionsFinder;
    }

    @Override
    protected ObjectConstructor<?> buildObjectConstructor(Class<?> targetClass) {
        final Constructor<?> constructor = reflectionsFinder.findDefaultConstructor(targetClass);
        return ObjectConstructor.of(targetClass, (values) -> createNew(constructor, values));
    }

    @SuppressWarnings("unchecked")
    protected <X> ObjectConstructor.CreatedObject<X> createNew(Constructor<?> constructor, Map<String, Object> values) {
        try {
            if (values.find(t2 -> t2._2 != null).isDefined()) {
                return ObjectConstructor.CreatedObject.of((X) constructor.newInstance());
            } else {
                // all properties are NULL - create NULL object
                return ObjectConstructor.CreatedObject.of(null);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionsException("failed to instantiate new object through default constructor of " +
                    constructor.getDeclaringClass().getName(), e);
        }
    }

    @Override
    protected PropertyInjector<?, ?> buildInjector(Class<?> targetClass, String propName) {
        final Either<Method, Field> setter = reflectionsFinder.findSetter(propName, targetClass);
        return buildInjector(targetClass, propName, setter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <X> PropertyInjector<X, ?> buildInjector(Class<X> sourceClass, String propName, Either<Method, Field> setter) {
        return setter.fold(
                method -> PropertyInjector.of(
                        sourceClass,
                        (Class) method.getParameterTypes()[0],
                        propName,
                        (o, v) -> inject(propName, method, o, v)),
                field -> PropertyInjector.of(
                        sourceClass,
                        (Class) field.getType(),
                        propName,
                        (o, v) -> inject(propName, field, o, v))
        );
    }

    protected static void inject(String paramName, Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionsException("failed to set value of property " + paramName, e);
        }
    }

    protected static void inject(String paramName, Method method, Object object, Object value) {
        try {
            method.invoke(object, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionsException("failed to obtain value of property " + paramName, e);
        }
    }

}
