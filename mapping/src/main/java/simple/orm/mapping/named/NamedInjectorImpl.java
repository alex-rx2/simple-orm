package simple.orm.mapping.named;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.mapping.ReflectionsException;
import simple.orm.mapping.builder.MappersFinder;
import simple.orm.mapping.builder.ReflectionsFinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link NamedInjector} implementation.
 */
public class NamedInjectorImpl<T> extends AbstractNamedInjector<T> {

    protected final ReflectionsFinder reflectionsFinder;

    public NamedInjectorImpl(MappersFinder mappersFinder,
                             ReflectionsFinder reflectionsFinder,
                             Seq<NamedParameter> parameters,
                             Class<T> sourceClass,
                             Seq<PropertyExtractor<T, ?>> extractors) {
        super(mappersFinder, parameters, sourceClass, extractors);
        if (reflectionsFinder == null) {
            throw new NullPointerException("reflectionsFinder is null");
        }
        this.reflectionsFinder = reflectionsFinder;
    }

    @Override
    protected <X> PropertyExtractor<X, ?> buildExtractor(String propName, Class<X> sourceClass) {
        final Either<Method, Field> getter = reflectionsFinder.findGetter(propName, sourceClass);
        return buildExtractor(sourceClass, propName, getter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <X> PropertyExtractor<X, ?> buildExtractor(Class<X> sourceClass, String propName, Either<Method, Field> getter) {
        return getter.fold(
                method -> PropertyExtractor.of(
                        sourceClass,
                        (Class) method.getReturnType(),
                        propName,
                        x -> extract(propName, method, x)),
                field -> PropertyExtractor.of(
                        sourceClass,
                        (Class) field.getType(),
                        propName,
                        x -> extract(propName, field, x))
        );
    }

    protected static <X> Object extract(String paramName, Field field, X x) {
        if (x == null) {
            return null;
        }
        try {
            return field.get(x);
        } catch (IllegalAccessException e) {
            throw new ReflectionsException("failed to obtain value of property " + paramName, e);
        }
    }

    protected static <X> Object extract(String paramName, Method method, X x) {
        if (x == null) {
            return null;
        }
        try {
            return method.invoke(x);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionsException("failed to obtain value of property " + paramName, e);
        }
    }

}
