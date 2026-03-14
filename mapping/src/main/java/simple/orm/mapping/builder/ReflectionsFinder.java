package simple.orm.mapping.builder;

import io.vavr.control.Either;
import simple.orm.mapping.NoAccessorFoundException;
import simple.orm.mapping.impl.cache.ReflectionsCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * An interface for utility class capable to find reflections (getters, setters, constructors or fields)
 * for actual injectors and extractors.
 */
public interface ReflectionsFinder {

    /**
     * Finds getter for the parameter (either {@link Method} or direct {@link Field}).
     *
     * @return found accessor.
     * @throws NoAccessorFoundException if no accessor found.
     */
    Either<Method, Field> findGetter(String pName, Class<?> target);

    /**
     * Finds setter for the parameter (either {@link Method} or direct {@link Field}).
     *
     * @return found accessor.
     * @throws NoAccessorFoundException if no accessor found.
     */
    Either<Method, Field> findSetter(String pName, Class<?> target);

    /**
     * Finds default constructor for specified class.
     *
     * @return found constructor.
     * @throws NoAccessorFoundException if no constructor found.
     */
    Constructor<?> findDefaultConstructor(Class<?> target);

    /**
     * Factory method for default {@link ReflectionsFinder} implementation.
     *
     * @return new default {@link ReflectionsFinder} implementation.
     */
    static ReflectionsFinder defaultFinder() {
        return new ReflectionsCache();
    }

}
